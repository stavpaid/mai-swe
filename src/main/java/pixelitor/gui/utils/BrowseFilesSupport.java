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

import pixelitor.gui.PixelitorWindow;
import pixelitor.io.FileUtils;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;

import static pixelitor.gui.utils.BrowseFilesSupport.SelectionMode.DIRECTORY;
import static pixelitor.gui.utils.BrowseFilesSupport.SelectionMode.FILE;

/**
 * The GUI elements of a file/directory chooser (a textfield and a
 * "Browse..." button) are separated into this non-component class
 * so that they can be reused with different layout managers
 */
public class BrowseFilesSupport {
    private JTextField nameTF;
    private final JButton button = new JButton("Browse...");
    private String fileChooserTitle;
    private FileNameExtensionFilter fileFilter; // used for filtering when in file selection mode

    public enum SelectionMode {DIRECTORY, FILE}

    private SelectionMode mode;

    public BrowseFilesSupport(String initialPath) {
        init(initialPath);
    }

    public BrowseFilesSupport(String initialPath,
                              String fileChooserTitle,
                              SelectionMode mode) {
        this.fileChooserTitle = fileChooserTitle;
        this.mode = mode;
        init(initialPath);
    }

    private void init(String initialPath) {
        nameTF = new JTextField(25);
        nameTF.setText(initialPath);
        button.addActionListener(e -> browseButtonClicked(fileChooserTitle));
    }

    public void setSelectionMode(SelectionMode mode) {
        this.mode = mode;
    }

    private void browseButtonClicked(String fileChooserTitle) {
        JFileChooser chooser;

        if (mode == DIRECTORY) {
            chooser = createChooserForDirectorySelection();
        } else {
            chooser = createChooserForFileSelection();
        }

        chooser.setDialogTitle(fileChooserTitle);
        chooser.showOpenDialog(PixelitorWindow.get());
        fillFileNameTextField(chooser.getSelectedFile());
    }

    private JFileChooser createChooserForDirectorySelection() {
        var chooser = new JFileChooser(nameTF.getText());
        chooser.setApproveButtonText("Select Folder");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        return chooser;
    }

    private JFileChooser createChooserForFileSelection() {
        File selectorCurrentDir;
        File f = new File(nameTF.getText());
        if (f.isDirectory()) {
            selectorCurrentDir = f;
        } else {
            selectorCurrentDir = f.getParentFile();
        }

        var chooser = new JFileChooser(selectorCurrentDir);
        chooser.setApproveButtonText("Select File");
        if (fileFilter != null) {
            // First remove the All Files option...
            chooser.setAcceptAllFileFilterUsed(false);
            // ... then add the extension filter corresponding to the saved file type...
            chooser.addChoosableFileFilter(fileFilter);
//            // ... then add back the All Files option so that it is at the end
//            chooser.setAcceptAllFileFilterUsed(true);
        }
        return chooser;
    }

    private void fillFileNameTextField(File selectedFile) {
        if (selectedFile != null) {
            String filePath = selectedFile.toString();

            if (mode == FILE) {
                boolean noExtGivenByUser = !FileUtils.hasExtension(selectedFile.getName());
                if (noExtGivenByUser && fileFilter != null) {
                    filePath = filePath + '.' + fileFilter.getExtensions()[0];
                }
            }

            nameTF.setText(filePath);
        }
    }

    public JTextField getNameTF() {
        return nameTF;
    }

    public JButton getBrowseButton() {
        return button;
    }

    public File getSelectedFile() {
        String s = nameTF.getText();

        return new File(s);
    }

    public void setFileChooserTitle(String fileChooserTitle) {
        this.fileChooserTitle = fileChooserTitle;
    }

    public void setFileFilter(FileNameExtensionFilter fileFilter) {
        this.fileFilter = fileFilter;
    }
}
