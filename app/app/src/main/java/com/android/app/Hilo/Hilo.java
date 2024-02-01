package com.android.app.Hilo;

import com.android.app.FireFunctions;
import com.android.app.Imagen;

public class Hilo extends Thread{
    FireFunctions firebase;
    Imagen imagen;

    public Hilo(Imagen imagen){
        firebase = new FireFunctions();
        this.imagen = imagen;
    }

}
