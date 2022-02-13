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

import com.jhlabs.image.PinchFilter;
import pixelitor.filters.ParametrizedFilter;
import pixelitor.filters.gui.AngleParam;
import pixelitor.filters.gui.ImagePositionParam;
import pixelitor.filters.gui.IntChoiceParam;
import pixelitor.filters.gui.RangeParam;
import pixelitor.gui.GUIText;

import java.awt.image.BufferedImage;

import static pixelitor.gui.GUIText.ZOOM;

/**
 * A Swirl, Pinch, Bulge filter based on the JHLabs PinchFilter
 */
public class JHSwirlPinchBulge extends ParametrizedFilter {
    public static final String NAME = "Swirl, Pinch, Bulge";

    private final ImagePositionParam center = new ImagePositionParam("Center");
    private final RangeParam radius = new RangeParam(GUIText.RADIUS, 1, 500, 999);
    private final RangeParam swirlAmount = new RangeParam("Swirl Amount", -360, 0, 360);
    private final RangeParam pinchBulgeAmount = new RangeParam("Pinch-Bulge Amount", -100, 0, 100);
    private final RangeParam zoom = new RangeParam(ZOOM + " (%)", 1, 100, 501);
    private final AngleParam rotateResult = new AngleParam("Rotate Result", 0);

    private final IntChoiceParam edgeAction = IntChoiceParam.forEdgeAction();
    private final IntChoiceParam interpolation = IntChoiceParam.forInterpolation();

    private PinchFilter filter;

    public JHSwirlPinchBulge() {
        super(true);

        showAffectedArea();

        setParams(
            swirlAmount,
            pinchBulgeAmount,
            radius.withAdjustedRange(1.0),
            center,
            zoom,
            rotateResult,
            edgeAction,
            interpolation
        );
    }

    @Override
    public BufferedImage doTransform(BufferedImage src, BufferedImage dest) {
        if (filter == null) {
            filter = new PinchFilter();
        }

        filter.setPinchBulgeAmount(pinchBulgeAmount.getPercentageValF());
        filter.setSwirlAmount(swirlAmount.getValueInRadians());
        filter.setRadius(radius.getValueAsFloat());
        filter.setCenterX(center.getRelativeX());
        filter.setCenterY(center.getRelativeY());
        filter.setZoom(zoom.getPercentageValF());
        filter.setRotateResultAngle((float) rotateResult.getValueInIntuitiveRadians());
        filter.setEdgeAction(edgeAction.getValue());
        filter.setInterpolation(interpolation.getValue());

        dest = filter.filter(src, dest);
        setAffectedAreaShapes(filter.getAffectedAreaShapes());
        return dest;
    }
}