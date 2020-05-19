package za.co.cspapp.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import za.co.cspapp.R;
import za.co.cspapp.UploadActivity;
import za.co.cspapp.adapters.PhotoViewAdapter;
import za.co.cspapp.objects.DealerObject;
import za.co.cspapp.objects.PhotoObject;
import za.co.cspapp.objects.StockObject;

import static android.support.v4.content.ContextCompat.checkSelfPermission;

public class PhotoFragment extends Fragment {

    private static final String TAG = "PhotoFragment";
    private DealerObject current_dealer;
    private ArrayList<DealerObject> dealers;
    private ArrayList<PhotoObject> photos;
    private StockObject stock;
    private int stock_id;
    private LinearLayoutManager lLayout;
    private static final int TAKE_PICTURE = 100;
    private static final int SELECT_PICTURE = 101;
//    private static final int CAMERA_REQUEST = 1888;
    String imageFilePath;

    ViewGroup container;

    public PhotoFragment(){

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            Bundle bundle = getArguments();
            stock = (StockObject) bundle.getSerializable("stock");
            dealers = (ArrayList<DealerObject>) bundle.getSerializable("dealers");
            current_dealer = (DealerObject) bundle.getSerializable("current_dealer");
            stock_id = stock.getId();
            photos = stock.getPhotos();
        }
        handlePermission();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.activity_photo, null);
        return root;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        FloatingActionButton fab = getView().findViewById(R.id.fab);
        FloatingActionButton select_fab = getView().findViewById(R.id.select_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent pictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if(pictureIntent.resolveActivity(getActivity().getPackageManager()) != null){
                    //Create a file to store the image
                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                    } catch (IOException ex) {
                        // Error occurred while creating the File ...
                    }
                    if (photoFile != null) {
                        Uri photoURI = FileProvider.getUriForFile(getActivity(),"za.co.cspapp.provider", photoFile);
                        pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                        if (checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(getActivity(), new String[] { Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE }, 0);
                        } else {
                            startActivityForResult(pictureIntent, TAKE_PICTURE);
                        }
                    }
                }
            }
        });

        select_fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);
            }
        });

        showPhotos(photos);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == TAKE_PICTURE) {
            //don't compare the data to null, it will always come as null because we are providing a file URI,
            // so load with the imageFilePath we obtained before opening the cameraIntent
            launchUploadActivity(true);
        }
        if (requestCode == SELECT_PICTURE) {
            // Get the url from data
            final Uri selectedImageUri = data.getData();
            if (null != selectedImageUri) {
                // Get the path from the Uri
                imageFilePath = getPathFromURI(selectedImageUri);
                launchUploadActivity(true);
            }
        }
    }

    /* Get the real path from the URI */
    public String getPathFromURI(Uri contentUri) {
        String filePath = "";
        String fileId = DocumentsContract.getDocumentId(contentUri);
        // Split at colon, use second item in the array
        String id = fileId.split(":")[1];
        String[] column = {MediaStore.Images.Media.DATA};
        String selector = MediaStore.Images.Media._ID + "=?";
        Cursor cursor = getContext().getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                column, selector, new String[]{id}, null);
        int columnIndex = cursor.getColumnIndex(column[0]);
        if (cursor.moveToFirst()) {
            filePath = cursor.getString(columnIndex);
        }
        cursor.close();
        return filePath;
    }


    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                        Locale.getDefault()).format(new Date());
        String imageFileName = "IMG_" + timeStamp + "_";
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        imageFilePath = image.getAbsolutePath();
        return image;
    }

    private void launchUploadActivity(boolean isImage){

        Bundle data = new Bundle();
        data.putSerializable("stock", stock);
        data.putSerializable("dealers", dealers);
        data.putSerializable("current_dealer", current_dealer);

        Intent i = new Intent(getActivity(), UploadActivity.class);
        i.putExtras(data);
        i.putExtra("filePath", imageFilePath);
        i.putExtra("isImage", isImage);
        i.putExtra("stock_id", stock_id);
        startActivity(i);
    }

    private void handlePermission() {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return;
        }

        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            //ask for permission
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    SELECT_PICTURE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case SELECT_PICTURE:
                for (int i = 0; i < permissions.length; i++) {
                    String permission = permissions[i];
                    if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        boolean showRationale = ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), permission);
                        if (showRationale) {
                            //  Show your own message here
                        } else {
                            showSettingsAlert();
                        }
                    }
                }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    private void showPhotos(ArrayList<PhotoObject> photos) {
        List<PhotoObject> photosList = photos;

        lLayout = new GridLayoutManager(getActivity(), 4);

        RecyclerView rView = getView().findViewById(R.id.recycler_view);
        rView.setHasFixedSize(true);
        rView.setLayoutManager(lLayout);

        PhotoViewAdapter rcAdapter = new PhotoViewAdapter(stock, dealers, current_dealer);
        rView.setAdapter(rcAdapter);

    }

    private void showSettingsAlert() {
        AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
        alertDialog.setTitle("Alert");
        alertDialog.setMessage("App needs to access the Camera.");
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "DONT ALLOW",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        //finish();
                    }
                });
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "SETTINGS",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        openAppSettings(getActivity());
                    }
                });
        alertDialog.show();
    }

    public static void openAppSettings(final Activity context) {
        if (context == null) {
            return;
        }
        final Intent i = new Intent();
        i.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        i.addCategory(Intent.CATEGORY_DEFAULT);
        i.setData(Uri.parse("package: za.co.cspapp"));
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        context.startActivity(i);
    }
}
