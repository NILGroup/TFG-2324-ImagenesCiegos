package com.android.app.server;

import androidx.annotation.NonNull;

import org.json.JSONException;

public class Genero extends Query{
    public Genero(String input) throws JSONException {
        super(input);
        texto = GeneroMasSeguro();
    }

    private String GeneroMasSeguro() throws JSONException {
        String sol;
        double score = json.getJSONObject(0).getDouble("score");
        double score1 = json.getJSONObject(1).getDouble("score");
        if(score > score1){
            sol = json.getJSONObject(0).getString("label");

        }else{
            sol = json.getJSONObject(1).getString("label");
        }
        if (sol.equals("man")) {
            sol = "hombre";
        } else if (sol.equals("woman")) {
            sol = "mujer";
        }
        return sol;
    }
    @NonNull
    public String toString() {
        return texto;
    }
}
