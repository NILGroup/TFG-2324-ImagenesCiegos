package com.android.app.server;

import com.android.app.imagen.Coordenadas;
import com.google.android.gms.common.api.Api;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Identificador {

    protected JSONArray json;
    private HashMap<String,String> descripDetallada;
    public Identificador(String input) throws JSONException {
        json = new JSONArray(input);
    }

    public String getObject(Coordenadas coord, int x, int y) throws JSONException {
        String ret = "";
        for(int i = 0; i<json.length(); i++){
            if(estaContenido(coord,json.getJSONObject(i).getJSONObject("box"),x,y)){
                ret += json.getJSONObject(i).getString("label") + ",";
            }
        }
        if(ret =="")
            return "No hay ningún objeto";
        else
            return ret;
    }

    public int[] getObjectBox(Coordenadas coord, int x, int y) throws JSONException {
        int[] ret = new int[4];
        for(int i = 0; i<json.length(); i++){
            JSONObject box = json.getJSONObject(i).getJSONObject("box");
            if(estaContenido(coord,box,x,y)){
                ret[0] = box.getInt("xmin");
                ret[1] = box.getInt("ymin");
                ret[2] = box.getInt("xmax");
                ret[3] = box.getInt("ymax");
                return ret;
            }
        }
        return null;
    }

    public JSONArray getJsons(){
        return json;
    }

    public String getLabels() throws JSONException {
        String ret = "";
        //Si no se ha detectado ningún objeto saltamos
        if(json.length()>0){
            for(int i = 0; i<json.length(); i++){
                ret += '"' + json.getJSONObject(i).getString("label") + '"' + ',';
            }
            ret = ret.substring(0,ret.length()-1);
        }
        
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

    private boolean estaContenido(Coordenadas coord, JSONObject box, int x, int y) throws JSONException {
        int[] ret = coord.convTam(box.getInt("xmin"),box.getInt("ymin"),box.getInt("xmax"),box.getInt("ymax"));

        return x> ret[0]  && x<ret[2] &&
                y> ret[1] && y<ret[3];
    }
}
