package com.android.app.imagen;

import android.content.Context;
import android.graphics.Bitmap;

import android.graphics.BitmapRegionDecoder;
import android.graphics.Color;
import android.graphics.ImageDecoder;

import android.graphics.Rect;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.Rotate;
import com.bumptech.glide.request.RequestOptions;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Objects;
import android.graphics.BitmapFactory;

import androidx.palette.graphics.Palette;


public class Imagen {
    protected Uri imageUri;
    private String base64;
    private float height,width;
    private float ratio;
    private boolean giro;
    private HashMap<String, String> croppedImages = new HashMap<>();
    protected Context contexto;

    public Imagen(Context contexto, Uri imageUri) throws IOException {

        this.imageUri=imageUri;
        this.contexto=contexto;
        codBase64(imageUri);
        convertirABitmap(imageUri);

    }
    public String getBase64() {return base64;}
    public Uri getUri() {return imageUri;}
    public float getHeight() {return height;}
    public float getWidth() { return width;}
    public float getRatio() { return ratio;}
    public boolean isGiro() {return giro;}

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
    private void convertirABitmap(Uri uri) throws IOException {
        InputStream inputStream = contexto.getContentResolver().openInputStream(uri);
        Bitmap bmap = BitmapFactory.decodeStream(inputStream);
    }

    public String extraerColorDominante(String base64) {
        String aux="";
        // Decodificar la cadena base64 en un array de bytes
        byte[] imageBytes = Base64.getDecoder().decode(base64);

        // Convertir los bytes decodificados en un Bitmap
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (bitmap != null) {
            Palette palette = Palette.from(bitmap).generate();
            Palette.Swatch colorDominante = palette.getDominantSwatch();
            int color = colorDominante.getRgb();
            int r = (color >>24) & 0xFF;
            int g = (color >>16) & 0xFF;
            int b = (color >> 8)  & 0xFF;
            if (colorDominante != null) {
                ColorUtils matchColor  = new ColorUtils();
                aux = matchColor.getColorNameFromRgb(r,g,b);
            }
        }
        return aux;
    }

    public boolean rotarImagen(Imagen imagen, ImageView ivPicture) throws IOException {
        giro = sacarRelacion(imagen);
        if (giro){
            Glide.with(contexto.getApplicationContext()).load(imagen.getUri()).apply(new RequestOptions().transform(new Rotate(90))) // Rotación de 90 grados
                    .into(ivPicture);
            ratio = 1/ratio;
        }
        else ivPicture.setImageURI(imagen.getUri());
        return giro;
    }
    private boolean sacarRelacion(Imagen imagen) throws IOException { //Ve si una imagen tiene que ir en vertical o en horizontal
        ImageDecoder.Source source = ImageDecoder.createSource(contexto.getContentResolver(), Objects.requireNonNull(imagen.getUri()));
        Bitmap bitmap = ImageDecoder.decodeBitmap(source);
        height = bitmap.getHeight();
        width = bitmap.getWidth();
        ratio = height / width;
        return ratio<1;
    }

    public String cortar(int[] coords) {
        // Crear una cadena a partir de las coordenadas
        String coordsString = Arrays.toString(coords);

        // Verificar si la imagen ya ha sido recortada
        if (croppedImages.containsKey(coordsString)) {
            Log.d("La imagen ya estaba guardada", croppedImages.get(coordsString));
            // Si ya ha sido recortada, devolver la imagen almacenada
            return croppedImages.get(coordsString);
        }

        // Si no ha sido recortada, proceder con el recorte
        byte[] imageBytes = Base64.getDecoder().decode(base64);
        BitmapRegionDecoder regionDecoder = null;
        try {
            regionDecoder = BitmapRegionDecoder.newInstance(imageBytes, 0, imageBytes.length, false);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Rect cropRect = new Rect(coords[0], coords[1], coords[0] + coords[2], coords[1] + coords[3]);
        assert regionDecoder != null;
        Bitmap croppedBitmap = regionDecoder.decodeRegion(cropRect, null);
        String croppedBase64 = encodeBitmapToBase64(croppedBitmap);

        // Almacenar la imagen recortada en el HashMap
        croppedImages.put(coordsString, croppedBase64);
        Log.d("Almacenado", croppedImages.get(coordsString));

        regionDecoder.recycle();
        croppedBitmap.recycle();

        return croppedBase64;
    }
    private String encodeBitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.getEncoder().encodeToString(byteArray);
    }
}
