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

package pixelitor.filters;

import pixelitor.filters.gui.GroupedRangeParam;
import pixelitor.filters.gui.IntChoiceParam;
import pixelitor.filters.gui.IntChoiceParam.Item;
import pixelitor.filters.gui.RangeParam;
import pixelitor.gui.GUIText;

import java.awt.geom.Path2D;

import static net.jafama.FastMath.cos;
import static net.jafama.FastMath.sin;
import static pixelitor.filters.gui.RandomizePolicy.IGNORE_RANDOMIZE;
import static pixelitor.gui.GUIText.ZOOM;

/**
 * "Spirograph" shape filter
 */
public class Spirograph extends ShapeFilter {
    public static final String NAME = "Spirograph";

    private static final int TYPE_HYPOTROCHOID = 1;
    private static final int TYPE_EPITROCHOID = 2;

    private final RangeParam time = new RangeParam("Time", 0, 185, 800);
    private final GroupedRangeParam radii = new GroupedRangeParam("Radii",
        new RangeParam[]{
            new RangeParam("r", 1, 224, 500),
            new RangeParam("R", 1, 114, 500),
            new RangeParam("d", 0, 189, 500)
        }, false);
    private final IntChoiceParam type = new IntChoiceParam(GUIText.TYPE, new Item[]{
        new Item("Hypotrochoid", TYPE_HYPOTROCHOID),
        new Item("Epitrochoid", TYPE_EPITROCHOID),
    }, IGNORE_RANDOMIZE);

    private final RangeParam zoom = new RangeParam(ZOOM + " (%)", 1, 100, 701);

    public Spirograph() {
        addParamsToFront(
            time,
            radii.notLinkable().withAdjustedRange(0.5),
            type,
            zoom
        );

        helpURL = "https://en.wikipedia.org/wiki/Spirograph";
    }

    @Override
    protected Path2D createShape(int width, int height) {
        Path2D shape = new Path2D.Double();

        double r = radii.getValueAsDouble(0);
        double R = radii.getValueAsDouble(1);
        double d = radii.getValueAsDouble(2);

        double z = zoom.getValueAsDouble() / 100.0;
        r *= z;
        R *= z;
        d *= z;

        double cx = width * center.getRelativeX();
        double cy = height * center.getRelativeY();

        double maxValue = time.getValue();
        if (maxValue == 0.0) {
            return null;
        }

        double combinedR;
        if (type.getValue() == TYPE_HYPOTROCHOID) {
            combinedR = R - r;
        } else if (type.getValue() == TYPE_EPITROCHOID) {
            combinedR = R + r;
        } else {
            throw new IllegalStateException("type = " + type.getValue());
        }

        double dt = 0.05;
        double startX = combinedR + d;
        double startY = 0;
        shape.moveTo(cx + startX, cy + startY);
        for (double t = dt; t < maxValue; t += dt) {
            double x = combinedR * cos(t) + d * cos(combinedR * t / r);
            double y = combinedR * sin(t) - d * sin(combinedR * t / r);

            shape.lineTo(cx + x, cy + y);
        }

        return shape;
    }
}