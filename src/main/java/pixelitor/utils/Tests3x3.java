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

package pixelitor.utils;

import pixelitor.Composition;
import pixelitor.OpenImages;
import pixelitor.colors.Colors;
import pixelitor.layers.Layer;
import pixelitor.layers.LayerMask;
import pixelitor.menus.view.ZoomLevel;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.Random;

import static java.awt.image.BufferedImage.TYPE_BYTE_GRAY;
import static java.lang.String.format;

/**
 * Support for testing with small (3x3) images,
 * where it is feasible to check every pixel
 */
public class Tests3x3 {
    public static final int RED = Color.RED.getRGB();
    public static final int GREEN = Color.GREEN.getRGB();
    public static final int BLUE = Color.BLUE.getRGB();

    public static final int WHITE = Color.WHITE.getRGB();
    public static final int GRAY = Color.GRAY.getRGB();
    public static final int BLACK = Color.BLACK.getRGB();

    public static final int YELLOW = Color.YELLOW.getRGB();
    public static final int MAGENTA = Color.MAGENTA.getRGB();
    public static final int CYAN = Color.CYAN.getRGB();

    public static final int SEMI_TRANSPARENT_WHITE = Colors.toPackedARGB(128, 255, 0, 0);
    public static final int SEMI_TRANSPARENT_GRAY = Colors.toPackedARGB(128, 0, 255, 0);
    public static final int SEMI_TRANSPARENT_BLACK = Colors.toPackedARGB(128, 0, 0, 255);

    private Tests3x3() {
    }

    public static BufferedImage getStandardImage1() {
        BufferedImage img = ImageUtils.createSysCompatibleImage(3, 3);
        img.setRGB(0, 0, Colors.toPackedARGB(255, 223, 235, 120));
        img.setRGB(0, 1, Colors.toPackedARGB(255, 35, 125, 43));
        img.setRGB(0, 2, Colors.toPackedARGB(255, 89, 28, 207));
        img.setRGB(1, 0, Colors.toPackedARGB(255, 101, 224, 114));
        img.setRGB(1, 1, Colors.toPackedARGB(255, 92, 49, 135));
        img.setRGB(1, 2, Colors.toPackedARGB(255, 27, 238, 72));
        img.setRGB(2, 0, Colors.toPackedARGB(255, 255, 91, 179));
        img.setRGB(2, 1, Colors.toPackedARGB(255, 88, 190, 199));
        img.setRGB(2, 2, Colors.toPackedARGB(255, 128, 128, 128));
        return img;
    }

    public static BufferedImage getStandardImage2() {
        BufferedImage img = ImageUtils.createSysCompatibleImage(3, 3);
        img.setRGB(0, 0, Colors.toPackedARGB(255, 190, 149, 66));
        img.setRGB(0, 1, Colors.toPackedARGB(255, 37, 159, 8));
        img.setRGB(0, 2, Colors.toPackedARGB(255, 63, 107, 198));
        img.setRGB(1, 0, Colors.toPackedARGB(255, 174, 25, 53));
        img.setRGB(1, 1, Colors.toPackedARGB(255, 146, 58, 135));
        img.setRGB(1, 2, Colors.toPackedARGB(255, 61, 71, 82));
        img.setRGB(2, 0, Colors.toPackedARGB(255, 143, 125, 211));
        img.setRGB(2, 1, Colors.toPackedARGB(255, 208, 84, 44));
        img.setRGB(2, 2, Colors.toPackedARGB(255, 209, 72, 111));
        return img;
    }


    public static BufferedImage getRandomImage(boolean withTransparency) {
        BufferedImage img = ImageUtils.createSysCompatibleImage(3, 3);
        Random rand = new Random();
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                int a;
                if (withTransparency) {
                    a = rand.nextInt(256);
                } else {
                    a = 255;
                }
                int r = rand.nextInt(256);
                int g = rand.nextInt(256);
                int b = rand.nextInt(256);
                img.setRGB(x, y, Colors.toPackedARGB(a, r, g, b));
            }
        }
        return img;
    }

    public static BufferedImage getStandardMaskImage() {
        BufferedImage img = new BufferedImage(3, 3, TYPE_BYTE_GRAY);
        img.setRGB(0, 0, WHITE);
        img.setRGB(1, 0, WHITE);
        img.setRGB(2, 0, WHITE);

        img.setRGB(0, 1, GRAY);
        img.setRGB(1, 1, WHITE);
        img.setRGB(2, 1, GRAY);

        img.setRGB(0, 2, BLACK);
        img.setRGB(1, 2, GRAY);
        img.setRGB(2, 2, BLACK);

        return img;
    }

    public static void addStandardImage(boolean withMask) {
        BufferedImage img = getRandomImage(false);
//        BufferedImage img = getStandardImage(false);
        var comp = Composition.fromImage(img, null, "3x3 Test");

        if (withMask) {
            BufferedImage maskImg = getStandardMaskImage();
            Layer layer = comp.getLayer(0);
            LayerMask mask = new LayerMask(comp, maskImg, layer, 0, 0);
            layer.addConfiguredMask(mask);
        }
        OpenImages.addAsNewComp(comp);
        comp.getView().setZoom(ZoomLevel.Z100);
    }

    // creates expected results from actual ones for regression tests
    public static String getExpectedFromActual(BufferedImage actual) {
        String s = "BufferedImage getExpectedImageAfter OP() {\n";
        int width = actual.getWidth();
        int height = actual.getHeight();
        s += format("BufferedImage img = ImageUtils.createCompatibleImage(%d, %d);\n",
            width, height);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int rgb = actual.getRGB(x, y);
                int a = (rgb >>> 24) & 0xFF;
                int r = (rgb >>> 16) & 0xFF;
                int g = (rgb >>> 8) & 0xFF;
                int b = rgb & 0xFF;

                s += format("    img.setRGB(%d, %d, toPackedInt(%d, %d, %d, %d));\n",
                    x, y, a, r, g, b);
            }
        }

        s += "    return img;\n}\n";
        return s;
    }

    public static void dumpCompositeOfActive() {
        BufferedImage img = OpenImages.getActiveCompositeImage();
        String actual = getExpectedFromActual(img);
        System.out.printf("Tests3x3::dumpCompositeOfActive: \n%s\n%n", actual);
    }
}
