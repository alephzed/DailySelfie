package herringbone.com.dailyselfie;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;

import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
//import com.facebook.share.ShareApi;
//import com.facebook.share.model.SharePhoto;
//import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import herringbone.com.dailyselfie.retrofit.SelfieClient;
import herringbone.com.dailyselfie.retrofit.ServiceGenerator;
import retrofit.client.Response;
import retrofit.mime.TypedFile;

public class SelfieDetailActivity extends Activity {

    EditText editText;
    private PhotoProcessorTask photoProcessorTask = null;
    TypedFile typedFile;
    ImageView mSelfieLarge;
    CheckBox gaussian;
    CheckBox charcoal;
    DatabaseHelper mydb;
    Integer selfieId;
    Selfie selfie;
    Bitmap screenPic;
//    ShareDialog shareDialog;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.selfie_detail_view);
        Bundle extras = getIntent().getExtras();
        mydb = new DatabaseHelper(this);
//        FacebookSdk.sdkInitialize(getApplicationContext());
//        shareDialog = new ShareDialog(this);

        if (extras != null) {
            selfieId = extras.getInt("Selfie_Record");
            selfie = null;
            String filePath = null;
            String filename = null;
            try {
                selfie = mydb.getData(selfieId);
                filename = selfie.getFilename();
                String processedFilename = selfie.getProcessedFilename();
                if (processedFilename != null && !processedFilename.equals("")) {
                    filePath = processedFilename;
                } else {
                    filePath = filename;
                }
               // String filePath = extras.getString("File_Location");
                String description = extras.getString("File_Description");
            } catch (Exception e) {
                e.printStackTrace();
            }
            mSelfieLarge = (ImageView) findViewById(R.id.fullsize);
            editText = (EditText)findViewById(R.id.editText1);
            if (selfie.getDescription() != null)
                editText.setText(selfie.getDescription());
            screenPic = SelfieAdapter.setPic(filePath, mSelfieLarge.getLayoutParams().width, mSelfieLarge.getLayoutParams().height);
            typedFile = new TypedFile("image/jpeg", new File(filename));
            mSelfieLarge.setImageBitmap(screenPic);
            gaussian = (CheckBox)findViewById(R.id.blurCheckBox);
            charcoal = (CheckBox)findViewById(R.id.charcoalCheckBox);
            if (selfie.getCharcoal() > 0) {
                charcoal.setChecked(true);
            }
            if (selfie.getGaussian() > 0) {
                gaussian.setChecked(true);
            }
        }

        Button cancelButton = (Button) findViewById(R.id.cancel_action);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DoCancel(v);
            }
        });

        Button saveButton= (Button) findViewById(R.id.save_action);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DoSave(v);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.detail_actionbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //TODO implement facebook sharing
//        if (id == R.id.action_share) {
//
//            if (ShareDialog.canShow(SharePhotoContent.class)) {
//                SharePhoto photo = new SharePhoto.Builder()
//                        .setBitmap(screenPic)
//                        .build();
//                SharePhotoContent content = new SharePhotoContent.Builder()
//                        .addPhoto(photo)
//                        .build();
//            ShareApi.share(content, null);
//                shareDialog.show(content);
//            }
//            return true;
//        }
        if (id == R.id.action_logout) {
            LoginManager.getInstance().logOut();
            finish();
            System.exit(0);
        }
        return super.onOptionsItemSelected(item);
    }

    protected void updateResults(String results) {
        editText.setText(results);
    }


    private void DoCancel(View v) {
        Intent intent = new Intent(this, SelfieListActivity.class);
        startActivity(intent);
    }

    private void DoSave(View v) {
        //TODO save routine to call server for image processing
        //TODO save metadata to the database
        //TODO update image on UI thread with changes
        photoProcessorTask = new PhotoProcessorTask(charcoal.isChecked(), gaussian.isChecked(), editText.getText().toString());
        photoProcessorTask.execute();
    }

    private class PhotoProcessorTask extends AsyncTask<ImageView, String, Bitmap> {

        private static final String TAG = "PhotoProcessorTask";
        private static final int DELAY = 5000; // 5 SECONDS
        private static final int RANDOM_MULTIPLIER = 10;
        private String charcoal, gaussian, description;

        public PhotoProcessorTask(Boolean charcoal, Boolean gaussian, String description) {
            this.charcoal=charcoal.toString();
            this.gaussian=gaussian.toString();
            this.description = description;
        }

        @Override
        protected void onPreExecute() {
            Log.v(TAG, "STARTING THE RANDOM NUMBER TASK");
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(String... VALUES) {
            Log.v(TAG, "REPORTING BACK FROM THE RANDOM NUMBER TASK");
            updateResults(VALUES[0].toString());
            super.onProgressUpdate(VALUES);
        }

        @Override
        protected void onCancelled(Bitmap RESULT) {
            Log.v(TAG, "CANCELLED THE RANDOM NUMBER TASK");
            super.onCancelled(RESULT);
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            mSelfieLarge.setImageBitmap(result);
        }

        @Override
        protected Bitmap doInBackground(ImageView... params) {
            Bitmap bitmap = null;
            try {
                //Call the webservice
                SelfieClient service = ServiceGenerator.createService(SelfieClient.class, "wimpydimple", "foo");
                Response response = service.process("{\"gaussianBlur\":\"" + gaussian + "\", \"charcoal\":\"" + charcoal + "\"}", typedFile);
                InputStream photo = response.getBody().in();
                byte[] retrievedFile = IOUtils.toByteArray(photo);
                bitmap = BitmapFactory.decodeByteArray(retrievedFile, 0,
                        retrievedFile.length);

                //Save the photo to the file system
                final Global login = (Global) getApplicationContext();
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String directory = Environment.DIRECTORY_PICTURES + "/Selfie/Processed/" + login.getUser() + "/";
                String imageFileName = "JPEG_" + timeStamp + "_";
                File storageDir = Environment.getExternalStoragePublicDirectory(directory);
                if(!storageDir.exists()) {
                    storageDir.mkdirs();
                }
                File file = new java.io.File(storageDir, imageFileName+ ".jpg");
                 file = File.createTempFile(
                        imageFileName,  /* prefix */
                        ".jpg",         /* suffix */
                        storageDir      /* directory */
                );
               // if (file.exists ()) file.delete ();
                try {
                    FileOutputStream out = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                    out.flush();
                    out.close();

                } catch (Exception e) {
                    e.printStackTrace();
                }
                Integer gaussianValue = 0;
                if (gaussian.equals("true")) {
                    gaussianValue = 1;
                }
                Integer charcoalValue = 0;
                if (charcoal.equals("true")) {
                    charcoalValue = 1;
                }
                Bitmap thumbPic = SelfieAdapter.setPic(file.getAbsolutePath(), 120, 90 );

                mydb.updateSelfie(selfie.getId(), selfie.getFilename(), charcoalValue, gaussianValue, thumbPic, login.getUser(), description,
                        selfie.getRecordDate(), file.getAbsolutePath());
//                Log.v(TAG, "DOING WORK IN RANDOM NUMBER TASK");
//                String TEXT = "";
//                while (true) {
//                    if (isCancelled()) {
//                        break;
//                    }
//                    int RANDNUM = (int) (Math.random() * RANDOM_MULTIPLIER);
//                    TEXT = String.format(getString(R.string.service_msg), RANDNUM);
//                    publishProgress(TEXT);
//                    try {
//                        Thread.sleep(DELAY);
//                    } catch (InterruptedException E) {
//                        Log.v(TAG, "INTERRUPTING THE RANDOM NUMBER TASK");
//                    }
//                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return bitmap;
        }
    }
}
