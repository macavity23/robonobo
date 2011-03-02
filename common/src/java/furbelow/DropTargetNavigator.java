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
import java.awt.Component;
import java.awt.Container;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.dnd.Autoscroll;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DragSourceMotionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.RootPaneContainer;
import javax.swing.Scrollable;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.text.JTextComponent;
import javax.swing.tree.TreePath;

/** Provides auto-scrolling behavior on scrollable components which do not
 * implement {@link Autoscroll}, and various other aids to positioning
 * drop targets during a drag.
 * <ul>
 * <li>Switch tabs in a {@link JTabbedPane}
 * <li>Expand nodes in a {@link JTree}
 * <li>Autoscroll components that wouldn't be otherwise
 * </ul>
 * Ideally, there would be a <i>DropTargetNavigable</i> interface similar
 * to {@link Navigator} here, and components would implement it as needed.
 * For this implementation, custom classes requiring drop target navigation
 * should register a handler for themselves in the static initializer for
 * that class.
 */
public class DropTargetNavigator implements DragSourceListener, DragSourceMotionListener {
    private static final int DEFAULT_NAVIGATION_DELAY = 500;
    /** This performs a more general navigation function than Autoscroll. */
    public interface Navigator {
        /** Return whether the given component should be subject to drop 
         * target navigation, given the current set of items under the cursor.
         */
        boolean isNavigating(Component c, Collection underCursor);
        /** Navigate the given drop target as appropriate, returning
         * a {@link Runnable} capable of restoring the component to its original
         * state when the component becomes no longer active either by moving
         * the cursor out or canceling the drag operation.
         */
        Runnable navigate(Component c, Point where, Runnable previousUndo);
    }
    private static DropTargetNavigator INSTANCE;
    private static int navigationDelay = DEFAULT_NAVIGATION_DELAY;
    private static Map navigators = new WeakHashMap();
    static {
        try {
            navigationDelay = 
                Integer.getInteger("DropTargetNavigator.delay", 
                                   DEFAULT_NAVIGATION_DELAY).intValue();
        }
        catch(SecurityException e) {
        }
        register(JTabbedPane.class, new JTabbedPaneNavigator());
        register(JTree.class, new JTreeNavigator());
    }
    /** Change the delay (in ms) in starting an automatic navigation. */
    public static synchronized void setNavigationDelay(int ms) {
        navigationDelay = ms;
    }
    /** Returns the current setting of the auto-navigation delay. */
    public static synchronized int getNavigationDelay() {
        return navigationDelay;
    }
    /** Register a new navigator for the given component class. */
    public static synchronized void register(Class cls, Navigator navigator) {
        navigators.put(cls, navigator);
    }
    /** Enable system-wide drop target navigation. */
    public static synchronized void enableDropTargetNavigation() {
        if (INSTANCE == null) {
            INSTANCE = new DropTargetNavigator(DragSource.getDefaultDragSource());
        }
    }
    /** Disable system-wide drop target navigation. */
    public static synchronized void disableDropTargetNavigation() {
        if (INSTANCE != null) {
            INSTANCE.dispose();
        }
    }
    private DragSource dragSource;
    private Map restoreMap = new HashMap();
    private Map timers = new WeakHashMap();
    private DropTrackingQueue queue;
    public DropTargetNavigator() {
        this(DragSource.getDefaultDragSource());
    }
    public DropTargetNavigator(DragSource src) {
        this.dragSource = src;
        //System.err.println("Install listeners");
        src.addDragSourceListener(this);
        src.addDragSourceMotionListener(this);
        //System.err.println("Install queue");
        try {
            queue = new DropTrackingQueue();
        }
        catch(Exception e) {
            // won't be able to track
        }
        //System.err.println("queue installed");
    }
    private List getWindows(Window root) {
        List list = new ArrayList();
        if (root == null) {
            Frame[] frames = Frame.getFrames();
            for (int i=0;i < frames.length;i++) {
                list.addAll(getWindows(frames[i]));
            }
        }
        else {
            list.add(root);
            Window[] subs = root.getOwnedWindows();
            for (int i=0;i < subs.length;i++) {
                list.addAll(getWindows(subs[i]));
            }
        }
        return list;
    }
    private Map getComponents(Point screen) {
        List windows = getWindows(null);
        Map comps = new HashMap();
        for (Iterator i=windows.iterator();i.hasNext();) {
            Window w = (Window)i.next();
            Point loc = new Point(screen.x-w.getX(), screen.y-w.getY());
            Component c = findComponentAt(w, loc.x, loc.y);
            if (c != null)
                comps.put(c, SwingUtilities.convertPoint(w, loc, c));
        }
        return comps;
    }

