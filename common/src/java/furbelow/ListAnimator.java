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

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import javax.swing.AbstractListModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;

/** Animates moving list cells out of the way for a potential drop.
 * This decorator completely over-paints the target JList, optionally
 * painting a dragged item and animating creation of a space for the
 * dragged item to be dropped. 
 * Thanks to Neil Cochran/keilly for a base visualization:
 * http://jroller.com/page/swinguistuff?entry=animated_jlist 
 */
public abstract class ListAnimator extends AbstractComponentDecorator {

    /** Animation repaint interval.  Make this larger to slow down the 
     * animation. 
     */
    private static final int INTERVAL = 1000 / 24;
    private static Timer timer = new Timer(true);
    /** Simple decorator to provide the ghosted drag image. */
    private final class GhostedDragImage extends AbstractComponentDecorator {
        private int index;
        private Point location;
        private Point offset;
        public GhostedDragImage(int cellIndex, Point origin) {
            super(list);
            this.index = cellIndex;
            Rectangle b = list.getCellBounds(index, index);
            location = origin;
            this.offset = new Point(0, origin.y - b.y);
        }
        public void setLocation(Point where) {
            this.location = where;
            getPainter().repaint();
        }
        public void paint(Graphics g) {
            Rectangle b = list.getCellBounds(index, index);
            Point origin = new Point(0, location.y-offset.y);
            origin.y = Math.max(0, origin.y);
            origin.y = Math.min(origin.y, list.getHeight() - b.height);
            g = g.create(origin.x, origin.y, b.width, b.height);
            ((Graphics2D)g).translate(-b.x, -b.y);
            ((Graphics2D)g).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
            list.paint(g);
        }
    }
    private final class Counter extends TimerTask {
        public void run() {
            synchronized(ListAnimator.this) {
                if (reposition()) {
                    repaint();
                }
            }
        }
    }
    private Counter counter;
    /** Index of insertion point. */
    private int insertionIndex = -1;
    /** Index of object being dragged, if any. */
    private int draggedIndex = -1;
    private JList list;
    private Map bounds = new TreeMap();
    private GhostedDragImage dragImage;
    private Point origin;
    
    public ListAnimator(final JList list) {
        super(list);
        this.list = list;
        counter = new Counter();
        timer.schedule(counter, INTERVAL, INTERVAL);
    }
    protected Object getPlaceholder() { return ""; }
    protected abstract void move(int fromIndex, int toIndex);
    protected void drop(Transferable t, int index) { } 
    
