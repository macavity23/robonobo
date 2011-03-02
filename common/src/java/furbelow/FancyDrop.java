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

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.Point;
import java.awt.Graphics;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalIconFactory;
import javax.swing.table.AbstractTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

public class FancyDrop {

    private static class ZoomRect extends AbstractComponentDecorator implements ActionListener {

        private Timer timer;
        private int alpha;
        private volatile boolean zoom;
        private Rectangle targetBounds;
        
        final int MPY = 4;
        final int INTERVAL = 1000/12;
        
        public ZoomRect(JComponent target) {
            super(target);
            timer = new Timer(INTERVAL, this);
            timer.start();
        }
        
        private void reset(Rectangle target) {
            this.targetBounds = target;
            alpha = 0;
            int x = target.x - target.width*(MPY-1)/2;
            int y = target.y - target.height*(MPY-1)/2;
            super.setDecorationBounds(new Rectangle(x, y, target.width*MPY, target.height*MPY));
        }
        
        public void setDecorationBounds(Rectangle targetBounds) {
            if (!targetBounds.equals(this.targetBounds)) {
                reset(targetBounds);
            }
        }
        
        final int SPEED = 2;
        private void zoom() {
            alpha += (255 - alpha) / SPEED;
            
            Rectangle r1 = getDecorationBounds();
            Rectangle r2 = targetBounds;
            int dw = (r2.width - r1.width) / SPEED;
            int dh = (r2.height - r1.height) / SPEED;
            r1.x += (r2.x - r1.x) / SPEED;
            r1.y += (r2.y - r1.y) / SPEED;
            r1.width += dw;
            r1.height += dh;
            
            super.setDecorationBounds(r1);
        }

        public void paint(Graphics g) {
            if (zoom) {
                zoom();
                zoom = false;
            }
            g = g.create();
            Color tgt = UIManager.getColor("Table.selectionBackground");
            g.setColor(new Color(tgt.getRed(), tgt.getGreen(), tgt.getBlue(), alpha));
            ((Graphics2D)g).setStroke(new BasicStroke(8f));
            Rectangle r = getDecorationBounds();
            g.drawRect(r.x, r.y, r.width-1, r.height-1);
            g.dispose();
        }

        public void dispose() {
            super.dispose();
            timer.stop();
        }
        
        public void actionPerformed(ActionEvent e) {
            zoom = true;
            repaint();
        }
    }
    
    /** Provide a marquee decoration. */
    private static class Marquee extends AbstractComponentDecorator implements ActionListener {
        private Timer timer;
        private float phase;
        
        final int LINE_WIDTH = 4;
        final int INTERVAL = 1000/24;
        final int SIZE = 8;
        
