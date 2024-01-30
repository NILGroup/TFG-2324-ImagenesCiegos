package com.android.app;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class FireFunctions {

    private final FirebaseFunctions fFunc = FirebaseFunctions.getInstance();

    public Task<Descripcion> callImagen(String name ){

        Map<String, Object> data = new HashMap<>();
        data.put("url", name);

        return fFunc.getHttpsCallable("descripImagen").call(data).continueWith(new Continuation<HttpsCallableResult, Descripcion>() {
            @Override
            public Descripcion then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                return new Descripcion((String) task.getResult().getData());
            }
        });
    }

    public Task<Traduccion> translatedImage(String name) throws IOException {

        Map<String, Object> data = new HashMap<>();
        data.put("texto", name);

        return fFunc.getHttpsCallable("traducDescrip").call(data).continueWith(new Continuation<HttpsCallableResult, Traduccion>() {
            @Override
            public Traduccion then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                return new Traduccion((String) task.getResult().getData());
            }
        });
    }
}
