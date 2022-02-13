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

import pixelitor.OpenImages;
import pixelitor.gui.View;
import pixelitor.gui.utils.PAction;
import pixelitor.utils.Icons;
import pixelitor.utils.ViewActivationListener;

import javax.swing.*;

/**
 * An {@link Action} that adds a new text layer to the active composition.
 */
public class AddTextLayerAction extends PAction implements ViewActivationListener {
    public static final AddTextLayerAction INSTANCE = new AddTextLayerAction();

    private AddTextLayerAction() {
        super("Add Text Layer", Icons.load("add_text_layer.png"));
        setToolTip("Adds a new text layer.");
        setEnabled(false);
        OpenImages.addActivationListener(this);
    }

    @Override
    public void onClick() {
        TextLayer.createNew();
    }

    @Override
    public void allViewsClosed() {
        setEnabled(false);
    }

    @Override
    public void viewActivated(View oldView, View newView) {
        setEnabled(true);
    }
}