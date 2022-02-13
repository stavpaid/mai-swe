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

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import org.jdesktop.swingx.JXTreeTable;
import pixelitor.OpenImages;
import pixelitor.gui.GUIText;
import pixelitor.gui.View;
import pixelitor.gui.utils.DialogBuilder;
import pixelitor.gui.utils.Dialogs;
import pixelitor.gui.utils.PAction;
import pixelitor.io.FileUtils;
import pixelitor.utils.Messages;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.*;
import java.io.File;
import java.io.IOException;
import java.util.List;

import static java.awt.BorderLayout.*;
import static java.awt.FlowLayout.LEFT;
import static java.lang.String.format;
import static pixelitor.gui.GUIText.CLOSE_DIALOG;

public class MetaDataPanel extends JPanel implements DropTargetListener {
    private final JXTreeTable treeTable;

    private MetaDataPanel(MetaDataTreeTableModel model) {
        super(new BorderLayout());

        treeTable = new JXTreeTable(model);
        treeTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        treeTable.setRootVisible(false);
        treeTable.setHorizontalScrollEnabled(true);

        JScrollPane sp = new JScrollPane(treeTable);
        add(sp, CENTER);

        JPanel northPanel = new JPanel(new BorderLayout());
        JPanel northLeftPanel = new JPanel(new FlowLayout(LEFT));

        JButton expandButton = new JButton(new PAction("Expand All") {
            @Override
            public void onClick() {
                treeTable.expandAll();
            }
        });
        expandButton.setName("expandButton");

        JButton collapseButton = new JButton(new PAction("Collapse All") {
            @Override
            public void onClick() {
                treeTable.collapseAll();
            }
        });
        collapseButton.setName("collapseButton");

        northLeftPanel.add(expandButton);
        northLeftPanel.add(collapseButton);
        northPanel.add(northLeftPanel, WEST);

        JButton helpButton = new JButton(new PAction(GUIText.HELP) {
            @Override
            public void onClick() {
                showHelp();
            }
        });

        JPanel northRightPanel = new JPanel();
        northRightPanel.add(helpButton);
        northPanel.add(northRightPanel, EAST);
        add(northPanel, NORTH);

        setupColumnsWidths();

        new DropTarget(this, this);
    }

    private void showHelp() {
        String txt = "<html>You can drag external multimedia files on the Metadata window " +
            "to see their Exif, IPTC, etc. information." +
            "<br>It can read a different set of files than the rest of Pixelitor." +
            "<p><br>Supported file types: <b>JPEG, TIFF, WebP, PNG, BMP, GIF, HEIC, PSD, " +
            "ICO, PCX, MP3, WAV, QuickTime, MP4, AVI</b>." +
            "<br>Supported Camera Raw file types: <b>NEF</b> (Nikon), <b>CR2</b> (Canon), " +
            "<b>ORF</b> (Olympus), <b>ARW</b> (Sony), <br><b>RW2</b> (Panasonic), " +
            "<b>RWL</b> (Leica), <b>SRW</b> (Samsung).";
        Dialogs.showInfoDialog(this, "Show Metadata Help", txt);
    }

    private void setupColumnsWidths() {
        treeTable.getColumnModel().getColumn(0).setMinWidth(200);
        treeTable.getColumnModel().getColumn(1).setMinWidth(200);
    }

    @Override
    public void dragEnter(DropTargetDragEvent dtde) {
        handleOngoingDrag(dtde);
    }

    @Override
    public void dragOver(DropTargetDragEvent dtde) {
        handleOngoingDrag(dtde);
    }

    @Override
    public void dropActionChanged(DropTargetDragEvent dtde) {

    }

    @Override
    public void dragExit(DropTargetEvent dte) {

    }

    @Override
    public void drop(DropTargetDropEvent e) {
        Transferable transferable = e.getTransferable();
        DataFlavor[] flavors = transferable.getTransferDataFlavors();
        for (DataFlavor flavor : flavors) {
            if (flavor.isFlavorJavaFileListType()) {
                // this is where we get after dropping a file or directory
                e.acceptDrop(DnDConstants.ACTION_COPY);

                try {
                    @SuppressWarnings("unchecked")
                    List<File> list = (List<File>) transferable.getTransferData(flavor);
                    File file = list.get(0);
                    if (file.isFile()) {
                        changeFile(file);
                    }
                } catch (UnsupportedFlavorException | IOException ex) {
                    ex.printStackTrace();
                    e.rejectDrop();
                }
                e.dropComplete(true);
                return;
            }
        }

        // DataFlavor not recognized
        e.rejectDrop();
    }

    private static void handleOngoingDrag(DropTargetDragEvent dtde) {
        if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
            dtde.acceptDrag(DnDConstants.ACTION_COPY);
        } else {
            dtde.rejectDrag();
        }
    }

    private void changeFile(File file) {
        Metadata metadata = extractMetadata(file);
        if (metadata != null) {
            treeTable.setTreeTableModel(new MetaDataTreeTableModel(metadata));
            setupColumnsWidths();
            JDialog d = (JDialog) SwingUtilities.getWindowAncestor(this);
            d.setTitle("Metadata for " + file.getName());
        }
    }

    private static Metadata extractMetadata(File file) {
        Metadata metadata;
        try {
            metadata = ImageMetadataReader.readMetadata(file);
        } catch (ImageProcessingException | IOException e) {
            Messages.showException(e);
            return null;
        }
        return metadata;
    }

    public static void showInDialog() {
        View view = OpenImages.getActiveView();
        var comp = view.getComp();
        File file = comp.getFile();
        if (file == null) {
            Dialogs.showInfoDialog(view, "No file", format(
                "<html>There is no file for <b>%s</b>.", comp.getName()));
            return;
        }
        if (!file.exists()) {
            String msg = format(
                "<html>The metadata for <b>%s</b> cannot be shown because the file<br>" +
                    "<b>%s</b><br>" +
                    "doesn't exist anymore.",
                comp.getName(), file.getAbsolutePath());
            Messages.showError("File not found", msg, view);
            return;
        }
        if (FileUtils.hasTGAExtension(file.getName())) {
            String msg = "Metadata for TGA files is not supported yet.";
            Messages.showError("TGA File", msg, view);
            return;
        }
        Metadata metadata = extractMetadata(file);
        MetaDataPanel panel = new MetaDataPanel(new MetaDataTreeTableModel(metadata));
        new DialogBuilder()
            .title("Metadata for " + file.getName())
            .content(panel)
            .okText(CLOSE_DIALOG)
            .noCancelButton()
            .show();
    }
}
