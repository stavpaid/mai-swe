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
package pixelitor.filters.gui;

/**
 * Something that has a default value (typically a {@link FilterParam})
 * and can be reset to it (typically by pressing a {@link DefaultButton}).
 */
public interface Resettable {
    public static final String RESET_ALL_TOOLTIP = "Reset all settings to their default values.";

    boolean isSetToDefault();

    /**
     * @param trigger If true, then the resetting can trigger
     *                some GUI update such as the recalculating
     *                of a filter preview
     */
    void reset(boolean trigger);

    String getResetToolTip();
}
