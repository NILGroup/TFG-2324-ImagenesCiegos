package com.android.app;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.Rotate;
import com.bumptech.glide.request.RequestOptions;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.Objects;

public class Imagen {
    protected Uri imageUri;
    private String base64;
    protected Context contexto;

    public Imagen(Context contexto, Uri imageUri) throws IOException {

        this.imageUri=imageUri;
        this.contexto=contexto;
        codBase64(imageUri);
    }
    public String getBase64() {return base64;}

    private void codBase64(Uri uri) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        InputStream inputStream = contexto.getContentResolver().openInputStream(uri);
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = Objects.requireNonNull(inputStream).read(buffer)) != -1) {
            byteArrayOutputStream.write(buffer, 0, bytesRead);
        }
        byte[] byteArray = byteArrayOutputStream.toByteArray();

        // Convertir el array de bytes a base64
        base64 = Base64.getEncoder().encodeToString(byteArray);
        inputStream.close();
    }

    public void rotarImagen(Intent data, ImageView ivPicture) throws IOException {
        sacarRelacion(data);
        if (sacarRelacion(data))
            Glide.with(contexto.getApplicationContext()).load(data.getData()).apply(new RequestOptions().transform(new Rotate(90))) // Rotación de 90 grados
                    .into(ivPicture);
        else ivPicture.setImageURI(data.getData());
    }
    private boolean sacarRelacion(Intent data) throws IOException { //Ve si una imagen tiene que ir en vertical o en horizontal
        ImageDecoder.Source source = ImageDecoder.createSource(contexto.getContentResolver(), Objects.requireNonNull(data.getData()));
        Bitmap bitmap = ImageDecoder.decodeBitmap(source);
        int imageHeight = bitmap.getHeight();
        int imageWidth = bitmap.getWidth();
        int ratio = imageHeight / imageWidth;
        return ratio<1;
    }

    public void cortarImagen(Bitmap b, int[] coordenadas, ImageView ivPicture) { // Recortar el objeto deseado
        ivPicture.setImageBitmap(Bitmap.createBitmap(b, coordenadas[0], coordenadas[1], coordenadas[2], coordenadas[3]));
    }
}
