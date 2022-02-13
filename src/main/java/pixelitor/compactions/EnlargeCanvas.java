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

package pixelitor.compactions;

import pixelitor.Canvas;
import pixelitor.OpenImages;
import pixelitor.gui.View;
import pixelitor.gui.utils.DialogBuilder;
import pixelitor.gui.utils.OpenImageEnabledAction;
import pixelitor.guides.Guides;
import pixelitor.layers.ContentLayer;

import javax.swing.*;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;

/**
 * Enlarges the canvas for all layers of a composition. It's the opposite of cropping:
 * it adds empty space around a layer or makes the hidden part of the layer visible.
 */
public class EnlargeCanvas extends SimpleCompAction {
    public static final String NAME = "Enlarge Canvas";
    private int north;
    private int east;
    private int south;
    private int west;
    private int newCanvasWidth;
    private int newCanvasHeight;

    public EnlargeCanvas(int north, int east, int south, int west) {
        super(NAME, true);
        this.north = north;
        this.east = east;
        this.south = south;
        this.west = west;
    }

    public void setupToFitContentOf(ContentLayer contentLayer) {
        Rectangle contentBounds = contentLayer.getContentBounds();
        Canvas canvas = contentLayer.getComp().getCanvas();

        if (contentBounds.x < -west) {
            west = -contentBounds.x;
        }

        if (contentBounds.y < -north) {
            north = -contentBounds.y;
        }

        int contentMaxX = contentBounds.x + contentBounds.width;
        if (contentMaxX > canvas.getWidth() + east) {
            east = contentMaxX - canvas.getWidth();
        }

        int contentMaxY = contentBounds.y + contentBounds.height;
        if (contentMaxY > canvas.getHeight() + south) {
            south = contentMaxY - canvas.getHeight();
        }
    }

    public boolean doesNothing() {
        return north == 0 && east == 0 && south == 0 && west == 0;
    }

    @Override
    protected void changeCanvasSize(Canvas newCanvas, View view) {
        newCanvasWidth = newCanvas.getWidth() + east + west;
        newCanvasHeight = newCanvas.getHeight() + north + south;
        newCanvas.changeSize(newCanvasWidth, newCanvasHeight, view, false);
    }

    @Override
    protected String getEditName() {
        return NAME;
    }

    @Override
    protected void transform(ContentLayer contentLayer) {
        contentLayer.enlargeCanvas(north, east, south, west);
    }

    @Override
    protected AffineTransform createCanvasTransform(Canvas canvas) {
        return AffineTransform.getTranslateInstance(west, north);
    }

    @Override
    protected Guides createGuidesCopy(Guides oldGuides, View view, Canvas oldCanvas) {
        return oldGuides.copyForEnlargedCanvas(north, east, south, west, view, oldCanvas);
    }

    @Override
    protected String getStatusBarMessage() {
        return "The canvas was enlarged to "
            + newCanvasWidth + " x " + newCanvasHeight + " pixels.";
    }

    public static Action getAction() {
        return new OpenImageEnabledAction("Enlarge Canvas...") {
            @Override
            public void onClick() {
                showInDialog();
            }
        };
    }

    private static void showInDialog() {
        var p = new EnlargeCanvasGUI();
        new DialogBuilder()
            .title(NAME)
            .content(p)
            .okAction(() -> {
                var comp = OpenImages.getActiveComp();
                p.getCompAction(comp.getCanvas()).process(comp);
            })
            .show();
    }
}
