package com.example.app;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import android.app.Activity;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int SELECT_MULTIPLE_IMAGES = 1;
    private EditText textField_message;
    private Button button_send_post;
    private TextView textView_response;
    private String url="http://192.168.1.54:5000";
    private String POST="POST";
    private String GET="GET";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textField_message=findViewById(R.id.txtField_message);
        button_send_post=findViewById(R.id.button_send_post);

        /*making a post request.*/
        button_send_post.setOnClickListener(view -> {

            //get the test in the text field.In this example you should type your name here
            openGallery();
            String text=textField_message.getText().toString();
            if(text.isEmpty()){
                textField_message.setError("This cannot be empty for post request");
            }else {
                /*if name text is not empty,then call the function to make the post request*/
                sendRequest(POST, "getname", "name", text);
            }
        });
    }
    void sendRequest(String type,String method,String paramname,String param){

        /* if url is of our get request, it should not have parameters according to our implementation.
         * But our post request should have 'name' parameter. */
        String fullURL=url+"/"+method+(param==null?"":"/"+param);
        Request request;

        OkHttpClient client = new OkHttpClient().newBuilder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS).build();

        /* If it is a post request, then we have to pass the parameters inside the request body*/
        if(type.equals(POST)){
            RequestBody formBody = new FormBody.Builder()
                    .add(paramname, param)
                    .build();

            request=new Request.Builder()
                    .url(fullURL)
                    .post(formBody)
                    .build();
        }else{
            /*If it's our get request, it doen't require parameters, hence just sending with the url*/
            request = new Request.Builder()
                    .url(fullURL)
                    .build();
        }
        /* this is how the callback get handled */
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {

                // Read data on the worker thread
                final String responseData = response.body().string();

                // Run view-related code back on the main thread.
                // Here we display the response message in our text view
                MainActivity.this.runOnUiThread(() -> textView_response.setText(responseData));
            }
        });
    }
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Selecciona una imagen"), PICK_IMAGE_REQUEST);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            // La imagen seleccionada se encuentra en data.getData()
            Uri selectedImageUri = data.getData();

            // Realiza acciones con la imagen seleccionada, como mostrarla en una ImageView
            ImageView imageView = findViewById(R.id.imageView);
            imageView.setImageURI(selectedImageUri);
        }
    }
}