/*
 * Copyright 2020 Laszlo Balazs-Csiki and Contributors
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

package pixelitor.menus.view;

import pixelitor.gui.StatusBar;
import pixelitor.gui.WorkSpace;

import javax.swing.*;

/**
 * The {@link Action} that either shows or hides the status bar,
 * depending on the current visibility
 */
public class ShowHideStatusBarAction extends ShowHideAction {
    public static final ShowHideAction INSTANCE = new ShowHideStatusBarAction();

    private ShowHideStatusBarAction() {
        super("Show Status Bar", "Hide Status Bar");
    }

    @Override
    public boolean getCurrentVisibility() {
        return StatusBar.isShown();
    }

    @Override
    public boolean getStartupVisibility() {
        return WorkSpace.getStatusBarVisibility();
    }

    @Override
    public void setVisibility(boolean value) {
        WorkSpace.setStatusBarVisibility(value, true);
    }
}
