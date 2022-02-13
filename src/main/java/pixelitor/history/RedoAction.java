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

import pixelitor.gui.utils.PAction;

import javax.swing.*;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;

public class RedoAction extends PAction implements UndoableEditListener {
    private static final String REDO_TEXT = UIManager.getString(
        "AbstractUndoableEdit.redoText");

    public static final Action INSTANCE = new RedoAction();

    private RedoAction() {
        super(REDO_TEXT);

        History.addUndoableEditListener(this);
        setEnabled(false);
    }

    @Override
    public void onClick() {
        History.redo();
    }

    @Override
    public void undoableEditHappened(UndoableEditEvent e) {
        setEnabled(History.canRedo());
        setText(History.getRedoPresentationName());
    }
}
