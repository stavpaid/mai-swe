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

package pixelitor.filters.util;

import org.jdesktop.swingx.JXList;
import org.jdesktop.swingx.JXTextField;
import org.jdesktop.swingx.prompt.BuddySupport;
import org.jdesktop.swingx.prompt.PromptSupport;
import pixelitor.gui.utils.DialogBuilder;
import pixelitor.gui.utils.GUIUtils;
import pixelitor.gui.utils.TFValidationLayerUI;
import pixelitor.gui.utils.VectorIcon;
import pixelitor.layers.LayerButton;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;

import static java.awt.event.KeyEvent.*;
import static org.jdesktop.swingx.prompt.PromptSupport.FocusBehavior.SHOW_PROMPT;
import static pixelitor.gui.utils.Screens.Align.SCREEN_CENTER;

public class FilterSearchPanel extends JPanel {
    private static final int GAP = 4;

    private JTextField searchTF;
    private JXList filtersList;
    private HighlightListCellRenderer highlighter;

    public FilterSearchPanel(FilterAction[] filters) {
        super(new BorderLayout(GAP, GAP));

        createSearchTextField();
        createFiltersList(filters);

        var searchLayerUI = new TFValidationLayerUI(tf -> numMatchingFilters() > 0);
        add(new JLayer<>(searchTF, searchLayerUI), BorderLayout.NORTH);
        add(new JScrollPane(filtersList), BorderLayout.CENTER);
        setBorder(BorderFactory.createEmptyBorder(GAP, GAP, GAP, GAP));
    }

    private void createSearchTextField() {
        searchTF = new JXTextField("Search");
        searchTF.setName("searchTF");
        PromptSupport.setFocusBehavior(SHOW_PROMPT, searchTF);
        JLabel searchBuddy = new JLabel(new SearchIcon());
        searchBuddy.setForeground(Color.GRAY);
        BuddySupport.addLeft(searchBuddy, searchTF);

        searchTF.requestFocusInWindow();

        searchTF.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                forwardUpDownToFilterList(e);
            }
        });
        searchTF.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                searchTermChanged();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                searchTermChanged();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                searchTermChanged();
            }
        });
    }

    public void forwardUpDownToFilterList(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (keyCode == VK_DOWN || keyCode == VK_UP) {
            int numFilters = numMatchingFilters();
            if (numFilters > 0) {
                if (filtersList.isSelectionEmpty()) {
                    if (keyCode == VK_DOWN) {
                        selectFilter(0);
                    } else if (keyCode == VK_UP) {
                        selectFilter(numFilters - 1);
                    }
                } else {
                    forwardEvent(filtersList, e);
                }
                filtersList.requestFocusInWindow();
            }
        }
    }

    private void createFiltersList(FilterAction[] filters) {
        filtersList = new JXList(filters);
        filtersList.setAutoCreateRowSorter(true);
        filtersList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        highlighter = new HighlightListCellRenderer("");
        filtersList.setCellRenderer(highlighter);

        filtersList.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == VK_UP) {
                    if (firstFilterIsSelected()) {
                        searchTF.requestFocusInWindow();
                    }
                } else if (e.getKeyCode() == VK_DOWN) {
                    if (lastFilterIsSelected()) {
                        searchTF.requestFocusInWindow();
                    }
                } else if (e.getKeyCode() == VK_BACK_SPACE) {
                    forwardEvent(searchTF, e);
                }
            }

            @Override
            public void keyTyped(KeyEvent e) {
                char keyChar = e.getKeyChar();
                //noinspection CharacterComparison
                if (keyChar >= 'A' && keyChar <= 'z') {
                    forwardEvent(searchTF, e);
                }
            }
        });
    }

    private static void forwardEvent(JComponent target, KeyEvent e) {
        e.setSource(target);
        target.dispatchEvent(e);
        target.requestFocusInWindow();
    }

    private int numMatchingFilters() {
        return filtersList.getElementCount();
    }

    private void selectFilter(int index) {
        filtersList.getSelectionModel().setSelectionInterval(index, index);
    }

    private boolean firstFilterIsSelected() {
        return filtersList.getSelectionModel().isSelectedIndex(0);
    }

    private boolean lastFilterIsSelected() {
        return filtersList.getSelectionModel().isSelectedIndex(numMatchingFilters() - 1);
    }

    private void searchTermChanged() {
        String filterText = searchTF.getText().trim().toLowerCase();

        filtersList.setRowFilter(new RowFilter<ListModel<FilterAction>, Integer>() {
            @Override
            public boolean include(Entry<? extends ListModel<FilterAction>, ? extends Integer> entry) {
                String filterName = entry.getStringValue(0).toLowerCase();
                return filterText.isEmpty() || filterName.contains(filterText);
            }
        });

        if (numMatchingFilters() > 0 && filtersList.isSelectionEmpty()) {
            selectFilter(0);
        }
        highlighter.setFilterText(filterText);
        filtersList.ensureIndexIsVisible(filtersList.getSelectedIndex());
    }

    private void startSelectedFilter() {
        FilterAction action = getSelectedFilter();
        if (action == null) {
            if (numMatchingFilters() == 1) {
                // nothing is selected, but there is only one remaining filter
                action = (FilterAction) filtersList.getElementAt(0);
            }
        }
        if (action != null) {
            action.actionPerformed(null);
        }
    }

    public FilterAction getSelectedFilter() {
        return (FilterAction) filtersList.getSelectedValue();
    }

    public boolean hasSelection() {
        return !filtersList.isSelectionEmpty();
    }

    public void addSelectionListener(ListSelectionListener listener) {
        filtersList.getSelectionModel().addListSelectionListener(listener);
    }

    public static void showInDialog() {
        FilterSearchPanel panel = new FilterSearchPanel(FilterUtils.getAllFiltersSorted());
        DialogBuilder builder = new DialogBuilder()
            .content(panel)
            .title("Filter Search")
            .okAction(panel::startSelectedFilter);

        JDialog dialog = builder.build();

        // this must be done after building, but before showing
        builder.getOkButton().setEnabled(false);
        panel.addSelectionListener(e ->
            builder.getOkButton().setEnabled(panel.hasSelection()));

        panel.filtersList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    // double click on a selected filter starts it
                    if (panel.hasSelection()) {
                        GUIUtils.closeDialog(dialog, true);
                        panel.startSelectedFilter();
                    }
                }
            }
        });

        GUIUtils.showDialog(dialog, SCREEN_CENTER);
    }

    private static class SearchIcon extends VectorIcon {
        public SearchIcon() {
            super(LayerButton.SELECTED_COLOR, 20, 14);
        }

        @Override
        protected void paintIcon(Graphics2D g) {
            // the shape is based on search.svg
            Path2D shape = new Path2D.Float();
            shape.moveTo(7.897525, 8.923619);
            shape.lineTo(12.009847, 12.81312);
            shape.curveTo(12.009847, 12.81312, 12.87233, 13.229692, 13.303571, 12.81312);
            shape.curveTo(13.734813, 12.396544, 13.303571, 11.563393, 13.303571, 11.563393);
            shape.lineTo(9.032443, 7.5904202);

            g.fill(shape);
            g.draw(shape);

            Ellipse2D circle = new Ellipse2D.Double(1.0, 1.0, 8.5, 8.5);
            g.draw(circle);
        }
    }
}
