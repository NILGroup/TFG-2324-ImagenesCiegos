package com.android.app.Hilo;

import java.io.IOException;

public class HiloDescrip extends Hilo{

    private String texto;
    private final String subImageBase64;

    public HiloDescrip(String subImageBase64) {
        super();
        this.subImageBase64 = subImageBase64;
    }

    public String getTexto() {return texto;}

    public void run(){
        firebase.callImagen(subImageBase64).addOnCompleteListener(task -> {
            try {
                firebase.translatedImage(task.getResult().getTexto()).addOnCompleteListener(task2 -> texto = task2.getResult().getTexto());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
