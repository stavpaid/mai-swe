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

package pixelitor.menus.file;

import pixelitor.Composition;
import pixelitor.OpenImages;
import pixelitor.gui.PixelitorWindow;
import pixelitor.gui.utils.DialogBuilder;
import pixelitor.gui.utils.GridBagHelper;
import pixelitor.gui.utils.PAction;
import pixelitor.utils.Messages;
import pixelitor.utils.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.TimeUnit;

import static pixelitor.utils.Texts.i18n;

/**
 * The {@link Action} for creating a screen capture.
 */
public class ScreenCaptureAction extends PAction {
    private static final String SCREEN_CAPTURE_STRING = i18n("screen_capture");
    private JCheckBox hidePixelitorCB;
    private static int captureCount = 1;

    public ScreenCaptureAction() {
        super(SCREEN_CAPTURE_STRING + "...");
    }

    @Override
    public void onClick() {
        new DialogBuilder()
            .content(getSettingsPanel())
            .title(SCREEN_CAPTURE_STRING)
            .okAction(this::capture)
            .show();
    }

    private JPanel getSettingsPanel() {
        var p = new JPanel();
        p.setLayout(new GridBagLayout());
        var gbh = new GridBagHelper(p);

        hidePixelitorCB = new JCheckBox();
        hidePixelitorCB.setSelected(true);
        gbh.addLabelAndControl("Hide Pixelitor", hidePixelitorCB);

        return p;
    }

    private void capture() {
        try {
            tryToCapture();
        } catch (Exception ex) {
            Messages.showException(ex);
        }
    }

    private void tryToCapture() throws AWTException {
        boolean hide = shouldHidePixelitor();
        if (hide) {
            hideApp();
        }

        BufferedImage screenShot = captureScreenShot();

        if (hide) {
            unHideApp();
        }

        addAsNewComp(screenShot);
    }

    private static BufferedImage captureScreenShot() throws AWTException {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        return new Robot().createScreenCapture(new Rectangle(screenSize));
    }

    private static void hideApp() {
        PixelitorWindow.get().iconify();
        Utils.sleep(500, TimeUnit.MILLISECONDS);
    }

    private static void unHideApp() {
        PixelitorWindow.get().deiconify();
    }

    private boolean shouldHidePixelitor() {
        return hidePixelitorCB.isSelected();
    }

    private static void addAsNewComp(BufferedImage screenCapture) {
        String name = "Screen Capture " + captureCount++;
        var comp = Composition.fromImage(screenCapture, null, name);
        comp.setDirty(true);
        OpenImages.addAsNewComp(comp);
    }
}