    private void initialize(Point where) {
        insertionIndex = draggedIndex = -1;
        origin = where;
        int size = list.getModel().getSize();
        for (int i=0;i < size;i++) {
            bounds.put(new Integer(i), getCellBoundsAfterInsertion(i));
        }
    }
    /** Track a drag which originated somewhere else. */
    public synchronized void startDragOver(Point where) {
        initialize(where);
        insertionIndex = getIndex(where, false);
    }
    /** Stop tracking an external drag. */
    public synchronized void endDragOver(Point where, Transferable t) {
        int idx = getIndex(where, false);
        if (idx != -1) {
            drop(t, idx);
        }
    }
    /** Start an internal drag. */
    public synchronized void startDrag(Point where) {
        initialize(where);
        draggedIndex = insertionIndex = getIndex(where, true);
        dragImage = new GhostedDragImage(draggedIndex, origin);
    }
    /** End an internal drag. */
    public synchronized void endDrag(Point where) {
        int toIndex = getIndex(where, true);
        int fromIndex = draggedIndex;
        dragImage.dispose();
        dragImage = null;
        draggedIndex = insertionIndex = -1;
        if (toIndex != -1 && toIndex != fromIndex) {
            Map newBounds = new TreeMap();
            newBounds.put(new Integer(toIndex),
                          bounds.get(new Integer(fromIndex)));
            if (fromIndex < toIndex) {
                for (int i=fromIndex+1;i <= toIndex;i++) {
                    newBounds.put(new Integer(i-1),
                                  bounds.get(new Integer(i)));
                }
            }
            else {
                for (int i=toIndex;i < fromIndex;i++) {
                    newBounds.put(new Integer(i+1),
                                  bounds.get(new Integer(i)));
                }
            }
            bounds.putAll(newBounds);
            move(fromIndex, toIndex);
        }
    }
    private boolean reposition() {
        boolean changed = false;
        for (Iterator i=bounds.keySet().iterator();i.hasNext();) {
            Integer x = (Integer)i.next();
            Rectangle current = getCurrentCellBounds(x.intValue());
            Rectangle end = getCellBoundsAfterInsertion(x.intValue());
            if (current.x != end.x || current.y != end.y) {
                int xdelta = (end.x - current.x)/2;
                int ydelta = (end.y - current.y)/2;
                if (xdelta == 0)
                    current.x = end.x;
                else
                    current.x += xdelta;
                if (ydelta == 0)
                    current.y = end.y;
                else
                    current.y += ydelta;
                bounds.put(x, current);
                changed = true;
            }
        }
        return changed;
    }
    private int getIndex(Point where, boolean restrict) {
        int idx = list.locationToIndex(where);
        if (!restrict) {
            int size = list.getModel().getSize();
            // Assumes the list considers points below the last item
            // be within last item
            Rectangle last = list.getCellBounds(size-1, size-1);
            if (idx == size-1 && where.y > last.y + last.height) {
                idx = size;
            }
        }
        return idx;
    }
    public synchronized void setInsertionLocation(Point where) {
        // Avoid painting focus and/or selection bgs, kind of a hack
        getPainter().requestFocus();
        list.clearSelection();
        setInsertionIndex(getIndex(where, draggedIndex != -1));
        dragImage.setLocation(where);
    }
    public synchronized void setInsertionIndex(int idx) {
        if (idx != insertionIndex) {
            insertionIndex = idx;
            repaint();
        }
    }
    private Rectangle getCellBoundsAfterInsertion(int index) {
        Rectangle r = list.getCellBounds(index, index);
        if (draggedIndex != -1) {
            if (index > draggedIndex) {
                if (index <= insertionIndex) {
                    Rectangle r2 = list.getCellBounds(draggedIndex, draggedIndex);
                    r.y -= r2.height;
                }
            }
            else if (index < draggedIndex) {
                if (index >= insertionIndex) {
                    Rectangle r2 = list.getCellBounds(draggedIndex, draggedIndex);
                    r.y += r2.height;
                }
            }
            else {
                Rectangle r2 = list.getCellBounds(insertionIndex, insertionIndex);
                r.y = r2.y;
            }
        }
        else if (insertionIndex != -1 && index > insertionIndex) {
            ListCellRenderer rnd = list.getCellRenderer();
            Component c = rnd.getListCellRendererComponent(list, getPlaceholder(), insertionIndex, false, false);
            r.y += c.getHeight();
        }
        return r;
    }
    private Rectangle getCurrentCellBounds(int cellIndex) {
        Rectangle r = getCellBoundsAfterInsertion(cellIndex);
        Rectangle r2 = (Rectangle)bounds.get(new Integer(cellIndex));
        if (r2 != null) {
            r.x = r2.x;
            r.y = r2.y;
        }
        return r;
    }
    public synchronized void paint(Graphics g) {
        boolean db = list.isDoubleBuffered();
        list.setDoubleBuffered(false);
        try { 
            Rectangle b = getDecorationBounds();
            g.setColor(list.getBackground());
            g.fillRect(b.x, b.y, b.width, b.height);
            for (int i=0;i < list.getModel().getSize();i++) {
                if (i == draggedIndex)
                    continue;
                Rectangle r = getCurrentCellBounds(i);
                Graphics g2 = g.create(r.x, r.y, r.width, r.height);
                Rectangle r2 = list.getCellBounds(i, i);
                ((Graphics2D)g2).translate(0, -r2.y);
                list.paint(g2);
            }
        }
        finally {
            list.setDoubleBuffered(db);
        }
    }
    
