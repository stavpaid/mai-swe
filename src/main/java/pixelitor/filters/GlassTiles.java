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

import pixelitor.filters.gui.AngleParam;
import pixelitor.filters.gui.GroupedRangeParam;
import pixelitor.filters.gui.IntChoiceParam;
import pixelitor.filters.impl.TilesFilter;

import java.awt.image.BufferedImage;

/**
 * Glass Tiles filter
 */
public class GlassTiles extends ParametrizedFilter {
    public static final String NAME = "Glass Tiles";

    private final GroupedRangeParam size = new GroupedRangeParam("Tile Size", 5, 100, 500);
    private final GroupedRangeParam curvature = new GroupedRangeParam("Curvature", 0, 10, 20);
    private final GroupedRangeParam phase = new GroupedRangeParam("Move Tiles", 0, 0, 10, false);
    private final AngleParam angle = new AngleParam("Rotate Tiles", 0);
    private final IntChoiceParam edgeAction = IntChoiceParam.forEdgeAction(true);
    private final IntChoiceParam interpolation = IntChoiceParam.forInterpolation();

    private TilesFilter filter;

    public GlassTiles() {
        super(true);

        setParams(
            size.withAdjustedRange(0.5),
            curvature,
            phase.notLinkable(),
            angle,
            edgeAction,
            interpolation
        );
    }

    @Override
    public BufferedImage doTransform(BufferedImage src, BufferedImage dest) {
        if (filter == null) {
            filter = new TilesFilter(NAME);
        }

        filter.setSizeX(size.getValue(0));
        filter.setSizeY(size.getValue(1));
        filter.setCurvatureX(curvature.getValue(0));
        filter.setCurvatureY(curvature.getValue(1));
        filter.setAngle(angle.getValueInIntuitiveRadians());
        filter.setEdgeAction(edgeAction.getValue());
        filter.setInterpolation(interpolation.getValue());
        filter.setShiftX(phase.getValueAsPercentage(0));
        filter.setShiftY(phase.getValueAsPercentage(1));

        return filter.filter(src, dest);
    }
}

