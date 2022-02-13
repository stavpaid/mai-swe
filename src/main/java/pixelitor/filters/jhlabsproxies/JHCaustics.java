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

import com.jhlabs.image.CausticsFilter;
import pixelitor.filters.ParametrizedFilter;
import pixelitor.filters.gui.ColorParam;
import pixelitor.filters.gui.RangeParam;

import java.awt.Color;
import java.awt.image.BufferedImage;

import static pixelitor.filters.gui.ColorParam.TransparencyPolicy.USER_ONLY_TRANSPARENCY;
import static pixelitor.filters.gui.RandomizePolicy.IGNORE_RANDOMIZE;
import static pixelitor.filters.gui.ReseedActions.reseedNoise;
import static pixelitor.gui.GUIText.ZOOM;
import static pixelitor.gui.utils.SliderSpinner.TextPosition.BORDER;

/**
 * Renders caustics based on the JHLabs CausticsFilter
 */
public class JHCaustics extends ParametrizedFilter {
    public static final String NAME = "Caustics";

    private final ColorParam bgColor = new ColorParam("Background", new Color(0, 200, 175), USER_ONLY_TRANSPARENCY);
    private final RangeParam zoom = new RangeParam(ZOOM + " (%)", 1, 100, 501);
    private final RangeParam brightness = new RangeParam("Brightness", 0, 7, 20);
    private final RangeParam focus = new RangeParam("Focus", 0, 50, 100);
    private final RangeParam dispersion = new RangeParam("Dispersion (Color Separation)", 0, 0, 100);
    private final RangeParam turbulence = new RangeParam("Turbulence", 0, 25, 100);
    private final RangeParam time = new RangeParam("Time", 0, 0, 800);
    private final RangeParam samples = new RangeParam("Samples (Quality)", 1, 1, 10,
        true, BORDER, IGNORE_RANDOMIZE);

    private CausticsFilter filter;

    public JHCaustics() {
        super(false);

        setParams(
            bgColor,
            zoom,
            brightness,
            turbulence,
            time,
            focus,
            dispersion,
            samples
        ).withAction(reseedNoise());
    }

    @Override
    public BufferedImage doTransform(BufferedImage src, BufferedImage dest) {
        if (filter == null) {
            filter = new CausticsFilter(NAME);
        }

        filter.setAmount(focus.getPercentageValF());
        filter.setBgColor(bgColor.getColor().getRGB());
        filter.setBrightness(brightness.getValue());
        filter.setDispersion(dispersion.getPercentageValF());
        filter.setSamples(samples.getValue());
        filter.setScale(zoom.getValueAsFloat());
        filter.setTime(time.getPercentageValF());
        filter.setTurbulence(turbulence.getValueAsFloat() / 25.0f);

        return filter.filter(src, dest);
    }
}