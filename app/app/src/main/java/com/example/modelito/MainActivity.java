package com.example.modelito;

import static android.webkit.ConsoleMessage.MessageLevel.LOG;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.app.appsearch.StorageInfo;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.nfc.Tag;
import android.util.Log;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;
import android.Manifest;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;
import com.google.mlkit.vision.objects.DetectedObject;
import com.google.mlkit.vision.objects.ObjectDetection;
import com.google.mlkit.vision.objects.ObjectDetector;
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions;
import com.google.mlkit.vision.objects.defaults.PredefinedCategory;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    ImageView ivPicture;
    TextView tvResult;
    TextView awita;
    Button btnChoosePicture;

    private static final int CAMERA_PERMISSION_CODE=223;
    private static final int READ_STORAGE_PERMISSION_CODE=144;
    private static final int WRITE_STORAGE_PERMISSION_CODE=144;
    private static final String TAG = "Numero Objetos detectados";

    ActivityResultLauncher<Intent> cameraLauncher;
    ActivityResultLauncher<Intent>galleryLauncher;

    private ObjectDetectorOptions options = new ObjectDetectorOptions.Builder()
                    .setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE)
                    .enableMultipleObjects()
                    .enableClassification()  // Optional
                    .build();
    ObjectDetector objectDetector = ObjectDetection.getClient(options);
    InputImage inputImage;
    ImageLabeler labeler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ivPicture = findViewById(R.id.ivPicture);
        tvResult = findViewById(R.id.tvResult);
        awita = findViewById(R.id.awita);
        btnChoosePicture = findViewById(R.id.btnChoosePicture);
        labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS);
        cameraLauncher=registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        Intent data = result.getData();
                        try {
                            Bitmap photo=(Bitmap) data.getExtras().get("data");
                            ivPicture.setImageBitmap(photo);
                            inputImage=InputImage.fromBitmap(photo,0);

                            processImage();

                        }catch (Exception e){
                            Log.d(TAG,"onActivityResult:"+ e.getMessage());
                        }
                    }
                }

        );
        galleryLauncher=registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        Intent data = result.getData();
                        try {
                            inputImage=InputImage.fromFilePath(MainActivity.this,data.getData());
                            ivPicture.setImageURI(data.getData());
                            processImage();
                        }catch (Exception e){
                            Log.d(TAG,"onActivityResult:"+ e.getMessage());
                        }
                    }
                }

        );
        btnChoosePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String [] options={"camera","gallery"};
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Elige una Opcion");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if( which==0){
                            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            cameraLauncher.launch(cameraIntent);
                        }else{
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
    }

    private void processImage() {
        labeler.process(inputImage)
                .addOnSuccessListener(new OnSuccessListener<List<ImageLabel>>(){
                    public void onSuccess(@NonNull @NotNull List<ImageLabel> imageLabels) {
                        String result="";
                        for(ImageLabel label:imageLabels){
                            result= result+"\n"+label.getText();
                        }
                        tvResult.setText(result);
                    }
                })
                .addOnFailureListener(new OnFailureListener(){
                    public void onFailure( @NonNull @NotNull Exception e) {
                        Log.d(TAG,"onFailure:"+ e.getMessage());
                    }
                });
        objectDetector.process(inputImage)
                .addOnSuccessListener(
                        new OnSuccessListener<List<DetectedObject>>() {
                            @Override
                            public void onSuccess(List<DetectedObject> detectedObjects) {
                                Log.d(TAG, String.valueOf(detectedObjects.size()));
                                for (DetectedObject detectedObject : detectedObjects) {
                                    Rect boundingBox = detectedObject.getBoundingBox();
                                    Integer trackingId = detectedObject.getTrackingId();
                                    String result="";
                                    for (DetectedObject.Label label : detectedObject.getLabels()) {
                                        String text = label.getText();
                                            Log.d(TAG, text);
                                            awita.setText(text);

                                        int index = label.getIndex();
                                        if (PredefinedCategory.FOOD_INDEX == index) {
                                            Log.d(TAG, "dos");
                                        }
                                    }

                                }
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d(TAG,"onFailure:"+ e.getMessage());
                            }
                        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        CheckPermission(Manifest.permission.CAMERA,CAMERA_PERMISSION_CODE);
    }
    public void CheckPermission(String permission, int requestCode){
        if(ContextCompat.checkSelfPermission(MainActivity.this,permission)== PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{permission},requestCode);
        }
    }
    public void onRequestPermissionsResult(int requestCode,@NonNull String[] permissions,@NonNull int[] grantResults){
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        if(requestCode== CAMERA_PERMISSION_CODE){
            if(!(grantResults.length>0 && grantResults[0]== PackageManager.PERMISSION_GRANTED)){
                Toast.makeText(MainActivity.this,"Permiso de cámara denegado",Toast.LENGTH_SHORT).show();
            }else{
                CheckPermission(Manifest.permission.READ_EXTERNAL_STORAGE, READ_STORAGE_PERMISSION_CODE);
            }
        }else if(requestCode== READ_STORAGE_PERMISSION_CODE){
            if(!(grantResults.length>0 && grantResults[0]== PackageManager.PERMISSION_GRANTED)){
                Toast.makeText(MainActivity.this,"Permiso de cámara denegado",Toast.LENGTH_SHORT).show();
            }else{
                CheckPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE,WRITE_STORAGE_PERMISSION_CODE);
            }
        } else if (requestCode== WRITE_STORAGE_PERMISSION_CODE) {
            if(!(grantResults.length>0 && grantResults[0]== PackageManager.PERMISSION_GRANTED)){
                Toast.makeText(MainActivity.this,"Permiso de cámara denegado",Toast.LENGTH_SHORT).show();
            }
        }
    }
}