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

package pixelitor.filters.gui;

import pixelitor.filters.Filter;
import pixelitor.layers.Drawable;

import javax.swing.*;

import static pixelitor.FilterContext.PREVIEWING;

/**
 * The superclass of all filter configuration panels.
 * Instances must not be cached (since they store a Drawable reference).
 */
public abstract class FilterGUI extends JPanel {
    protected Filter filter;
    private final Drawable dr;

    protected FilterGUI(Filter filter, Drawable dr) {
        this.filter = filter;
        this.dr = dr;
    }

    public void runFilterPreview() {
        filter.startOn(dr, PREVIEWING, this);
    }
}
