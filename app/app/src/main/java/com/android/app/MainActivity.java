package com.android.app;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageButton;
import android.widget.ImageView;

import android.speech.tts.TextToSpeech;

import com.android.app.Hilo.HiloTag;
import com.android.app.imagen.Coordenadas;
import com.android.app.imagen.Imagen;
import com.android.app.imagen.RectangleOverlay;
import com.android.app.server.FireFunctions;
import com.android.app.server.Identificador;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    //Seleccion de imagen
    private static final int CAMERA_PERMISSION_CODE = 223;
    private static final String TAG = "Numero Objetos detectados";
    ActivityResultLauncher<Intent> cameraLauncher;
    ActivityResultLauncher<Intent> galleryLauncher;

    //TextToSpeech
    private String textTo;
    private TextToSpeech textToSpeech;
    //Firebase
    private FireFunctions firebase;
    //Variables xml
    private ImageView ivPicture;
    private RectangleOverlay rectangleOverlay;
    private Coordenadas coord;

    //Objetos necesarios
    private Imagen imagen;
    private HiloTag tags;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ivPicture = findViewById(R.id.ivPicture);
        ImageButton decirDescripcion = findViewById(R.id.decirDescripcion);
        ImageButton btnChoosePicture = findViewById(R.id.btnChoosePicture);

        rectangleOverlay = findViewById(R.id.rectangleOverlay);

        firebase = new FireFunctions();
        textToSpeech = new TextToSpeech(this, status -> {});

        cameraLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            Intent data = result.getData();
            try {
                Bitmap photo = (Bitmap) Objects.requireNonNull(Objects.requireNonNull(data).getExtras()).get("data");
                ivPicture.setImageBitmap(photo);
                tratamientoImagen(data);
            } catch (Exception e) {
                Log.d(TAG, "onActivityResult:" + e.getMessage());
            }
        }

        );
        galleryLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            Intent data = result.getData();
            try {
                tratamientoImagen(Objects.requireNonNull(data));
            } catch (Exception e) {
                Log.d(TAG, "onActivityResult:" + e.getMessage());
            }
        }
        );
        btnChoosePicture.setOnClickListener(view -> {
            String[] options = {"camara", "galeria"};
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Elige una Opción");
            builder.setItems(options, (dialog, which) -> {
                if (which == 0) {
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    cameraLauncher.launch(cameraIntent);
                } else {
                    Intent storageIntent = new Intent();
                    storageIntent.setType("image/*");
                    storageIntent.setAction(Intent.ACTION_GET_CONTENT);
                    galleryLauncher.launch(storageIntent);
                }
            });
            builder.show();
        });
        decirDescripcion.setOnClickListener(v -> textToSpeech.speak(textTo, TextToSpeech.QUEUE_FLUSH, null, null));
        ivPicture.setOnTouchListener((v, event) -> {
            String msg;
            if (imagen!=null && event.getAction() == MotionEvent.ACTION_DOWN) {
                float x = event.getX();
                float y = event.getY();
                try {
                    Identificador identificador = tags.getIdentificador();
                    if(coord.zonaVacia(x,y)){
                        msg = "Estás fuera de la imagen";
                    }
                    else{
                        for(int i=0;i<identificador.getJsons().length();i++){
                            dibujarBoundingBoxes(identificador.getJsons().getJSONObject(i));
                        }
                        int[] newCoords = coord.convCoord(x,y);
                        msg = identificador.getObject(newCoords[0], newCoords[1]);
                    }
                    textToSpeech.speak(msg, TextToSpeech.QUEUE_FLUSH, null, null);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
            return true;
        });
    }
    public void dibujarBoundingBoxes(JSONObject o) throws JSONException {
        int[] ret = coord.convTam(o.getJSONObject("box").getInt("xmin"),
                    o.getJSONObject("box").getInt("ymin"),
                    o.getJSONObject("box").getInt("xmax"),
                    o.getJSONObject("box").getInt("ymax"));
            rectangleOverlay.addCoordinates(ret);
    }

    private void tratamientoImagen(Intent data) throws IOException {
        imagen = new Imagen(MainActivity.this,data.getData());
        if(imagen.rotarImagen(data,ivPicture)){
            textToSpeech.speak("La imagen está en horizontal", TextToSpeech.QUEUE_FLUSH, null, null);
        }
        coord = new Coordenadas(ivPicture, imagen);
        tags = new HiloTag(imagen);
        tags.run();
        firebase.callImagen(imagen.getBase64()).addOnCompleteListener(task -> {
            try {
                firebase.translatedImage(task.getResult().getTexto()).addOnCompleteListener(task2 -> {
                    textTo = task2.getResult().getTexto();
                    textToSpeech.speak(textTo, TextToSpeech.QUEUE_ADD, null, null);
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
    protected void onDestroy() {
        // Detener la síntesis de voz y liberar recursos de TextToSpeech
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
            textToSpeech = null; // Asegúrate de establecer la instancia en null después de cerrarla
        }
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        CheckPermission(android.Manifest.permission.CAMERA, CAMERA_PERMISSION_CODE);
    }
    public void CheckPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(MainActivity.this, permission) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{permission}, requestCode);
        }
    }
}