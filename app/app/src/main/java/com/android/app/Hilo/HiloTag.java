package com.android.app.Hilo;

import com.android.app.server.FireFunctions;
import com.android.app.server.Identificador;
import com.android.app.imagen.Imagen;

import org.json.JSONException;

import java.io.IOException;

public class HiloTag extends Hilo{
    private Identificador identificador;
    private Imagen photo;

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
                        if(!identificador.getTexto().equals("")){
                            firebase.translatedImage(identificador.getTexto()).addOnCompleteListener(task2 -> {
                                try {
                                    String s = task2.getResult().getTexto();
                                    String h = identificador.sisisi(s);
                                    identificador.setTexto(h);
                                } catch (JSONException e) {
                                    throw new RuntimeException(e);
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                } catch (InterruptedException e) {
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
