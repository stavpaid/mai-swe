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

package pixelitor.utils.test;

import org.jdesktop.swingx.painter.AbstractLayoutPainter.HorizontalAlignment;
import org.jdesktop.swingx.painter.AbstractLayoutPainter.VerticalAlignment;
import org.jdesktop.swingx.painter.effects.ShadowPathEffect;
import pixelitor.Canvas;
import pixelitor.Composition;
import pixelitor.Composition.LayerAdder;
import pixelitor.NewImage;
import pixelitor.Pixelitor;
import pixelitor.automate.SingleDirChooser;
import pixelitor.colors.FgBgColors;
import pixelitor.colors.FillType;
import pixelitor.filters.jhlabsproxies.JHBrushedMetal;
import pixelitor.filters.painters.AreaEffects;
import pixelitor.filters.painters.TextSettings;
import pixelitor.io.Dirs;
import pixelitor.io.FileFormat;
import pixelitor.io.FileUtils;
import pixelitor.io.SaveSettings;
import pixelitor.layers.Drawable;
import pixelitor.layers.ImageLayer;
import pixelitor.layers.TextLayer;
import pixelitor.tools.gradient.Gradient;
import pixelitor.tools.gradient.GradientType;
import pixelitor.tools.util.Drag;
import pixelitor.utils.Messages;
import pixelitor.utils.ProgressHandler;
import pixelitor.utils.Rnd;
import pixelitor.utils.Utils;

import java.awt.Font;
import java.awt.font.TextAttribute;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static java.awt.Color.WHITE;
import static java.awt.MultipleGradientPaint.CycleMethod.REFLECT;
import static java.awt.font.TextAttribute.*;
import static java.lang.String.format;
import static pixelitor.Composition.LayerAdder.Position.TOP;
import static pixelitor.FilterContext.FILTER_WITHOUT_DIALOG;
import static pixelitor.layers.BlendingMode.MULTIPLY;
import static pixelitor.layers.BlendingMode.NORMAL;
import static pixelitor.tools.gradient.GradientColorType.BLACK_TO_WHITE;
import static pixelitor.tools.gradient.GradientColorType.FG_TO_BG;
import static pixelitor.utils.Threads.*;

/**
 * Static methods for creating the splash images
 */
public class SplashImageCreator {
    private static final String SPLASH_SMALL_FONT = "DejaVu Sans Light";
    private static final String SPLASH_MAIN_FONT = "Segoe Print";
    private static final int MAIN_FONT_SIZE = 62;

    private static final int SPLASH_WIDTH = 400;
    private static final int SPLASH_HEIGHT = 247;

    private SplashImageCreator() {
    }

    public static void saveManySplashImages(int numImages) {
        assert calledOnEDT() : threadInfo();

        boolean okPressed = SingleDirChooser.selectOutputDir(FileFormat.PNG);
        if (!okPressed) {
            return;
        }

        String msg = format("Save %d Splash Images: ", numImages);
        var progressHandler = Messages.startProgress(msg, numImages);

        CompletableFuture<Void> cf = CompletableFuture.completedFuture(null);
        FileFormat format = FileFormat.getLastOutput();
        for (int i = 0; i < numImages; i++) {
            int seqNo = i;
            cf = cf.thenCompose(v -> makeSplashAsync(progressHandler, seqNo, format));
        }
        cf.thenRunAsync(() -> cleanupAfterManySplashes(numImages, progressHandler), onEDT);
    }

    private static CompletableFuture<Void> makeSplashAsync(ProgressHandler ph, int seqNo, FileFormat format) {
        return CompletableFuture
            .supplyAsync(() -> makeOneSplashImage(ph, seqNo, format), onEDT)
            .thenCompose(comp -> saveAndCloseOneSplash(comp, format));
    }

    private static Composition makeOneSplashImage(ProgressHandler ph, int seqNo, FileFormat format) {
        ph.updateProgress(seqNo);

        var comp = createSplashComp();
        comp.getView().paintImmediately();

        String fileName = format("splash%04d.%s", seqNo, format);
        comp.setFile(new File(Dirs.getLastSave(), fileName));

        return comp;
    }

