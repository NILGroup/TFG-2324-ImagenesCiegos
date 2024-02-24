package com.android.app.Hilo;

import com.android.app.server.FireFunctions;
import com.android.app.server.Identificador;
import com.android.app.imagen.Imagen;

import org.json.JSONException;

import java.io.IOException;

public class HiloTag extends Hilo{
    private Identificador identificador;

    public HiloTag(String imagen, FireFunctions firebase) {
        super(imagen,firebase);
    }

    public Identificador getIdentificador(){
        return identificador;
    }
    public void run(){
        firebase.callTags(imagen).addOnCompleteListener(task -> {
                identificador = task.getResult();
                    try {
                        if(!identificador.getTexto().equals("")){
                            firebase.translatedImage(identificador.getTexto()).addOnCompleteListener(task2 -> {
                                try {
                                    identificador.setTexto(task2.getResult().getTexto());
                                } catch (JSONException e) {
                                    throw new RuntimeException(e);
                                }
                            });
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
        );
    }
}
