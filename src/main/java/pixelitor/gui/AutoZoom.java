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

package pixelitor.gui;

import pixelitor.OpenImages;
import pixelitor.gui.utils.OpenImageEnabledAction;
import pixelitor.menus.view.ZoomMenu;

import javax.swing.*;

/**
 * Ways to calculate zoom levels automatically, based on the available space
 */
public enum AutoZoom {
    FIT_SPACE("Fit Space", ZoomMenu.FIT_SPACE_TOOLTIP) {
        @Override
        public double selectRatio(double hor, double ver) {
            return Math.max(hor, ver);
        }
    }, FIT_WIDTH("Fit Width", "Fit the width of the image to the available horizontal space") {
        @Override
        public double selectRatio(double hor, double ver) {
            return hor;
        }
    }, FIT_HEIGHT("Fit Height", "Fit the height of the image to the available vertical space") {
        @Override
        public double selectRatio(double hor, double ver) {
            return ver;
        }
    }, ACTUAL_PIXELS("Actual Pixels", ZoomMenu.ACTUAL_PIXELS_TOOLTIP) {
        @Override
        public double selectRatio(double hor, double ver) {
            throw new IllegalStateException("should not be called");
        }
    };

    public static final Action ACTUAL_PIXELS_ACTION = ACTUAL_PIXELS.asAction();
    public static final Action FIT_HEIGHT_ACTION = FIT_HEIGHT.asAction();
    public static final Action FIT_WIDTH_ACTION = FIT_WIDTH.asAction();
    public static final Action FIT_SPACE_ACTION = FIT_SPACE.asAction();

    private final String guiName;
    private final String toolTip;

    AutoZoom(String guiName, String toolTip) {
        this.guiName = guiName;
        this.toolTip = toolTip;
    }

    /**
     * Selects the image-to-available-area ratio that
     * will be used for the auto zoom calculations
     */
    public abstract double selectRatio(double hor, double ver);

    private Action asAction() {
        var action = new OpenImageEnabledAction(guiName) {
            @Override
            public void onClick() {
                OpenImages.fitActive(AutoZoom.this);
            }
        };
        action.setToolTip(toolTip);
        return action;
    }
}
