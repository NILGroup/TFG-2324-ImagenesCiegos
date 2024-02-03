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

    public boolean rotarImagen(Intent data, ImageView ivPicture) throws IOException {
        boolean giro = sacarRelacion(data);
        if (giro)
            Glide.with(contexto.getApplicationContext()).load(data.getData()).apply(new RequestOptions().transform(new Rotate(90))) // Rotación de 90 grados
                    .into(ivPicture);
        else ivPicture.setImageURI(data.getData());
        return giro;
    }
    private boolean sacarRelacion(Intent data) throws IOException { //Ve si una imagen tiene que ir en vertical o en horizontal
        ImageDecoder.Source source = ImageDecoder.createSource(contexto.getContentResolver(), Objects.requireNonNull(data.getData()));
        Bitmap bitmap = ImageDecoder.decodeBitmap(source);
        int imageHeight = bitmap.getHeight();
        int imageWidth = bitmap.getWidth();
        int ratio = imageHeight / imageWidth;
        return ratio<1;
    }

    public Imagen cortarImagen(int[] coordenadas) throws IOException {
        // Obtener la imagen original como un bitmap
        ImageDecoder.Source source = ImageDecoder.createSource(contexto.getContentResolver(), imageUri);
        Bitmap originalBitmap = ImageDecoder.decodeBitmap(source);

        // Crear un nuevo bitmap que sea el recorte
        Bitmap croppedBitmap = Bitmap.createBitmap(originalBitmap, coordenadas[0], coordenadas[1], coordenadas[2], coordenadas[3]);

        // Convertir el nuevo bitmap a base64
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        croppedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] croppedByteArray = byteArrayOutputStream.toByteArray();
        String croppedBase64 = Base64.getEncoder().encodeToString(croppedByteArray);

        // Crear y devolver un nuevo objeto Imagen con la imagen recortada
        Imagen imagenRecortada = new Imagen(contexto, imageUri);
        imagenRecortada.base64 = croppedBase64;  // Actualizar el base64 con el recorte

        return imagenRecortada;
    }
}
