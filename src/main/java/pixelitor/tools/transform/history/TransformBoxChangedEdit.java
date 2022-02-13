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

package pixelitor.tools.transform.history;

import pixelitor.Composition;
import pixelitor.history.PixelitorEdit;
import pixelitor.tools.transform.TransformBox;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

/**
 * Represents a change to a {@link TransformBox}
 */
public class TransformBoxChangedEdit extends PixelitorEdit {
    private final TransformBox box;
    private final TransformBox.Memento before;
    private final TransformBox.Memento after;

    public TransformBoxChangedEdit(String editName, Composition comp, TransformBox box,
                                   TransformBox.Memento before,
                                   TransformBox.Memento after) {
        super(editName, comp);
        this.box = box;
        this.before = before;
        this.after = after;
    }

    @Override
    public void undo() throws CannotUndoException {
        super.undo();

        box.restoreFrom(before);
    }

    @Override
    public void redo() throws CannotRedoException {
        super.redo();

        box.restoreFrom(after);
    }
}
