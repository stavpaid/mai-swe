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

package pixelitor.layers;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A utility class that checks that the image layer icon images
 * and the mask icon images are updated the correct number of times.
 */
public class IconUpdateChecker {
    private final TestLayerUI ui;
    private final LayerMask mask;

    private final int layerIconUpdatesAtStart;
    private final int maskIconUpdatesAtStart;

    public IconUpdateChecker(Layer layer, LayerMask mask,
                             int layerIconUpdatesAtStart,
                             int maskIconUpdatesAtStart) {
        this.ui = (TestLayerUI) layer.getUI();
        this.mask = mask;
        this.layerIconUpdatesAtStart = layerIconUpdatesAtStart;
        this.maskIconUpdatesAtStart = maskIconUpdatesAtStart;

        // check that the start update counts are correct
        check(0, 0);
    }

    public void check(int numLayer, int numMask) {
        checkLayer(numLayer);
        checkMask(numMask);
    }

    private void checkLayer(int num) {
        assertThat(ui.getNumLayerIconUpdates()).isEqualTo(layerIconUpdatesAtStart + num);
    }

    private void checkMask(int num) {
        if (mask == null) {
            // this was a test without a mask
            return;
        }
        assertThat(ui.getNumMaskIconUpdates()).isEqualTo(maskIconUpdatesAtStart + num);
    }
}
