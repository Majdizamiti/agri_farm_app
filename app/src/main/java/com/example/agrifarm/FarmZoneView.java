package com.example.agrifarm;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class FarmZoneView extends View {

    private Paint pointPaint;
    private Paint linePaint;
    private Paint fillPaint;
    private List<PointF> points = new ArrayList<>();
    private Path zonePath = new Path();

    private int selectedPointIndex = -1;
    private static final float TOUCH_TOLERANCE = 40f;

    public FarmZoneView(Context context) {
        super(context);
        init();
    }

    public FarmZoneView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private Paint highlightPaint;

    private void init() {
        pointPaint = new Paint();
        pointPaint.setColor(Color.parseColor("#E91E63")); // Pinkish red
        pointPaint.setStrokeWidth(12f);
        pointPaint.setStyle(Paint.Style.FILL);
        pointPaint.setAntiAlias(true);

        linePaint = new Paint();
        linePaint.setColor(Color.parseColor("#3F51B5")); // Indigo
        linePaint.setStrokeWidth(6f);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeJoin(Paint.Join.ROUND);
        linePaint.setStrokeCap(Paint.Cap.ROUND);
        linePaint.setAntiAlias(true);

        fillPaint = new Paint();
        fillPaint.setColor(Color.argb(80, 63, 81, 181)); // Semi-transparent Indigo
        fillPaint.setStyle(Paint.Style.FILL);
        fillPaint.setAntiAlias(true);

        highlightPaint = new Paint();
        highlightPaint.setColor(Color.YELLOW);
        highlightPaint.setStrokeWidth(20f);
        highlightPaint.setStyle(Paint.Style.FILL);
        highlightPaint.setAntiAlias(true);
    }

    private boolean isClosed = false;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (points.size() > 0) {
            zonePath.reset();
            zonePath.moveTo(points.get(0).x, points.get(0).y);
            for (int i = 1; i < points.size(); i++) {
                zonePath.lineTo(points.get(i).x, points.get(i).y);
            }

            if (isClosed) {
                zonePath.close();
                canvas.drawPath(zonePath, fillPaint);
            }

            canvas.drawPath(zonePath, linePaint);

            for (int i = 0; i < points.size(); i++) {
                PointF point = points.get(i);
                if (i == 0 && !isClosed && points.size() >= 3) {
                    // Highlight first point when it can be closed
                    canvas.drawCircle(point.x, point.y, 18, highlightPaint);
                }
                canvas.drawCircle(point.x, point.y, i == selectedPointIndex ? 20 : 12, pointPaint);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (isClosed) {
                    // Optional: allow moving points even when closed, or prevent adding new ones
                    selectedPointIndex = findNearestPoint(x, y);
                } else {
                    int nearest = findNearestPoint(x, y);
                    if (nearest == 0 && points.size() >= 3) {
                        // Close the polygon if clicking the first point
                        isClosed = true;
                        selectedPointIndex = -1;
                    } else if (nearest == -1) {
                        points.add(new PointF(x, y));
                        selectedPointIndex = points.size() - 1;
                    } else {
                        selectedPointIndex = nearest;
                    }
                }
                invalidate();
                return true;

            case MotionEvent.ACTION_MOVE:
                if (selectedPointIndex != -1) {
                    points.get(selectedPointIndex).set(x, y);
                    invalidate();
                }
                return true;

            case MotionEvent.ACTION_UP:
                selectedPointIndex = -1;
                performClick();
                invalidate();
                return true;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    private int findNearestPoint(float x, float y) {
        for (int i = 0; i < points.size(); i++) {
            PointF p = points.get(i);
            float distance = (float) Math.sqrt(Math.pow(p.x - x, 2) + Math.pow(p.y - y, 2));
            if (distance < TOUCH_TOLERANCE) {
                return i;
            }
        }
        return -1;
    }

    public void addPoint(float x, float y) {
        if (!isClosed) {
            points.add(new PointF(x, y));
            invalidate();
        }
    }

    public void clearPoints() {
        points.clear();
        isClosed = false;
        invalidate();
    }

    public List<PointF> getPoints() {
        return points;
    }
}