    private JTabbedPane getTabbedPaneForTab(Component c) {
        JTabbedPane p = (JTabbedPane)
            SwingUtilities.getAncestorOfClass(JTabbedPane.class, c);
        if (p != null) {
            int count = p.getTabCount();
            for (int i=0;i < count;i++) {
                if (p.getComponentAt(i) == c) {
                    break;
                }
            }
        }
        return p;
    }

    /** Find the most likely drop target component at the given coordinate.
     */
    private Component findComponentAt(Window w, int wx, int wy) {
        // Prefer content pane contents, then expand to other contents;
        // this prevents stuff in other layers (glass pane, menus, drag images) 
        // from interfering with the drop targeting.
        Container root = w;
        RootPaneContainer rpc = findRootPaneContainer(w);
        if (rpc != null) {
            root = rpc.getContentPane();
        }
        Point where = SwingUtilities.convertPoint(w, wx, wy, root);
        Component target = 
            SwingUtilities.getDeepestComponentAt(root, where.x, where.y);
        if (target == null)
            target = SwingUtilities.getDeepestComponentAt(w, wx, wy);

        // Special case JTabbedPane tabs
        JTabbedPane p = getTabbedPaneForTab(target);
        if (p != null) {
            target = p;
        }
        return target;
    }
    private void update(Point screen) {
        if (screen != null) {
            Map comps = getComponents(screen);
            restore(comps.keySet());
            for (Iterator i=comps.keySet().iterator();i.hasNext();) {
                Component c = (Component)i.next();
                update(c, (Point)comps.get(c));
            }
        }
    }
    
    private boolean needsAutoscroll(Component c) {
        if (c instanceof JTree || c instanceof JTable
            || c instanceof JList || c instanceof JTextComponent) {
            return false;
        }
        return !(c instanceof Autoscroll); 
    }
    
    private void update(Component c, Point where) {

        if (needsAutoscroll(c) 
            && c instanceof JComponent && c instanceof Scrollable) {
            JComponent jc = (JComponent)c;
            Rectangle visible = jc.getVisibleRect();
            Autoscroller scroller = new Autoscroller(jc);
            scroller.autoscroll(where);
            Rectangle visible2 = jc.getVisibleRect();
            if (!visible.equals(visible2)) {
                return;
            }
        }
        Navigator navigator = getNavigator(c);
        if (navigator != null) {
            NavigationTimer timer = (NavigationTimer)timers.get(c);
            if (timer != null) {
                if (!timer.update(where)) {
                    timer = null;
                }
            }
            else {
                timer = new NavigationTimer(c, where);
                timers.put(c, timer);
                timer.start();
            }
        }
    }
    
    public static abstract class AbstractNavigator implements Navigator {
        public boolean isNavigating(Component comp, Collection active) {
            return active.contains(comp);
        }
    }
    
    private static class JTabbedPaneNavigator extends AbstractNavigator {
        /** A tabbed pane should stay "active" if we're over one of its panes. */
        public boolean isNavigating(Component comp, Collection active) {
            for (Iterator i=active.iterator();i.hasNext();) {
                Component c = (Component)i.next();
                if (SwingUtilities.isDescendingFrom(c, comp)) {
                    return true;
                }
            }
            return false;
        }
        public Runnable navigate(Component c, Point where, Runnable prev) {
            final JTabbedPane tab = (JTabbedPane)c;
            final int current = tab.getSelectedIndex();
            int idx = tab.indexAtLocation(where.x, where.y);
            Runnable undo = prev;
            if (idx != -1 && current != idx) {
                if (undo == null) {
                    undo = new Runnable() {
                        public void run() {
                            tab.setSelectedIndex(current);
                        }
                    };
                }
                tab.setSelectedIndex(idx);
            }
            return undo;
        }
    }
    
    private static class JTreeNavigator extends AbstractNavigator {
        public Runnable navigate(Component c, Point where, final Runnable prev) {
            final JTree tree = (JTree)c;
            Runnable undo = prev;
            final TreePath path = tree.getPathForLocation(where.x, where.y);
            if (path != null) {
                if (!tree.isExpanded(path)) {
                    undo = new Runnable() {
                        public void run() {
                            tree.collapsePath(path);
                            if (prev != null)
                                prev.run();
                        }
                    };
                    tree.expandPath(path);
                }
            }
            return undo;
        }
    }
    
