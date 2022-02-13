/*
 * $Id: AbstractAreaEffect.java 3471 2009-08-27 13:10:39Z kleopatra $
 *
 * Copyright 2006 Sun Microsystems, Inc., 4150 Network Circle,
 * Santa Clara, California 95054, U.S.A. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */


package org.jdesktop.swingx.painter.effects;

import com.jhlabs.image.ImageMath;
import pixelitor.colors.Colors;
import pixelitor.filters.gui.UserPreset;

import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Objects;

/**
 * The abstract base class for path effects. It takes care
 * of soft clipping and interpolating brush sizes and colors. Subclasses
 * can change these values to provide prefab effect behavior, like
 * dropshadows and glows.
 *
 * @author joshy
 */
public class AbstractAreaEffect implements AreaEffect {
    private static final boolean debug = false;

    // for compatibility with pixelitor versions before 4.2.0
    private static final long serialVersionUID = -9104855683480422662L;

    /**
     * Creates a new instance of AreaEffect
     */
    public AbstractAreaEffect() {
        setBrushColor(Color.BLACK);
        setBrushSteps(10);
        setEffectWidth(8);
        setRenderInsideShape(false);
        setOffset(new Point(4, 4));
        setShouldFillShape(true);
        setShapeMasked(true);
    }

    @Override
    public void apply(Graphics2D g, Shape clipShape, int width, int height) {
        // opacity support added by lbalazscs
        Composite savedComposite = g.getComposite();
        if (opacity < 1.0f) {
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
        }

        // create a rect to hold the bounds
        Rectangle2D clipShapeBounds = clipShape.getBounds2D();

        if (clipShapeBounds.isEmpty()) {
            // check added by lbalazscs
            return;
        }

        width = (int) (clipShapeBounds.getWidth() + clipShapeBounds.getX());
        height = (int) (clipShapeBounds.getHeight() + clipShapeBounds.getY());
        Rectangle effectBounds = new Rectangle(0, 0,
            (int) (width + getEffectWidth() * 2 + 1),
            (int) (height + getEffectWidth() * 2 + 1));

        if (effectBounds.isEmpty()) {
            // check added by lbalazscs
            // this can be empty even if the clip shape bounds is not
            // when the clip shape starts at large negative coordinates
            return;
        }

        // Apply the border glow effect
        if (isShapeMasked()) {
            shapedMaskBorderGlowEffect(g, clipShape, width, height, effectBounds);
        } else {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            paintBorderGlow(g, clipShape, width, height);
        }

        //g.setColor(Color.MAGENTA);
        //g.draw(clipShape.getBounds2D());
        //g.drawRect(0,0,width,height);

        g.setComposite(savedComposite);
    }

	private void shapedMaskBorderGlowEffect(Graphics2D g, Shape clipShape, int width, int height,
			Rectangle effectBounds) {
		BufferedImage clipImage = getClipImage(effectBounds);
		Graphics2D g2 = clipImage.createGraphics();

		// lbalazscs: moved here from getClipImage
		// in order to avoid two createGraphics calls
		g2.clearRect(0, 0, clipImage.getWidth(), clipImage.getHeight());

		try {
		    // clear the buffer
		    g2.setPaint(Color.BLACK);
		    g2.setComposite(AlphaComposite.Clear);
		    g2.fillRect(0, 0, effectBounds.width, effectBounds.height);

		    if (debug) {
		        g2.setPaint(Color.WHITE);
		        g2.setComposite(AlphaComposite.SrcOver);
		        g2.drawRect(0, 0, effectBounds.width - 1,
		            effectBounds.height - 1);
		    }

		    // turn on smoothing
		    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
		        RenderingHints.VALUE_ANTIALIAS_ON);
		    g2.translate(getEffectWidth() - getOffset().getX(),
		        getEffectWidth() - getOffset().getY());
		    paintBorderGlow(g2, clipShape, width, height);

		    // clip out the parts we don't want
		    g2.setComposite(AlphaComposite.Clear);
		    g2.setColor(Color.WHITE);
		    if (isRenderInsideShape()) {
		        // clip the outside
		        Area area = new Area(effectBounds);
		        area.subtract(new Area(clipShape));
		        g2.fill(area);
		    } else {
		        // clip the inside
		        g2.fill(clipShape);
		    }
		} finally {
		    // draw the final image
		    g2.dispose();
		}

