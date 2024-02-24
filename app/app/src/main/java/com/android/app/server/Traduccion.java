package com.android.app.server;

import org.json.JSONException;

public class Traduccion extends Query{
    public Traduccion(String input) throws JSONException {
        super(input);
        texto = json.getJSONObject(0).getString("translation_text");
    }

}
