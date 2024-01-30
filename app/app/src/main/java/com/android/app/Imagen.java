package com.android.app;

import android.content.Context;
import android.net.Uri;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

public class Imagen {
    private Uri imageUri;

    private String base64;
    private Context contexto;

    public Imagen(Context contexto, Uri imageUri) throws IOException {

        this.imageUri=imageUri;
        this.contexto=contexto;
        codBase64(imageUri);
    }

    public String getImageUri() {return imageUri.toString();}
    public String getBase64() {return base64;}

    private void codBase64(Uri uri) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        InputStream inputStream = contexto.getContentResolver().openInputStream(uri);
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            byteArrayOutputStream.write(buffer, 0, bytesRead);
        }
        byte[] byteArray = byteArrayOutputStream.toByteArray();

        // Convertir el array de bytes a base64
        base64 = Base64.getEncoder().encodeToString(byteArray);
    }
}
