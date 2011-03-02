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

import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.dnd.Autoscroll;
import java.awt.dnd.DropTarget;
import javax.swing.JComponent;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;

/** Provides consistent drag/drop autoscrolling behavior for components
 * which implement {@link Autoscroll}.  The component's implementation 
 * of {@link Autoscroll#getAutoscrollInsets} and 
 * {@link Autoscroll#autoscroll(Point)} should call through to this object's
 * methods.<p>
 * The default behavior is to scroll by the unit/line increment when near
 * the edge of the viewport or the block/page increment when <em>very</em>
 * near the edge. 
 */
public class Autoscroller implements Autoscroll {

    private static final int DEFAULT_MARGIN = 32;
    public static int MARGIN = DEFAULT_MARGIN;
    private static final int ASK = -1;
    
    private JComponent component;
    private int unitIncrement;
    private int blockIncrement;
    private Insets margins;
    private Insets fastMargins;
    
    static {
        try {
            MARGIN = Integer.getInteger("Autoscroller.margin", DEFAULT_MARGIN).intValue();
        }
        catch(SecurityException e) { }
    }
    
    public Autoscroller(JComponent scrolled) {
        this(scrolled, MARGIN, MARGIN/2, ASK, ASK);
    }
    public Autoscroller(JComponent scrolled, 
                        int normal, int fast, int unit, int block) {
        this.component = scrolled;
        margins = new Insets(normal, normal, normal, normal);
        fastMargins = new Insets(fast, fast, fast, fast);
        unitIncrement = unit;
        blockIncrement = block;
    }
    protected Insets fastScrollInsets() {
        return fastMargins;
    }
    protected Insets scrollInsets() {
        return margins;
    }
    /** Return the insets of the component's rectangle that should be 
     * active.  Note that the actual inset values represent pixels in the
     * component coordinate space which appear in the viewport.
     */
    public Insets getAutoscrollInsets() {
        Insets insets = scrollInsets();
        Rectangle rect = component.getVisibleRect();
        return new Insets(rect.y + insets.top, rect.x + insets.left,
                          component.getHeight() - rect.y - rect.height 
                          + insets.bottom - 1,
                          component.getWidth() - rect.x - rect.width 
                          + insets.right - 1);
    }
    
    private int unit(Rectangle visible, int orientation, int direction) {
        if (unitIncrement == ASK) {
            if (component instanceof Scrollable) {
                return ((Scrollable)component).getScrollableUnitIncrement(visible, orientation, direction);
            }
            return 1;
        }
        return unitIncrement;
    }
    private int block(Rectangle visible, int orientation, int direction) {
        if (blockIncrement == ASK) {
            if (component instanceof Scrollable) {
                return ((Scrollable)component).getScrollableBlockIncrement(visible, orientation, direction);
            }
            return 1;
        }
        return blockIncrement;
    }

    /** Invoke from {@link Autoscroll#autoscroll(Point)}. 
     * @param where The current cursor location in the target component
     * coordinate space
     */
    public void autoscroll(Point where) {
        Insets insets = scrollInsets();
        Insets fast = fastScrollInsets();
        Rectangle visible = component.getVisibleRect();
        if (where.x <= visible.x + insets.left) {
            int delta = where.x < visible.x + fast.left
            ? block(visible, SwingConstants.HORIZONTAL, -1)
                : unit(visible, SwingConstants.HORIZONTAL, -1);
            visible.x = Math.max(visible.x - delta, 0);
        }
        else if (where.x >= visible.x + visible.width - insets.right - 1) {
            int delta = where.x >= visible.x + visible.width - fast.right - 1
            ? block(visible, SwingConstants.HORIZONTAL, 1)
                : unit(visible, SwingConstants.HORIZONTAL, 1);
            visible.x = Math.min(visible.x + delta, 
                                 visible.x + component.getWidth() - visible.width - 1);
        }
        if (where.y <= visible.y + insets.top) {
            int delta = where.y < visible.y + fast.top
            ? block(visible, SwingConstants.VERTICAL, -1)
                : unit(visible, SwingConstants.VERTICAL, -1);
            visible.y = Math.max(visible.y - delta, 0);
        }
        else if (where.y >= visible.y + visible.height - insets.bottom - 1) {
            int delta = where.y >= visible.y + visible.height - fast.bottom - 1
            ? block(visible, SwingConstants.VERTICAL, 1)
                : unit(visible, SwingConstants.VERTICAL, 1);
            visible.y = Math.min(visible.y + delta, 
                                 visible.y + component.getHeight() - visible.height - 1);
        }
        component.scrollRectToVisible(visible);
    }
}