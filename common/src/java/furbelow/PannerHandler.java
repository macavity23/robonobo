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

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.border.EmptyBorder;

/** Provide key-activated display of a Panner directly on a panned component. 
 *  The panner may be given an absolute or relative size, and an arbitrary 
 *  offset for placement relative to the parent component's edges.  If the 
 *  percentage size is less than 50%, the panner will appear in each corner
 *  in succession on successive key activations. 
 */
public class PannerHandler {
    public static final int PANNER_MASK = 
        Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()|InputEvent.SHIFT_MASK;
    public static final long MOVE_TIMEOUT = 1000;
    private JComponent target;
    private Dimension size;
    private Point offset;
    private Panner panner;
    private long lastHidden;
    private int corner = UL;
    private boolean affixed;
    private int percent = 100;
    private float transparency = 0.9f;
    private boolean preserveAspect = true;
    
    private static final int CENTER = -1;
    private static final int UL = 0;
    private static final int UR = 1;
    private static final int LR = 2;
    private static final int LL = 3;
    
    public PannerHandler(JComponent target, Dimension size) {
        this(target, size, 100, new Point(0, 0));
    }
    
    public PannerHandler(JComponent target, int percent) {
        this(target, percent, new Point(0, 0));
    }
    
    public PannerHandler(JComponent target, int percent, Point offset) {
        this(target, null, percent, offset);
    }
    
    public PannerHandler(JComponent target, Dimension size, 
                         int percent, Point offset) {
        this.target = target;
        this.size = size;
        this.percent = percent;
        this.offset = offset;
        this.corner = percent < 50 ? UL : CENTER;
        target.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (!affixed) {
                    int modifiers = e.getModifiers();
                    if ((modifiers & PANNER_MASK) == PANNER_MASK) {
                        if (panner == null || !panner.isAttached())
                            show(false);
                    }
                }
            }
            public void keyReleased(KeyEvent e) {
                if (!affixed) {
                    hide();
                }
            }
        });
        ComponentListener listener = new ComponentAdapter() {
            private void refresh() {
                if (panner != null && panner.isShowing())
                    show(affixed);
            }
            public void componentResized(ComponentEvent e) {
                refresh();
            }
            public void componentMoved(ComponentEvent e) {
                refresh();
            }
        };
        target.addComponentListener(listener);
        if (target.getParent() instanceof JViewport) {
            target.getParent().addComponentListener(listener);
        }
    }
    /** Display the panner until {@link #hide} is called. */
    public void show() {
        show(true);
    }
    
    private void show(boolean fixed) {
        this.affixed = fixed;
        if (panner == null) {
            panner = new Panner(target);
            panner.setTransparency(transparency);
            panner.setPreserveAspect(preserveAspect);
        }
        attach(panner, !fixed);
    }

    public void setPreserveAspect(boolean preserve) {
        this.preserveAspect = preserve;
        if (panner != null)
            panner.setPreserveAspect(preserve);
    }
    
    public void setTransparency(float t) {
        this.transparency = t;
        if (panner != null)
            panner.setTransparency(t);
    }
    
    private void attach(Panner panner, boolean nextCorner) {

        Rectangle visible = target.getVisibleRect();
        Dimension sz = size;
        if (sz == null) {
            sz = new Dimension(visible.width, visible.height);
            sz.width = sz.width * percent / 100;
            sz.height = sz.height * percent / 100;
        }
        panner.setSize(sz);
        if (corner != CENTER && nextCorner) {
            if (System.currentTimeMillis() - lastHidden < MOVE_TIMEOUT) {
                corner = (corner + 1) & 0x3;
            }
            else {
                corner = 0;
            }
        }
        int x = visible.x;
        int y = visible.y;
        switch(corner) {
        case UL:
            x += offset.x;
            y += offset.y;
            break;
        case UR:
            x += visible.width - offset.x - panner.getPreferredSize().width;
            y += offset.y;
            break;
        case LR:
            x += visible.width - offset.x - panner.getPreferredSize().width;
            y += visible.height - offset.y - panner.getPreferredSize().height; 
            break;
        case LL:
            x += offset.x;
            y += visible.height - offset.y - panner.getPreferredSize().height;
            break;
        case CENTER:
        default:
            x += (visible.width - panner.getPreferredSize().width)/2;
            y += (visible.height - panner.getPreferredSize().height)/2;
            break;
        }
        panner.attach(x, y);
        panner.revalidate();
        panner.repaint();
    }
    public void hide() {
        this.affixed = false;
        if (panner != null) {
            lastHidden = System.currentTimeMillis();
            panner.detach();
            panner = null;
        }
    }

    /** @return whether the panner is currently visible. */
    public boolean isShowing() {
        return panner != null;
    }
    
    /** Demo. */
    public static void main(String[] args) {
        try {
            JFrame frame = new JFrame("Panner");
            JPanel p = (JPanel)frame.getContentPane();
            URL url = PannerHandler.class.getResource("desert.jpg");
            String key = System.getProperty("os.name").toLowerCase().indexOf("mac") != -1
                ? "\u2318" : "control";
            JLabel label = new JLabel("Press " + key + 
                                      "+shift to display panner, click/drag to navigate");
            label.setBorder(new EmptyBorder(4,4,4,4));
            p.add(label, BorderLayout.NORTH);
            JLabel image = new JLabel(new ImageIcon(url));
            image.setFocusable(true);
            PannerHandler handler = new PannerHandler(image, 95, new Point(0, 0));
            image.putClientProperty("panner", handler);
            p.add(new JScrollPane(image));
            frame.pack();
            Dimension size = image.getPreferredSize();
            size.width /= 2;
            size.height /= 2;
            Dimension ssize = frame.getToolkit().getScreenSize();
            frame.setLocation((ssize.width-size.width)/2, (ssize.height-size.height)/2);
            frame.setSize(size);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
            image.requestFocus();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
}