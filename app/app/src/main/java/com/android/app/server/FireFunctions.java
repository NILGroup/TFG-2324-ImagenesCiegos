package com.android.app.server;

import com.google.android.gms.tasks.Task;
import com.google.firebase.functions.FirebaseFunctions;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class FireFunctions {

    protected FirebaseFunctions fFunc = FirebaseFunctions.getInstance();

    public Task<Descripcion> callImagen(String name ){

        Map<String, Object> data = new HashMap<>();
        data.put("url", name);

        return fFunc.getHttpsCallable("descripImagen").call(data).continueWith(task -> new Descripcion((String) task.getResult().getData()));
    }

    public Task<Identificador> callTags(String name ){

        Map<String, Object> data = new HashMap<>();
        data.put("url", name);

        return fFunc.getHttpsCallable("tagsImagen").call(data).continueWith(task -> new Identificador((String) task.getResult().getData()));
    }

    public Task<Traduccion> translatedImage(String name) throws IOException {

        Map<String, Object> data = new HashMap<>();
        data.put("texto", name);

        return fFunc.getHttpsCallable("traducDescrip").call(data).continueWith(task -> new Traduccion((String) task.getResult().getData()));
    }
}
