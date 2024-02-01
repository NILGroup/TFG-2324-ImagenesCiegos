package com.android.app;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

<<<<<<< HEAD
import java.util.Arrays;
import java.util.List;

public class Identificador extends Query{
=======
public class Identificador {
    protected JSONArray json;
>>>>>>> parent of 3b86046 (Hilo Tagging)
    public Identificador(String input) throws JSONException {
        super(input);
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
