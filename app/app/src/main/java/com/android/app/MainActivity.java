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
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageButton;
import android.widget.ImageView;

import android.speech.tts.TextToSpeech;

import com.android.app.hilos.HiloTag;

import org.json.JSONException;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    //Variables de seleccion de imagen
    private static final int CAMERA_PERMISSION_CODE = 223;
    ActivityResultLauncher<Intent> cameraLauncher;
    ActivityResultLauncher<Intent> galleryLauncher;

    //TODO Apartir de aquí las variables estan colocadas
    //Variables xml
    private ImageView ivPicture;
    private TextView tvResult;
    private ImageButton btnChoosePicture;
    private ImageButton decirDescripcion;

    //Objetos necesarios
    private TextToSpeech textToSpeech;
    private FireFunctions firebase;
    private Imagen imagen;
    private Identificador identificador;
<<<<<<< HEAD
    private Traduccion descripTraducida;
    private HiloTag tags;
=======
>>>>>>> parent of 3b86046 (Hilo Tagging)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ivPicture = findViewById(R.id.ivPicture);
        tvResult = findViewById(R.id.tvResult);
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
                imagen = new Imagen(MainActivity.this, data.getData());
                firebase.callImagen(imagen.getImageUri()).addOnCompleteListener(task -> tvResult.setText(task.getResult().getTexto()));
            } catch (Exception e) {
                Log.d("TAG", "onActivityResult:" + e.getMessage());
            }
        }

        );
        galleryLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            Intent data = result.getData();
            try {
                imagen = new Imagen(MainActivity.this,data.getData());
                imagen.rotarImagen(data,ivPicture);//Rota si es necesario y muestra la imagen
                //ivPicture.setImageURI(data.getData());
                firebase.callImagen(imagen.getBase64()).addOnCompleteListener(task -> {
                    try {
                        firebase.callTags(imagen.getBase64()).addOnCompleteListener(new OnCompleteListener<Identificador>() {
                            @Override
                            public void onComplete(@NonNull Task<Identificador> task) {
                                identificador = task.getResult();
                            }
                        });
                        firebase.translatedImage(task.getResult().getTexto()).addOnCompleteListener(task2 -> {
                            //TODO llamar identificador
                            textTo = task2.getResult().getTexto();
                            tvResult.setText(task2.getResult().getTexto());
                        });
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            } catch (Exception e) {
                Log.d("TAG", "onActivityResult:" + e.getMessage());
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
        decirDescripcion.setOnClickListener(v -> textToSpeech.speak(descripTraducida.getTexto(), TextToSpeech.QUEUE_FLUSH, null, null));
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
                    textToSpeech.speak(identificador.getObject(x,y), TextToSpeech.QUEUE_FLUSH, null, null);

                    int width = ivPicture.getWidth();
                    int height = ivPicture.getHeight();
                    Rect rect = new Rect();
                    ivPicture.getHitRect(rect);
                    Bitmap bitmap = ivPicture.getDrawingCache();

                    if(y>= bitmap.getHeight()){
                        textToSpeech.speak("Estás fuera de la imagen", TextToSpeech.QUEUE_FLUSH, null, null);
                        return true;
                    }else{
                        int pixel = bitmap.getPixel(x, y);
                        if (Color.alpha(pixel) == 0) {
                            textToSpeech.speak("Estás fuera de la imagen", TextToSpeech.QUEUE_FLUSH, null, null);
                            return true;
                        }
                        if (rect.contains(x, y)) { //del imagavew general
                    int width = ivPicture.getWidth();
                    int height = ivPicture.getHeight();
                    textToSpeech.speak(identificador.getObject(x,y), TextToSpeech.QUEUE_FLUSH, null, null);

                        }
                        else {

                        }
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                };
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

<<<<<<< HEAD
    private void tratamientoImagen(Intent data) throws IOException {
        imagen = new Imagen(MainActivity.this,data.getData());
        imagen.rotarImagen(data,ivPicture);//Rota si es necesario y muestra la imagen
        tags = new HiloTag(imagen);
        tags.start();
        firebase.callImagen(imagen.getBase64()).addOnCompleteListener(task -> {
            try {
                firebase.translatedImage(task.getResult().getTexto()).addOnCompleteListener(task2 -> {
                    descripTraducida = task2.getResult();
                    textToSpeech.speak(descripTraducida.getTexto(), TextToSpeech.QUEUE_FLUSH, null, null);
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

=======
>>>>>>> parent of 3b86046 (Hilo Tagging)
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