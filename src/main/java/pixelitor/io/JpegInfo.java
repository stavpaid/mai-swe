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

package pixelitor.io;

import javax.imageio.ImageWriteParam;
import java.util.function.Consumer;

import static javax.imageio.ImageWriteParam.*;

/**
 * Custom configuration for JPEG images
 */
public record JpegInfo(float quality, boolean progressive) {
    private static final float DEFAULT_QUALITY = 0.87f;

    public static final JpegInfo DEFAULTS = new JpegInfo(DEFAULT_QUALITY, false);

    public Consumer<ImageWriteParam> toCustomizer() {
        return imageWriteParam -> {
            if (progressive) {
                imageWriteParam.setProgressiveMode(MODE_DEFAULT);
            } else {
                imageWriteParam.setProgressiveMode(MODE_DISABLED);
            }

            imageWriteParam.setCompressionMode(MODE_EXPLICIT);
            imageWriteParam.setCompressionQuality(quality);
        };
    }
}
