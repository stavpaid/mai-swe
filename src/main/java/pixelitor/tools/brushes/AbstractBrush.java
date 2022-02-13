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

package pixelitor.tools.brushes;

import pixelitor.Composition;
import pixelitor.tools.AbstractBrushTool;
import pixelitor.tools.util.PPoint;
import pixelitor.utils.debug.DebugNode;

import java.awt.Graphics2D;

/**
 * An abstract base class for the brushes that are
 * "real" in the sense that they are not decorators
 */
public abstract class AbstractBrush implements Brush {
    protected Graphics2D targetG;
    protected Composition comp;

    protected double radius = AbstractBrushTool.DEFAULT_BRUSH_RADIUS;
    protected double diameter;
    private double maxRadiusDuringStroke = 0;
    protected PPoint previous;

    // true when the mouse is down
    private boolean drawing;

    protected AbstractBrush(double radius) {
        setRadius(radius);
    }

    @Override
    public void setRadius(double radius) {
        this.radius = radius;
        this.diameter = 2 * radius;
        if (radius > maxRadiusDuringStroke) {
            maxRadiusDuringStroke = radius;
        }
    }

    @Override
    public double getMaxEffectiveRadius() {
        // add one to make sure rounding errors don't ruin the undo
        return maxRadiusDuringStroke + 1.0;
    }

    @Override
    public void setTarget(Composition comp, Graphics2D g) {
        this.comp = comp;
        targetG = g;
    }

    // always call it before rememberPrevious!
    protected void repaintComp(PPoint p) {
        comp.repaintRegion(previous, p, diameter);
    }

    // A simple setter, but with a more expressive name.
    // Always call it after repaintComp!
    @Override
    public void rememberPrevious(PPoint p) {
        previous = p;
    }

    @Override
    public PPoint getPrevious() {
        return previous;
    }

    @Override
    public void startAt(PPoint p) {
        // when starting a new stroke, the previous
        // variables should not be set to 0, 0
        // because it causes unnecessary repainting
        rememberPrevious(p);

        initDrawing(p);

        // initialize at the beginning of the brush stroke
        maxRadiusDuringStroke = radius;
    }

    @Override
    public void initDrawing(PPoint p) {
        assert !drawing : "already initialized in " + getClass().getSimpleName();
        drawing = true;
    }

    @Override
    public void lineConnectTo(PPoint p) {
        assert !drawing : "already initialized in " + getClass().getSimpleName();

        if (previous == null) {
            // can happen if the first click (in the tool of after a
            // symmetry activation) is a shift-click
            startAt(p);
        } else {
            initDrawing(p);

            // most brushes connect with lines anyway, but if there is an
            // extra smoothing, this must be overridden
            continueTo(p);
        }
    }

    @Override
    public void finishBrushStroke() {
        assert drawing : "uninitialized brush stroke in " + getClass().getSimpleName();
        drawing = false;
    }

    public double getRadius() {
        return radius;
    }

    @Override
    public boolean isDrawing() {
        return drawing;
    }

    public void settingsChanged() {
        // do nothing as a convenience for subclasses without settings
    }

    @Override
    public DebugNode createDebugNode() {
        var node = new DebugNode("brush", this);
        node.addClass();
        node.addDouble("radius", radius);
        if (previous != null) {
            node.addDouble("previous x", previous.getImX());
            node.addDouble("previous y", previous.getImY());
        }

        return node;
    }
}
