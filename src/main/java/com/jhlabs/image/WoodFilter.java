/*
Copyright 2006 Jerry Huxtable

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package com.jhlabs.image;

import com.jhlabs.math.Noise;

/**
 * A filter which produces a simulated wood texture. This is a bit of a hack, but might be usefult to some people.
 */
public class WoodFilter extends PointFilter {
    private float scale = 200;
    private float stretch = 10.0f;
    private float angle = (float) Math.PI / 2;
    private float rings = 0.5f;
    private float turbulence = 0.0f;
    private float fibres = 0.5f;
    private float gain = 0.8f;
    private float m00 = 1.0f;
    private float m01 = 0.0f;
    private float m10 = 0.0f;
    private float m11 = 1.0f;
    private Colormap colormap = new LinearColormap(0xffe5c494, 0xff987b51);

    /**
     * Construct a WoodFilter.
     */
    public WoodFilter(String filterName) {
        super(filterName);
    }

    /**
     * Specifies the rings value.
     *
     * @param rings the rings value.
     * @min-value 0
     * @max-value 1
     * @see #getRings
     */
    public void setRings(float rings) {
        this.rings = rings;
    }

    /**
     * Returns the rings value.
     *
     * @return the rings value.
     * @see #setRings
     */
    public float getRings() {
        return rings;
    }

    /**
     * Specifies the scale of the texture.
     *
     * @param scale the scale of the texture.
     * @min-value 1
     * @max-value 300+
     * @see #getScale
     */
    public void setScale(float scale) {
        this.scale = scale;
    }

    /**
     * Returns the scale of the texture.
     *
     * @return the scale of the texture.
     * @see #setScale
     */
    public float getScale() {
        return scale;
    }

    /**
     * Specifies the stretch factor of the texture.
     *
     * @param stretch the stretch factor of the texture.
     * @min-value 1
     * @max-value 50+
     * @see #getStretch
     */
    public void setStretch(float stretch) {
        this.stretch = stretch;
    }

    /**
     * Returns the stretch factor of the texture.
     *
     * @return the stretch factor of the texture.
     * @see #setStretch
     */
    public float getStretch() {
        return stretch;
    }

    /**
     * Specifies the angle of the texture.
     *
     * @param angle the angle of the texture.
     * @angle
     * @see #getAngle
     */
    public void setAngle(float angle) {
        this.angle = angle;
        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);
        m00 = cos;
        m01 = sin;
        m10 = -sin;
        m11 = cos;
    }

    /**
     * Returns the angle of the texture.
     *
     * @return the angle of the texture.
     * @see #setAngle
     */
    public float getAngle() {
        return angle;
    }

    /**
     * Specifies the turbulence of the texture.
     *
     * @param turbulence the turbulence of the texture.
     * @min-value 0
     * @max-value 1
     * @see #getTurbulence
     */
    public void setTurbulence(float turbulence) {
        this.turbulence = turbulence;
    }

    /**
     * Returns the turbulence of the texture.
     *
     * @return the turbulence of the texture.
     * @see #setTurbulence
     */
    public float getTurbulence() {
        return turbulence;
    }

    /**
     * Specifies the amount of fibres in the texture.
     *
     * @param fibres the amount of fibres in the texture.
     * @min-value 0
     * @max-value 1
     * @see #getFibres
     */
    public void setFibres(float fibres) {
        this.fibres = fibres;
    }

    /**
     * Returns the amount of fibres in  the texture.
     *
     * @return the amount of fibres in the texture.
     * @see #setFibres
     */
    public float getFibres() {
        return fibres;
    }

    /**
     * Specifies the gain of the texture.
     *
     * @param gain the gain of the texture.
     * @min-value 0
     * @max-value 1
     * @see #getGain
     */
    public void setGain(float gain) {
        this.gain = gain;
    }

    /**
     * Returns the gain of the texture.
     *
     * @return the gain of the texture.
     * @see #setGain
     */
    public float getGain() {
        return gain;
    }

    /**
     * Set the colormap to be used for the filter.
     *
     * @param colormap the colormap
     * @see #getColormap
     */
    public void setColormap(Colormap colormap) {
        this.colormap = colormap;
    }

    /**
     * Get the colormap to be used for the filter.
     *
     * @return the colormap
     * @see #setColormap
     */
    public Colormap getColormap() {
        return colormap;
    }

    @Override
    public int filterRGB(int x, int y, int rgb) {
        float nx = m00 * x + m01 * y;
        float ny = m10 * x + m11 * y;
        nx /= scale;
        ny /= scale * stretch;
        float f = Noise.noise2(nx, ny);
        f += 0.1f * turbulence * Noise.noise2(nx * 0.05f, ny * 20);
        f = (f * 0.5f) + 0.5f;

        f *= rings * 50;
        f = f - (int) f;
        f *= 1 - ImageMath.smoothStep(gain, 1.0f, f);

        f += fibres * Noise.noise2(nx * scale, ny * 50);

        // happened during robot tests
        if (Float.isNaN(f)) {
            System.out.println("WoodFilter::filterRGB: x = " + x + ", y = " + y);
            System.out
                .printf("WoodFilter::filterRGB: m00 = %.2f, m01 = %.2f, m10 = %.2f, m11 = %.2f%n", m00, m01, m10, m11);
            System.out
                .printf("WoodFilter::filterRGB: scale = %.2f, stretch = %.2f, angle = %.2f%n", scale, stretch, angle);
            System.out
                .printf("WoodFilter::filterRGB: rings = %.2f, turbulence = %.2f, fibres = %.2f, gain = %.2f%n", rings, turbulence, fibres, gain);
        }

        int a = rgb & 0xff000000;
        int v;
        if (colormap != null) {
            v = colormap.getColor(f);
        } else {
            v = PixelUtils.clamp((int) (f * 255));
            int r = v << 16;
            int g = v << 8;
            int b = v;
            v = a | r | g | b;
        }

        return v;
    }

    @Override
    public String toString() {
        return "Texture/Wood...";
    }
}
