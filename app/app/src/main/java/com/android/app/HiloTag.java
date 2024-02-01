package com.android.app;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class HiloTag extends Thread{

    FireFunctions firebase;
    Imagen imagen;
    Identificador identificador;

    public HiloTag(FireFunctions firebase, Imagen imagen) {
        this.firebase = firebase;
        this.imagen = imagen;
    }

    public Identificador getIdentificador(){
        return identificador;
    }
    public void run(){
        firebase.callTags(imagen.getBase64()).addOnCompleteListener(new OnCompleteListener<Identificador>() {
            @Override
            public void onComplete(@NonNull Task<Identificador> task) {
                identificador = task.getResult();
            }
        });
    }
}
