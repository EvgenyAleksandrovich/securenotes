package sectool.securenotes;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DB extends SQLiteOpenHelper {

    private static final byte DB_VERSION = 1;
    private static final String secnotesDB = "secnotesBD";

    /** ********************  NOTES ************************* */
    private static final String TABLE_NOTES = "NOTES";
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_SOURCE = "source";

    /** ********************* PASS ************************ */
    private static final String TABLE_PASS = "PASS";
    private static final String KEY_URL = "url";
    private static final String KEY_INFO = "info";
    private static final String KEY_LOGIN = "login";
    private static final String KEY_PASS = "pass";


    static String getSecnotesDB() {
        return secnotesDB;
    }

    static String getTableNotes() {
        return TABLE_NOTES;
    }

    static String getKeyName() {
        return KEY_NAME;
    }

    static String getKeySource() {
        return KEY_SOURCE;
    }

    static String getTablePass() {
        return TABLE_PASS;
    }

    static String getKeyUrl() {
        return KEY_URL;
    }

    static String getKeyInfo() {
        return KEY_INFO;
    }

    static String getKeyLogin() {
        return KEY_LOGIN;
    }

    static String getKeyPass() {
        return KEY_PASS;
    }

    DB(Context context) {
        super(context, secnotesDB, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // СОЗДАНИЕ ТАБЛИЦЫ

        // NOTES
        db.execSQL("create table " + TABLE_NOTES + "(" + KEY_ID + " integer primary key," + KEY_NAME
                + " text, " + KEY_SOURCE + " text);");

        // PASS
        db.execSQL("create table " + TABLE_PASS + "(" + KEY_ID + " integer primary key," + KEY_NAME
                + " text, " + KEY_URL + " text, " + KEY_INFO + " text, " + KEY_LOGIN + " text, " + KEY_PASS + " text);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
