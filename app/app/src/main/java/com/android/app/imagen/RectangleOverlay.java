package com.android.app.imagen;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class RectangleOverlay extends View {

    private Paint paint;
    private List<int[]> coordinatesList;

    public RectangleOverlay(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5); // Grosor del borde del rectángulo
        coordinatesList = new ArrayList<>();
    }

    public void addCoordinates(int[] coordinates) {
        coordinatesList.add(coordinates);
        invalidate(); // Vuelve a dibujar la vista
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        for (int[] coordinates : coordinatesList) {
            if (coordinates.length == 4) {
                // Dibuja el rectángulo utilizando las coordenadas
                canvas.drawRect(coordinates[0], coordinates[1], coordinates[2], coordinates[3], paint);
            }
        }
    }
}