package org.outlander.views;

import org.outlander.R;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.util.AttributeSet;
import android.view.View;

public class ArtificialHorizontView extends View {

    int[]            borderGradientColors;
    float[]          borderGradientPositions;

    int[]            glassGradientColors;
    float[]          glassGradientPositions;

    int              skyHorizonColorFrom;
    int              skyHorizonColorTo;
    int              groundHorizonColorFrom;
    int              groundHorizonColorTo;

    private Paint    markerPaint;
    private Paint    textPaint;
    private Paint    circlePaint;
    private int      textHeight;
    private String[] compassDirection;

    private float    bearing;
    float            pitch = 0;
    float            roll  = 0;

    public void setBearing(final float _bearing) {
        bearing = _bearing;
    }

    public float getBearing() {
        return bearing;
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(final float pitch) {
        this.pitch = pitch;
    }

    public float getRoll() {
        return roll;
    }

    public void setRoll(final float roll) {
        this.roll = roll;
    }

    public ArtificialHorizontView(final Context context) {
        super(context);
        initCompassView();
    }

    public ArtificialHorizontView(final Context context,
            final AttributeSet attrs) {
        super(context, attrs);
        initCompassView();
    }

    public ArtificialHorizontView(final Context context,
            final AttributeSet ats, final int defaultStyle) {
        super(context, ats, defaultStyle);
        initCompassView();
    }

    protected void initCompassView() {
        setFocusable(true);
        // Get external resources
        final Resources r = getResources();

        compassDirection = r.getStringArray(R.array.compassValues);

        circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circlePaint.setColor(R.color.background_color);
        circlePaint.setStrokeWidth(1);
        circlePaint.setStyle(Paint.Style.STROKE);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(r.getColor(R.color.text_color));
        textPaint.setFakeBoldText(true);
        textPaint.setSubpixelText(true);
        textPaint.setTextAlign(Align.LEFT);

        textHeight = (int) textPaint.measureText("yY");

        markerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        markerPaint.setColor(r.getColor(R.color.marker_color));
        markerPaint.setAlpha(200);
        markerPaint.setStrokeWidth(1);
        markerPaint.setStyle(Paint.Style.STROKE);
        markerPaint.setShadowLayer(2, 1, 1, r.getColor(R.color.shadow_color));

        borderGradientColors = new int[4];
        borderGradientPositions = new float[4];

        borderGradientColors[3] = r.getColor(R.color.outer_border);
        borderGradientColors[2] = r.getColor(R.color.inner_border_one);
        borderGradientColors[1] = r.getColor(R.color.inner_border_two);
        borderGradientColors[0] = r.getColor(R.color.inner_border);
        borderGradientPositions[3] = 0.0f;
        borderGradientPositions[2] = 1 - 0.03f;
        borderGradientPositions[1] = 1 - 0.06f;
        borderGradientPositions[0] = 1.0f;

        glassGradientColors = new int[5];
        glassGradientPositions = new float[5];

        final int glassColor = 245;
        glassGradientColors[4] = Color.argb(65, glassColor, glassColor,
                glassColor);
        glassGradientColors[3] = Color.argb(100, glassColor, glassColor,
                glassColor);
        glassGradientColors[2] = Color.argb(50, glassColor, glassColor,
                glassColor);
        glassGradientColors[1] = Color.argb(0, glassColor, glassColor,
                glassColor);
        glassGradientColors[0] = Color.argb(0, glassColor, glassColor,
                glassColor);
        glassGradientPositions[4] = 1 - 0.0f;
        glassGradientPositions[3] = 1 - 0.06f;
        glassGradientPositions[2] = 1 - 0.10f;
        glassGradientPositions[1] = 1 - 0.20f;
        glassGradientPositions[0] = 1 - 1.0f;

        skyHorizonColorFrom = r.getColor(R.color.horizon_sky_from);
        skyHorizonColorTo = r.getColor(R.color.horizon_sky_to);

        groundHorizonColorFrom = r.getColor(R.color.horizon_ground_from);
        groundHorizonColorTo = r.getColor(R.color.horizon_ground_to);
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec,
            final int heightMeasureSpec) {
        // The compass is a circle that fills as much space as possible.
        // Set the measured dimensions by figuring out the shortest boundary,
        // height or width.
        final int measuredWidth = measure(widthMeasureSpec);
        final int measuredHeight = measure(heightMeasureSpec);

        final int d = Math.min(measuredWidth, measuredHeight);

        setMeasuredDimension(d, d);
    }

    private int measure(final int measureSpec) {
        final int result = 200;

        //
        //
        // // Decode the measurement specifications.
        // final int specMode = MeasureSpec.getMode(measureSpec);
        // final int specSize = MeasureSpec.getSize(measureSpec);
        //
        // if (specMode == MeasureSpec.UNSPECIFIED) {
        // // Return a default size of 200 if no bounds are specified.
        // result = 200;
        // } else {
        // // As you want to fill the available space
        // // always return the full available bounds.
        // result = specSize;
        // }
        return result;
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        final float ringWidth = textHeight + 4;
        final int height = getMeasuredHeight();
        final int width = getMeasuredWidth();

        final int px = width / 2;
        final int py = height / 2;
        final Point center = new Point(px, py);

        final int radius = Math.min(px, py) - 2;

        final RectF boundingBox = new RectF(center.x - radius, center.y
                - radius, center.x + radius, center.y + radius);

        final RectF innerBoundingBox = new RectF((center.x - radius)
                + ringWidth, (center.y - radius) + ringWidth,
                (center.x + radius) - ringWidth, (center.y + radius)
                        - ringWidth);

        final float innerRadius = innerBoundingBox.height() / 2;
        final RadialGradient borderGradient = new RadialGradient(px, py,
                radius, borderGradientColors, borderGradientPositions,
                TileMode.CLAMP);

        final Paint pgb = new Paint();
        pgb.setShader(borderGradient);

        final Path outerRingPath = new Path();
        outerRingPath.addOval(boundingBox, Direction.CW);

        canvas.drawPath(outerRingPath, pgb);
        final LinearGradient skyShader = new LinearGradient(center.x,
                innerBoundingBox.top, center.x, innerBoundingBox.bottom,
                skyHorizonColorFrom, skyHorizonColorTo, TileMode.CLAMP);

        final Paint skyPaint = new Paint();
        skyPaint.setShader(skyShader);

        final LinearGradient groundShader = new LinearGradient(center.x,
                innerBoundingBox.top, center.x, innerBoundingBox.bottom,
                groundHorizonColorFrom, groundHorizonColorTo, TileMode.CLAMP);

        final Paint groundPaint = new Paint();
        groundPaint.setShader(groundShader);
        float tiltDegree = pitch;
        while ((tiltDegree > 90) || (tiltDegree < -90)) {
            if (tiltDegree > 90) {
                tiltDegree = -90 + (tiltDegree - 90);
            }
            if (tiltDegree < -90) {
                tiltDegree = 90 - (tiltDegree + 90);
            }
        }

        float rollDegree = roll;
        while ((rollDegree > 180) || (rollDegree < -180)) {
            if (rollDegree > 180) {
                rollDegree = -180 + (rollDegree - 180);
            }
            if (rollDegree < -180) {
                rollDegree = 180 - (rollDegree + 180);
            }
        }
        final Path skyPath = new Path();
        skyPath.addArc(innerBoundingBox, -tiltDegree, (180 + (2 * tiltDegree)));
        canvas.rotate(-rollDegree, px, py);
        canvas.drawOval(innerBoundingBox, groundPaint);
        canvas.drawPath(skyPath, skyPaint);
        canvas.drawPath(skyPath, markerPaint);
        final int markWidth = radius / 3;
        final int startX = center.x - markWidth;
        final int endX = center.x + markWidth;

        final double h = innerRadius
                * Math.cos(Math.toRadians(90 - tiltDegree));
        final double justTiltY = center.y - h;

        final float pxPerDegree = (innerBoundingBox.height() / 2) / 45f;
        for (int i = 90; i >= -90; i -= 10) {
            final double ypos = justTiltY + (i * pxPerDegree);

            // Only display the scale within the inner face.
            if ((ypos < (innerBoundingBox.top + textHeight))
                    || (ypos > (innerBoundingBox.bottom - textHeight))) {
                continue;
            }

            // Draw a line and the tilt angle for each scale increment.
            canvas.drawLine(startX, (float) ypos, endX, (float) ypos,
                    markerPaint);
            final int displayPos = (int) (tiltDegree - i);
            final String displayString = String.valueOf(displayPos);
            final float stringSizeWidth = textPaint.measureText(displayString);
            canvas.drawText(displayString,
                    (int) (center.x - (stringSizeWidth / 2)), (int) (ypos) + 1,
                    textPaint);
        }
        markerPaint.setStrokeWidth(2);
        canvas.drawLine(center.x - (radius / 2), (float) justTiltY, center.x
                + (radius / 2), (float) justTiltY, markerPaint);
        markerPaint.setStrokeWidth(1);

        // Draw the arrow
        final Path rollArrow = new Path();
        rollArrow.moveTo(center.x - 3, (int) innerBoundingBox.top + 14);
        rollArrow.lineTo(center.x, (int) innerBoundingBox.top + 10);
        rollArrow.moveTo(center.x + 3, innerBoundingBox.top + 14);
        rollArrow.lineTo(center.x, innerBoundingBox.top + 10);
        canvas.drawPath(rollArrow, markerPaint);
        // Draw the string
        final String rollText = String.valueOf(rollDegree);
        final double rollTextWidth = textPaint.measureText(rollText);
        canvas.drawText(rollText, (float) (center.x - (rollTextWidth / 2)),
                innerBoundingBox.top + textHeight + 2, textPaint);
        canvas.restore();

        canvas.save();
        canvas.rotate(180, center.x, center.y);
        for (int i = -180; i < 180; i += 10) {
            // Show a numeric value every 30 degrees
            if ((i % 30) == 0) {
                final String rollString = String.valueOf(i * -1);
                final float rollStringWidth = textPaint.measureText(rollString);
                final PointF rollStringCenter = new PointF(center.x
                        - (rollStringWidth / 2), innerBoundingBox.top + 1
                        + textHeight);
                canvas.drawText(rollString, rollStringCenter.x,
                        rollStringCenter.y, textPaint);
            }
            // Otherwise draw a marker line
            else {
                canvas.drawLine(center.x, (int) innerBoundingBox.top, center.x,
                        (int) innerBoundingBox.top + 5, markerPaint);
            }

            canvas.rotate(10, center.x, center.y);
        }
        canvas.restore();
        canvas.save();
        canvas.rotate(-1 * (bearing), px, py);

        final double increment = 22.5;

        for (double i = 0; i < 360; i += increment) {
            // CompassDirection cd = CompassDirection.values()[(int) (i /
            // 22.5)];
            // String headString = cd.toString();

            final String headString = compassDirection[(int) (i / 22.5)];
            final float headStringWidth = textPaint.measureText(headString);
            final PointF headStringCenter = new PointF(center.x
                    - (headStringWidth / 2), boundingBox.top + 1 + textHeight);

            if ((i % increment) == 0) {
                canvas.drawText(headString, headStringCenter.x,
                        headStringCenter.y, textPaint);
            } else {
                canvas.drawLine(center.x, (int) boundingBox.top, center.x,
                        (int) boundingBox.top + 3, markerPaint);
            }

            canvas.rotate((int) increment, center.x, center.y);
        }
        canvas.restore();
        final RadialGradient glassShader = new RadialGradient(px, py,
                (int) innerRadius, glassGradientColors, glassGradientPositions,
                TileMode.CLAMP);
        final Paint glassPaint = new Paint();
        glassPaint.setShader(glassShader);

        canvas.drawOval(innerBoundingBox, glassPaint);
        // Draw the outer ring
        canvas.drawOval(boundingBox, circlePaint);

        // Draw the inner ring
        circlePaint.setStrokeWidth(2);
        canvas.drawOval(innerBoundingBox, circlePaint);

        canvas.restore();
    }
}