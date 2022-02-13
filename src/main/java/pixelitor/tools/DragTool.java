/*
 * Copyright 2021 Laszlo Balazs-Csiki and Contributors
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

package pixelitor.tools;

import pixelitor.Composition;
import pixelitor.gui.GlobalEvents;
import pixelitor.tools.util.Drag;
import pixelitor.tools.util.DragDisplayType;
import pixelitor.tools.util.PMouseEvent;

import java.awt.Cursor;
import java.awt.Graphics2D;

/**
 * An abstract superclass for tools which only care about the mouse drag
 * start and end positions, and not about the intermediate mouse positions.
 * The start and end points of the drag gesture are continuously
 * updated in the {@link Drag} object.
 */
public abstract class DragTool extends Tool {
    protected Drag drag;

    private boolean endPointInitialized = false;
    protected boolean spaceDragStartPoint = false;

    // subclasses will automatically support constrained
    // movement when Shift is pressed if this is set to true
    private final boolean constrainIfShiftDown;

    protected DragTool(String name, char activationKeyChar, String iconFileName,
                       String toolMessage, Cursor cursor,
                       boolean constrainIfShiftDown) {

        super(name, activationKeyChar, iconFileName, toolMessage, cursor);

        this.constrainIfShiftDown = constrainIfShiftDown;
    }

    @Override
    public void mousePressed(PMouseEvent e) {
        drag = new Drag();
        drag.setStart(e);

        dragStarted(e);

        endPointInitialized = false;
    }

    @Override
    public void mouseDragged(PMouseEvent e) {
        if (drag.isCanceled()) {
            return;
        }
        if (spaceDragStartPoint) {
            drag.saveEndValues();
        }
        if (constrainIfShiftDown) {
            drag.setConstrained(e.isShiftDown());
        }

        drag.setEnd(e);

        if (spaceDragStartPoint) {
            if (endPointInitialized && GlobalEvents.isSpaceDown()) {
                drag.adjustStartForSpaceDownDrag(e.getView());
            }

            endPointInitialized = true;
        }

        ongoingDrag(e);
    }

    @Override
    public void mouseReleased(PMouseEvent e) {
        if (drag.isCanceled()) {
            return;
        }
        drag.setEnd(e);
        drag.mouseReleased();
        dragFinished(e);
        endPointInitialized = false;
    }

    public abstract void dragStarted(PMouseEvent e);

    public abstract void ongoingDrag(PMouseEvent e);

    public abstract void dragFinished(PMouseEvent e);

    @Override
    public void paintOverImage(Graphics2D g2, Composition comp) {
        if (drag == null || !drag.isDragging()) {
            return;
        }

        getDragDisplayType().draw(g2, drag);
    }

    public DragDisplayType getDragDisplayType() {
        return DragDisplayType.WIDTH_HEIGHT;
    }
}
