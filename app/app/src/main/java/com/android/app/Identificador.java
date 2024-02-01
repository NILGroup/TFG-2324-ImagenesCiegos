package com.android.app;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

public class Identificador extends Query{
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

    public String getLabels() throws JSONException {
        String ret = "";
        for(int i = 0; i<json.length(); i++){
            ret += '"' + json.getJSONObject(i).getString("label") + '"' + ',';
        }
        ret = ret.substring(0,ret.length()-1);
        return ret;
    }

    public void changeLabels(String input) throws JSONException {

        String[] lista = input.replaceAll("\"","").split(",");
        List<String> newLabels = Arrays.asList(lista);

        for(int i = 0; i<json.length(); i++){
            json.getJSONObject(i).put("label", newLabels.get(i));
        }
    }

    private boolean estaContenido(JSONObject box, int x, int y) throws JSONException {
        return x>box.getInt("xmin") && x<box.getInt("xmax")
                &&
                y>box.getInt("ymin") && y<box.getInt("ymax");
    }

}
