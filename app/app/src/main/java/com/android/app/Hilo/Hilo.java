package com.android.app.Hilo;

import com.android.app.server.FireFunctions;
import com.android.app.imagen.Imagen;

public class Hilo{
    FireFunctions firebase;
    Imagen imagen;

    public Hilo(){}

    public Hilo(Imagen imagen){
        firebase = new FireFunctions();
        this.imagen = imagen;
    }

}
