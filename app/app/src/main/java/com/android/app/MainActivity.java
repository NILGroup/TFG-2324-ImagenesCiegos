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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.Rotate;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.database.DatabaseReference;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private FirebaseFunctions fFunc = FirebaseFunctions.getInstance();
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageReference = storage.getReference();
    private DatabaseReference root = FirebaseDatabase.getInstance().getReference().child("imagenes");
    private Uri imageUri;
    ImageView ivPicture;
    TextView tvResult;
    TextView awita;
    Button btnChoosePicture;
    Button decirDescripcion;

    private static final int CAMERA_PERMISSION_CODE = 223;
    private static final int READ_STORAGE_PERMISSION_CODE = 144;
    private static final int WRITE_STORAGE_PERMISSION_CODE = 144;
    private static final String TAG = "Numero Objetos detectados";

    ActivityResultLauncher<Intent> cameraLauncher;
    ActivityResultLauncher<Intent> galleryLauncher;
    String textToSpeech;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ivPicture = findViewById(R.id.ivPicture);
        tvResult = findViewById(R.id.tvResult);
        decirDescripcion = findViewById(R.id.decirDescripcion);
        awita = findViewById(R.id.awita);

        btnChoosePicture = findViewById(R.id.btnChoosePicture);
        cameraLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        Intent data = result.getData();
                        try {
                            Bitmap photo = (Bitmap) data.getExtras().get("data");
                            ivPicture.setImageBitmap(photo);
                            imageUri = data.getData();
                            callImagen(imageUri.toString()).addOnCompleteListener(new OnCompleteListener<String>() {
                                @Override
                                public void onComplete(@NonNull Task<String> task) {

                                    awita.setText(task.getResult());
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
                            imageUri = data.getData();

                            String texto = codificar(imageUri);
                            callImagen(texto).addOnCompleteListener(new OnCompleteListener<String>() {
                                @Override
                                public void onComplete(@NonNull Task<String> task) {
                                    String salida;
                                    salida=convertirString(task.getResult());
                                    awita.setText(salida);
                                    try {
                                        translatedImage(salida).addOnCompleteListener(new OnCompleteListener<String>() {
                                            @Override
                                            public void onComplete(@NonNull Task<String> task2) {
                                                String salida2;
                                                salida2 = convertirString(task2.getResult());
                                                textToSpeech = salida2;
                                                awita.setText(salida2);
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
                String[] options = {"camera", "gallery"};
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Elige una Opcion");
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
                decirDescripcion.setContentDescription(textToSpeech);
            }
        });
    }

    private String convertirString(String result) {
        String[] parts = result.split(":");
        return parts[1].replace("]"," ");
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
        //Con esto se puede recortar la imagen.
        //ivPicture.setImageBitmap(Bitmap.createBitmap(bitmap,0,0,imageWidth/2,imageHeight/2));
        int ratio = imageHeight / imageWidth;
        if (ratio >= 1) {
            return false;
        } else {
            return true;
        }
    }

    private String codificar(Uri uri) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        InputStream inputStream = getContentResolver().openInputStream(uri);
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            byteArrayOutputStream.write(buffer, 0, bytesRead);
        }
        byte[] byteArray = byteArrayOutputStream.toByteArray();

        // Convertir el array de bytes a base64
        String base64Image = Base64.getEncoder().encodeToString(byteArray);
        return base64Image;
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
    /*public void onRequestPermissionsResult(int requestCode,@NonNull String[] permissions,@NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Toast.makeText(MainActivity.this, "Permiso de cámara denegado1", Toast.LENGTH_SHORT).show();
            } else {
                CheckPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE, READ_STORAGE_PERMISSION_CODE);
            }
        } else if (requestCode == READ_STORAGE_PERMISSION_CODE) {
            if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Toast.makeText(MainActivity.this, "Permiso de cámara denegado2", Toast.LENGTH_SHORT).show();
            } else {
                CheckPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, WRITE_STORAGE_PERMISSION_CODE);
            }
        } else if (requestCode == WRITE_STORAGE_PERMISSION_CODE) {
            if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Toast.makeText(MainActivity.this, "Permiso de cámara denegado3", Toast.LENGTH_SHORT).show();
            }
        }
    }*/

    //funcion que llama a la cloud function
    private Task<String> callImagen(String name) throws IOException {

        Map<String, Object> data = new HashMap<>();
        data.put("url", name);

        return fFunc.getHttpsCallable("descripImagen").call(data).continueWith(new Continuation<HttpsCallableResult, String>() {
            @Override
            public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                return (String) task.getResult().getData();
            }
        });
    }

    private Task<String> translatedImage(String name) throws IOException {

        Map<String, Object> data = new HashMap<>();
        data.put("texto", name);

        return fFunc.getHttpsCallable("traducDescrip").call(data).continueWith(new Continuation<HttpsCallableResult, String>() {
            @Override
            public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                return (String) task.getResult().getData();
            }
        });
    }


}