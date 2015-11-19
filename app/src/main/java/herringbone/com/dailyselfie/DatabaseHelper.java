package herringbone.com.dailyselfie;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;

import java.io.ByteArrayOutputStream;
import java.sql.Blob;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "Selfie.db";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 4);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table selfies " +
                "(id integer primary key autoincrement, filename text, charcoal integer default 0, "
                + "gaussian integer default 0, thumb blob, user text, description text, recordDate text, "
                +"processedFilename)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS selfies");
        onCreate(db);
    }

    public boolean insertSelfie(String filename, Integer charcoal, Integer gaussian, Bitmap thumbnail, String username,
                                String description, Date recordDate, String processedFilename) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] thumbnailBitmapBytes = stream.toByteArray();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String date = sdf.format(recordDate);
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("filename", filename);
        contentValues.put("charcoal", charcoal);
        contentValues.put("gaussian", gaussian);
        contentValues.put("thumb", thumbnailBitmapBytes);
        contentValues.put("user", username);
        contentValues.put("description", description);
        contentValues.put("recordDate", date);
        contentValues.put("processedFilename", processedFilename);
        db.insert("selfies", null, contentValues);
        return true;
    }

    public Selfie getData(int id) throws Exception {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from selfies where id="+id+"", null );
        res.moveToFirst();
        Selfie selfie = getSelfie(res);

        return selfie;
    }

    public boolean updateSelfie (Integer id, String filename, Integer charcoal, Integer gaussian, Bitmap thumbnail, String username,
                                String description, Date recordDate, String processedFilename) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] thumbnailBitmapBytes = stream.toByteArray();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String date = sdf.format(recordDate);
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("filename", filename);
        contentValues.put("charcoal", charcoal);
        contentValues.put("gaussian", gaussian);
        contentValues.put("thumb", thumbnailBitmapBytes);
        contentValues.put("user", username);
        contentValues.put("description", description);
        contentValues.put("recordDate", date);
        contentValues.put("processedFilename", processedFilename);
        db.update("selfies", contentValues, "id = ? ", new String[] { Integer.toString(id) } );
        return true;
    }

    public Integer deleteSelfie (Integer id)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete("contacts",
                "id = ? ",
                new String[] { Integer.toString(id) });
    }

    public ArrayList<Selfie> getAllSelfies(String user) throws Exception {
        ArrayList<Selfie> array_list = new ArrayList<Selfie>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from selfies where user = ? ", new String[] { user } );
        res.moveToFirst();
        while(res.isAfterLast() == false){
            Selfie selfie = getSelfie(res);

            array_list.add(selfie);
            res.moveToNext();
        }
        return array_list;
    }

    @NonNull
    private Selfie getSelfie(Cursor res) throws ParseException {
        Selfie selfie = new Selfie();
        selfie.setId(res.getInt(res.getColumnIndex("id")));
        selfie.setFilename(res.getString(res.getColumnIndex("filename")));
        selfie.setCharcoal(res.getInt(res.getColumnIndex("charcoal")));
        selfie.setGaussian(res.getInt(res.getColumnIndex("gaussian")));
        byte[] image = res.getBlob(res.getColumnIndex("thumb"));
        BitmapFactory.Options options = new BitmapFactory.Options();
        Bitmap thumbnail = BitmapFactory.decodeByteArray(image, 0, image.length, options);
        selfie.setThumbnail(thumbnail);
        selfie.setDescription(res.getString(res.getColumnIndex("description")));
        String recordDate = res.getString(res.getColumnIndex("recordDate"));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date = sdf.parse(recordDate);
        selfie.setRecordDate(date);
        selfie.setProcessedFilename(res.getString(res.getColumnIndex("processedFilename")));
        return selfie;
    }
}
