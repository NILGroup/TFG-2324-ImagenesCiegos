package com.android.app.server;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Identificador {

    protected JSONArray json;
    public Identificador(String input) throws JSONException {
        json = new JSONArray(input);
    }

    public String getObject(int x, int y) throws JSONException {
        String ret = "";
        for(int i = 0; i<json.length(); i++){
            if(estaContenido(json.getJSONObject(i).getJSONObject("box"),x,y)){
                ret += json.getJSONObject(i).getString("label") + ",";
            }
        }
        if(ret =="")
            return "No hay ningún objeto";
        else
            return ret;
    }
    public JSONArray getJsons(){
        return json;
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
        List<String> newNewLabels = new ArrayList<>();

        //Para añadir sufijo cuaando dos objetos tengan el mismo nombre
        for(int j=0;j<json.length();j++){
                String nuevaEtiqueta = newLabels.get(j);
                if (newNewLabels.contains(nuevaEtiqueta)) {
                    int sufijo = 1;
                    while (newNewLabels.contains(nuevaEtiqueta + sufijo)) {
                        sufijo++;
                    }
                    nuevaEtiqueta = nuevaEtiqueta + sufijo;
                }
                newNewLabels.add(nuevaEtiqueta);
        }

        for(int i = 0; i<json.length(); i++){
            json.getJSONObject(i).put("label", newNewLabels.get(i));
        }
    }

    private boolean estaContenido(JSONObject box, int x, int y) throws JSONException {
        return x> box.getInt("xmin") && x<box.getInt("xmax") &&
                y> box.getInt("ymin") && y<box.getInt("ymax");
    }

}
