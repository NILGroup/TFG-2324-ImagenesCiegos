package com.android.app.Hilo;

import com.android.app.Imagen;

import java.io.IOException;

public class HiloDescrip extends Hilo{

    private String texto;

    public HiloDescrip(Imagen imagen) {
        super(imagen);
    }

    public String getTexto() {return texto;}
    public void run(){
        firebase.callImagen(imagen.getBase64()).addOnCompleteListener(task -> {
            try {
                firebase.translatedImage(task.getResult().getTexto()).addOnCompleteListener(task2 -> texto = task2.getResult().getTexto());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
