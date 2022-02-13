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

package pixelitor.gui.utils;

import pixelitor.gui.GUIText;
import pixelitor.gui.GlobalEvents;
import pixelitor.utils.Messages;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Frame;

import static java.awt.BorderLayout.*;
import static javax.swing.BorderFactory.createEmptyBorder;
import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED;
import static pixelitor.gui.utils.Screens.Align.SCREEN_CENTER;
import static pixelitor.utils.Threads.calledOnEDT;
import static pixelitor.utils.Threads.threadInfo;

/**
 * A dialog with OK and Cancel buttons at the bottom.
 *
 * Usually a better alternative for creating dialogs is {@link DialogBuilder}.
 */
public abstract class OKCancelDialog extends JDialog {
    private JComponent formPanel;
    private JLabel msgLabel;
    private JScrollPane scrollPane;
    private final JButton okButton;

    protected OKCancelDialog(JComponent form, Frame owner,
                             String title, String okText) {
        super(owner, title, true);
        assert calledOnEDT() : threadInfo();

        formPanel = form;

        setLayout(new BorderLayout());
        addForm(form);

        JPanel southPanel = new JPanel();
        okButton = new JButton(okText);
        JButton cancelButton = new JButton(GUIText.CANCEL);
        cancelButton.setName("cancel");

        GlobalEvents.dialogOpened(getTitle());

        GUIUtils.addOKCancelButtons(southPanel, okButton, cancelButton);
        add(southPanel, SOUTH);
        setupOKButton();
        setupCancelButton(cancelButton);

        pack();
        Screens.position(this, SCREEN_CENTER);
    }

    private void setupOKButton() {
        okButton.setName("ok");
        getRootPane().setDefaultButton(okButton);
        okButton.addActionListener(e -> {
            try {
                okAction();
            } catch (Exception ex) {
                Messages.showException(ex);
            }
        });
    }

    private void setupCancelButton(JButton cancelButton) {
        cancelButton.addActionListener(e -> {
            try {
                cancelAction();
            } catch (Exception ex) {
                Messages.showException(ex);
            }
        });

        GUIUtils.setupCancelWhenTheDialogIsClosed(this, this::cancelAction);
        GUIUtils.setupCancelWhenEscIsPressed(this, this::cancelAction);
    }

    public void setOKButtonText(String text) {
        okButton.setText(text);
    }

    public void close() {
        setVisible(false);
        GlobalEvents.dialogClosed(getTitle());
        dispose();
    }

    protected abstract void okAction();

    /**
     * The default implementation only calls close()
     * If overridden, call close() manually
     */
    protected void cancelAction() {
        close();
    }

    public void setHeaderMessage(String msg) {
        if (msgLabel != null) { // there was a message before
            remove(msgLabel);
        }
        msgLabel = new JLabel(msg);
        msgLabel.setBorder(createEmptyBorder(0, 5, 0, 5));
        add(msgLabel, NORTH);
        revalidate();
    }

    public void changeForm(JComponent form) {
        if (scrollPane != null) {
            remove(scrollPane);
        } else {
            remove(formPanel);
        }
        formPanel = form;
        addForm(formPanel);
        revalidate();
    }

    private void addForm(JComponent form) {
        scrollPane = new JScrollPane(form,
            VERTICAL_SCROLLBAR_AS_NEEDED,
            HORIZONTAL_SCROLLBAR_NEVER);
        add(scrollPane, CENTER);
    }

    public JButton getOkButton() {
        return okButton;
    }
}
