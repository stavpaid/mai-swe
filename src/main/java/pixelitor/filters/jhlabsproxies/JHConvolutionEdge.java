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

import com.jhlabs.image.EdgeFilter;
import pixelitor.filters.Invert;
import pixelitor.filters.ParametrizedFilter;
import pixelitor.filters.gui.BooleanParam;
import pixelitor.filters.gui.IntChoiceParam;
import pixelitor.filters.gui.IntChoiceParam.Item;

import java.awt.image.BufferedImage;

import static pixelitor.filters.gui.RandomizePolicy.IGNORE_RANDOMIZE;

/**
 * Convolution Edge Detection filter based on the JHLabs EdgeFilter
 */
public class JHConvolutionEdge extends ParametrizedFilter {
    public static final String NAME = "Convolution Edge Detection";

    private final IntChoiceParam horizontalMethod = new IntChoiceParam("Horizontal Edges", new Item[]{
        new Item("Sobel", METHOD_SOBEL),
        new Item("Prewitt", METHOD_PREWITT),
        new Item("Roberts", METHOD_ROBERTS),
        new Item("Frei-Chen", METHOD_FREI_CHEN),
        new Item("None", METHOD_NONE),
    });

    private final IntChoiceParam verticalMethod = new IntChoiceParam("Vertical Edges", new Item[]{
        new Item("Sobel", METHOD_SOBEL),
        new Item("Prewitt", METHOD_PREWITT),
        new Item("Roberts", METHOD_ROBERTS),
        new Item("Frei-Chen", METHOD_FREI_CHEN),
        new Item("None", METHOD_NONE),
    });

    private final BooleanParam invertImage = new BooleanParam("Invert", false, IGNORE_RANDOMIZE);

    private EdgeFilter filter;
    private static final int METHOD_SOBEL = 1;
    private static final int METHOD_PREWITT = 2;
    private static final int METHOD_ROBERTS = 3;
    private static final int METHOD_FREI_CHEN = 4;
    private static final int METHOD_NONE = 5;

    private static final float[] NONE_MATRIX = {
        0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f
    };

    public JHConvolutionEdge() {
        super(true);

        setParams(
            horizontalMethod,
            verticalMethod,
            invertImage
        );
    }

    @Override
    public BufferedImage doTransform(BufferedImage src, BufferedImage dest) {
        if (filter == null) {
            filter = new EdgeFilter(NAME);
        }

        int horizontal = horizontalMethod.getValue();
        switch (horizontal) {
            case METHOD_SOBEL -> filter.setHEdgeMatrix(EdgeFilter.SOBEL_H);
            case METHOD_PREWITT -> filter.setHEdgeMatrix(EdgeFilter.PREWITT_H);
            case METHOD_ROBERTS -> filter.setHEdgeMatrix(EdgeFilter.ROBERTS_H);
            case METHOD_FREI_CHEN -> filter.setHEdgeMatrix(EdgeFilter.FREI_CHEN_H);
            case METHOD_NONE -> filter.setHEdgeMatrix(NONE_MATRIX);
            default -> throw new IllegalStateException("horizontal = " + horizontal);
        }

        int vertical = verticalMethod.getValue();
        switch (vertical) {
            case METHOD_SOBEL -> filter.setVEdgeMatrix(EdgeFilter.SOBEL_V);
            case METHOD_PREWITT -> filter.setVEdgeMatrix(EdgeFilter.PREWITT_V);
            case METHOD_ROBERTS -> filter.setVEdgeMatrix(EdgeFilter.ROBERTS_V);
            case METHOD_FREI_CHEN -> filter.setVEdgeMatrix(EdgeFilter.FREI_CHEN_V);
            case METHOD_NONE -> filter.setVEdgeMatrix(NONE_MATRIX);
            default -> throw new IllegalStateException("vertical = " + vertical);
        }

        dest = filter.filter(src, dest);

        if (invertImage.isChecked()) {
            dest = Invert.invertImage(dest);
        }

        return dest;
    }
}