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

package pixelitor.gui.utils;

import javax.swing.*;

public enum Theme {
    NIMBUS("Nimbus", false, true) {
        @Override
        String getLAFClassName() {
            return "javax.swing.plaf.nimbus.NimbusLookAndFeel";
        }
//    }, FLAT_DARK("Flat Dark", true, false) {
//        @Override
//        String getLAFClassName() {
//            return "com.formdev.flatlaf.FlatDarculaLaf";
//        }
//    }, FLAT_LIGHT("Flat Light", false, false) {
//        @Override
//        String getLAFClassName() {
//            return "com.formdev.flatlaf.FlatIntelliJLaf";
//        }
    }, SYSTEM("System", false, false) {
        @Override
        String getLAFClassName() {
            return UIManager.getSystemLookAndFeelClassName();
        }
    };

    private final String guiName;
    private final boolean dark;
    private final boolean nimbus;

    Theme(String guiName, boolean dark, boolean nimbus) {
        this.guiName = guiName;
        this.dark = dark;
        this.nimbus = nimbus;
    }

    abstract String getLAFClassName();

    public boolean isDark() {
        return dark;
    }

    public boolean isNimbus() {
        return nimbus;
    }

    public String getSaveCode() {
        return guiName;
    }

    @Override
    public String toString() {
        return guiName;
    }
}
