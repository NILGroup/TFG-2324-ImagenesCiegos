package com.android.app;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Identificador {
    RectangleOverlay rectangleOverlay;
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
    public void dibujarBoundingBoxes() throws JSONException {
        int[] ret = new int[4];;
        for(int i = 0; i<json.length(); i++){
            ret[0] = json.getJSONObject(i).getJSONObject("box").getInt("xmin");
            ret[1] = json.getJSONObject(i).getJSONObject("box").getInt("ymin");
            ret[2] = json.getJSONObject(i).getJSONObject("box").getInt("xmax");
            ret[3] = json.getJSONObject(i).getJSONObject("box").getInt("ymax");
        rectangleOverlay.setCoordinates(ret);
        }
    }
    public int[] getCoords(int x, int y) throws JSONException {

        int[] ret = new int[4];

        for(int i = 0; i<json.length(); i++){
            if(estaContenido(json.getJSONObject(i).getJSONObject("box"),x,y)){
                ret[0] = json.getJSONObject(i).getJSONObject("box").getInt("xmin");
                ret[1] = json.getJSONObject(i).getJSONObject("box").getInt("ymin");
                ret[2] = json.getJSONObject(i).getJSONObject("box").getInt("xmax");
                ret[3] = json.getJSONObject(i).getJSONObject("box").getInt("ymax");
            }
        }
        return ret;
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
                if (newNewLabels.contains(nuevaEtiqueta +1)) {
                    int sufijo = 1;
                    while (newNewLabels.contains(nuevaEtiqueta + sufijo)) {
                        sufijo++;
                    }
                    nuevaEtiqueta = nuevaEtiqueta + sufijo;
                }else{
                    nuevaEtiqueta +="1";
                }
                newNewLabels.add(nuevaEtiqueta);
        }

        for(int i = 0; i<json.length(); i++){
            json.getJSONObject(i).put("label", newNewLabels.get(i));
        }

    }

    private boolean estaContenido(JSONObject box, int x, int y) throws JSONException {
        return x>box.getInt("xmin") && x<box.getInt("xmax")
                &&
                y>box.getInt("ymin") && y<box.getInt("ymax");
    }

}
