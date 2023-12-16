package com.android.app;

public class Model {

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    private String imageUrl;

    public Model(){

    }
    public Model (String imageUrl){
        this.imageUrl= imageUrl;
    }

}
