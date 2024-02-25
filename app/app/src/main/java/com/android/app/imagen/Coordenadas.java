package com.android.app.imagen;

import android.widget.ImageView;

public class Coordenadas {

    private final Imagen imagen;

    private final ImageView imageView;

    private final float ajusteY;
    private final float ajusteX;
    private final float proporcion;

    public Coordenadas(ImageView imageView, Imagen imagen) {
        this.imageView = imageView;
        this.imagen = imagen;
        float ivRatio = (float) imageView.getHeight() / imageView.getWidth();

        if(imagen.isGiro()){
            if (ivRatio >= imagen.getRatio()) {
                proporcion = imageView.getWidth() / imagen.getHeight();
                ajusteY = 0;
                ajusteX = (imageView.getHeight() - imagen.getWidth() * proporcion) / 2;
            } else {
                proporcion = imageView.getHeight() / imagen.getWidth();
                ajusteY = (imageView.getWidth() - imagen.getHeight() * proporcion) / 2;
                ajusteX = 0;
            }
        }else{
            if (ivRatio >= imagen.getRatio()) {
                proporcion = imageView.getWidth() / imagen.getWidth();
                ajusteY = 0;
                ajusteX = (imageView.getHeight() - imagen.getHeight() * proporcion) / 2;
            } else {
                proporcion = imageView.getHeight() / imagen.getHeight();
                ajusteY = (imageView.getWidth() - imagen.getWidth() * proporcion) / 2;
                ajusteX = 0;
            }
        }
    }
    public int[] convTam(int x,int y, int tamX, int tamY){
        int[] ret = new int[4];
        if(imagen.isGiro()){
            ret[0] = (int) (imageView.getWidth()-(y*proporcion)-ajusteY-33);
            ret[1] = (int) (x*proporcion+ajusteX);
            ret[2] = (int) (imageView.getWidth()-(tamY*proporcion)-ajusteY-33);
            ret[3] = (int) (tamX*proporcion+ajusteX);
        }
        else{
            ret[0] = (int) (x*proporcion+ajusteY-33);
            ret[1] = (int) (y*proporcion+ajusteX);
            ret[2] = (int) (tamX*proporcion+ajusteY-33);
            ret[3] = (int) (tamY*proporcion+ajusteX);
        }
        return ret;
    }

    public boolean zonaVacia(float x, float y){
        return x<ajusteY || y<ajusteX || x>imageView.getWidth()-ajusteY || y>imageView.getHeight()-ajusteX;
    }
}
