package za.co.cspapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.IOException;

import za.co.cspapp.utils.CSPMultiPartEntity;
import za.co.cspapp.utils.CSPMultiPartEntity.ProgressListener;

public class UploadActivity extends Activity {
    private static final String TAG = UploadActivity.class.getSimpleName();
    private static final String SHARED_PREFS = "MyDetails";
    private static final String API_UPLOAD_PHOTO = "https://iris.carsalesportal.co.za/api/photo/upload/";

    private ProgressBar progressBar;
    private String filePath = null;
    private TextView txtPercentage;
    private ImageView imgPreview;
    private Bundle bundle;
    int stock_id;
    long totalSize = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);
        txtPercentage = findViewById(R.id.txtPercentage);
        Button btnUpload = findViewById(R.id.btnUpload);
        Button btnCancel = findViewById(R.id.btnCancel);
        progressBar = findViewById(R.id.progressBar);
        imgPreview = findViewById(R.id.imgPreview);

        Intent i = getIntent();
        bundle = getIntent().getExtras();
        filePath = i.getStringExtra("filePath");
        stock_id = i.getIntExtra("stock_id", stock_id);
        boolean isImage = i.getBooleanExtra("isImage", true);

        if (filePath != null) {
            // Displaying the image or video on the screen
            previewMedia(isImage);
        } else {
            Toast.makeText(getApplicationContext(),"Sorry, file path is missing!", Toast.LENGTH_LONG).show();
        }

        btnUpload.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // uploading the file to server
                new UploadFileToServer().execute();
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // uploading the file to server
                backToPhotos();
            }
        });

    }

    /**
     * Displaying captured image/video on the screen
     * */
    private void previewMedia(boolean isImage) {
        // Checking whether captured media is image or video
        if (isImage) {
            imgPreview.setVisibility(View.VISIBLE);
            // bitmap factory
            BitmapFactory.Options options = new BitmapFactory.Options();

            // down sizing image as it throws OutOfMemory Exception for larger images
            options.inSampleSize = 8;

            final Bitmap bitmap = BitmapFactory.decodeFile(filePath, options);

            imgPreview.setImageBitmap(bitmap);
        } else {
            imgPreview.setVisibility(View.GONE);
        }
    }

    public void backToPhotos(){
        Intent i = new Intent(UploadActivity.this, StockActivity.class);
        i.putExtras(bundle);
        i.putExtra("reload", true);
        startActivity(i);
    }
    /**
     * Uploading the file to server
     * */
    private class UploadFileToServer extends AsyncTask<Void, Integer, String> {
        @Override
        protected void onPreExecute() {
            // setting progress bar to zero
            progressBar.setProgress(0);
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            // Making progress bar visible
            progressBar.setVisibility(View.VISIBLE);

            // updating progress bar value
            progressBar.setProgress(progress[0]);

            // updating percentage value
            txtPercentage.setText(String.valueOf(progress[0]) + "%");
        }

        @Override
        protected String doInBackground(Void... params) {
            return uploadFile();
        }

        @SuppressWarnings("deprecation")
        private String uploadFile() {

            String responseString = null;

            SharedPreferences settings = getApplicationContext().getSharedPreferences(SHARED_PREFS, 0);
            String api_token = settings.getString("api_token", "0");

            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(API_UPLOAD_PHOTO + Integer.toString(stock_id));
            httppost.addHeader("Authorization", api_token);

            try {
                CSPMultiPartEntity entity = new CSPMultiPartEntity(
                        new ProgressListener() {

                            @Override
                            public void transferred(long num) {
                                publishProgress((int) ((num / (float) totalSize) * 100));
                            }
                        });

                File sourceFile = new File(filePath);

                // Adding file data to http body
                entity.addPart("image", new FileBody(sourceFile));

                totalSize = entity.getContentLength();
                httppost.setEntity(entity);
                httppost.setHeader("Authorization", api_token);

                // Making server call
                HttpResponse response = httpclient.execute(httppost);
                HttpEntity r_entity = response.getEntity();

                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == 200) {
                    // Server response
                    responseString = EntityUtils.toString(r_entity);
                    backToPhotos();
                } else {
                    responseString = "Error occurred! Http Status Code: "
                            + statusCode;
                }

            } catch (ClientProtocolException e) {
                responseString = e.toString();
            } catch (IOException e) {
                responseString = e.toString();
            }

            return responseString;

        }

        @Override
        protected void onPostExecute(String result) {
            // showing the server response in an alert dialog
            showAlert(result);

            super.onPostExecute(result);
        }

    }

    /**
     * Method to show alert dialog
     * */
    private void showAlert(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message).setTitle("Response from Servers")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // do nothing
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

}