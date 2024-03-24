package com.android.app.imagen;

public class ColorClassifier {
    public String classifyColor(String hexColor) {
        int r = Integer.parseInt(hexColor.substring(1, 3), 16);
        int g = Integer.parseInt(hexColor.substring(3, 5), 16);
        int b = Integer.parseInt(hexColor.substring(5, 7), 16);

        if (r > g && r > b) {
            return "Rojo";
        } else if (g > r && g > b) {
            return "Verde";
        } else if (b > r && b > g) {
            return "Azul";
        } else if (r > b && g > b) {
            return "Amarillo";
        } else if (r > g && b > g) {
            return "Magenta";
        } else if (g > r && b > r) {
            return "Cian";
        } else if (r == g && g == b) {
            return "Gris";
        } else if (r == 255 && g == 255 && b == 255) {
            return "Blanco";
        } else if (r == 0 && g == 0 && b == 0) {
            return "Negro";
        } else {
            return "Desconocido";
        }
    }
}
