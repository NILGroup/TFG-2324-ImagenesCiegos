package com.android.app.server;

import androidx.annotation.NonNull;

import org.json.JSONException;

public class Edad extends Query{
    public Edad(String input) throws JSONException {
        super(input);
        texto = getEdad();
    }


    private String getEdad() throws JSONException {
        String sol = "";
        double score = json.getJSONObject(0).getDouble("score");
        double score1 = json.getJSONObject(1).getDouble("score");
        double score2 = json.getJSONObject(2).getDouble("score");

        if(score > score1 && score > score2){
            sol = json.getJSONObject(0).getString("label");

        }else if (score1 > score && score1 > score2){
            sol = json.getJSONObject(1).getString("label");
        }else if(score2 > score1 && score2 > score){
            sol = json.getJSONObject(2).getString("label");
        }
        if (sol.equals("MIDDLE")) {
            sol = " de mediana edad";
        } else if (sol.equals("OLD")) {
            sol = " de avanzada edad";
        }else{
            sol = "joven";
        }
        return sol;
    }
    @NonNull
    public String toString() {
        return texto;
    }
}
