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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import android.speech.tts.TextToSpeech;

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
    private TextView tvResult;
    private ImageButton btnChoosePicture;
    private ImageButton decirDescripcion;

    //Objetos necesarios
    private Imagen imagen;

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
                imagen = new Imagen(MainActivity.this, data.getData());
                firebase.callImagen(imagen.getImageUri()).addOnCompleteListener(task -> tvResult.setText(task.getResult().getTexto()));
            } catch (Exception e) {
                Log.d(TAG, "onActivityResult:" + e.getMessage());
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
                        firebase.translatedImage(task.getResult().getTexto()).addOnCompleteListener(task2 -> {
                            //TODO llamar identificador
                            //firebase.callTags(imagen.getBase64()).getResult();
                            textTo = task2.getResult().getTexto();
                            tvResult.setText(task2.getResult().getTexto());
                        });
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
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