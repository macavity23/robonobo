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

import java.awt.*;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.*;
import java.util.Timer;
import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;

/**
 * Animates moving tree cells out of the way for a potential drop. This
 * decorator completely over-paints the target JTree, optionally
 * painting a dragged item and animating creation of a space for the
 * dragged item to be dropped.
 */
// TODO: limit drag to below root node, if visible
// TODO: spring-loaded folders: how to drop on/before
// TODO: un-spring folders: but collapsing a previous folder is disruptive
// TODO: change graphics to left of node (e.g. if next-to-last becomes last)
// TODO: animate canceled drops (slide dragged object back to its home)
//
public class TreeAnimator extends AbstractComponentDecorator implements TreeExpansionListener {
    
    protected class DragDestination {
        /** Path to new parent. */
        public TreePath parentPath;
        /** Index within new parent. */
        public int index;
        /** Actual visible location of the insertion space. */
        public int placeholderRow;

        public DragDestination(TreePath path, int i, int insertionSpaceRow) {
            this.parentPath = path;
            this.index = i;
            this.placeholderRow = insertionSpaceRow;
        }
        public String toString() {
            return parentPath.toString() + ":" + index + " (" + placeholderRow + ")";
        }
    }

    /**
     * Animation repaint interval. Make this larger to slow down the
     * animation.
     */
    private static final int INTERVAL = 1000 / 24;
    private static Timer timer = new Timer(true);

    static final int HORIZONTAL_THRESHOLD = 5;

    /** Simple decorator to provide the ghosted image being dragged. */
    private final class GhostedDragImage extends AbstractComponentDecorator {
        private TreePath path;
        private Point location;
        private Point offset;

        public GhostedDragImage(TreePath path, Point origin) {
            super(tree, JLayeredPane.DRAG_LAYER.intValue());
            this.path = path;
            Rectangle b = tree.getPathBounds(path);
            location = origin;
            this.offset = new Point(origin.x - b.x, origin.y - b.y);
        }

        public void setLocation(Point where, TreePath parentPath) {
            this.location = new Point(where);
            Rectangle b = tree.getPathBounds(path);
            Rectangle lastRow = tree.getRowBounds(tree.getRowCount()-1);
            int height = lastRow.y + lastRow.height;
            Point origin = new Point(b.x, location.y - offset.y);
            // Select a horizontal offset appropriate for a child of the
            // given parent path
            if (parentPath != null) {
                int count = path.getPathCount();
                if (!tree.isRootVisible() || !tree.getShowsRootHandles())
                    --count;
                Insets insets = tree.getInsets();
                int delta = (origin.x - (insets != null ? insets.left : 0)) / count;
                b = tree.getPathBounds(parentPath);
                origin.x = b.x + delta;
            }
            location.x = origin.x;
            location.y = Math.max(0, origin.y);
            location.y = Math.min(location.y, height - b.height);
            getPainter().repaint();
        }
        
        public Point getLocation() {
            return location;
        }
        
        public Rectangle getBounds() {
            return new Rectangle(location.x, location.y, 
                                 getPainter().getWidth(), getPainter().getHeight());
        }