        public Marquee(JComponent target) {
            super(target);
            timer = new Timer(INTERVAL, this);
            timer.start();
        }
        public void setDecorationBounds(int x, int y, int w, int h) {
            setDecorationBounds(new Rectangle(x, y, w, h));
        }
        public void setDecorationBounds(Rectangle r) {
            r.y -= LINE_WIDTH/2;
            r.height += LINE_WIDTH;
            if (r.height == LINE_WIDTH)
                r.height = LINE_WIDTH*2;
            r.x -= LINE_WIDTH/2;
            r.width += LINE_WIDTH;
            super.setDecorationBounds(r);
        }
        private void light(Graphics g, Color color, int x, int y, int size) {
            g.setColor(color.darker());
            g.fillArc(x, y, size, size, 0, 360);
            g.setColor(color);
            g.fillArc(x+size/8, y+size/8, size/2, size/2, 0, 360);
            g.setColor(color.brighter());
            g.fillArc(x+size/8, y+size/4, size/4, size/4, 0, 360);
        }
        public void paint(Graphics g) {
            Rectangle r = getDecorationBounds();
            g.setColor(UIManager.getColor("Tree.selectionBackground"));
            ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                             RenderingHints.VALUE_ANTIALIAS_ON);
            int w = LINE_WIDTH/2;
            int dw = LINE_WIDTH-w;
            ((Graphics2D)g).setStroke(new BasicStroke(LINE_WIDTH, BasicStroke.CAP_BUTT, 
                                                      BasicStroke.JOIN_ROUND, 10.0f, 
                                                      new float[]{10f, 2f}, phase));
            g.drawRect(r.x+w, r.y+w, r.width-2-dw, r.height-2-dw);
        }
        public void actionPerformed(ActionEvent e) {
            phase += 2f;
            getPainter().repaint();
        }
        public void dispose() {
            timer.stop();
            timer = null;
            super.dispose();
        }
    }
    
    public static void main(String[] args) {
        JFrame dragFrame = new JFrame("Fancy Drops");
        JPanel p = (JPanel)dragFrame.getContentPane();
        
        final JLabel label = new JLabel("Drag Me");
        Font font = label.getFont();
        label.setForeground(Color.green.darker());
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setFont(font.deriveFont(Font.BOLD, font.getSize()*2));
        label.setBorder(BorderFactory.createEmptyBorder(4,4,4,4));
        new DragHandler(label, DnDConstants.ACTION_COPY) {
            protected Transferable getTransferable(DragGestureEvent e) {
                return new StringSelection(label.getText());
            }
            protected Icon getDragIcon(DragGestureEvent e, Point offset) {
                return new ComponentIcon(label, true);
            }
        };
        
        DataFlavor[] acceptableFlavors =  {
            DataFlavor.stringFlavor,
        };
        final JList list = new JList();
        list.setFont(label.getFont());
        new DropHandler(list, DnDConstants.ACTION_COPY, acceptableFlavors) {
            private AbstractComponentDecorator dropArea;
            /** Always drop at the end of the list. */
            protected void drop(DropTargetDropEvent e, int action) throws UnsupportedFlavorException, IOException {
                final List data = new ArrayList();
                for (int i=0;i < list.getModel().getSize();i++) {
                    data.add(list.getModel().getElementAt(i));
                }
                data.add(e.getTransferable().getTransferData(DataFlavor.stringFlavor));
                list.setModel(new AbstractListModel() {
                    public int getSize() {
                        return data.size();
                    }
                    public Object getElementAt(int index) {
                        return data.get(index);
                    }
                });
            }
            protected void paintDropTarget(DropTargetEvent e, int action, Point location) {
                if (action != DnDConstants.ACTION_NONE && location != null) {
                    if (dropArea == null) {
                        dropArea = new Marquee(list);
                    }
                    int count = list.getModel().getSize();
                    if (count == 0) {
                        Dimension size = list.getSize();
                        dropArea.setDecorationBounds(new Rectangle(0, 0, size.width, size.height));
                    }
                    else {
                        Rectangle r = list.getCellBounds(count-1, count-1);
                        r.y += r.height;
                        dropArea.setDecorationBounds(r);
                    }
                }
                else if (dropArea != null) {
                    dropArea.dispose();
                    dropArea = null;
                }
            }            
        };

        final JTree tree = new JTree();
        tree.setFont(label.getFont());
        FontMetrics m = label.getFontMetrics(label.getFont()); 
        int h = m.getHeight();
        tree.setRowHeight(h);
        new DropHandler(tree, DnDConstants.ACTION_COPY, acceptableFlavors) {
            final int MARGIN = 4;
            private AbstractComponentDecorator dropArea;
            class DropLocation {
                TreePath parent;
                int index;
                DropLocation(TreePath p, int idx) {
                    this.parent = p;
                    this.index = idx;
                }
            }
            /** Here's the fancy logic that says where the drop should actually
             * go.
             */
            private DropLocation getDropLocation(Point location) {
                TreePath path = tree.getClosestPathForLocation(location.x, location.y);
                Object o = path.getLastPathComponent();
                Rectangle bounds = tree.getPathBounds(path);
                if (Math.abs(bounds.y - location.y) < MARGIN
                    || Math.abs(bounds.y + bounds.height - location.y) < MARGIN) {
                    // Drop between paths
                    int row = tree.getRowForPath(path);
                    if (location.y < bounds.y + bounds.height / 2) {
                        if (row > 0) {
                            path = tree.getPathForRow(--row);
                            o = path.getLastPathComponent();
                        }
                        else if (!tree.isRootVisible()) {
                            o = tree.getModel().getRoot();
                            return new DropLocation(new TreePath(o), 0);
                        }
                    }
                    if (tree.getModel().isLeaf(o)) {
                        TreePath parentPath = path.getParentPath();
                        Object parent = parentPath.getLastPathComponent();
                        int idx = tree.getModel().getIndexOfChild(parent, o) + 1;
                        return new DropLocation(parentPath, idx);
                    }
                    return new DropLocation(path, 0);
                }
                else if (!tree.getModel().isLeaf(o)) {
                    int count = tree.getModel().getChildCount(o);
                    return new DropLocation(path, count);
                }
                return null;
            }
            /** Figure out where the insertion marker should be drawn. */
            private Rectangle getInsertionBounds(DropLocation loc) {
                final int HEIGHT = 4;
                Rectangle r = tree.getPathBounds(loc.parent);
                if (tree.isExpanded(loc.parent)) {
                    Object parent = loc.parent.getLastPathComponent();
                    if (tree.getModel().getChildCount(parent) > loc.index) {
                        Object child = tree.getModel().getChild(parent, loc.index);
                        r = tree.getPathBounds(loc.parent.pathByAddingChild(child));
                        r.height = 0;
                    }
                    else {
                        r.y += r.height;
                        r.height = 0;
                    }
                }
                return r;
            }
            /** Only allow drops on non-leaf nodes, or between sibling leaf 
             * nodes. 
             */
            protected boolean canDrop(DropTargetEvent e, int action, Point location) {
                DropLocation loc = getDropLocation(location);
                return loc != null;
            }
            /** Decorate the current drop target, removing the decoration if
             * the action is {@link DnDConstants#ACTION_NONE} or if the location
             * is <code>null</code>. 
             */
            protected void paintDropTarget(DropTargetEvent e, int action, Point location) {
                DropLocation loc;
                if (action != DnDConstants.ACTION_NONE && location != null
                    && (loc = getDropLocation(location)) != null) {
                    if (dropArea == null) {
                        dropArea = new Marquee(tree);
                    }
                    Rectangle r = getInsertionBounds(loc);
                    dropArea.setDecorationBounds(r);
                }
                else {
                    if (dropArea != null) {
                        dropArea.dispose();
                        dropArea = null;
                    }
                }
            }
            /** Handle the actual drop. */
            protected void drop(DropTargetDropEvent e, int action) throws UnsupportedFlavorException, IOException {
                DropLocation loc = getDropLocation(e.getLocation());
                if (loc != null) {
                    DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
                    String value = (String)e.getTransferable().getTransferData(DataFlavor.stringFlavor);
                    DefaultMutableTreeNode child = new DefaultMutableTreeNode(value);
                    DefaultMutableTreeNode parent = (DefaultMutableTreeNode)
                        loc.parent.getLastPathComponent();
                    model.insertNodeInto(child, parent, loc.index);
                }
            }
        };
        
        p.add(label);
        JLabel label2 = new JLabel("Drag the label");
        label2.setBorder(BorderFactory.createEmptyBorder(4,4,4,4));
        p.add(label2, BorderLayout.SOUTH);
        
        dragFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        dragFrame.pack();
        dragFrame.setSize(200, 300);
        dragFrame.setLocation(100, 100);
        dragFrame.setVisible(true);
        
        JFrame dropFrame = new JFrame("Drop Over Here");
        dropFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        dropFrame.getContentPane().add(new JScrollPane(list));
        JLabel label3 = new JLabel("Onto the list");
        label3.setBorder(BorderFactory.createEmptyBorder(4,4,4,4));
        dropFrame.getContentPane().add(label3, BorderLayout.SOUTH);
        dropFrame.pack();
        dropFrame.setSize(200, 300);
        dropFrame.setLocation(300, 100);
        dropFrame.setVisible(true);
        
        JFrame dropFrame2 = new JFrame("Drop Over Here");
        dropFrame2.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        dropFrame2.getContentPane().add(new JScrollPane(tree));
        JLabel label4 = new JLabel("Or onto the tree");
        label4.setBorder(BorderFactory.createEmptyBorder(4,4,4,4));
        dropFrame2.getContentPane().add(label4, BorderLayout.SOUTH);
        dropFrame2.pack();
        dropFrame2.setSize(200, 300);
        dropFrame2.setLocation(500, 100);
        dropFrame2.setVisible(true);
        
    }
}
