package com.android.app.imagen;

import android.graphics.Color;


public class ColorClassifier {
    public String classifyColor(String hexColor) {
        int color = hexToColor(hexColor);

        double[] lab = rgbToLab(color);
        double l = lab[0];
        double a = lab[1];
        double b = lab[2];

        if (l > 90) {
            return "Blanco";
        } else if (l < 10) {
            return "Negro";
        } else if (a > 20) {
            return "Rojo";
        } else if (a < -20) {
            return "Verde";
        } else if (b > 20) {
            return "Amarillo";
        } else if (b < -20) {
            return "Azul";
        } else if (l > 70 && Math.abs(a) < 10 && Math.abs(b) < 10) {
            return "Gris";
        } else {
            return "Color desconocido";
        }
    }

    private int hexToColor(String hexColor) {
        return Color.parseColor(hexColor);
    }

    private double[] rgbToLab(int color) {
        double r = Color.red(color) / 255.0;
        double g = Color.green(color) / 255.0;
        double b = Color.blue(color) / 255.0;

        // Conversion to XYZ space
        r = (r > 0.04045) ? Math.pow((r + 0.055) / 1.055, 2.4) : (r / 12.92);
        g = (g > 0.04045) ? Math.pow((g + 0.055) / 1.055, 2.4) : (g / 12.92);
        b = (b > 0.04045) ? Math.pow((b + 0.055) / 1.055, 2.4) : (b / 12.92);

        r *= 100.0;
        g *= 100.0;
        b *= 100.0;

        double x = r * 0.4124 + g * 0.3576 + b * 0.1805;
        double y = r * 0.2126 + g * 0.7152 + b * 0.0722;
        double z = r * 0.0193 + g * 0.1192 + b * 0.9505;

        // Conversion to LAB space
        x /= 95.047;
        y /= 100.000;
        z /= 108.883;

        x = (x > 0.008856) ? Math.pow(x, 1.0 / 3.0) : (7.787 * x + 16.0 / 116.0);
        y = (y > 0.008856) ? Math.pow(y, 1.0 / 3.0) : (7.787 * y + 16.0 / 116.0);
        z = (z > 0.008856) ? Math.pow(z, 1.0 / 3.0) : (7.787 * z + 16.0 / 116.0);

        double[] lab = new double[3];
        lab[0] = (116.0 * y) - 16.0;
        lab[1] = 500.0 * (x - y);
        lab[2] = 200.0 * (y - z);

        return lab;
    }
}