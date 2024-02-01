package com.android.app;

import org.json.JSONArray;
import org.json.JSONException;

public class Query{

    protected JSONArray json;

    public Query(String input) throws JSONException {
        json = new JSONArray(input);
    }

}
