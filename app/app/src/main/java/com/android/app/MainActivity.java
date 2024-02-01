package com.android.app;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageButton;
import android.widget.ImageView;

import android.speech.tts.TextToSpeech;

import com.android.app.Hilo.HiloTag;

import org.json.JSONException;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private static final int CAMERA_PERMISSION_CODE = 223;
    private static final String TAG = "Numero Objetos detectados";

    ActivityResultLauncher<Intent> cameraLauncher;
    ActivityResultLauncher<Intent> galleryLauncher;
    String textTo;

    private TextToSpeech textToSpeech;

    //TODO Apartir de aquí las variables estan colocadas
    private FireFunctions firebase;
    //Variables xml
    private ImageView ivPicture;
    private ImageButton btnChoosePicture;
    private ImageButton decirDescripcion;

    //Objetos necesarios
    private Imagen imagen;
    private Identificador identificador;
    private HiloTag tags;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ivPicture = findViewById(R.id.ivPicture);
        decirDescripcion = findViewById(R.id.decirDescripcion);
        btnChoosePicture = findViewById(R.id.btnChoosePicture);
        firebase = new FireFunctions();
        //Inicializar el text To speech
        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                // Text-to-Speech está listo para su uso
            } else {
                // Algo salió mal, Text-to-Speech no está disponible
                Log.e("TextToSpeech", "Initialization failed");
            }
        });

        cameraLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            Intent data = result.getData();
            try {
                Bitmap photo = (Bitmap) data.getExtras().get("data");
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
                tratamientoImagen(data);
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
        //TODO: Sincronizarlo bien para que haya respuesta o hacer que diga que aun no hay respuesta
        //algo de esso (añadirlo al onComplete alomejor)
        decirDescripcion.setOnClickListener(v -> textToSpeech.speak(textTo, TextToSpeech.QUEUE_FLUSH, null, null));
    }

    public boolean onTouchEvent(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN :
                // Acción cuando se presiona la pantalla
                try {
                    tags.join();
                    identificador = tags.getIdentificador();

                    int width = ivPicture.getWidth();
                    int height = ivPicture.getHeight();

                    ivPicture.setEnabled(false);

                    Rect rect = new Rect();
                    ivPicture.getHitRect(rect);
                    ivPicture.setDrawingCacheEnabled(true);
                    ivPicture.buildDrawingCache();
                    Bitmap bitmap = ivPicture.getDrawingCache();
                    if(y>= height){
                        textToSpeech.speak("Estás fuera de la imagen", TextToSpeech.QUEUE_FLUSH, null, null);
                        return true;
                    }else{
                        int pixel = bitmap.getPixel(x, y);
                        if (Color.alpha(pixel) == 0) {
                            textToSpeech.speak("Estás fuera de la imagen", TextToSpeech.QUEUE_FLUSH, null, null);
                            return true;
                        }else{
                            textToSpeech.speak(identificador.getObject(x,y), TextToSpeech.QUEUE_FLUSH, null, null);
                        }
                        if (rect.contains(x, y)) { //del imagavew general
                        } else {
                        }
                    }


                } catch (JSONException e) {
                    throw new RuntimeException(e);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                ;
                break;
            case MotionEvent.ACTION_MOVE:
                // Acción cuando se mueve el dedo sobre la pantalla

                break;
            case MotionEvent.ACTION_UP:
                // Acción cuando se levanta el dedo de la pantalla
                break;
        }

        return true;
    }

    private void tratamientoImagen(Intent data) throws IOException {
        imagen = new Imagen(MainActivity.this,data.getData());
        imagen.rotarImagen(data,ivPicture);//Rota si es necesario y muestra la imagen
        firebase.callImagen(imagen.getBase64()).addOnCompleteListener(task -> {
            try {
                tags = new HiloTag(imagen);
                tags.start();
                firebase.translatedImage(task.getResult().getTexto()).addOnCompleteListener(task2 -> {
                    textTo = task2.getResult().getTexto();
                    textToSpeech.speak(textTo, TextToSpeech.QUEUE_FLUSH, null, null);
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