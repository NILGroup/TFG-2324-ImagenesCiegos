package com.android.app;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.Rotate;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import android.speech.tts.TextToSpeech;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

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
    private Descripcion descripcion;
    private Traduccion traduccion;
    private Identificador identificador;

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
        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    // Text-to-Speech está listo para su uso
                } else {
                    // Algo salió mal, Text-to-Speech no está disponible
                    Log.e("TextToSpeech", "Initialization failed");
                }
            }
        });

        cameraLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        Intent data = result.getData();
                        try {
                            Bitmap photo = (Bitmap) data.getExtras().get("data");
                            ivPicture.setImageBitmap(photo);
                            imagen = new Imagen(MainActivity.this, data.getData());
                            firebase.callImagen(imagen.getImageUri()).addOnCompleteListener(new OnCompleteListener<Descripcion>() {
                                @Override
                                public void onComplete(@NonNull Task<Descripcion> task) {

                                    tvResult.setText(task.getResult().getTexto());
                                }
                            });
                        } catch (Exception e) {
                            Log.d(TAG, "onActivityResult:" + e.getMessage());
                        }
                    }
                }

        );
        galleryLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        Intent data = result.getData();
                        try {
                            rotarImagen(data);//Rota si es necesario y muestra la imagen
                            //ivPicture.setImageURI(data.getData());
                            imagen = new Imagen(MainActivity.this,data.getData());
                            firebase.callImagen(imagen.getBase64()).addOnCompleteListener(new OnCompleteListener<Descripcion>() {
                                @Override
                                public void onComplete(@NonNull Task<Descripcion> task) {
                                    try {
                                        firebase.translatedImage(task.getResult().getTexto()).addOnCompleteListener(new OnCompleteListener<Traduccion>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Traduccion> task2) {
                                                textTo = task2.getResult().getTexto();
                                                tvResult.setText(task2.getResult().getTexto());
                                            }
                                        });
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                            });
                        } catch (Exception e) {
                            Log.d(TAG, "onActivityResult:" + e.getMessage());
                        }
                    }
                }

        );
        btnChoosePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String[] options = {"camara", "galeria"};
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Elige una Opción");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            cameraLauncher.launch(cameraIntent);
                        } else {
                            Intent storageIntent = new Intent();
                            storageIntent.setType("image/*");
                            storageIntent.setAction(Intent.ACTION_GET_CONTENT);
                            galleryLauncher.launch(storageIntent);
                        }
                    }
                });
                builder.show();
            }
        });
        //TODO: Sincronizarlo bien para que haya respuesta o hacer que diga que aun no hay respuesta
        //algo de esso (añadirlo al onComplete alomejor)
        decirDescripcion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textToSpeech.speak(textTo, TextToSpeech.QUEUE_FLUSH, null, null);
            }
        });
    }

    private void rotarImagen(Intent data) throws IOException {
        sacarRelacion(data);
        if (sacarRelacion(data))
            Glide.with(getApplicationContext()).load(data.getData()).apply(new RequestOptions().transform(new Rotate(90))) // Rotación de 90 grados
                    .into(ivPicture);
        else ivPicture.setImageURI(data.getData());
    }

    private boolean sacarRelacion(Intent data) throws IOException { //Ve si una imagen tiene que ir en vertical o en horizontal
        ImageDecoder.Source source = ImageDecoder.createSource(this.getContentResolver(), data.getData());
        Bitmap bitmap = ImageDecoder.decodeBitmap(source);
        int imageHeight = bitmap.getHeight();
        int imageWidth = bitmap.getWidth();
        int ratio = imageHeight / imageWidth;
        return ratio<1;
    }

    public void cortarImagen(Bitmap b, int[] coordenadas) { // Recortar el objeto deseado
        ivPicture.setImageBitmap(Bitmap.createBitmap(b, coordenadas[0], coordenadas[1], coordenadas[2], coordenadas[3]));
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