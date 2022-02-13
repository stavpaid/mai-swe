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

import org.jdesktop.swingx.JXErrorPane;
import org.jdesktop.swingx.error.ErrorInfo;
import pixelitor.AppContext;
import pixelitor.gui.GUIText;
import pixelitor.gui.GlobalEvents;
import pixelitor.gui.PixelitorWindow;
import pixelitor.layers.Layer;
import pixelitor.utils.Utils;
import pixelitor.utils.test.Events;
import pixelitor.utils.test.RandomGUITest;

import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Synthesizer;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.UncheckedIOException;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import static java.lang.String.format;
import static javax.swing.JOptionPane.*;
import static pixelitor.utils.Threads.calledOutsideEDT;
import static pixelitor.utils.Threads.threadName;

/**
 * Static utility methods related to dialogs
 */
public class Dialogs {
    private static boolean mainWindowInitialized = false;

    private Dialogs() { // should not be instantiated
    }

    public static void setMainWindowInitialized(boolean mainWindowInitialized) {
        Dialogs.mainWindowInitialized = mainWindowInitialized;
    }

    private static Frame getMainWindow() {
        if (mainWindowInitialized) {
            return PixelitorWindow.get();
        }
        return null;
    }

    public static void showInfoDialog(String title, String msg) {
        showInfoDialog(getMainWindow(), title, msg);
    }

    public static void showInfoDialog(Component parent, String title, String msg) {
        if (RandomGUITest.isRunning()) { // avoid dialogs
            if (!msg.startsWith("As a result of")) {
                System.out.println("\nInfo: " + msg);
                Thread.dumpStack();
            }
            return;
        }

        if (parent == null) { // can happen when called via Messages
            parent = getMainWindow();
        }

        GlobalEvents.dialogOpened(title);
        showMessageDialog(parent, msg, title, INFORMATION_MESSAGE);
        GlobalEvents.dialogClosed(title);
    }

    public static boolean showYesNoQuestionDialog(String title, String msg) {
        return showYesNoQuestionDialog(getMainWindow(), title, msg);
    }

    public static boolean showYesNoQuestionDialog(Component parent, String title,
                                                  String msg) {
        return showYesNoDialog(parent, title, msg, QUESTION_MESSAGE);
    }

    public static int showYesNoCancelDialog(String title, String question,
                                            Object[] options, int messageType) {
        return showYesNoCancelDialog(getMainWindow(), title, question, options, messageType);
    }

    public static int showYesNoCancelDialog(Component parent, String title,
                                            String question, Object[] options,
                                            int messageType) {
        GlobalEvents.dialogOpened(title);
        int answer = showOptionDialog(parent, new JLabel(question),
            title, YES_NO_CANCEL_OPTION,
            messageType, null, options, options[0]);
        GlobalEvents.dialogClosed(title);
        return answer;
    }

    public static boolean showYesNoWarningDialog(String title, String msg) {
        return showYesNoWarningDialog(getMainWindow(), title, msg);
    }

    public static boolean showYesNoWarningDialog(Component parent, String title,
                                                 String msg) {
        return showYesNoDialog(parent, title, msg, WARNING_MESSAGE);
    }

    public static boolean showYesNoDialog(Component parent, String title,
                                          String msg, int messageType) {
        GlobalEvents.dialogOpened(title);
        int reply = showConfirmDialog(parent, msg, title, YES_NO_OPTION, messageType);
        GlobalEvents.dialogClosed(title);

        return reply == YES_OPTION;
    }

    public static boolean showOKCancelWarningDialog(String msg, String title,
                                                    Object[] options,
                                                    int initialOptionIndex) {
        return showOKCancelDialog(msg, title, options, initialOptionIndex, WARNING_MESSAGE);
    }

    public static boolean showOKCancelDialog(Object msg, String title,
                                             Object[] options,
                                             int initialOptionIndex,
                                             int messageType) {
        return showOKCancelDialog(getMainWindow(), msg, title, options,
            initialOptionIndex, messageType);
    }

    public static boolean showOKCancelDialog(Component parent,
                                             Object msg, String title,
                                             Object[] options,
                                             int initialOptionIndex,
                                             int messageType) {
        GlobalEvents.dialogOpened(title);
        int userAnswer = showOptionDialog(parent, msg, title,
            OK_CANCEL_OPTION, messageType, null,
            options, options[initialOptionIndex]);
        GlobalEvents.dialogClosed(title);

        return userAnswer == OK_OPTION;
    }

    public static void showErrorDialog(String title, String msg) {
        showErrorDialog(getMainWindow(), title, msg);
    }

    public static void showErrorDialog(Component parent, String title, String msg) {
        if (RandomGUITest.isRunning()) {
            System.err.println("\nError: " + msg); // avoid dialogs
            Thread.dumpStack();
            return;
        }

        if (parent == null) { // can happen when called via Messages
            parent = getMainWindow();
        }

        GlobalEvents.dialogOpened(title);
        showMessageDialog(parent, msg, title, ERROR_MESSAGE);
        GlobalEvents.dialogClosed(title);
    }

    public static String showInputDialog(Component parent, String title, String msg) {
        GlobalEvents.dialogOpened(title);
        String userInput = JOptionPane.showInputDialog(parent, msg, title, QUESTION_MESSAGE);
        GlobalEvents.dialogClosed(title);

        return userInput;
    }

    public static void showFileNotWritableDialog(File file) {
        showFileNotWritableDialog(getMainWindow(), file);
    }

    public static void showFileNotWritableDialog(Component parent, File file) {
        String msg = format("<html>The file <b>%s</b> is not writable." +
            "<br>To keep your changes, save the image " +
            "with a new name or in another folder.", file.getAbsolutePath());
        showErrorDialog(parent, "File not writable", msg);
    }

