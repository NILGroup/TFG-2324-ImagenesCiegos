package com.android.app.server;


import android.util.Pair;

import com.android.app.imagen.Coordenadas;
import com.android.app.imagen.Imagen;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Identificador extends Query{
    Imagen imagen;
    FireFunctions firebase;
    private HashMap<String, Integer> descrpcionCuan = new HashMap<>();
    private static String descripcionCuantitativa;
    public Identificador(String input, Imagen _imagen) throws JSONException{
        super(input);
        imagen = _imagen;
        firebase = new FireFunctions();
        texto = getLabels();

    }
    private String getLabels() throws JSONException {
        StringBuilder ret = new StringBuilder();
        //Si no se ha detectado ningún objeto saltamos
        if(json.length()>0){
            for(int i = 0; i<json.length(); i++){
                ret.append('"').append(json.getJSONObject(i).getString("label")).append('"').append(',');
            }
            ret = new StringBuilder(ret.substring(0, ret.length() - 1));
        }
        return ret.toString();
    }
    public String getCortarGenero(JSONObject box) throws JSONException{
        int[] coords = new int[4];
        coords[0] = box.getInt("xmin");
        coords[1] = box.getInt("ymin");
        coords[2] = box.getInt("xmax");
        coords[3] = box.getInt("ymax");
        return imagen.cortar(coords);
    }

    public String getObject(Coordenadas coord, int x, int y,boolean giro) throws JSONException {
        StringBuilder ret = new StringBuilder();
        for(int i = 0; i<json.length(); i++){
            if(estaContenido(coord,json.getJSONObject(i).getJSONObject("box"),x,y,giro)){

                ret.append(json.getJSONObject(i).getString("label")).append(",");
            }
        }
        if(ret.toString().isEmpty())
            return "No hay ningún objeto";
        else
            return ret.toString();
    }

    public int[] getObjectBox(Coordenadas coord, int x, int y,boolean giro) throws JSONException {
        int[] ret = new int[4];
        int min = Integer.MAX_VALUE;
        for(int i = 0; i<json.length(); i++){
            JSONObject box = json.getJSONObject(i).getJSONObject("box");
            if(estaContenido(coord,box,x,y,giro)){
                int d = distanciaManhattan(coord,json.getJSONObject(i).getJSONObject("box"),x,y);
                if(d<min){
                    ret[0] = box.getInt("xmin");
                    ret[1] = box.getInt("ymin");
                    ret[2] = box.getInt("xmax");
                    ret[3] = box.getInt("ymax");
                    min = d;
                }
            }
        }
       return ret;
    }

    public void setTexto(String input) throws JSONException, IOException, InterruptedException {

        String[] lista = input.replaceAll("\"","").split(",");
        List<String> newLabels = Arrays.asList(lista);
        List<String> newNewLabels = new ArrayList<>();
        HashMap<Integer,String> h = new HashMap<>();
        HashMap<String,Integer> sufijosPersona = new HashMap<>();

        //Para añadir sufijo cuaando dos objetos tengan el mismo nombre
        for(int j=0;j<json.length();j++) {
            String nuevaEtiqueta = newLabels.get(j);
            nuevaEtiqueta = nuevaEtiqueta.trim();
            if (newNewLabels.contains(nuevaEtiqueta)) {
                descrpcionCuan.put(nuevaEtiqueta,descrpcionCuan.get(nuevaEtiqueta)+1);
                int sufijo = 1;
                while (newNewLabels.contains(nuevaEtiqueta + sufijo)) {
                    sufijo++;
                }
                nuevaEtiqueta = nuevaEtiqueta + sufijo;
                newNewLabels.add(nuevaEtiqueta);
                h.put(j, nuevaEtiqueta);
                json.getJSONObject(j).put("label", newNewLabels.get(j));
            } else {
                newNewLabels.add(nuevaEtiqueta);
                h.put(j, nuevaEtiqueta);
                descrpcionCuan.put(nuevaEtiqueta,1);
                json.getJSONObject(j).put("label", h.get(j));
            }

            if (nuevaEtiqueta.contains("persona")) {
                String corte = getCortarGenero(json.getJSONObject(j).getJSONObject("box"));
                try {
                    int finalJ = j;
                    String finalNuevaEtiqueta = nuevaEtiqueta;
                    firebase.generoPersona(corte).addOnCompleteListener(task -> {
                        newNewLabels.add(finalNuevaEtiqueta);
                        try {
                            firebase.edadPersona(corte).addOnCompleteListener(task1 -> {
                                String s = finalNuevaEtiqueta + task.getResult().getTexto() + task1.getResult().getTexto();
                                String persona= construirPersona(s);

                                if(sufijosPersona.containsKey(persona.trim()))
                                    sufijosPersona.put(persona.trim(),sufijosPersona.get(persona.trim())+1);
                                else{
                                    sufijosPersona.put(persona.trim(),0);
                                }
                                if(sufijosPersona.get(persona.trim()) != 0)
                                    persona += " " + sufijosPersona.get(persona.trim());

                                h.put(finalJ,persona);

                                try {
                                    json.getJSONObject(finalJ).put("label", h.get(finalJ));
                                } catch (JSONException ex) {
                                    throw new RuntimeException(ex);
                                }
                            });

                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            descripcionCuantitativa = costruirBarrido(descrpcionCuan);
        }
    }

    private String costruirBarrido(HashMap<String,Integer> descrpcionCuan) {
        String sol="Se han detectado ";

        for (String key : descrpcionCuan.keySet()) {
            List<Character> vocales = new ArrayList<>();
            vocales.add('a');
            vocales.add('e');
            vocales.add('i');
            vocales.add('o');
            vocales.add('u');
            String objeto =key;
            if(descrpcionCuan.get(key)>1){
                if(vocales.contains(objeto.charAt(objeto.length()-1)))
                    sol += descrpcionCuan.get(key) + key +"s, ";
                else
                    sol += descrpcionCuan.get(key) + key +"es, ";
            }else{
                sol +=" un o una " + key +", ";
            }
        }
        sol = sol.substring(0, sol.length()-2);
        for(int i = sol.length()-1; i>= 0; i--){
            if(sol.charAt(i) == ','){
                sol = sol.substring(0,i) + " y " + sol.substring(i+1);
                i=-1;
            }
        }
        sol+= " en la imagen";
        return sol;
    }

    private String construirPersona(String persona){
        if(!persona.contains("definido")) {
            persona = persona.replace("persona", "");
            if (Character.isDigit(persona.charAt(0))) {
                persona = (String) persona.subSequence(2, persona.length());
            }
        }
       return persona;
    }
    private int distanciaManhattan(Coordenadas coord, JSONObject box, int x, int y) throws JSONException {
        int[] ret = coord.convTam(box.getInt("xmin"),box.getInt("ymin"),box.getInt("xmax"),box.getInt("ymax"));
        int dist;
        int[] centro = new int[2];
        centro[0] = (ret[0]+ret[2])/2;
        centro[1] = (ret[1]+ret[3])/2;
        dist = Math.abs(x - centro[0]) + Math.abs(y - centro[1]);
        return dist;
    }

    private boolean estaContenido(Coordenadas coord, JSONObject box, int x, int y,boolean giro) throws JSONException {
        int[] ret = coord.convTam(box.getInt("xmin"),box.getInt("ymin"),box.getInt("xmax"),box.getInt("ymax"));
        if(giro){
            return x> ret[2]  && x<ret[0] &&
                    y> ret[1] && y<ret[3];
        }
        return x> ret[0]  && x<ret[2] &&
                y> ret[1] && y<ret[3];
    }
    public static String getDescripcionCuan(){
        return descripcionCuantitativa;
    }

    public List<Pair<String,int[]>> getObjects(Coordenadas coord, int x, int y, boolean giro) throws JSONException {

        List<Pair<String,int[]>> ret = new ArrayList<>();
        for(int i = 0; i<json.length(); i++){
            JSONObject box= json.getJSONObject(i).getJSONObject("box");
            if(estaContenido(coord,box,x,y,giro)){
                int[] coords = new int[4];
                coords[0] = box.getInt("xmin");
                coords[1] = box.getInt("ymin");
                coords[2] = box.getInt("xmax");
                coords[3] = box.getInt("ymax");

                Pair<String,int[]> aux = new Pair<>(json.getJSONObject(i).getString("label"),coords);
                ret.add(aux);
            }
        }
        if(ret.isEmpty())
            ret.add(new Pair<>("No hay ningún objeto",new int[4]));
        return ret;
    }
}
