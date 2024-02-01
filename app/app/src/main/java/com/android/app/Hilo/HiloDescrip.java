package com.android.app.Hilo;

import android.speech.tts.TextToSpeech;

import com.android.app.Imagen;
import com.android.app.Traduccion;

import java.io.IOException;

public class HiloDescrip extends Hilo{

    private Traduccion descripFinal;

    public HiloDescrip(Imagen imagen) {
        super(imagen);
    }

    public Traduccion getDescripFinal() {return descripFinal;}
    public void run(){
        firebase.callImagen(imagen.getBase64()).addOnCompleteListener(task -> {
            try {
                firebase.translatedImage(task.getResult().getTexto()).addOnCompleteListener(task2 -> {
                    descripFinal = task2.getResult();
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
