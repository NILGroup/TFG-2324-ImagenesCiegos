package com.android.app.server;

import org.json.JSONException;

public class Descripcion extends Query{
    protected String texto;

    public Descripcion(String input) throws JSONException {
        super(input);
        texto = json.getString("generated_text");
    }

    public String getTexto() {
        return texto;
    }
}