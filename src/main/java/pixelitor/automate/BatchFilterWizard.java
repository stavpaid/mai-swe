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

package pixelitor.automate;

import pixelitor.compactions.CompAction;
import pixelitor.filters.Filter;
import pixelitor.gui.PixelitorWindow;
import pixelitor.layers.Drawable;

import java.util.concurrent.CompletableFuture;

import static pixelitor.FilterContext.BATCH_AUTOMATE;
import static pixelitor.automate.BatchFilterWizardPage.SELECT_FILTER_AND_DIRS;

/**
 * The batch filter wizard
 */
public class BatchFilterWizard extends Wizard {
    private Filter filter;

    public BatchFilterWizard(Drawable dr) {
        super(SELECT_FILTER_AND_DIRS, "Batch Filter",
            "Start Processing", 490, 500, dr);
    }

    @Override
    protected void finalAction() {
        var busyCursorParent = PixelitorWindow.get();
        var dialogTitle = "Batch Filter Progress";

        CompAction batchFilterAction = comp -> {
            filter.startOn(comp.getActiveDrawableOrThrow(), BATCH_AUTOMATE, busyCursorParent);
            return CompletableFuture.completedFuture(comp);
        };
        Automate.processFiles(batchFilterAction, dialogTitle);
    }

    @Override
    protected void finalCleanup() {
        // nothing to do
    }

    public Filter getFilter() {
        return filter;
    }

    public void setFilter(Filter filter) {
        this.filter = filter;
    }
}
