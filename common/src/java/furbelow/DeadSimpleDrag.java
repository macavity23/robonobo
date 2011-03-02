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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.Point;
import java.awt.Graphics;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.io.IOException;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.plaf.metal.MetalIconFactory;
import javax.swing.table.AbstractTableModel;
import javax.swing.tree.TreePath;

public class DeadSimpleDrag {
    public static void main(String[] args) {
        JFrame dragFrame = new JFrame("Dead Simple Drag");
        JFrame dropFrame = new JFrame("Drop Over Here");
        JPanel p = (JPanel)dragFrame.getContentPane();
        
        final JLabel label = new JLabel("Drag Me");
        label.setBorder(BorderFactory.createEmptyBorder(4,4,4,4));
        new DragHandler(label, DnDConstants.ACTION_COPY) {
            protected Transferable getTransferable(DragGestureEvent e) {
                return new StringSelection(label.getText());
            }
            protected Icon getDragIcon(DragGestureEvent e, Point offset) {
                return new ComponentIcon(label, true);
            }
        };
        
        final JTree tree = new JTree();
        // Turn off selection of rows by dragging
        tree.setDragEnabled(true);
        // Turn off built-in swing drag handling
        tree.setTransferHandler(null);
        new DragHandler(tree, DnDConstants.ACTION_COPY) {
            protected boolean canDrag(DragGestureEvent e) {
                Point where = e.getDragOrigin();
                int row = tree.getRowForLocation(where.x, where.y);
                if (row != -1) {
                    TreePath path = tree.getPathForRow(row);
                    return tree.getModel().isLeaf(path.getLastPathComponent());
                }
                return true;
            }
            protected Transferable getTransferable(DragGestureEvent e) {
                Point where = e.getDragOrigin();
                final int row = tree.getRowForLocation(where.x, where.y);
                if (row == -1) {
                    return new StringSelection("full tree");
                }
                Object value = tree.getPathForRow(row).getLastPathComponent();
                return new StringSelection(String.valueOf(value));
            }
            protected Icon getDragIcon(DragGestureEvent e, Point offset) {
                Point where = e.getDragOrigin();
                final int row = tree.getRowForLocation(where.x, where.y);
                if (row != -1) {
                    Rectangle r = tree.getRowBounds(row);
                    offset.setLocation(r.x, r.y);
                }
                return new ComponentIcon(tree, true) {
                    public void paintIcon(Component c, Graphics g, int x, int y) {
                        g = g.create();
                        if (row != -1) {
                            Rectangle r = tree.getRowBounds(row);
                            g.translate(-r.x, -r.y);
                            g.setClip(new Rectangle(x+r.x, y+r.y, r.width, r.height));
                            super.paintIcon(c, g, x, y);
                        }
                        else {
                            super.paintIcon(c, g, x, y);
                        }
                        g.dispose();
                    }
                };
            }
        };
        
        p.add(label, BorderLayout.NORTH);
        p.add(new JScrollPane(tree));
        JLabel label2 = new JLabel("Drag label, leaves, or entire tree");
        label2.setBorder(BorderFactory.createEmptyBorder(4,4,4,4));
        p.add(label2, BorderLayout.SOUTH);
        
        dragFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        dragFrame.pack();
        dragFrame.setSize(200, 300);
        dragFrame.setLocation(100, 100);
        dragFrame.setVisible(true);
        
        dropFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        dropFrame.pack();
        dropFrame.setSize(200, 300);
        dropFrame.setLocation(300, 100);
        dropFrame.setVisible(true);
        
    }
}
