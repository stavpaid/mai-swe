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

package pixelitor.gui.utils;

import pixelitor.utils.Messages;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * The "Pixelitor action" is the common superclass of most actions.
 */
public abstract class PAction extends NamedAction {
    public PAction() {
    }

    public PAction(String name) {
        super(name);
    }

    public PAction(String name, Icon icon) {
        super(name, icon);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            onClick();
        } catch (Exception ex) {
            Messages.showException(ex);
        }
    }

    public abstract void onClick();
}
