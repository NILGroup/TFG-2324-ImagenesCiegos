package com.android.app;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Identificador {
    protected JSONArray json;
    public Identificador(String input) throws JSONException {
        json = new JSONArray(input);
    }

    public String getObject(int x, int y) throws JSONException {

        for(int i = 0; i<json.length(); i++){
            if(estaContenido(json.getJSONObject(i).getJSONObject("box"),x,y)){
                //TODO mas de un objeto
                //TODO Distancia Manhattan
                return json.getJSONObject(i).getString("label");
            }
        }
        return "No hay ningún objeto";
    }

    private boolean estaContenido(JSONObject box, int x, int y) throws JSONException {
        return x>box.getInt("xmin") && x<box.getInt("xmax")
                &&
                y>box.getInt("ymin") && y<box.getInt("ymax");
    }

}
