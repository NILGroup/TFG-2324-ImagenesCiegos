package com.android.app.hilos;

import com.android.app.FireFunctions;
import com.android.app.Imagen;

public class Hilo extends Thread{

    protected FireFunctions firebase;
    protected Imagen imagen;

    public Hilo(Imagen imagen) {
        this.firebase = new FireFunctions();
        this.imagen = imagen;
    }
}
