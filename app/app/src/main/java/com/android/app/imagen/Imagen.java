package com.android.app.imagen;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;

import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;

import android.graphics.Matrix;
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
    private float height,width;
    private float ratio;
    private boolean giro;
    protected Context contexto;

    public Imagen(Context contexto, Uri imageUri) throws IOException {

        this.imageUri=imageUri;
        this.contexto=contexto;
        codBase642(imageUri);
    }
    public String getBase64() {return base64;}
    public float getHeight() {return height;}
    public float getWidth() { return width;}
    public float getRatio() { return ratio;}
    public boolean isGiro() {return giro;}

    private void codBase642(Uri uri) throws IOException {
        // Obtener el bitmap desde la URI
        Bitmap originalBitmap = BitmapFactory.decodeStream(contexto.getContentResolver().openInputStream(uri));
        // Rotar la imagen si es necesario
        if (ratio < 1) {
            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            originalBitmap = Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.getWidth(), originalBitmap.getHeight(), matrix, true);
        }

        // Convertir el bitmap a un array de bytes
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        originalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();

        // Convertir el array de bytes a Base64
        base64 = Base64.getEncoder().encodeToString(byteArray);

        // Cerrar streams y liberar recursos
        byteArrayOutputStream.close();
        originalBitmap.recycle();
    }

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
        giro = sacarRelacion(data);
        /*if (giro)
            Glide.with(contexto.getApplicationContext()).load(data.getData()).apply(new RequestOptions().transform(new Rotate(90))) // Rotación de 90 grados
                    .into(ivPicture);
        else ivPicture.setImageURI(data.getData());
        return giro;*/
        ivPicture.setImageURI(data.getData());
        return false;
    }
    private boolean sacarRelacion(Intent data) throws IOException { //Ve si una imagen tiene que ir en vertical o en horizontal
        ImageDecoder.Source source = ImageDecoder.createSource(contexto.getContentResolver(), Objects.requireNonNull(data.getData()));
        Bitmap bitmap = ImageDecoder.decodeBitmap(source);
        height = bitmap.getHeight();
        width = bitmap.getWidth();
        ratio = height / width;
        return ratio<1;
    }
}
