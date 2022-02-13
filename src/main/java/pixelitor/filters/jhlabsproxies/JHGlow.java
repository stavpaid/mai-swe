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

import com.jhlabs.image.GlowFilter;
import pixelitor.filters.ParametrizedFilter;
import pixelitor.filters.gui.RangeParam;

import java.awt.image.BufferedImage;

/**
 * Glow filter based on the JHLabs GlowFilter
 */
public class JHGlow extends ParametrizedFilter {
    public static final String NAME = "Glow";

    private final RangeParam amount = new RangeParam("Amount", 0, 15, 100);
    private final RangeParam softness = new RangeParam("Softness Radius", 0, 20, 100);

    private GlowFilter filter;

    public JHGlow() {
        super(true);

        setParams(
            amount,
            softness
        );
    }

    @Override
    public BufferedImage doTransform(BufferedImage src, BufferedImage dest) {
        float amountValue = amount.getPercentageValF();
        if (amountValue == 0.0f) {
            return src;
        }

        if (src.getWidth() == 1 || src.getHeight() == 1) {
            // otherwise we get ArrayIndexOutOfBoundsException in BoxBlurFilter
            return src;
        }

        if (filter == null) {
            filter = new GlowFilter(NAME);
        }

        filter.setAmount(amountValue);
        filter.setRadius(softness.getValueAsFloat());

        return filter.filter(src, dest);
    }

    @Override
    public boolean supportsGray() {
        return false;
    }
}