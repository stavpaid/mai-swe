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

import pixelitor.Composition;
import pixelitor.ConsistencyChecks;
import pixelitor.Layers;
import pixelitor.OpenImages;
import pixelitor.gui.View;
import pixelitor.gui.utils.NamedAction;
import pixelitor.utils.Icons;
import pixelitor.utils.Messages;
import pixelitor.utils.ViewActivationListener;

import javax.swing.*;
import java.awt.event.ActionEvent;

import static java.awt.event.ActionEvent.CTRL_MASK;
import static pixelitor.layers.LayerMaskAddType.*;

/**
 * An {@link Action} that adds a new layer mask
 * to the active layer of the active composition.
 */
public class AddLayerMaskAction extends NamedAction
    implements ViewActivationListener, ActiveMaskListener, ActiveCompositionListener {

    public static final AddLayerMaskAction INSTANCE = new AddLayerMaskAction();

    private AddLayerMaskAction() {
        super("Add Layer Mask", Icons.load("add_layer_mask.png"));
        setToolTip("<html>Adds a layer mask to the active layer. " +
            "<br><b>Ctrl-click</b> to add an inverted layer mask.");
        setEnabled(false);
        OpenImages.addActivationListener(this);
        Layers.addCompositionListener(this);
        Layers.addMaskListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        boolean ctrlPressed = false;
        if (e != null) { // could be null in tests
            ctrlPressed = (e.getModifiers() & CTRL_MASK) == CTRL_MASK;
        }

        try {
            onClick(ctrlPressed);
        } catch (Exception ex) {
            Messages.showException(ex);
        }
    }

    private void onClick(boolean ctrlPressed) {
        var comp = OpenImages.getActiveComp();
        var layer = comp.getActiveLayer();
        assert !layer.hasMask();

        if (comp.hasSelection()) {
            if (ctrlPressed) {
                layer.addMask(HIDE_SELECTION);
            } else {
                layer.addMask(REVEAL_SELECTION);
            }
        } else { // there is no selection
            if (ctrlPressed) {
                layer.addMask(HIDE_ALL);
            } else {
                layer.addMask(REVEAL_ALL);
            }
        }
    }

    @Override
    public void allViewsClosed() {
        setEnabled(false);
    }

    @Override
    public void viewActivated(View oldView, View newView) {
        boolean hasMask = newView.getComp().getActiveLayer().hasMask();
        setEnabled(!hasMask);
    }

    @Override
    public void maskAddedTo(Layer layer) {
        assert layer.hasMask();
        setEnabled(false);
    }

    @Override
    public void maskDeletedFrom(Layer layer) {
        assert !layer.hasMask();
        setEnabled(true);
    }

    @Override
    public void numLayersChanged(Composition comp, int newLayerCount) {
    }

    @Override
    public void layerActivated(Layer newActiveLayer) {
        setEnabled(!newActiveLayer.hasMask());
    }

    @Override
    public void layerOrderChanged(Composition comp) {
    }

    @Override
    public void setEnabled(boolean newValue) {
        super.setEnabled(newValue);

        assert ConsistencyChecks.addMaskActionEnabled();
    }
}