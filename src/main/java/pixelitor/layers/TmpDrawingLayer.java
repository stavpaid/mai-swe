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

package pixelitor.layers;

import pixelitor.Composition;
import pixelitor.selection.Selection;
import pixelitor.tools.util.Drag;
import pixelitor.utils.ImageUtils;

import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.util.Objects;

/**
 * A temporary drawing layer for the tools that use blending modes.
 */
public class TmpDrawingLayer {
    private BufferedImage image;
    private final Graphics2D g;
    private final Composite composite;

    private final boolean smallImage;
    private int selStartX = 0;
    private int selStartY = 0;

    public TmpDrawingLayer(ImageLayer imageLayer, Composite composite, boolean softSelection) {
        this.composite = Objects.requireNonNull(composite);

        Composition comp = imageLayer.getComp();
        Selection sel = comp.getSelection();
        if (sel != null) {
            Shape selShape = sel.getShape();
            if (sel.isRectangular() || !softSelection) {
                // hard selection clipping
                image = comp.getCanvas().createTmpImage();
                g = image.createGraphics();
                g.setClip(selShape);
                smallImage = false;
            } else {
                // Sets up the image of this temporary layer to act as
                // the intermediate image of a soft selection clipping.
                //
                Rectangle bounds = selShape.getBounds();
                selStartX = bounds.x;
                selStartY = bounds.y;
                image = ImageUtils.createSysCompatibleImage(bounds.width, bounds.height);
                g = ImageUtils.setupForSoftSelection(image, selShape, selStartX, selStartY);
//                g.translate(selStartX, selStartY);
                smallImage = true;
            }
        } else {
            // no selection
            image = comp.getCanvas().createTmpImage();
            g = image.createGraphics();
            smallImage = false;
        }
    }

    public Graphics2D getGraphics() {
        return g;
    }

    public boolean hasSmallImage() {
        return smallImage;
    }

    public int getWidth() {
        return image.getWidth();
    }

    public int getHeight() {
        return image.getHeight();
    }

    public void dispose() {
        g.dispose();
        image.flush();
        image = null;
    }

    public void paintOn(Graphics2D g, int tx, int ty) {
        g.setComposite(composite);

        assert smallImage || (selStartX == 0 && selStartY == 0);
        g.drawImage(image, tx + selStartX, ty + selStartY, null);
    }

    public Drag translateDrag(Drag drag) {
        if (smallImage) {
            // the drag was relative to the canvas, but if small images are used,
            // then it must be transformed to be relative to the selection
            return drag.translatedCopy(-selStartX, -selStartY);
        } else {
            return drag;
        }
    }
}
