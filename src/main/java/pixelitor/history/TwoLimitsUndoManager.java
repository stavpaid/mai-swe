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

package pixelitor.history;

import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;

/**
 * An undo manager that has a separate limit for heavyweight and lightweight edits.
 * An edit is considered heavyweight if storing it potentially requires a lot of memory.
 */
public class TwoLimitsUndoManager extends UndoManager {
    private int heavyEditLimit;
    private int lightEditLimit;

    public TwoLimitsUndoManager() {
        this(64, 256);
    }

    public TwoLimitsUndoManager(int heavyEditLimit, int lightEditLimit) {
        setHeavyEditLimit(heavyEditLimit);
        setLightEditLimit(lightEditLimit);
        int limit = getLimit();
        edits.ensureCapacity(limit);
        super.setLimit(limit);
    }

    @Override
    public synchronized int getLimit() {
        return getHeavyEditLimit() + getLightEditLimit();
    }

    public synchronized int getHeavyEditLimit() {
        return heavyEditLimit;
    }

    public synchronized int getLightEditLimit() {
        return lightEditLimit;
    }

    @Override
    public synchronized void setLimit(int lightEditLimit) {
        setHeavyEditLimit(lightEditLimit);
        super.setLimit(getLimit());
    }

    public synchronized void setHeavyEditLimit(int heavyEditLimit) {
        this.heavyEditLimit = heavyEditLimit;
    }

    public synchronized void setLightEditLimit(int lightEditLimit) {
        this.lightEditLimit = lightEditLimit;
    }

    @Override
    protected void trimForLimit() {
        super.trimForLimit();

        int extraHeavyEdits = getHeavyEditCount() - heavyEditLimit;
        if (extraHeavyEdits > 0) {
            for (int i = 0, c = 0; i < edits.size(); i++) {
                PixelitorEdit edit = (PixelitorEdit) edits.get(i);
                if (edit.isHeavy()) {
                    c++;
                }
                if (c == extraHeavyEdits) {
                    trimEdits(0, i);
                    break;
                }
            }
        }

        int extraLightEdits = getLightEditCount() - lightEditLimit;
        if (extraLightEdits > 0) {
            for (int i = 0, c = 0; i < edits.size(); i++) {
                PixelitorEdit edit = (PixelitorEdit) edits.get(i);
                if (!edit.isHeavy()) {
                    c++;
                }
                if (c == extraLightEdits) {
                    trimEdits(0, i);
                    break;
                }
            }
        }
    }

    public int getHeavyEditCount() {
        int count = 0;
        for (UndoableEdit edit : edits) {
            if (((PixelitorEdit) edit).isHeavy()) {
                count++;
            }
        }
        return count;
    }

    public int getLightEditCount() {
        int count = 0;
        for (UndoableEdit edit : edits) {
            if (!((PixelitorEdit) edit).isHeavy()) {
                count++;
            }
        }
        return count;
    }

    public int getSize() {
        return edits.size();
    }
}
