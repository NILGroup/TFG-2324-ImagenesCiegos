package com.android.app.Hilo;

import com.android.app.server.FireFunctions;
import com.android.app.server.Identificador;
import com.android.app.imagen.Imagen;

import org.json.JSONException;

import java.io.IOException;

public class HiloTag extends Hilo{
    private Identificador identificador;
    private final Imagen photo;

    public HiloTag(String imagen, FireFunctions firebase,Imagen _photo) {
        super(imagen,firebase);
        photo=_photo;
    }

    public Identificador getIdentificador(){
        return identificador;
    }
    public void run(){
        firebase.callTags(imagen,photo).addOnCompleteListener(task -> {
                identificador = task.getResult();
                    try {
                        if(!identificador.getTexto().isEmpty()){
                            firebase.translatedImage(identificador.getTexto().toLowerCase()).addOnCompleteListener(task2 -> {
                                try {
                                    String s = task2.getResult().getTexto();
                                    identificador.setTexto(s);
                                } catch (JSONException | InterruptedException | IOException e) {
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
