package com.android.app;

import org.json.JSONException;
import org.json.JSONObject;

public class Query{

    protected JSONObject json;

    public Query(String input) throws JSONException {
        input = input.substring(1,input.length()-1);
        json = new JSONObject(input);
    }

}
