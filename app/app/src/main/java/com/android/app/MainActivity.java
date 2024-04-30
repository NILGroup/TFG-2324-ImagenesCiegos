package com.android.app;

import static java.lang.Thread.sleep;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.Window;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class MainActivity extends AppCompatActivity{

    //Seleccion de imagen
    private static final int CAMERA_PERMISSION_CODE = 223;
    private static final String TAG = "Numero Objetos detectados";
    ActivityResultLauncher<Intent> cameraLauncher;
    ActivityResultLauncher<Intent> galleryLauncher;

    //TextToSpeech
    private String textTo;
    private HashMap<String,String> descripDetallada;
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
    private GestureDetector gestureDetector;
    private static boolean resultadoCallback;

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
                    Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
                    ivPicture.setImageBitmap(imageBitmap);
                    Uri imageUri = saveImageToExternalStorage(imageBitmap);
                    imagen = new Imagen(MainActivity.this,imageUri);
                    tratamientoImagen(imagen);
                } catch (Exception e) {
                    Log.d(TAG, "onActivityResult:" + e.getMessage());
                }
        });
        galleryLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                Intent data = result.getData();
                try {
                    imagen = new Imagen(MainActivity.this,data.getData());
                    tratamientoImagen(imagen);
                } catch (Exception e) {
                    Log.d(TAG, "onActivityResult:" + e.getMessage());
                }
        });
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
            gestureDetector.onTouchEvent(event);
            return true;
        });

        gestureDetector = new GestureDetector(ivPicture.getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public void onLongPress(@NonNull MotionEvent e) {

                if (imagen!=null && e.getAction() == MotionEvent.ACTION_DOWN) {

                    float x = e.getX();
                    float y = e.getY();
                    try {
                        tags.join();
                    } catch (InterruptedException ex) {
                        throw new RuntimeException(ex);
                    }
                    Identificador identificador = tags.getIdentificador();
                    if(coord.zonaVacia(x,y)){
                        textToSpeech.speak("Estás fuera de la imagen", TextToSpeech.QUEUE_ADD, null, null);
                    }
                    else{
                        int[] box;
                        try {
                            String objeto = identificador.getObject(coord,(int) x, (int) y,imagen.isGiro());
                            if(!objeto.isEmpty() && descripDetallada.containsKey(objeto)){
                                textToSpeech.speak(descripDetallada.get(objeto), TextToSpeech.QUEUE_ADD, null, null);
                            }
                            else {

                                box = identificador.getObjectBox(coord, (int) x, (int) y,imagen.isGiro());
                                if (box != null) {
                                    firebase.callImagen(imagen.cortar(box)).addOnCompleteListener(task -> {
                                        try {
                                            firebase.translatedImage(task.getResult().getTexto()).addOnCompleteListener(task2 -> {
                                                textTo = task2.getResult().getTexto();
                                                descripDetallada.put(objeto,textTo);
                                                textToSpeech.speak(textTo, TextToSpeech.QUEUE_ADD, null, null);
                                            });
                                        } catch (IOException ex) {
                                            throw new RuntimeException(ex);
                                        }

                                    });
                                }
                            }
                        } catch (JSONException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                }
            }
            public boolean onSingleTapConfirmed(@NonNull MotionEvent event){
                String msg;
                if (imagen!=null && event.getAction() == MotionEvent.ACTION_DOWN) {
                    float x = event.getX();
                    float y = event.getY();

                    try {
                        tags.join();

                        int[] box;
                        String aux="";
                        Identificador identificador = tags.getIdentificador();

                        textToSpeech.speak(Identificador.getDescripcionCuan(), TextToSpeech.QUEUE_ADD, null, null);
                        box = identificador.getObjectBox(coord, (int) x, (int) y,imagen.isGiro());
                        if(coord.zonaVacia(x,y)){
                            msg = "Estás fuera de la imagen";
                        }
                        else{
                            for(int i=0;i<identificador.getJson().length();i++){
                                dibujarBoundingBoxes(identificador.getJson().getJSONObject(i));
                            }
                            msg = identificador.getObject(coord,(int) x, (int) y,imagen.isGiro());
                            if (!(msg.contains("mujer") || msg.contains("hombre") || msg.contains("persona")) && !msg.equals("No hay ningún objeto"))
                                aux = imagen.extraerColorDominante(imagen.cortar(box));
                        }
                        String msgFinal = (msg + aux).replace("persona","").replace(",","");
                        textToSpeech.speak(msgFinal, TextToSpeech.QUEUE_ADD, null, null);
                    } catch (JSONException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                return false;
            }
        });
    }
    public void dibujarBoundingBoxes(JSONObject o) throws JSONException {
        int[] ret = coord.convTam(o.getJSONObject("box").getInt("xmin"),
                o.getJSONObject("box").getInt("ymin"),
                o.getJSONObject("box").getInt("xmax"),
                o.getJSONObject("box").getInt("ymax"));
        rectangleOverlay.addCoordinates(ret);

    }

    private void tratamientoImagen(Imagen imagen) throws IOException {
        descripDetallada = new HashMap<>();
        rectangleOverlay.clearRectangles();
        if(imagen.rotarImagen(imagen,ivPicture)){
            textToSpeech.speak("La imagen está en horizontal, gire el dispositivo hacia la izquierda", TextToSpeech.QUEUE_FLUSH, null, null);
        }
        coord = new Coordenadas(ivPicture, imagen);
        tags = new HiloTag(imagen.getBase64(),firebase,imagen);
        tags.start();
        textToSpeech.speak("Obteniendo descripción", TextToSpeech.QUEUE_ADD, null, null);
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

    private Uri saveImageToExternalStorage(Bitmap imageBitmap) {
        String state = Environment.getExternalStorageState();
        if (!Environment.MEDIA_MOUNTED.equals(state)) {
            Log.e(TAG, "No se puede escribir en almacenamiento externo");
            return null;
        }

        // Crear un directorio para almacenar las imágenes
        File directory = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "NombreDelDirectorio");
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                Log.e(TAG, "Error al crear el directorio");
                return null;
            }
        }

        // Crear un nombre de archivo único para la imagen
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String fileName = "IMG_" + timeStamp + ".jpg";

        // Guardar la imagen en el directorio
        File imageFile = new File(directory, fileName);
        try {
            FileOutputStream fos = new FileOutputStream(imageFile);
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.close();
            // Devolver la URI del archivo guardado
            return Uri.fromFile(imageFile);
        } catch (IOException e) {
            Log.e(TAG, "Error al guardar la imagen en el almacenamiento externo: " + e.getMessage());
            return null;
        }
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
    public void onCallBack(Boolean bool){

    }


}