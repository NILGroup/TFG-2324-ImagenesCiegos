package com.android.app.Hilo;

import com.android.app.server.Descripcion;
import com.android.app.server.FireFunctions;
import com.android.app.server.Traduccion;

import java.io.IOException;

public class HiloDescrip extends Hilo{
    private Traduccion traduccion;

    public HiloDescrip(String imagen, FireFunctions firebase) {
        super(imagen,firebase);
    }

    public Traduccion getTraduccion() {return traduccion;}

    public void run(){
        firebase.callImagen(imagen).addOnCompleteListener(task -> {
            Descripcion descripcion = task.getResult();
            try {
                firebase.translatedImage(descripcion.getTexto()).addOnCompleteListener(task2 -> traduccion =task2.getResult());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
