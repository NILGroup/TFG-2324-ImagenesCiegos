package com.android.app.Hilo;

import com.android.app.server.FireFunctions;
import com.android.app.imagen.Imagen;

public class Hilo extends Thread{
    protected FireFunctions firebase;
    protected String imagen;

    public Hilo(String imagen,FireFunctions firebase){
        this.firebase = firebase;
        this.imagen = imagen;
    }
}