    public static Composition createSplashComp() {
        assert calledOnEDT() : threadInfo();

        FgBgColors.setFGColor(WHITE);
        FgBgColors.setBGColor(Rnd.createRandomColor().darker().darker().darker());

        var comp = NewImage.addNewImage(FillType.WHITE,
            SPLASH_WIDTH, SPLASH_HEIGHT, "Splash");
        ImageLayer layer = (ImageLayer) comp.getLayer(0);

        for (int i = 0; i < 3; i++) {
            GradientType gradientType = Rnd.chooseFrom(GradientType.values());

            Drag randomDrag = Drag.createRandom(
                SPLASH_WIDTH, SPLASH_HEIGHT, SPLASH_HEIGHT / 2);
            Gradient gradient = new Gradient(randomDrag,
                gradientType, REFLECT, FG_TO_BG,
                false, MULTIPLY, 1.0f);
            gradient.drawOn(layer);
        }

        layer = addNewLayer(comp, "rendered");
        var brushedMetal = new JHBrushedMetal();
        brushedMetal.startOn(layer, FILTER_WITHOUT_DIALOG);
        layer.setBlendingMode(MULTIPLY, true);

        addTextLayers(comp);

        return comp;
    }

    private static void addTextLayers(Composition comp) {
        FgBgColors.setFGColor(WHITE);

        Font font = createSplashFont(SPLASH_SMALL_FONT, 20);
        addTextLayer(comp, "version " + Pixelitor.VERSION_NUMBER,
            font, 50);

        font = createSplashFont(SPLASH_MAIN_FONT, MAIN_FONT_SIZE);
        addTextLayer(comp, "Pixelitor",
            font, -17);

        font = createSplashFont(SPLASH_SMALL_FONT, 22);
        addTextLayer(comp, "Loading...",
            font, -70);
    }

    private static Font createSplashFont(String name, int size) {
        Font font = new Font(name, Font.PLAIN, size);

        // check that the font exists
        assert font.getName().equals(name) : font.getName();

        Map<TextAttribute, Object> attr = new HashMap<>();
        attr.put(KERNING, KERNING_ON);
        attr.put(LIGATURES, LIGATURES_ON);
        font = font.deriveFont(attr);

        return font;
    }

    private static ImageLayer addNewLayer(Composition comp, String name) {
        ImageLayer imageLayer = comp.addNewEmptyImageLayer(name, false);
        imageLayer.setName(name, true);
        return imageLayer;
    }

    private static TextLayer addNewTextLayer(Composition comp, String name, TextSettings settings, int translationY) {
        var layer = new TextLayer(comp, name, settings);

        layer.setTranslation(0, translationY);

        new LayerAdder(comp).atPosition(TOP).add(layer);
        return layer;
    }

    private static void addTextLayer(Composition comp, String text,
                                     Font font,
                                     int translationY) {
        AreaEffects effects = createDropShadowEffect();
        var settings = new TextSettings(text, font, WHITE, effects,
            HorizontalAlignment.CENTER,
            VerticalAlignment.CENTER, false, 0, null);

        TextLayer layer = addNewTextLayer(comp, text, settings, translationY);
    }

    private static AreaEffects createDropShadowEffect() {
        var effects = new AreaEffects();
        var dropShadowEffect = new ShadowPathEffect(0.6f);
        dropShadowEffect.setEffectWidth(3);
        dropShadowEffect.setOffset(Utils.offsetFromPolar(4, 0.7));
        effects.setDropShadow(dropShadowEffect);
        return effects;
    }

    private static void addBWGradientTo(Drawable dr) {
        Canvas canvas = dr.getComp().getCanvas();
        int canvasWidth = canvas.getWidth();
        int canvasHeight = canvas.getHeight();

        int startX = canvasWidth / 2;
        int startY = canvasHeight / 2;

        int endX = 0;
        int endY = 0;
        if (canvasWidth > canvasHeight) {
            endX = startX;
        } else {
            endY = startY;
        }

        Gradient gradient = new Gradient(
            new Drag(startX, startY, endX, endY),
            GradientType.RADIAL, REFLECT, BLACK_TO_WHITE,
            false, NORMAL, 1.0f);
        gradient.drawOn(dr);
    }

    private static CompletableFuture<Void> saveAndCloseOneSplash(Composition comp, FileFormat format) {
        var flatSettings = new SaveSettings(format, comp.getFile());

        var pxcFile = new File(comp.getFile().getParent(),
            FileUtils.replaceExt(comp.getFile().getName(), "pxc"));
        var psxSettings = new SaveSettings(FileFormat.PXC, pxcFile);

        return comp.saveAsync(flatSettings, false)
            .thenCompose(v -> comp.saveAsync(psxSettings, false))
            .thenAcceptAsync(v -> comp.getView().close(), onEDT);
    }

    private static void cleanupAfterManySplashes(int numCreatedImages,
                                                 ProgressHandler progressHandler) {
        progressHandler.stopProgress();
        Messages.showPlainInStatusBar(format(
            "Finished saving %d splash images to %s",
            numCreatedImages, Dirs.getLastSave()));
    }
}
