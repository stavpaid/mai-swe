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

package pixelitor.filters.painters;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.GeneralPath;

import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * A rectangle rotated around its center
 */
public class RotatedRectangle {
    private final double origTopLeftX;
    private final double origTopLeftY;
    private final double origTopRightX;
    private final double origTopRightY;
    private final double origBottomRightX;
    private final double origBottomRightY;
    private final double origBottomLeftX;
    private final double origBottomLeftY;

    private double topLeftX;
    private double topLeftY;
    private double topRightX;
    private double topRightY;
    private double bottomRightX;
    private double bottomRightY;
    private double bottomLeftX;
    private double bottomLeftY;

    public RotatedRectangle(Rectangle r, double theta) {
        this(r.getX(), r.getY(), r.getWidth(), r.getHeight(), theta);
    }

    public RotatedRectangle(double x, double y, double width, double height, double theta) {
        origTopLeftX = x;
        origTopLeftY = y;

        origTopRightX = x + width;
        origTopRightY = y;

        origBottomRightX = origTopRightX;
        origBottomRightY = y + height;

        origBottomLeftX = x;
        origBottomLeftY = origBottomRightY;

        double cx = x + width / 2.0;
        double cy = y + height / 2.0;

        double xDist = origTopLeftX - cx;
        double yDist = origTopLeftY - cy;

        double cos = Math.cos(theta);
        double sin = Math.sin(theta);

        double xCos = xDist * cos;
        double ySin = yDist * sin;
        double xSin = xDist * sin;
        double yCos = yDist * cos;

        topLeftX = cx + xCos - ySin;
        topLeftY = cy + xSin + yCos;

        topRightX = cx - xCos - ySin;
        topRightY = cy - xSin + yCos;

        // taking advantage of rotation symmetry
        double dx = topLeftX - origTopLeftX;
        double dy = topLeftY - origTopLeftY;
        bottomRightX = origBottomRightX - dx;
        bottomRightY = origBottomRightY - dy;

        dx = topRightX - origTopRightX;
        dy = topRightY - origTopRightY;
        bottomLeftX = origBottomLeftX - dx;
        bottomLeftY = origBottomLeftY - dy;
    }

    public double getTopLeftX() {
        return topLeftX;
    }

    public double getTopLeftY() {
        return topLeftY;
    }

    public Shape asShape() {
        var path = new GeneralPath();
        path.moveTo(topLeftX, topLeftY);
        path.lineTo(topRightX, topRightY);
        path.lineTo(bottomRightX, bottomRightY);
        path.lineTo(bottomLeftX, bottomLeftY);
        path.closePath();
        return path;
    }

    public Rectangle getBoundingBox() {
        double minX = min(min(topLeftX, topRightX), min(bottomRightX, bottomLeftX));
        double minY = min(min(topLeftY, topRightY), min(bottomRightY, bottomLeftY));
        double maxX = max(max(topLeftX, topRightX), max(bottomRightX, bottomLeftX));
        double maxY = max(max(topLeftY, topRightY), max(bottomRightY, bottomLeftY));

        int width = (int) (maxX - minX);
        int height = (int) (maxY - minY);
        return new Rectangle((int) minX, (int) minY, width, height);
    }

    public void translate(double dx, double dy) {
        topLeftX += dx;
        topLeftY += dy;
        topRightX += dx;
        topRightY += dy;
        bottomRightX += dx;
        bottomRightY += dy;
        bottomLeftX += dx;
        bottomLeftY += dy;
    }

    // paints the original and rotated corners for debugging
    public void paintCorners(Graphics2D g) {
        g.setColor(Color.RED);
        g.fillOval((int) topLeftX - 5, (int) topLeftY - 5, 10, 10);
        g.fillOval((int) origTopLeftX - 5, (int) origTopLeftY - 5, 10, 10);

        g.setColor(Color.BLUE);
        g.fillOval((int) topRightX - 5, (int) topRightY - 5, 10, 10);
        g.fillOval((int) origTopRightX - 5, (int) origTopRightY - 5, 10, 10);

        g.setColor(Color.YELLOW);
        g.fillOval((int) bottomRightX - 5, (int) bottomRightY - 5, 10, 10);
        g.fillOval((int) origBottomRightX - 5, (int) origBottomRightY - 5, 10, 10);

        g.setColor(new Color(0, 100, 0));
        g.fillOval((int) bottomLeftX - 5, (int) bottomLeftY - 5, 10, 10);
        g.fillOval((int) origBottomLeftX - 5, (int) origBottomLeftY - 5, 10, 10);
    }
}
