package com.android.app.server;

import org.json.JSONException;

public class Descripcion extends Query{

    public Descripcion(String input) throws JSONException {
        super(input);
        texto = json.getJSONObject(0).getString("generated_text");
    }
}
