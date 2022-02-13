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

import pixelitor.colors.Colors;
import pixelitor.filters.gui.*;
import pixelitor.utils.ImageUtils;
import pixelitor.utils.Shapes;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.Serial;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.awt.Color.BLACK;
import static java.awt.Color.WHITE;
import static java.awt.RenderingHints.KEY_ANTIALIASING;
import static java.awt.RenderingHints.VALUE_ANTIALIAS_ON;
import static pixelitor.filters.gui.ColorParam.TransparencyPolicy.USER_ONLY_TRANSPARENCY;

/**
 * Fill with Starburst filter
 */
public class Starburst extends ParametrizedFilter {
    public static final String NAME = "Starburst";
    private static final int SPIRAL_RESOLUTION = 100;

    @Serial
    private static final long serialVersionUID = 1337459373010709379L;

    private final RangeParam numRaysParam = new RangeParam("Number of Rays", 2, 12, 100);
    private final ImagePositionParam center = new ImagePositionParam("Center");
    private final ColorParam bgColor = new ColorParam("Background Color", BLACK, USER_ONLY_TRANSPARENCY);
    private final ColorListParam rayColorsParam = new ColorListParam("Ray Colors",
        1, WHITE, Colors.CW_RED, Colors.CW_GREEN, Colors.CW_BLUE,
        Colors.CW_ORANGE, Colors.CW_TEAL, Colors.CW_VIOLET, Colors.CW_YELLOW);
    private final AngleParam rotate = new AngleParam("Rotate", 0);
    private final RangeParam spiralParam = new RangeParam("Spiral", -200, 0, 200);

    public Starburst() {
        super(false);

        setParams(
            numRaysParam,
            bgColor,
            rayColorsParam,
            center,
            rotate,
            spiralParam
        );
    }

    @Override
    public BufferedImage doTransform(BufferedImage src, BufferedImage dest) {
        dest = ImageUtils.copyImage(src);

        int width = dest.getWidth();
        int height = dest.getHeight();

        Graphics2D g = dest.createGraphics();
        Colors.fillWith(bgColor.getColor(), g, width, height);
        g.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);

        float cx = width * center.getRelativeX();
        float cy = height * center.getRelativeY();

        int numberOfRays = numRaysParam.getValue();

        double sliceWidthAngle = Math.PI / numberOfRays;
        double sliceAngle = rotate.getValueInRadians();

        double radius = width + height; // should be enough even if the center is outside the image
        double spiral = spiralParam.getPercentageValD();

        Color[] rayColors = rayColorsParam.getColors();
        int numRayColors = rayColors.length;
        for (int i = 0; i < numberOfRays; i++) {
            var slice = new Path2D.Double();
            slice.moveTo(cx, cy);

            if (spiral == 0) {
                double p1x = cx + radius * Math.cos(sliceAngle);
                double p1y = cy + radius * Math.sin(sliceAngle);
                slice.lineTo(p1x, p1y);
            } else {
                List<Point2D> points = calcSpiralPathPoints(cx, cy,
                    sliceAngle, radius, SPIRAL_RESOLUTION, spiral);
                Shapes.smoothConnect(points, slice);
            }

            sliceAngle += sliceWidthAngle; // move to the slice's other side

            if (spiral == 0) {
                double p2x = cx + radius * Math.cos(sliceAngle);
                double p2y = cy + radius * Math.sin(sliceAngle);
                slice.lineTo(p2x, p2y);
            } else {
                List<Point2D> points = calcSpiralPathPoints(cx, cy,
                    sliceAngle, radius, SPIRAL_RESOLUTION, spiral);
                Collections.reverse(points);
                Point2D first = points.get(0);
                slice.lineTo(first.getX(), first.getY());
                Shapes.smoothConnect(points, slice);
            }
            slice.closePath();

            g.setColor(rayColors[i % numRayColors]);
            g.fill(slice);

            sliceAngle += sliceWidthAngle; // leave out a slice
        }

        g.dispose();
        return dest;
    }

    @Override
    protected boolean createDefaultDestImg() {
        return false;
    }

    private static List<Point2D> calcSpiralPathPoints(double cx, double cy,
                                                      double startAngle, double radius,
                                                      int resolution, double spiral) {
        List<Point2D> points = new ArrayList<>();
        points.add(new Point2D.Double(cx, cy));
        for (int j = 1; j <= resolution; j++) {
            double r = j * radius / resolution;
            double a = spiral * j / SPIRAL_RESOLUTION;
            double x = cx + r * Math.cos(startAngle + a);
            double y = cy + r * Math.sin(startAngle + a);
            points.add(new Point2D.Double(x, y));
        }
        return points;
    }
}