        public void paint(Graphics g) {
            Rectangle b = tree.getPathBounds(path);
            g = g.create(location.x, location.y, b.width, b.height);
            ((Graphics2D)g).translate(-b.x, -b.y);
            ((Graphics2D)g).setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
                                                                    0.5f));
            tree.paint(g);
        }
    }
    private final class Counter extends TimerTask {
        public boolean painted;
        public synchronized void painted() {
            painted = true;
        }
        public void run() {
            synchronized(bounds) {
                if (painted && moveTowardProjectedLocation()) {
                    synchronized(this) {
                        painted = false;
                        repaint();
                    }
                }
            }
        }
    }

    private Counter counter;
    /** Index of insertion point. */
    private int placeholderRow = -1;
    private TreePath placeholderParentPath;
    private int placeholderIndex = -1;
    /** Actual point used to determine placeholder row. */
    private Point placeholderLocation;
    /** Object being dragged, if any. */
    private TreePath draggedPath;
    private JTree tree;
    private Map bounds = new HashMap();
    private GhostedDragImage dragImage;
    private Point origin;
    private boolean dragActive;

    public TreeAnimator(final JTree tree) {
        super(tree);
        this.tree = tree;
    }

    /** The default assumes any node except the root may be moved. */
    protected boolean canMove(TreePath path) {
        Object o = path.getLastPathComponent();
        return !o.equals(tree.getModel().getRoot());
    }

    /**
     * Returns whether the node at the given path may be moved to the given
     * index on the given target path.  The default disallows moves only
     * if the target is a descendent of the moved path.
     */
    protected boolean canMove(TreePath fromPath, TreePath toPath, int index) {
        if (canMove(fromPath)) {
            TreePath testPath = toPath;
            while (testPath != null) {
                if (testPath.equals(fromPath))
                    return false;
                testPath = testPath.getParentPath();
            }
            return true;
        }
        return false;
    }

    /**
     * Request that the node on the given path be moved to the given
     * index on the given target path. If toPath is the parent path to
     * fromPath, then the index represents the insertion index
     * <em>after</em> the object is removed from its current index.
     */
    protected void moveNode(TreePath fromPath, TreePath toPath, int index) {
        Object moved = fromPath.getLastPathComponent();
        Object movedTo = toPath.getLastPathComponent();
        if (tree.getModel() instanceof DefaultTreeModel
            && moved instanceof MutableTreeNode
            && movedTo instanceof MutableTreeNode) {
            DefaultTreeModel treeModel = (DefaultTreeModel)tree.getModel();
            MutableTreeNode child = (MutableTreeNode)moved;
            MutableTreeNode newParent = (MutableTreeNode)movedTo;
            treeModel.removeNodeFromParent(child);
            treeModel.insertNodeInto(child, newParent, index);
        }
        else {
            throw new UnsupportedOperationException("You must override move()");
        }
    }        
    
    private int getIndex(TreePath parentPath, TreePath childPath) {
        Object parent = parentPath.getLastPathComponent();
        Object child = childPath.getLastPathComponent();
        return tree.getModel().getIndexOfChild(parent, child);
    }

    /** Start a local drag. Returns whether the drag is started. */
    public boolean startDrag(Point where) {
        draggedPath = tree.getPathForLocation(where.x, where.y);
        if (draggedPath != null && canMove(draggedPath)) {
            dragActive = true;
            tree.collapsePath(draggedPath);
            origin = where;
            placeholderRow = tree.getRowForPath(draggedPath);
            placeholderParentPath = draggedPath.getParentPath();
            placeholderIndex = getIndex(placeholderParentPath, draggedPath);
            placeholderLocation = new Point(where);
            dragImage = new GhostedDragImage(draggedPath, origin);
            return true;
        }
        return false;
    }
    
    public void treeExpanded(TreeExpansionEvent e) {
        synchronized(bounds) {
            int oldRowCount = bounds.size();
            int rows = tree.getRowCount();
            int start = tree.getRowForPath(e.getPath()) + 1;
            Rectangle rect = tree.getPathBounds(e.getPath());
            for (int i=0;i < rows - oldRowCount;i++) {
                TreePath path = tree.getPathForRow(start + i);
                bounds.put(path, new Rectangle(rect));
            }
        }
        repaint();
    }
    
    public void treeCollapsed(TreeExpansionEvent e) {
        synchronized(bounds) {
            for (Iterator i=bounds.keySet().iterator();i.hasNext();) {
                TreePath path = (TreePath)i.next();
                if (tree.getRowForPath(path) == -1) {
                    i.remove();
                }
            }
        }
        repaint();
    }
    
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            tree.addTreeExpansionListener(this);
            int size = tree.getRowCount();
            synchronized(bounds) {
                for (int i = 0; i < size; i++) {
                    TreePath path = tree.getPathForRow(i);
                    bounds.put(path, getProjectedPathBounds(path));
                }
            }
            counter = new Counter();
            timer.schedule(counter, INTERVAL, INTERVAL);
        }
        else {
            tree.removeTreeExpansionListener(this);
            synchronized(bounds) {
                bounds.clear();
            }
            if (counter != null) {
                counter.cancel();
                counter = null;
            }
        }
    }

    /** End an internal drag. */
    public void endDrag(Point where) {
        if (!dragActive)
            throw new IllegalStateException("Not dragging");
        DragDestination loc = getDragDestination(where);
        int draggedRow = tree.getRowForPath(draggedPath);
        Rectangle ghostBounds = dragImage.getBounds();
        dragImage.dispose();
        dragImage = null;
        placeholderRow = -1;
        placeholderParentPath = null;
        placeholderIndex = -1;
        placeholderLocation = null;
        if (loc != null && loc.placeholderRow != -1 
            && (loc.placeholderRow != draggedRow
                || !loc.parentPath.equals(draggedPath.getParentPath()))) {
            moveNode(draggedPath, loc.parentPath, loc.index);
            synchronized(bounds) {
                // Set the dragged item's location to the current ghost location
                bounds.put(tree.getPathForRow(loc.placeholderRow), ghostBounds);
            }
        }
        draggedPath = null;
        dragActive = false;
    }
    
    public void dispose() {
        tree.removeTreeExpansionListener(this);
        super.dispose();
    }

    private boolean moveTowardProjectedLocation() {
        boolean changed = false;
        int count = 0;
        for (Iterator i = bounds.keySet().iterator(); i.hasNext();) {
            TreePath path = (TreePath)i.next();
            Rectangle current = (Rectangle)bounds.get(path);
            if (current == null) {
                System.err.println("warning: no current bounds for " + path);
                i.remove();
                continue;
            }
            Rectangle end = getProjectedPathBounds(path);
            if (end == null) {
                System.err.println("warning: no final bounds for " + path);
                i.remove();
                continue;
            }
            if (current.x != end.x || current.y != end.y) {
                int xdelta = (end.x - current.x) / 2;
                int ydelta = (end.y - current.y) / 2;
                if (xdelta == 0)
                    current.x = end.x;
                else
                    current.x += xdelta;
                if (ydelta == 0)
                    current.y = end.y;
                else
                    current.y += ydelta;
                bounds.put(path, current);
                changed = true;
                ++count;
            }
        }
        return changed;
    }

    /** Return a proposed insertion location for the given coordinate given in
     * actual JTree coordinate space.  If there is no vertical row change from
     * the current insertion position, then horizontal movement is used to
     * make changes in depth.
     * Returns null if no insertion is allowed at the given location.
     */
    protected DragDestination getDragDestination(Point where) {
        int x = where.x;
        int y = where.y;
        int size = tree.getRowCount();
        Rectangle appendBounds = tree.getRowBounds(size - 1);
        appendBounds.y += appendBounds.height;
        appendBounds.height = 0;
        int draggedRow = tree.getRowForPath(draggedPath);
        int cursorRow = tree.getClosestRowForLocation(x, y);
        TreePath parentPath = null;
        int index = 0;
        if (cursorRow == draggedRow 
            && Math.abs(where.x-placeholderLocation.x) < HORIZONTAL_THRESHOLD) {
            // no-op
            parentPath = draggedPath.getParentPath();
            index = getIndex(parentPath, draggedPath);
        }
        else if (cursorRow == 0) {
            // Can't insert above root
            if (tree.isRootVisible()) {
                return null;
            }
            // Insert as first child of root
            parentPath = new TreePath(tree.getModel().getRoot());
            index = 0;
        }
        else {
            // Use the previous path as reference
            int priorRow = cursorRow - 1;
            if (draggedRow <= priorRow) {
                ++priorRow;
            }
            TreePath priorPath = tree.getPathForRow(priorRow);
            boolean isLeaf = tree.getModel().isLeaf(priorPath.getLastPathComponent());
            if (!isLeaf && tree.isExpanded(priorPath)) {
                parentPath = priorPath;
                index = 0;
            }
            else {
                parentPath = priorPath.getParentPath();
                index = getIndex(parentPath, priorPath) + 1;
                index = adjustIndex(parentPath, index);
            }
        }
        
        // If there's no row change, check for horizontal movement to 
        // change the target depth
        if (cursorRow == placeholderRow
            && horizontalMovementAllowed(cursorRow, draggedRow)) {
            // move deeper
            if (where.x >= placeholderLocation.x + HORIZONTAL_THRESHOLD) {
                if (parentPath.equals(placeholderParentPath)) {
                    index = placeholderIndex;
                }
                else {
                    while (!parentPath.getParentPath().equals(placeholderParentPath)) {
                        parentPath = parentPath.getParentPath();
                    }
                    index = tree.getModel().getChildCount(parentPath.getLastPathComponent());
                    index = adjustIndex(parentPath, index);
                }
            }
            // move up in hierarchy
            else if (where.x <= placeholderLocation.x - HORIZONTAL_THRESHOLD) {
                if (placeholderParentPath.getParentPath() != null) {
                    parentPath = placeholderParentPath.getParentPath();
                    index = getIndex(parentPath, placeholderParentPath) + 1;
                    index = adjustIndex(parentPath, index);
                }
                else {
                    parentPath = placeholderParentPath;
                    index = placeholderIndex;
                }
            }
            else {
                parentPath = placeholderParentPath;
                index = placeholderIndex;
            }
        }

        return new DragDestination(parentPath, index, cursorRow);
    }

    /** Disallow horizontal movement if the preceding and following nodes
     * have the same parent, or if the dragged node is the first child and
     * there are subsequent children.
     */
    private boolean horizontalMovementAllowed(int cursorRow, int draggedRow) {
        int priorRow = cursorRow - 1;
        if (draggedRow <= priorRow) {
            ++priorRow;
        }
        TreePath priorPath = tree.getPathForRow(priorRow);
        int nextRow = cursorRow;
        if (draggedRow <= nextRow) {
            ++nextRow;
        }
        TreePath nextPath = tree.getPathForRow(nextRow);
        TreePath parentPath = priorPath.getParentPath();
        if (parentPath != null) {
            if (nextPath != null) {
                if (parentPath.equals(nextPath.getParentPath())
                    && parentPath.equals(placeholderParentPath)) {
                    return false;
                }
            }
        }
        if (placeholderParentPath.equals(priorPath)
            && priorPath.isDescendant(nextPath)) {
            return false;
        }
        return true;
    }

    private int adjustIndex(TreePath parentPath, int index) {
        if (parentPath.equals(draggedPath.getParentPath())) {
            int draggedIndex = getIndex(parentPath, draggedPath);
            if (draggedIndex < index) {
                --index;
            }
        }
        return index;
    }

    /** Invoke this method as the cursor location changes. */
    public void setPlaceholderLocation(Point where) {
        if (!dragActive)
            throw new IllegalStateException("Not dragging");
        // Avoid painting focus border and/or selection bgs, kind of a hack
        getPainter().requestFocus();
        tree.clearSelection();
        // end hack
        DragDestination loc = getDragDestination(where);
        TreePath parentPath = null;
        if (loc != null && draggedPath != null) {
            if (canMove(draggedPath, loc.parentPath, loc.index)) {
                int lastRow = placeholderRow;
                parentPath = loc.parentPath;
                setPlaceholderRow(loc.placeholderRow);
                if (lastRow != loc.placeholderRow 
                    || !parentPath.equals(placeholderParentPath)
                    || Math.abs(where.x - placeholderLocation.x) >= HORIZONTAL_THRESHOLD) {
                    placeholderLocation = new Point(where);
                }
                placeholderParentPath = parentPath;
                placeholderIndex = loc.index;
            }
        }
        dragImage.setLocation(where, parentPath);
    }
    
    protected int getPlaceholderRow() { return placeholderRow; }

    private void setPlaceholderRow(int idx) {
        if (idx != placeholderRow) {
            placeholderRow = idx;
            repaint();
        }
    }

    private Rectangle getProjectedPathBounds(TreePath path) {
        Rectangle pathBounds = tree.getPathBounds(path);
        if (draggedPath != null) {
            int row = tree.getRowForPath(path);
            int removalRow = tree.getRowForPath(draggedPath);
            Rectangle draggedBounds = tree.getPathBounds(draggedPath);
            if (removalRow < row && row <= placeholderRow) {
                pathBounds.y -= draggedBounds.height;
            }
            else if (placeholderRow <= row && row < removalRow) {
                pathBounds.y += draggedBounds.height;
            }
        }
        return pathBounds;
    }

    /** Returns the bounds of the current path, which may be in motion
     * towards its final destination.
     */
    private Rectangle getCurrentCellBounds(TreePath path) {
        synchronized(bounds) {
            Rectangle after = getProjectedPathBounds(path);
            Rectangle current = (Rectangle)bounds.get(path);
            if (current != null) {
                after.x = current.x;
                after.y = current.y;
            }
            return after;
        }
    }

    public void paint(Graphics g) {
        boolean db = tree.isDoubleBuffered();
        tree.setDoubleBuffered(false);
        try {
            Rectangle b = getDecorationBounds();
            g.setColor(tree.getBackground());
            g.fillRect(b.x, b.y, b.width, b.height);
            int prevIndex = -1;
            Rectangle prevBounds = null;
            // Draw last to first, to avoid having children occlude parents
            // when nodes expand
            for (int i = tree.getRowCount()-1; i >=0; i--) {
                TreePath path = tree.getPathForRow(i);
                if (path.equals(draggedPath)) {
                    continue;
                }
                // visible bounds of the row (may be in motion)
                Rectangle visibleBounds = getCurrentCellBounds(tree.getPathForRow(i));
                // actual offset of the row in the tree
                Rectangle treeRowBounds = tree.getRowBounds(i);
                // If there's a gap between the previous and current rows,
                // repeat the left-most graphics of the current row in the gap
                if (prevIndex != -1 && prevBounds.y > visibleBounds.y + visibleBounds.height) {
                    Rectangle space = 
                        new Rectangle(0, visibleBounds.y + visibleBounds.height, 
                                      prevBounds.x, prevBounds.y - visibleBounds.y - visibleBounds.height);
                    Rectangle prevTreeRowBounds = tree.getRowBounds(prevIndex);
                    for (int j = 0; j < space.height; j++) {
                        Graphics g2 = g.create(space.x, space.y + j,
                                               space.width, 1);
                        ((Graphics2D)g2).translate(0, -prevTreeRowBounds.y - 1);
                        tree.paint(g2);
                    }
                }
                Graphics g2 = g.create(0, visibleBounds.y, visibleBounds.x + visibleBounds.width, visibleBounds.height);
                ((Graphics2D)g2).translate(0, -treeRowBounds.y);
                tree.paint(g2);
                prevIndex = i;
                prevBounds = visibleBounds;
            }
            if (counter != null)
                counter.painted();
        }
        finally {
            tree.setDoubleBuffered(db);
        }
    }

    /**
     * Simple JTree-local drag/drop handler. Invokes the animator
     * according to user input. A similar method could be used to accept
     * drags originating outside of the JTree.
     */
    static class Listener extends MouseAdapter implements MouseMotionListener {
        private TreeAnimator animator;
        private boolean dragActive;
        private Point origin;

        public Listener(TreeAnimator smoother) {
            this.animator = smoother;
        }

        private boolean sufficientMove(Point where) {
            int dx = Math.abs(origin.x - where.x);
            int dy = Math.abs(origin.y - where.y);
            return Math.sqrt(dx * dx + dy * dy) > 5;
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
                    dragActive = animator.startDrag(origin);
                }
            }
            if (dragActive)
                animator.setPlaceholderLocation(e.getPoint());
        }

        public void mouseExited(MouseEvent e) {
            if (dragActive)
                animator.setPlaceholderLocation(e.getPoint());
        }

        public void mouseEntered(MouseEvent e) {
            if (dragActive)
                animator.setPlaceholderLocation(e.getPoint());
        }

        public void mouseMoved(MouseEvent e) {
        }
    }

    /**
     * Throw up a frame to demonstrate the animator at work. 
     */
    public static void main(String[] args) {
        JFrame f = new JFrame("Animated Tree Effects");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        final JTree tree = new JTree();
        tree.setFont(tree.getFont().deriveFont(Font.BOLD,
                                               tree.getFont().getSize() * 1.4f));
        TreeAnimator animator = new TreeAnimator(tree);
        Listener listener = new Listener(animator);
        tree.addMouseListener(listener);
        tree.addMouseMotionListener(listener);
        JLabel label = new JLabel("Drag items to reorder");
        label.setBorder(new EmptyBorder(4, 4, 4, 4));
        label.setFont(label.getFont().deriveFont(Font.BOLD,
                                                 label.getFont().getSize() * 2));
        label.putClientProperty("decorator",
                                new AbstractComponentDecorator(label, -1) {
                                    public void paint(Graphics g) {
                                        Rectangle b = getDecorationBounds();
                                        ((Graphics2D)g).setPaint(new GradientPaint(0, b.height / 2,
                                                                                   UIManager.getColor("Tree.selectionBackground"),
                                                                                   b.width / 2,
                                                                                   b.height / 2,
                                                                                   Color.white));
                                        g.fillRect(b.x, b.y, b.width, b.height);
                                    }
                                });
        f.getContentPane().add(label, BorderLayout.NORTH);
        f.getContentPane().add(new JScrollPane(tree));
        f.pack();
        f.setVisible(true);
    }
}
