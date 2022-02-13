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

import org.jdesktop.swingx.VerticalLayout;
import pixelitor.filters.Filter;
import pixelitor.filters.gui.FilterWithGUI;
import pixelitor.filters.util.FilterSearchPanel;
import pixelitor.filters.util.FilterUtils;
import pixelitor.gui.utils.OKCancelDialog;
import pixelitor.layers.Drawable;

import javax.swing.*;
import java.util.Optional;

/**
 * A page in the batch filter wizard
 */
public enum BatchFilterWizardPage implements WizardPage {
    SELECT_FILTER_AND_DIRS {
        private OpenSaveDirsPanel openSaveDirsPanel;
        private FilterSearchPanel searchPanel;

        @Override
        public String getHelpText(Wizard wizard) {
            return "<html> Apply a filter to every image in a folder.";
        }

        @Override
        public Optional<WizardPage> getNext() {
            var selectedAction = searchPanel.getSelectedFilter();
            var filter = selectedAction.getFilter();
            if (filter instanceof FilterWithGUI) {
                return Optional.of(FILTER_GUI);
            } else {
                return Optional.empty();
            }
        }

        @Override
        public JComponent createPanel(Wizard wizard, Drawable dr) {
            searchPanel = new FilterSearchPanel(FilterUtils.getAllFiltersSorted());
            searchPanel.setBorder(BorderFactory.createTitledBorder("Select Filter"));

            var mainPanel = new JPanel(new VerticalLayout());
            mainPanel.add(searchPanel);
            if (openSaveDirsPanel == null) {
                openSaveDirsPanel = new OpenSaveDirsPanel();
            }
            mainPanel.add(openSaveDirsPanel);

            return mainPanel;
        }

        @Override
        public void onShowingInDialog(OKCancelDialog dialog) {
            JButton okButton = dialog.getOkButton();

            okButton.setEnabled(false);
            searchPanel.addSelectionListener(e ->
                okButton.setEnabled(searchPanel.hasSelection()));
        }

        @Override
        public void onWizardCanceled(Drawable dr) {
            // do nothing
        }

        @Override
        public void finish(Wizard wizard, Drawable dr) {
            var selectedAction = searchPanel.getSelectedFilter();
            var filter = selectedAction.getFilter();

            ((BatchFilterWizard) wizard).setFilter(filter);

            openSaveDirsPanel.rememberValues();
        }
    }, FILTER_GUI {
        @Override
        public String getHelpText(Wizard wizard) {
            return "<html> Select filter settings.";
        }

        @Override
        public Optional<WizardPage> getNext() {
            return Optional.empty();
        }

        @Override
        public JComponent createPanel(Wizard wizard, Drawable dr) {
            Filter filter = ((BatchFilterWizard) wizard).getFilter();

            // this page will be invoked only if
            // the selected filter is a filter with GUI
            FilterWithGUI guiFilter = (FilterWithGUI) filter;

            dr.startPreviewing();

            return guiFilter.createGUI(dr, true);
        }

        @Override
        public void onWizardCanceled(Drawable dr) {
            // we get here only if the chosen filter is a filter with GUI
            dr.onFilterDialogCanceled();
        }

        @Override
        public void finish(Wizard wizard, Drawable dr) {
            // cancel the previewing
            onWizardCanceled(dr);
        }
    }
}
