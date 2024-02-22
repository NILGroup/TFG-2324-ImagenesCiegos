package com.android.app.server;

import org.json.JSONException;

public class Traduccion extends Query{

    protected String texto;

    public Traduccion(String input) throws JSONException {
        super(input);
        texto = json.getString("translation_text");
    }

    public String getTexto() {
        return texto;
    }

}
