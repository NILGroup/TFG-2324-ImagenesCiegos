package com.android.app.Hilo;

import com.android.app.Identificador;
import com.android.app.Imagen;

import org.json.JSONException;

import java.io.IOException;

public class HiloTag extends Hilo{
    Identificador identificador;

    public HiloTag(Imagen imagen) {
        super(imagen);
        this.imagen = imagen;
    }

    public Identificador getIdentificador(){
        return identificador;
    }
    public void run(){
        firebase.callTags(imagen.getBase64()).addOnCompleteListener(task -> {
                identificador = task.getResult();
                    try {
                        firebase.translatedImage(identificador.getLabels()).addOnCompleteListener(task2 -> {
                            try {
                                identificador.changeLabels(task2.getResult().getTexto());
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                        });
                    } catch (IOException | JSONException e) {
                        throw new RuntimeException(e);
                    }
                }
        );
    }
}
