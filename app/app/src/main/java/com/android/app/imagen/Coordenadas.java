package com.android.app.imagen;

import android.widget.ImageView;

public class Coordenadas {

    private final Imagen imagen;
    private final ImageView imageView;

    private final float ajusteHorizontal;
    private final float ajusteVertical;
    private final float proporcion;

    public Coordenadas(ImageView imageView, Imagen imagen) {
        this.imageView = imageView;
        this.imagen = imagen;

        float ivRatio = (float) imageView.getHeight() / imageView.getWidth();

        if (ivRatio >= imagen.getRatio()) {
            proporcion = imageView.getWidth() / imagen.getWidth();

            ajusteHorizontal = 0;
            ajusteVertical = (imageView.getHeight() - imagen.getHeight() * proporcion) / 2;
        } else {
            proporcion = imageView.getHeight() / imagen.getHeight();
            ajusteHorizontal = (imageView.getWidth() - imagen.getWidth() * proporcion) / 2;
            ajusteVertical = 0;
        }

        /*if(imagen.isGiro()){
            if(ivRatio >= 1/imagen.getRatio()){
                proporcion = imageView.getWidth()/imagen.getHeight();
                ajusteHorizontal = 0;
                ajusteVertical = (imageView.getHeight()-imagen.getWidth()*proporcion)/2;
            }
            else{
                proporcion = imageView.getHeight()/imagen.getWidth();
                ajusteHorizontal = (imageView.getWidth()-imagen.getHeight()*proporcion)/2;
                ajusteVertical = 0;
            }
        }else{
            if(ivRatio >= imagen.getRatio()){
                proporcion = imageView.getWidth()/imagen.getWidth();
                ajusteHorizontal = 0;
                ajusteVertical = (imageView.getHeight()-imagen.getHeight()*proporcion)/2;
            }
            else{
                proporcion = imageView.getHeight()/imagen.getHeight();
                ajusteHorizontal = (imageView.getWidth()-imagen.getWidth()*proporcion)/2;
                ajusteVertical = 0;
            }
        }*/
    }
    public int[] convTam(int x,int y, int tamX, int tamY){
        int[] ret = new int[4];
        /*if(imagen.isGiro()){
            ret[0] = (int) ( y*proporcion+ajusteHorizontal);
            ret[1] = (int) ( imageView.getWidth()-x*proporcion+ajusteVertical);
            ret[2] = (int) ( tamY*proporcion+ajusteHorizontal);
            ret[3] = (int) ( imageView.getWidth()-tamX*proporcion+ajusteVertical);
        }
        else{
            ret[1] = (int) (x*proporcion+ajusteVertical);
            ret[0] = (int) (y*proporcion+ajusteHorizontal);
            ret[3] = (int) (tamX*proporcion+ajusteVertical);
            ret[2] = (int) (tamY*proporcion+ajusteHorizontal);
        }*/


        ret[0] = (int) (x*proporcion+ajusteHorizontal);
        ret[1] = (int) (y*proporcion+ajusteVertical);
        ret[2] = (int) (tamX*proporcion+ajusteHorizontal);
        ret[3] = (int) (tamY*proporcion+ajusteVertical);
        return ret;
    }

    public int[] convCoord(float x, float y){
        int[] ret = new int[2];
        if(imagen.isGiro()){
            ret[0] = (int)  ((y / proporcion) - ajusteVertical);
            ret[1] = (int) ((x / proporcion) - ajusteHorizontal);
        }
        else {
            ret[0] = (int) ((x / proporcion) - ajusteHorizontal);
            ret[1] = (int) ((y / proporcion) - ajusteVertical);
        }
        return ret;
    }

    public boolean zonaVacia(float x, float y){
        return x<ajusteHorizontal || y<ajusteVertical || x>imageView.getWidth()-ajusteHorizontal || y>imageView.getHeight()-ajusteVertical;
    }
}
