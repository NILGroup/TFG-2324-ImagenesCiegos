package com.android.app;

import org.json.JSONException;

public class Traduccion extends Query{

    private String texto;

    public Traduccion(String input) throws JSONException {
        super(input);
        texto = json.getString("translation_text");
    }

    public String getTexto() {
        return texto;
    }

}
