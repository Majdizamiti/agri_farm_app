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
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (points.size() > 0) {
            zonePath.reset();
            zonePath.moveTo(points.get(0).x, points.get(0).y);
            for (int i = 1; i < points.size(); i++) {
                zonePath.lineTo(points.get(i).x, points.get(i).y);
            }
            
            if (points.size() > 2) {
                zonePath.close();
                canvas.drawPath(zonePath, fillPaint);
            }
            
            canvas.drawPath(zonePath, linePaint);

            for (int i = 0; i < points.size(); i++) {
                PointF point = points.get(i);
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
                selectedPointIndex = findNearestPoint(x, y);
                if (selectedPointIndex == -1) {
                    points.add(new PointF(x, y));
                    selectedPointIndex = points.size() - 1;
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

    public void clearPoints() {
        points.clear();
        invalidate();
    }

    public List<PointF> getPoints() {
        return points;
    }
}
