package com.android.app.server;

import org.json.JSONArray;
import org.json.JSONException;

public abstract class Query{

    protected JSONArray json;
    protected String texto;

    public Query(String input) throws JSONException {
        json = new JSONArray(input);
    }

    public String getTexto() {
        return texto;
    }
    public JSONArray getJson() {return json; }
}
