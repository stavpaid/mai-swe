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

import com.jhlabs.image.ShadowFilter;
import pixelitor.filters.ParametrizedFilter;
import pixelitor.filters.ResizingFilterHelper;
import pixelitor.filters.gui.AngleParam;
import pixelitor.filters.gui.BooleanParam;
import pixelitor.filters.gui.ColorParam;
import pixelitor.filters.gui.RangeParam;
import pixelitor.utils.ProgressTracker;
import pixelitor.utils.StatusBarProgressTracker;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import static java.awt.Color.BLACK;
import static pixelitor.filters.ResizingFilterHelper.ScaleUpQuality.BILINEAR_FAST;
import static pixelitor.filters.gui.ColorParam.TransparencyPolicy.NO_TRANSPARENCY;
import static pixelitor.gui.GUIText.OPACITY;
import static pixelitor.utils.AngleUnit.CCW_DEGREES;

/**
 * Drop Shadow filter based on the JHLabs ShadowFilter
 */
public class JHDropShadow extends ParametrizedFilter {
    public static final String NAME = "Drop Shadow";

    private final AngleParam angle = new AngleParam("Angle", 315, CCW_DEGREES);
    private final RangeParam distance = new RangeParam("Distance", 0, 10, 100);
    private final RangeParam opacity = new RangeParam(OPACITY, 0, 90, 100);
    private final RangeParam softness = new RangeParam("Softness", 0, 10, 25);
    private final BooleanParam shadowOnly = new BooleanParam("Shadow Only", false);
    private final ColorParam color = new ColorParam("Color", BLACK, NO_TRANSPARENCY);

    private ShadowFilter filter;

    public JHDropShadow() {
        super(true);

        setParams(
            angle,
            distance.withAdjustedRange(0.1),
            opacity,
            softness.withAdjustedRange(0.025),
            color,
            shadowOnly
        );
    }

    @Override
    public BufferedImage doTransform(BufferedImage src, BufferedImage dest) {
        if (filter == null) {
            filter = new ShadowFilter(NAME);
        }

        filter.setAddMargins(false);
        filter.setAngle((float) angle.getValueInIntuitiveRadians());
        filter.setOpacity(opacity.getPercentageValF());
        filter.setShadowColor(color.getColor().getRGB());
        filter.setShadowOnly(shadowOnly.isChecked());

        var helper = new ResizingFilterHelper(src);
        boolean shouldResize = helper.shouldResize();
        if (shouldResize) {
            boolean addSource = !shadowOnly.isChecked();

            int resizeUnits = helper.getResizeWorkUnits(BILINEAR_FAST);
            int filterUnits = 2; // estimated
            int workUnits = resizeUnits + filterUnits;
            if (addSource) {
                workUnits++;
            }
            var pt = new StatusBarProgressTracker(NAME, workUnits);
//            var pt = new DebugProgressTracker(NAME, workUnits);
            filter.setProgressTracker(ProgressTracker.NULL_TRACKER);

            double resizeFactor = helper.getResizeFactor();
            filter.setDistance((float) (distance.getValueAsFloat() / resizeFactor));
            filter.setRadius((float) (softness.getValueAsFloat() / resizeFactor));
            filter.setShadowOnly(true); // we only want to resize the shadow

            dest = helper.invoke(BILINEAR_FAST, filter, pt, filterUnits);

            if (addSource) {
                Graphics2D g = dest.createGraphics();
                g.setComposite(AlphaComposite.SrcOver);
                g.drawRenderedImage(src, null);
                g.dispose();

                pt.unitDone();
            }
            pt.finished();
        } else {
            // normal case, no resizing
            filter.setDistance(distance.getValueAsFloat());
            filter.setRadius(softness.getValueAsFloat());
            dest = filter.filter(src, dest);
        }

        return dest;
    }

    @Override
    public boolean supportsGray() {
        return false;
    }
}