    /** Simple JList-local drag/drop handler.  Invokes the animator according
     * to user input.  A similar method could be used to accept drags
     * originating outside of the JList.
     */
    static class Listener extends MouseAdapter implements MouseMotionListener {
        private ListAnimator animator;
        private boolean dragActive;
        private Point origin;
        public Listener(ListAnimator animator) {
            this.animator = animator;
        }
        private boolean sufficientMove(Point where) {
            int dx = Math.abs(origin.x-where.x);
            int dy = Math.abs(origin.y-where.y);
            return Math.sqrt(dx*dx+dy*dy) > 5;
        }
        public void mousePressed(MouseEvent e) {
            origin = e.getPoint();
        }
        
        public void mouseReleased(MouseEvent e) {
            if (dragActive) {
                animator.endDrag(e.getPoint());
                dragActive = false;
            }
        }
        
        public void mouseDragged(MouseEvent e) {
            if (!dragActive) {
                if (sufficientMove(e.getPoint())) {
                    animator.startDrag(origin);
                    dragActive = true;
                }
            }
            if (dragActive)
                animator.setInsertionLocation(e.getPoint());                
        }

        public void mouseExited(MouseEvent e) {
            if (dragActive)
                animator.setInsertionIndex(-1);
        }
        
        public void mouseEntered(MouseEvent e) {
            if (dragActive)
                animator.setInsertionLocation(e.getPoint());
        }

        public void mouseMoved(MouseEvent e) {
        }
    }

    private static final List DATA = new ArrayList(Arrays.asList(new Object[] {
        "Happy", "Bashful", "Grumpy", //"Sneezy", "Dopey", "Sleepy", "Doc",
        //"Snow White", "Prince Charming", "Wicked Witch", 
    }));
    
    /** Throw up a frame to demonstrate the animator at work.  Funkify the
     * list renderer to demonstrate that customized renderers are handled
     * properly.
     */
    public static void main(String[] args) {
        JFrame f = new JFrame("Smooth List Drop");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        final JList list = new JList(new AbstractListModel() {
            public int getSize() {
                return DATA.size();
            }
            public Object getElementAt(int index) {
                return DATA.get(index);
            }
        });
        ListAnimator smoother = new ListAnimator(list) {
            protected void move(int fromIndex, int toIndex) {
                Object o = DATA.remove(fromIndex);
                DATA.add(toIndex, o);
                list.revalidate();
                list.repaint();
            }
        };
        Listener listener = new Listener(smoother);
        list.addMouseListener(listener);
        list.addMouseMotionListener(listener);
        list.setCellRenderer(new DefaultListCellRenderer() {
            public Component getListCellRendererComponent(JList list, Object value,
                                                          int index, boolean sel, boolean focus) {
                Component c = super.getListCellRendererComponent(list, value, index, sel, focus);
                Color bg = c.getBackground();
                if ((index % 2) == 0) {
                    int GRAY = 190;
                    c.setBackground(new Color((GRAY + bg.getRed()*2)/3, 
                                              (GRAY + bg.getGreen()*2)/3, 
                                              (GRAY + bg.getBlue()*2)/3));
                    ((JComponent)c).setOpaque(true);
                }
                else if (c.getBackground().equals(list.getBackground())){
                    ((JComponent)c).setOpaque(false);
                }
                if (value.toString().startsWith("S")) {
                    c.setForeground(Color.blue);
                }
                else if (value.toString().startsWith("D")) {
                    Font font = list.getFont();
                    c.setFont(font.deriveFont(Font.ITALIC));
                }
                return c;
            }
        });
        JLabel label = new JLabel("Drag items to reorder");
        label.setBorder(new EmptyBorder(4,4,4,4));
        label.setFont(label.getFont().deriveFont(Font.BOLD, label.getFont().getSize()*2));
        label.putClientProperty("decorator", new AbstractComponentDecorator(label, -1) {
            public void paint(Graphics g) {
                Rectangle b = getDecorationBounds();
                ((Graphics2D)g).setPaint(new GradientPaint(0, b.height/2, list.getSelectionBackground(),
                                                           b.width/2, b.height/2, Color.white));
                g.fillRect(b.x, b.y, b.width, b.height);
            }
        });
        f.getContentPane().add(label, BorderLayout.NORTH);
        f.getContentPane().add(new JScrollPane(list));
        f.pack();
        f.setVisible(true);
    }
}
