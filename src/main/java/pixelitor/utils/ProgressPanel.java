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

package pixelitor.utils;

import pixelitor.gui.utils.GUIUtils;

import javax.swing.*;
import java.awt.CardLayout;
import java.awt.Dimension;

import static pixelitor.utils.Threads.calledOnEDT;
import static pixelitor.utils.Threads.threadInfo;

/**
 * A panel that can show or hide a {@link JProgressBar}.
 * The setVisibility method wouldn't work because of layout problems.
 */
public class ProgressPanel extends JPanel {
    private final JProgressBar progressBar;
    private final CardLayout cardLayout;

    public ProgressPanel() {
        cardLayout = new CardLayout();
        setLayout(cardLayout);

        progressBar = new JProgressBar();
        int ph = progressBar.getPreferredSize().height;
        add(Box.createRigidArea(new Dimension(ph, ph)));
        add(progressBar);
    }

    public void showProgressBar() {
        cardLayout.next(this);
    }

    public void hideProgressBar() {
        cardLayout.first(this);
    }

    public void setProgress(int p) {
        progressBar.setValue(p);
    }

    public void paintImmediately() {
        assert calledOnEDT() : threadInfo();

        GUIUtils.paintImmediately(progressBar);
    }
}