    public void dragEnter(DragSourceDragEvent e) {
        update(e.getLocation());
    }
    public void dragOver(DragSourceDragEvent e) {
        update(e.getLocation());
    }
    public void dropActionChanged(DragSourceDragEvent e) { }
    public void dragExit(DragSourceEvent e) { 
        update(e.getLocation());
    }
    public void dragMouseMoved(DragSourceDragEvent e) {
        update(e.getLocation());
    }
    public void dragDropEnd(DragSourceDropEvent e) {
        update(e.getLocation());
        if (e.getDropSuccess()) {
            // Remove the actual drop target from the restore list
            restoreMap.clear();
        }
        restore(Collections.EMPTY_LIST);
    }
    
    private Navigator getNavigator(Component c) {
        Class cls = c.getClass();
        while (cls != null) {
            Navigator n = (Navigator)navigators.get(cls);
            if (n != null) {
                return n;
            }
            cls = cls.getSuperclass();
        }
        return null;
    }
    /** Return whether the given component is still being navigated
     * Usually this just means the cursor is over the component,
     * but a JTabbedPane is still being navigated if the cursor
     * is over one of its tabbed panes.
     */
    private boolean isNavigating(Component comp, Collection active) {
        Navigator n = getNavigator(comp);
        if (n != null) {
            return n.isNavigating(comp, active);
        }
        return active.contains(comp);
    }
    /** Restore any components that have been navigated that are no longer
     * navigating.
     */
    private void restore(Collection active) {
        for (Iterator i=restoreMap.keySet().iterator();i.hasNext();) {
            Component c = (Component)i.next();
            if (!isNavigating(c, active)) {
                Runnable action = (Runnable)restoreMap.get(c);
                action.run();
                i.remove();
                NavigationTimer timer = (NavigationTimer)timers.get(c);
                if (timer != null)
                    timer.dispose();
            }
        }
    }
    public void dispose() {
        dragSource.removeDragSourceListener(this);
        dragSource.removeDragSourceMotionListener(this);
        dragSource = null;
        if (queue != null) {
            queue.dispose();
            queue = null;
        }
    }
    private class DropTrackingQueue extends EventQueue {
        public DropTrackingQueue() {
            Toolkit.getDefaultToolkit().getSystemEventQueue().push(this);
        }
        protected void dispatchEvent(AWTEvent e) {
            if (e instanceof MouseEvent) {
                // May not have security access to package sun.awt.dnd
                if (e.getClass().getName().indexOf("SunDropTargetEvent") != -1) {
                    MouseEvent me = (MouseEvent)e;
                    Point loc = me.getComponent().getLocationOnScreen();
                    loc.translate(me.getX(), me.getY());
                    update(loc);
                }
            }
            super.dispatchEvent(e);
        }
        public void dispose() {
            try { pop(); }
            catch(EmptyStackException e) { }
        }
    }
    private class NavigationTimer extends Timer implements ActionListener {
        private Component component;
        private Point origin, current;
        private Runnable undo;
        public NavigationTimer(Component c, Point where) {
            super(navigationDelay, null);
            this.component = c;
            this.origin = current = where;
            this.undo = (Runnable)restoreMap.get(c);
            addActionListener(this);
            setRepeats(false);
        }
        public boolean update(Point current) {
            this.current = current;
            int dx = Math.abs(current.x - origin.x);
            int dy = Math.abs(current.y - origin.y);
            if (dx > 5 || dy > 5) {
                dispose();
                return false;
            }
            return true;
        }
        public void dispose() {
            stop();
            timers.remove(component);
        }
        public void actionPerformed(ActionEvent e) {
            Navigator navigator = getNavigator(component);
            Runnable r = navigator.navigate(component, current, undo);
            if (r != null) {
                restoreMap.put(component, r);
            }
            dispose();
        }
    }
    /** Find the first instance of {@link RootPaneContainer} in the given
     * container.  Basically finds applets.
     */
    public static RootPaneContainer findRootPaneContainer(Container c) {
        if (c instanceof RootPaneContainer) {
            return (RootPaneContainer)c;
        }
        Component[] kids = c.getComponents();
        for (int i=0;i < kids.length;i++) {
            if (kids[i] instanceof RootPaneContainer)
                return (RootPaneContainer)kids[i];
            if (kids[i] instanceof Container) {
                RootPaneContainer rcp = findRootPaneContainer((Container)kids[i]);
                if (rcp != null)
                    return rcp;
            }
        }
        return null;
    }
}