package herringbone.com.dailyselfie;

import android.app.ListActivity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.facebook.login.LoginManager;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


public class SelfieListActivity extends ListActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final String FILE_LOCATION = "File_Location";
    static final String PROCESSED_FILE_LOCATION = "Processed_File_Location";
    static final String FILE_DESCRIPTION = "File_Description";
    static final String SELFIE_RECORD = "Selfie_Record";

    private SelfieAdapter mAdapter;
    String mCurrentPhotoPath;

    DatabaseHelper mydb;
    List<Selfie> selfies = null;
    String user = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Global login = (Global) getApplicationContext();
        if (login.getUser() == null || login.getUser().equals("")) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent); //TODO this doesn't work
        }

        mydb = new DatabaseHelper(this);


        mAdapter = new SelfieAdapter(getApplicationContext());
        setListAdapter(mAdapter);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            boolean fromNotification = extras.getBoolean("fromNotification");
            if (fromNotification) {
                dispatchTakePictureIntent();
            }
    //        userName = extras.getString("user_name");
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        final Global login = (Global) getApplicationContext();

        try {
            user = login.getUser();
            selfies = mydb.getAllSelfies(user);
            for (Selfie selfie : selfies) {
                mAdapter.add(selfie);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


//        File storageDir = Environment.getExternalStoragePublicDirectory(
//                Environment.DIRECTORY_PICTURES + "/Selfie/Original/" + user + "/");
//        if(storageDir.exists()) {
//            //Get all files
//            File[] files = storageDir.listFiles();
//            if (files != null) {
//                for (File file : files) {
//                    SelfieRecord record = new SelfieRecord();
//                    Date lastModDate = new Date(file.lastModified());
//                    record.setDateModified(lastModDate.toString());
//                    record.setmRecordLocation(file.getAbsolutePath());
//                    mAdapter.add(record);
//                }
//            }
//        }
    }

    @Override
    protected void onListItemClick (ListView l, View v, int position, long id) {
        Selfie clickedRecord = (Selfie)getListAdapter().getItem(position);
        Intent intent = new Intent(this, SelfieDetailActivity.class);
//        String location = clickedRecord.getmRecordLocation();
        String location = clickedRecord.getFilename();
        intent.putExtra(FILE_LOCATION, location);
        intent.putExtra(PROCESSED_FILE_LOCATION, clickedRecord.getProcessedFilename());
        intent.putExtra(FILE_DESCRIPTION, "");
        intent.putExtra(SELFIE_RECORD, clickedRecord.getId());
        startActivity(intent);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.custom_actionbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_camera) {
            dispatchTakePictureIntent();
            return true;
        }
        if (id == R.id.action_logout) {
            LoginManager.getInstance().logOut();
            finish();
            System.exit(0);
        }
        return super.onOptionsItemSelected(item);
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                ex.printStackTrace();
                // Error occurred while creating the File
            }

            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {;
            saveSelfie();
        }
    }

    private void saveSelfie() {
        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/120, photoH/90);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        //insert image into database
        mydb.insertSelfie(mCurrentPhotoPath, 0, 0, bitmap, user, "", new Date(), "");
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String directory = Environment.DIRECTORY_PICTURES + "/Selfie/Original/" + user + "/";
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(directory);
        if(!storageDir.exists()) {
            if(storageDir.mkdirs()) {
                Log.i("Daily Selfie", "Directory Created...");
            }else{
                Log.i("DailySelfie", "Directory Not Created ..........:");
            }
        }else{
            Log.i("DailySelfie", "Directory Exists ..........:");
        }
        File image = null;
        try {
            image = new java.io.File(Environment.getExternalStoragePublicDirectory(
                    directory) + imageFileName + ".jpg");
            image = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".jpg",         /* suffix */
                    storageDir      /* directory */
            );
        } catch(Exception e) {
            e.printStackTrace();
        }

        // Save a file: path for use with ACTION_VIEW intents
         mCurrentPhotoPath = image.getAbsolutePath();

        return image;
    }

}
