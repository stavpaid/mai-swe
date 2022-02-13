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

package pixelitor.filters;

import com.jhlabs.image.ImageMath;
import pixelitor.ThreadPool;
import pixelitor.filters.gui.ColorParam;
import pixelitor.filters.gui.EnumParam;
import pixelitor.filters.gui.RangeParam;
import pixelitor.filters.util.NoiseInterpolation;
import pixelitor.utils.ImageUtils;
import pixelitor.utils.StatusBarProgressTracker;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.Random;
import java.util.concurrent.Future;

import static java.awt.Color.BLACK;
import static java.awt.Color.WHITE;
import static pixelitor.filters.gui.ColorParam.TransparencyPolicy.USER_ONLY_TRANSPARENCY;
import static pixelitor.filters.gui.ReseedActions.reseedByCalling;
import static pixelitor.gui.GUIText.ZOOM;

/**
 * Renders value noise
 */
public class ValueNoise extends ParametrizedFilter {
    public static final String NAME = "Value Noise";

    private static final Random rand = new Random();
    private static int r1;
    private static int r2;
    private static int r3;

    static {
        reseed();
    }

    private final RangeParam scale = new RangeParam(ZOOM, 5, 100, 300);
    private final RangeParam details = new RangeParam("Octaves (Details)", 1, 5, 8);
    private final RangeParam persistenceParam =
        new RangeParam("Roughness (%)", 0, 60, 100);
    private final EnumParam<NoiseInterpolation> interpolation
        = new EnumParam<>("Interpolation", NoiseInterpolation.class);

    private final ColorParam color1 = new ColorParam("Color 1", BLACK, USER_ONLY_TRANSPARENCY);
    private final ColorParam color2 = new ColorParam("Color 2", WHITE, USER_ONLY_TRANSPARENCY);

    public ValueNoise() {
        super(false);

        setParams(
            scale.withAdjustedRange(0.3),
            details,
            persistenceParam,
            interpolation.withDefault(NoiseInterpolation.CUBIC),
            color1,
            color2
        ).withAction(reseedByCalling(ValueNoise::reseed));

        helpURL = "https://en.wikipedia.org/wiki/Value_noise";
    }

    @Override
    public BufferedImage doTransform(BufferedImage src, BufferedImage dest) {
        int[] lookupTable = new int[256];
        Color c1 = color1.getColor();
        Color c2 = color2.getColor();
        int[] colorArray1 = {c1.getAlpha(), c1.getRed(), c1.getGreen(), c1.getBlue()};
        int[] colorArray2 = {c2.getAlpha(), c2.getRed(), c2.getGreen(), c2.getBlue()};

        for (int i = 0, lookupTableLength = lookupTable.length; i < lookupTableLength; i++) {
            lookupTable[i] = ImageUtils.lerpAndPremultiply(
                i / 255.0f, colorArray1, colorArray2);
        }

        int[] destData = ImageUtils.getPixelsAsArray(dest);
        int width = dest.getWidth();
        int height = dest.getHeight();
        float frequency = 1.0f / scale.getValueAsFloat();

        float persistence = persistenceParam.getPercentageValF();

        var pt = new StatusBarProgressTracker(NAME, height);
        NoiseInterpolation interp = interpolation.getSelected();

        Future<?>[] futures = new Future[height];
        for (int y = 0; y < height; y++) {
            int finalY = y;
            Runnable lineTask = () -> calculateLine(lookupTable, destData,
                width, frequency, persistence, finalY, interp);
            futures[y] = ThreadPool.submit(lineTask);
        }
        ThreadPool.waitFor(futures, pt);

        pt.finished();

        return dest;
    }

    private void calculateLine(int[] lookupTable, int[] destData,
                               int width, float frequency, float persistence,
                               int y, NoiseInterpolation interp) {
        for (int x = 0; x < width; x++) {
            int octaves = details.getValue();

            int noise = (int) (255 * generateValueNoise(x, y,
                octaves, frequency, persistence, interp));

            int value = lookupTable[noise];
            destData[x + y * width] = value;
        }
    }

    /**
     * Returns a float between 0 and 1
     */
    @SuppressWarnings("WeakerAccess")
    public static float generateValueNoise(int x, int y,
                                           int octaves,
                                           float frequency,
                                           float persistence,
                                           NoiseInterpolation interp) {
        float total = 0.0f;

        float amplitude = 1.0f;
        for (int lcv = 0; lcv < octaves; lcv++) {
            total += smooth(x * frequency, y * frequency, interp) * amplitude;
            frequency *= 2;
            amplitude *= persistence;
        }

        return ImageMath.clamp01(total);
    }

    private static float smooth(float x, float y, NoiseInterpolation interp) {
        float n1 = noise((int) x, (int) y);
        float n2 = noise((int) x + 1, (int) y);
        float n3 = noise((int) x, (int) y + 1);
        float n4 = noise((int) x + 1, (int) y + 1);

        float i1 = interpolate(n1, n2, x - (int) x, interp);
        float i2 = interpolate(n3, n4, x - (int) x, interp);

        return interpolate(i1, i2, y - (int) y, interp);
    }

    public static void reseed() {
        r1 = 1000 + rand.nextInt(90000);
        r2 = 10000 + rand.nextInt(900000);
        r3 = 100000 + rand.nextInt(1000000000);
    }

    private static float noise(int x, int y) {
        int n = x + y * 57;
        n = (n << 13) ^ n;

        return (1.0f - ((n * (n * n * r1 + r2) + r3) & 0x7fffffff) / 1.07374182E+9f);
    }

    private static float interpolate(float x, float y, float a, NoiseInterpolation interp) {
        float t = interp.step(a);
        return ImageMath.lerp(t, x, y);
    }

    public void setDetails(int newDetails) {
        details.setValue(newDetails);
    }

    @Override
    public boolean supportsGray() {
        return false;
    }
}