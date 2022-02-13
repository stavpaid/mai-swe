/*
 * Copyright 2018 Laszlo Balazs-Csiki and Contributors
 *
 * This file is part of Pixelitor. Pixelitor is free software: you
 * can redistribute it and/or modify it under the terms of the GNU
 * General Public License, version 3 as published by the Free
 * Software Foundation.
 *
 * Pixelitor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Pixelitor. If not, see <http://www.gnu.org/licenses/>.
 */

package pixelitor.tools.transform;

import pixelitor.gui.View;
import pixelitor.tools.util.DragDisplay;
import pixelitor.tools.util.DraggablePoint;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Dimension2D;

import static pixelitor.tools.util.DragDisplay.BG_WIDTH_PIXEL;

/**
 * The common functionality of corner and edge handles
 */
public abstract class PositionHandle extends DraggablePoint {
    protected final TransformBox box;

    // The sine and cosine of the current rotation angle
    protected double sin;
    protected double cos;

    private final int cursorIndex;
    private final int cursorIndexIO;

    private Direction direction;

    protected PositionHandle(String name, TransformBox box,
                             double x, double y, View view,
                             Color color, Color activeColor,
                             int cursorIndex, int cursorIndexIO) {
        super(name, x, y, view, color, activeColor);
        this.box = box;
        this.cursorIndex = cursorIndex;
        this.cursorIndexIO = cursorIndexIO;
    }

    @Override
    public void mousePressed(double x, double y) {
        super.mousePressed(x, y); // sets dragStartX, dragStartY

        sin = box.getSin();
        cos = box.getCos();
    }

    /**
     * Determines the direction as the box is rotating
     */
    public void recalcDirection(boolean isInsideOut, int cursorOffset) {
        int offset;
        if (isInsideOut) {
            offset = cursorIndexIO + cursorOffset;
        } else {
            // the corners are in default order
            offset = cursorIndex + cursorOffset;
        }
        direction = Direction.atOffset(offset);
        cursor = direction.getCursor();
    }

    public Direction getDirection() {
        return direction;
    }

    @Override
    public void paintHandle(Graphics2D g) {
        super.paintHandle(g);

        if (isActive()) {
            Dimension2D size = box.getRotatedImSize();
            DragDisplay dd = new DragDisplay(g, BG_WIDTH_PIXEL);

            drawDragDisplays(dd, size);

            dd.finish();
        }
    }

    protected abstract void drawDragDisplays(DragDisplay dd, Dimension2D size);
}
