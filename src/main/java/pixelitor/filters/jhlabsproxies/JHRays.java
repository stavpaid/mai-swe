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

package pixelitor.filters.jhlabsproxies;

import com.jhlabs.composite.MiscComposite;
import com.jhlabs.image.RaysFilter;
import pixelitor.filters.ParametrizedFilter;
import pixelitor.filters.ResizingFilterHelper;
import pixelitor.filters.gui.BooleanParam;
import pixelitor.filters.gui.ImagePositionParam;
import pixelitor.filters.gui.RangeParam;
import pixelitor.utils.ImageUtils;
import pixelitor.utils.StatusBarProgressTracker;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import static pixelitor.filters.ResizingFilterHelper.ScaleUpQuality.BILINEAR_FAST;
import static pixelitor.filters.gui.RandomizePolicy.IGNORE_RANDOMIZE;
import static pixelitor.gui.GUIText.OPACITY;

/**
 * Rays filter based on the JHLabs RaysFilter
 */
public class JHRays extends ParametrizedFilter {
    public static final String NAME = "Rays";

    private final ImagePositionParam center = new ImagePositionParam("Light Source");
    private final RangeParam rotation = new RangeParam("Twirl", -90, 0, 90);
    private final RangeParam length = new RangeParam("Length", 0, 20, 200);
    private final RangeParam opacity = new RangeParam(OPACITY, 0, 80, 100);
    private final RangeParam strength = new RangeParam("Strength", 0, 200, 500);
    private final RangeParam threshold = new RangeParam("Threshold (%)", 0, 25, 100);
    private final BooleanParam raysOnly = new BooleanParam("Rays Only", false, IGNORE_RANDOMIZE);

    private RaysFilter filter;

    public JHRays() {
        super(true);

        setParams(
            center,
            length,
            threshold,
            strength,
            opacity,
            rotation,
            raysOnly
        );
    }

    @Override
    public BufferedImage doTransform(BufferedImage src, BufferedImage dest) {
        if (filter == null) {
            filter = new RaysFilter(NAME);
        }

        filter.setCentreX(center.getRelativeX());
        filter.setCentreY(center.getRelativeY());
        filter.setStrength(strength.getPercentageValF());
        filter.setRotation(rotation.getValueInRadians());
//        filter.setOpacity(opacity.getValueAsPercentage());
        filter.setThreshold(threshold.getPercentageValF());
//        filter.setRaysOnly(raysOnly.isChecked());

        // this value should not be divided by resizeFactor because
        // this is a scale and not really a length
        filter.setZoom(length.getPercentageValF());

        var helper = new ResizingFilterHelper(src);
        boolean shouldResize = helper.shouldResize();

        int filterUnits = 3;
        int workUnits = filterUnits + 3; // +3 for the rays only step at the end
        if (shouldResize) {
            int resizeUnits = helper.getResizeWorkUnits(BILINEAR_FAST);
            workUnits += resizeUnits;
        }
        var pt = new StatusBarProgressTracker(NAME, workUnits);
//        var pt = new DebugProgressTracker(NAME, workUnits);
        filter.setProgressTracker(pt);

        BufferedImage rays;
        if (shouldResize) {
            rays = helper.invoke(BILINEAR_FAST, filter, pt, 0);
        } else {
            // normal case, no resizing
            rays = filter.filter(src, dest);
        }

        // so far we have the rays image,
        // which contains white rays on a black background
        if (raysOnly.isChecked()) {
            // make sure we have a transparent background
            if (dest == null) {
                dest = filter.createCompatibleDestImage(src, null);
            }
            Graphics2D g = dest.createGraphics();
            g.setComposite(MiscComposite.getInstance(MiscComposite.ADD, opacity.getPercentageValF()));
            g.drawRenderedImage(rays, null);
            g.dispose();

            pt.unitsDone(3);
            pt.finished();

            return dest;
        }

        // add the rays on top of the source
        dest = ImageUtils.copyImage(src);
        Graphics2D g = dest.createGraphics();
        g.setComposite(MiscComposite.getInstance(MiscComposite.ADD, opacity.getPercentageValF()));
        g.drawRenderedImage(rays, null);
        g.dispose();

        pt.unitsDone(3);
        pt.finished();

        return dest;
    }

    @Override
    public boolean supportsGray() {
        return false;
    }
}