		int drawX = (int) (-getEffectWidth() + getOffset().getX());
		int drawY = (int) (-getEffectWidth() + getOffset().getY());
		g.drawImage(clipImage, drawX, drawY, null);
	}

    transient BufferedImage _clipImage = null;

    protected BufferedImage getClipImage(final Rectangle effectBounds) {
        // set up a temp buffer
        if (_clipImage == null ||
            _clipImage.getWidth() != effectBounds.width ||
            _clipImage.getHeight() != effectBounds.height) {
            _clipImage = new BufferedImage(
                effectBounds.width,
                effectBounds.height, BufferedImage.TYPE_INT_ARGB);
        }
        return _clipImage;
    }


    /*
    private BufferedImage createClipImage(Shape s, Graphics2D g, int width, int height) {
        // Create a translucent intermediate image in which we can perform
        // the soft clipping

        GraphicsConfiguration gc = g.getDeviceConfiguration();
        BufferedImage img = gc.createCompatibleImage(width, height, Transparency.TRANSLUCENT);
        Graphics2D g2 = img.createGraphics();

        // Clear the image so all pixels have zero alpha
        g2.setComposite(AlphaComposite.Clear);
        g2.fillRect(0, 0, width, height);

        // Render our clip shape into the image.  Note that we enable
        // antialiasing to achieve the soft clipping effect.  Try
        // commenting out the line that enables antialiasing, and
        // you will see that you end up with the usual hard clipping.
        g2.setComposite(AlphaComposite.Src);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Color.WHITE);
        g2.fill(s);
        g2.dispose();

        return img;
    }*/


    /* draws the actual shaded border to the specified graphics
     */

    /**
     * Paints the border glow
     *
     * @param g2
     * @param clipShape
     * @param width
     * @param height
     */
    protected void paintBorderGlow(Graphics2D g2,
                                   Shape clipShape, int width, int height) {

        int steps = getBrushSteps();
        boolean inside = isRenderInsideShape();

        g2.setPaint(getBrushColor());
        g2.translate(offset.getX(), offset.getY());

        if (isShouldFillShape()) {
            // set the inside/outside mode
            if (inside) {
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 1f));
                Area a1 = new Area(new Rectangle(
                    (int) -offset.getX() - 20,
                    (int) -offset.getY() - 20,
                    width + 40, height + 40));
                Area a2 = new Area(clipShape);
                a1.subtract(a2);
                g2.fill(a1);
            } else {
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.DST_OVER, 1f));
                g2.fill(clipShape);
            }
        }

        // set the inside/outside mode
        /*
        if(inside) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, brushAlpha));
        } else {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.DST_OVER, brushAlpha));
        }*/
        float brushAlpha = 1.0f / steps;
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.DST_OVER, brushAlpha));

        // draw the effect
        for (float i = 0; i < steps; i = i + 1f) {
            float brushWidth = (float) (i * effectWidthDouble / steps);
            g2.setStroke(new BasicStroke(brushWidth,
                BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.draw(clipShape);
        }
        g2.translate(-offset.getX(), -offset.getY());

    }

    /**
     * Holds value of property brushColor.
     */
    private Color brushColor;

    /**
     * Utility field used by bound properties.
     */
    private java.beans.PropertyChangeSupport propertyChangeSupport = new java.beans.PropertyChangeSupport(this);

    /**
     * Adds a PropertyChangeListener to the listener list.
     *
     * @param l The listener to add.
     */
    public void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        propertyChangeSupport.addPropertyChangeListener(l);
    }

    /**
     * Removes a PropertyChangeListener from the listener list.
     *
     * @param l The listener to remove.
     */
    public void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        propertyChangeSupport.removePropertyChangeListener(l);
    }

    /**
     * Getter for property brushColor.
     *
     * @return Value of property brushColor.
     */
    public Color getBrushColor() {
        return this.brushColor;
    }

    /**
     * Setter for property brushColor.
     *
     * @param brushColor New value of property brushColor.
     */
    public void setBrushColor(Color brushColor) {
        Color oldBrushColor = this.brushColor;
        this.brushColor = brushColor;
        propertyChangeSupport.firePropertyChange("brushColor", oldBrushColor, brushColor);
    }

    /**
     * Holds value of property brushSteps.
     */
    private int brushSteps;

    /**
     * Getter for property brushSteps.
     *
     * @return Value of property brushSteps.
     */
    public int getBrushSteps() {
        return this.brushSteps;
    }

    /**
     * Setter for property brushSteps.
     *
     * @param brushSteps New value of property brushSteps.
     */
    public void setBrushSteps(int brushSteps) {
        int oldBrushSteps = this.brushSteps;
        this.brushSteps = brushSteps;
        propertyChangeSupport.firePropertyChange("brushSteps",
            Integer.valueOf(oldBrushSteps),
            Integer.valueOf(brushSteps));
    }

    public void setAutoBrushSteps() {
        setBrushSteps((int) (1 + effectWidthDouble / 3));
    }

    /**
     * Holds value of property effectWidth.
     */
    protected double effectWidthDouble;

    /**
     * This is only for serialization compatibility with Pixelitor 4.2.3:
     * previously effectWidth was an int, now it is a double
     */
    protected int effectWidth;

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        if (effectWidthDouble < 0.01) { // 4.2.3 file => effectWidthDouble is uninitialized
            effectWidthDouble = effectWidth;
        }
    }

    /**
     * Getter for property effectWidth.
     *
     * @return Value of property effectWidth.
     */
    public double getEffectWidth() {
        return this.effectWidthDouble;
    }

    public int getEffectWidthInt() {
        return (int) this.effectWidthDouble;
    }

    /**
     * Setter for property effectWidth.
     *
     * @param effectWidth New value of property effectWidth.
     */
    public void setEffectWidth(double effectWidth) {
        double oldEffectWidth = this.effectWidthDouble;
        this.effectWidthDouble = effectWidth;
        propertyChangeSupport.firePropertyChange("effectWidth",
            oldEffectWidth,
            effectWidth);
    }

    /**
     * Holds value of property renderInsideShape.
     */
    private boolean renderInsideShape;

    /**
     * Getter for property renderInsideShape.
     *
     * @return Value of property renderInsideShape.
     */
    public boolean isRenderInsideShape() {
        return this.renderInsideShape;
    }

    /**
     * Setter for property renderInsideShape.
     *
     * @param renderInsideShape New value of property renderInsideShape.
     */
    public void setRenderInsideShape(boolean renderInsideShape) {
        boolean oldRenderInsideShape = this.renderInsideShape;
        this.renderInsideShape = renderInsideShape;
        propertyChangeSupport.firePropertyChange("renderInsideShape",
            Boolean.valueOf(oldRenderInsideShape),
            Boolean.valueOf(renderInsideShape));
    }

    /**
     * Holds value of property offset.
     */
    private Point2D offset;

    /**
     * Getter for property offset.
     *
     * @return Value of property offset.
     */
    public Point2D getOffset() {
        return this.offset;
    }

    /**
     * Setter for property offset.
     *
     * @param offset New value of property offset.
     */
    public void setOffset(Point2D offset) {
        Point2D oldOffset = this.offset;
        this.offset = offset;
        propertyChangeSupport.firePropertyChange("offset", oldOffset, offset);
    }

    /**
     * Holds value of property shouldFillShape.
     */
    private boolean shouldFillShape;

    /**
     * Getter for property shouldFillShape.
     *
     * @return Value of property shouldFillShape.
     */
    public boolean isShouldFillShape() {
        return this.shouldFillShape;
    }

    /**
     * Setter for property shouldFillShape.
     *
     * @param shouldFillShape New value of property shouldFillShape.
     */
    public void setShouldFillShape(boolean shouldFillShape) {
        boolean oldShouldFillShape = this.shouldFillShape;
        this.shouldFillShape = shouldFillShape;
        propertyChangeSupport.firePropertyChange("shouldFillShape",
            Boolean.valueOf(oldShouldFillShape),
            Boolean.valueOf(shouldFillShape));
    }

    /**
     * Holds value of property shapeMasked.
     */
    private boolean shapeMasked;

    /**
     * Getter for property shapeMasked.
     *
     * @return Value of property shapeMasked.
     */
    public boolean isShapeMasked() {
        return this.shapeMasked;
    }

    /**
     * Setter for property shapeMasked.
     *
     * @param shapeMasked New value of property shapeMasked.
     */
    public void setShapeMasked(boolean shapeMasked) {
        boolean oldShapeMasked = this.shapeMasked;
        this.shapeMasked = shapeMasked;
        propertyChangeSupport.firePropertyChange("shapeMasked",
            Boolean.valueOf(oldShapeMasked),
            Boolean.valueOf(shapeMasked));
    }

    protected float opacity = 1.0f;

    // opacity support added by lbalazscs
    protected void setOpacity(float opacity) {
        this.opacity = opacity;
    }

    public float getOpacity() {
        return opacity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AbstractAreaEffect that = (AbstractAreaEffect) o;
        return brushSteps == that.brushSteps &&
            effectWidthDouble == that.effectWidthDouble &&
            renderInsideShape == that.renderInsideShape &&
            shouldFillShape == that.shouldFillShape &&
            shapeMasked == that.shapeMasked &&
            Float.compare(that.opacity, opacity) == 0 &&
            brushColor.equals(that.brushColor) &&
            Objects.equals(offset, that.offset);
    }

    @Override
    public int hashCode() {
        return Objects.hash(brushColor, brushSteps,
            effectWidthDouble, renderInsideShape, offset,
            shouldFillShape, shapeMasked, opacity);
    }

    public Color interpolateBrushColor(Color endColor, float progress) {
        return Colors.rgbInterpolate(brushColor, endColor, progress);
    }

    public float interpolateOpacity(float endOpacity, float progress) {
        return ImageMath.lerp(progress, opacity, endOpacity);
    }

    public double interpolateEffectWidth(double endWidth, double progress) {
        return ImageMath.lerp(progress, effectWidthDouble, endWidth);
    }

    public void saveStateTo(UserPreset preset, String keyPrefix, boolean includeOffset) {
        preset.putColor(keyPrefix + "Color", this.brushColor);
        preset.putFloat(keyPrefix + "Opacity", opacity);
        preset.putFloat(keyPrefix + "Width", (float) this.effectWidthDouble);

        if (includeOffset) {
            preset.putFloat(keyPrefix + "OffsetX", (float) offset.getX());
            preset.putFloat(keyPrefix + "OffsetY", (float) offset.getY());
        }
    }

    public void loadStateFrom(UserPreset preset, String keyPrefix, boolean checkOffset) {
        setBrushColor(preset.getColor(keyPrefix + "Color"));
        setOpacity(preset.getFloat(keyPrefix + "Opacity"));
        setEffectWidth(preset.getFloat(keyPrefix + "Width"));
        setAutoBrushSteps();

        if (checkOffset) {
            double offsetX = preset.getFloat(keyPrefix + "OffsetX");
            double offsetY = preset.getFloat(keyPrefix + "OffsetY");
            setOffset(new Point2D.Double(offsetX, offsetY));
        }
    }
}
