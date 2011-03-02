/* Copyright (c) 2006-2007 Timothy Wall, All Rights Reserved
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * <p/>
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.  
 */
package furbelow;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.event.*;
import javax.swing.text.DefaultEditorKit;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/** Demonstration of context-sensitive menu items based on {@link Action}s
 * exported by a {@link JComponent}.
 * @author twall
 */
public class DelegatingAction extends AbstractAction implements PropertyChangeListener {

    private class FocusChangeListener implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent e) {
            Component c = (Component)e.getNewValue();
            if (c instanceof JComponent) {
                ActionMap map = ((JComponent)c).getActionMap();
                Action a = map.get(actionKey);
                if (a == null) {
                    a = map.get(actionKey2);
                }
                setDelegate(a);
                target = c;
            }
            else {
                setDelegate(null);
                target = null;
            }
        }
    }

    public static class DisabledAction extends AbstractAction {
        public DisabledAction(String name) { 
            super(name);
            setEnabled(false);
        }
        public void actionPerformed(ActionEvent e) { }
    }
    
    private static class ListSelectionDimmer {
        public ListSelectionDimmer(final JList list) {
            KeyboardFocusManager fm = 
                KeyboardFocusManager.getCurrentKeyboardFocusManager();
            fm.addPropertyChangeListener("permanentFocusOwner", new PropertyChangeListener() {
                private Color oldColor;
                public void propertyChange(PropertyChangeEvent e) {
                    if (e.getOldValue() == list) {
                        if (oldColor == null) {
                            oldColor = list.getSelectionBackground();
                            Color bg = list.getBackground();
                            bg = new Color((bg.getRed() + oldColor.getRed())/2,
                                           (bg.getGreen() + oldColor.getGreen())/2,
                                           (bg.getBlue() + oldColor.getBlue())/2);
                            list.setSelectionBackground(bg);
                        }
                    }
                    else if (e.getNewValue() == list) {
                        if (oldColor != null) {
                            list.setSelectionBackground(oldColor);
                            oldColor = null;
                        }
                    }
                }
            });
        }
    }
    
    private Action delegate;
    private Component target;
    private String actionKey;
    private String actionKey2;
    public DelegatingAction(String name, String key, String altKey) {
        super(name);
        actionKey = key;
        actionKey2 = altKey;
        KeyboardFocusManager fm = 
            KeyboardFocusManager.getCurrentKeyboardFocusManager();
        fm.addPropertyChangeListener("permanentFocusOwner", new FocusChangeListener());
    }

    /** Enables and delegates to the target action only if the clipboard
     * has contents.
     */
    public static abstract class PasteAction extends AbstractAction {
        public PasteAction() {
            super("paste");
            KeyboardFocusManager fm = 
                KeyboardFocusManager.getCurrentKeyboardFocusManager();
            fm.addPropertyChangeListener("permanentFocusOwner", new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent e) {
                    setEnabled(clipboardHasContents());
                }
            });
        }
    }

    protected static String getClipboardContents() {
        try {
            Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
            Transferable t = clip.getContents(null);
            return (String)t.getTransferData(DataFlavor.stringFlavor);
        }
        catch(UnsupportedFlavorException ex) {
            return null;
        }
        catch (IOException e) {
            return null;
        }
        
    }
    
    protected static boolean clipboardHasContents() {
        String contents = getClipboardContents();
        return contents != null && !"".equals(contents);
    }
    
    private void setDelegate(Action delegate) {
        if (this.delegate != null) {
            this.delegate.removePropertyChangeListener(this);
        }
        this.delegate = delegate;
        if (delegate != null) {
            delegate.addPropertyChangeListener(this);
        }
        setEnabled(delegate != null && delegate.isEnabled());
    }
    
    public void propertyChange(PropertyChangeEvent e) {
        if ("enabled".equals(e.getPropertyName())) {
            setEnabled(Boolean.TRUE.equals(e.getNewValue()));
        }
    }
    
    public void actionPerformed(ActionEvent e) {
        if (delegate != null) {
            // Have to keep track of the target since TransferHandler
            // actions use a single, shared Action instance
            if (target != null) {
                e = new ActionEvent(target, e.getID(), e.getActionCommand(),
                                    e.getWhen(), e.getModifiers());
            }
            delegate.actionPerformed(e);
        }
    }
    
    private static JList createList(boolean readOnly) {
        final Object[] data = {
            "Apples",
            "Oranges",
            "Bananas",
            "Kiwis",
            "Turnips",
            "Rutabagas",
        };
        final JList list = new JList(data);
        new ListSelectionDimmer(list);
        final ActionMap map = list.getActionMap();
        if (readOnly) {
            // Have to override these to avoid having Swing automatically
            // delegate to an installed TransferHandler
            map.put("cut", new DisabledAction("cut"));
            map.put("paste", new DisabledAction("paste"));
        }
        else {
            map.put("cut", new AbstractAction("cut") {
                public void actionPerformed(ActionEvent e) {
                    map.get("copy").actionPerformed(e);
                    final List contents = new ArrayList();
                    ListModel model = list.getModel();
                    int[] indices = list.getSelectedIndices();
                    for (int i=0;i < model.getSize();i++) {
                        if (!list.isSelectedIndex(i)) {
                            contents.add(model.getElementAt(i));
                        }
                    }
                    list.setModel(new AbstractListModel() {
                        public int getSize() {
                            return contents.size();
                        }
                        public Object getElementAt(int index) {
                            return contents.get(index);
                        }
                    });
                    if (indices.length == 1) {
                        if (indices[0] < contents.size()) {
                            list.setSelectedIndex(indices[0]);
                        }
                        else if (indices[0] == contents.size()) {
                            list.setSelectedIndex(contents.size()-1);
                        }
                    }
                }
            });
            // Only enable "paste" if the clipboard is non-empty
            map.put("paste", new PasteAction() {
                public void actionPerformed(ActionEvent e) {
                    final List contents = new ArrayList();
                    ListModel model = list.getModel();
                    for (int i=0;i < model.getSize();i++) {
                        contents.add(model.getElementAt(i));
                    }
                    int idx = list.getSelectedIndex();
                    if (idx == -1 && model.getSize() == 0) {
                        idx = 0;
                    }
                    String s = getClipboardContents();
                    if (idx != -1 && s != null) {
                        contents.add(idx, s);
                        list.setModel(new AbstractListModel() {
                            public int getSize() {
                                return contents.size();
                            }
                            public Object getElementAt(int index) {
                                return contents.get(index);
                            }
                        });
                        list.setSelectedIndex(idx);
                    }
                }
            });
        }
        return list;
    }
    
    public static void main(String[] args) {
        JFrame frame = new JFrame("Delegating Edit Menu");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JMenuBar mb = new JMenuBar();
        JMenu menu = new JMenu("Edit");
        menu.add(new JMenuItem(new DelegatingAction("Cut", "cut", DefaultEditorKit.cutAction)));
        menu.add(new JMenuItem(new DelegatingAction("Copy", "copy", DefaultEditorKit.copyAction)));
        menu.add(new JMenuItem(new DelegatingAction("Paste", "paste", DefaultEditorKit.pasteAction)));
        mb.add(menu);
        frame.setJMenuBar(mb);
        
        JTextField textField = new JTextField("Select this text and copy");
        final MessageFormat fmt = new MessageFormat("<html><i>Clipboard Contents:</i><br>{0}</html>");
        final JLabel label = new JLabel(fmt.format(new Object[] {"<br>"}));
        
        JPanel p = (JPanel)frame.getContentPane();
        JPanel split = new JPanel(new GridLayout(0, 2));
        JPanel pleft = new JPanel(new BorderLayout());
        pleft.add(new JLabel("Copy only"), BorderLayout.NORTH);
        pleft.add(new JScrollPane(createList(true)));
        split.add(pleft);
        JPanel pright = new JPanel(new BorderLayout());
        pright.add(new JLabel("Copy, Cut, or Paste non-empty text"), BorderLayout.NORTH);
        pright.add(new JScrollPane(createList(false)));
        split.add(pright);
        p.add(split, BorderLayout.CENTER);
        p.add(textField, BorderLayout.PAGE_START);
        p.add(label, BorderLayout.PAGE_END);

        // Display what's currently on the clipboard
        Timer timer = new Timer(500, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String s = getClipboardContents();
                if (s == null)
                    s = "{empty or not a String}";
                final int MAX = 200;
                if (s.length() > MAX) {
                    s = s.substring(0, MAX-3) + "...";
                }
                label.setText(fmt.format(new Object[] { s }));
            }
        });
        timer.start();
        timer.setRepeats(true);

        frame.pack();
        frame.setSize(400, 300);
        frame.setLocation(100, 100);
        frame.setVisible(true);
    }

}