    public static void showWarningDialog(String title, String msg) {
        showWarningDialog(getMainWindow(), title, msg);
    }

    public static void showNotAColorOnClipboardDialog(Window parent) {
        showWarningDialog(parent, "Not a Color",
            "The clipboard contents could not be interpreted as a color");
    }

    public static void showWarningDialog(Component parent, String title, String msg) {
        GlobalEvents.dialogOpened(title);
        showMessageDialog(parent, msg, title, WARNING_MESSAGE);
        GlobalEvents.dialogClosed(title);
    }

    public static void showNotImageLayerDialog(Layer layer) {
        if (!RandomGUITest.isRunning()) {
            String msg = format("The active layer \"%s\" is not an image layer.",
                layer.getName());
            showErrorDialog("Not an image layer", msg);
        }
    }

    public static void showNotDrawableDialog(Layer layer) {
        String msg = format("The active layer \"%s\" is not an image layer or mask.",
            layer.getName());
        showErrorDialog("Not an image layer or mask", msg);
    }

    public static void showExceptionDialog(Throwable e) {
        Thread currentThread = Thread.currentThread();
        showExceptionDialog(e, currentThread);
    }

    public static void showExceptionDialog(Throwable e, Thread srcThread) {
        if (calledOutsideEDT()) {
            System.err.printf("ERROR: Dialogs.showExceptionDialog called on %s%n", threadName());

            // call this method on the EDT
            Throwable finalE = e;
            EventQueue.invokeLater(() -> showExceptionDialog(finalE, srcThread));
            return;
        }

        System.err.printf("%nDialogs.showExceptionDialog: Exception in the thread '%s'%n",
            srcThread.getName());
        e.printStackTrace();

        RandomGUITest.stop();

        if (e instanceof OutOfMemoryError) {
            showOutOfMemoryDialog((OutOfMemoryError) e);
            return;
        }

        showMoreDevelopmentInfo(e);

        if (e instanceof CompletionException) {
            e = e.getCause();
        }
        if (e instanceof UncheckedIOException) {
            e = e.getCause();
        }
        if (e instanceof InvocationTargetException) {
            e = e.getCause();
        }

        Frame parent = getMainWindow();
        String basicErrorMessage = """
            A program error occurred.
                        
            Please consider reporting this error to the developers by creating a new issue on github (see "Help/Report an Issue..." in the menus).
            If you do, then open "Details", click "Copy to Clipboard", and paste the details into the issue.""";
        var errorInfo = new ErrorInfo("Program Error",
            basicErrorMessage, null, null, e,
            Level.SEVERE, null);
        JXErrorPane.showDialog(parent, errorInfo);
    }

    private static void showMoreDevelopmentInfo(Throwable e) {
        if (AppContext.isFinal()) {
            return;
        }

        boolean randomGUITest = false;
        boolean assertJSwingTest = false;
        StackTraceElement[] stackTraceElements = e.getStackTrace();
        for (StackTraceElement ste : stackTraceElements) {
            String className = ste.getClassName();
            if (className.contains("RandomGUITest")) {
                randomGUITest = true;
                break;
            } else if (className.contains("AssertJSwingTest")) {
                assertJSwingTest = true;
                break;
            }
        }

        // the following should happen only for the GUI tests,
        // which are running for a long time
        boolean guiTest = randomGUITest || assertJSwingTest;
        if (!guiTest) {
            return;
        }

        // avoid the mixing of the stack trace with
        // the event dumps
        Utils.sleep(2, TimeUnit.SECONDS);

        if (randomGUITest) {
            Events.dumpAll();
        }
        Toolkit.getDefaultToolkit().beep();
        playWarningSound();
    }

    public static void playWarningSound() {
        try {
            int maxVolume = 90;
            int sound = 65;
            Synthesizer synthesizer = MidiSystem.getSynthesizer();
            synthesizer.open();
            MidiChannel channel = synthesizer.getChannels()[9];  // drums channel.
            for (int i = 0; i < 10; i++) {
                Thread.sleep(100);
                channel.noteOn(sound + i, maxVolume);
                Thread.sleep(100);
                channel.noteOff(sound + i);
            }
        } catch (MidiUnavailableException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
        }
    }

    public static void showOutOfMemoryDialog(OutOfMemoryError e) {
        if (AppContext.isDevelopment()) {
            e.printStackTrace();
        }
        String msg = "<html><b>Out of memory error.</b> You can try <ul>" +
            "<li>decreasing the undo levels" +
            "<li>decreasing the number of layers" +
            "<li>working with smaller images" +
            "<li>putting more RAM into your computer";
        String title = "Out of memory error.";
        showErrorDialog(title, msg);
    }

    public static int showCloseWarningDialog(String compName) {
        Object[] options = {"Save", "Don't Save", GUIText.CANCEL};
        String question = format(
            "<html><b>Do you want to save the changes made to %s?</b>" +
                "<br>Your changes will be lost if you don't save them.</html>",
            compName);

        return showYesNoCancelDialog("Unsaved changes",
            question, options, WARNING_MESSAGE);
    }

    public static void showFileNotReadableError(Component parent, File f) {
        showErrorDialog(parent, "File not readable",
            "<html>The file <b>" + f.getAbsolutePath()
            + " </b> isn't readable. " +
            "<br>Change the file's permissions and try again."
        );
    }

    public static void showNoExtensionDialog(JComponent parent) {
        String title = "No File Extension";
        String msg = """
            The file name has no extension.
            An extension (such as ".png") must be added at the end,
            because the file format depends on it.""";
        if (parent == null) {
            showMessageDialog(getMainWindow(), msg, title, ERROR_MESSAGE);
        } else {
            showMessageDialog(parent, msg, title, ERROR_MESSAGE);
        }
    }
}
