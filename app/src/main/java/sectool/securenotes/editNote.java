package sectool.securenotes;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import sectool.securenot.R;

public class editNote extends AppCompatActivity {

    public static void setNameNoteText(String nameNoteText) {
        editNote.nameNoteText = nameNoteText;
    }

    public static void setEditNoteEnabled(boolean editNoteEnabled) {
        editNote.editNoteEnabled = editNoteEnabled;
    }

    public static void setAutoSave(boolean autoSave) {
        editNote.autoSave = autoSave;
    }

    private static String nameNoteText;
    private static boolean editNoteEnabled;
    private static boolean autoSave;

    @SuppressLint("StaticFieldLeak")
    public static EditText editNoteText;
    public TextView nameNote;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_note);
        editNoteText = findViewById(R.id.editNoteText);
        nameNote = findViewById(R.id.nameNote);
        nameNote.setText(nameNoteText);

        if (!editNoteEnabled) setTitle(getString(R.string.txtCreateNote)); else {
            setTitle(getString(R.string.txtEditNote));
            MainActivity.db = new DB(this);
            MainActivity.baza = MainActivity.db.getWritableDatabase();
            try{
                MainActivity.cursor = MainActivity.baza.query(DB.getTableNotes(), new String[]{DB.getKeySource()},
                DB.getKeyName() + "='" + cryptMod.encrypt(nameNoteText, MainActivity.pwd) + "'", null, null, null, null);
                MainActivity.cursor.moveToFirst();
                editNoteText.setText(cryptMod.decrypt(MainActivity.cursor.getString(MainActivity.cursor.getColumnIndex(DB.getKeySource())), MainActivity.pwd));
            } catch (Exception e) {
                Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
            }
            MainActivity.baza.close();
            MainActivity.cursor.close();
        }
        nameNote.setText(nameNoteText);
        nameNote.setTextColor(getColor(android.R.color.holo_green_dark));
    }

    @Override
    public void onStop() {
        if (autoSave && editNoteEnabled) {
            try{
            savaNote(this);} catch (Exception e) {e.printStackTrace();}
        }
        super.onStop();
    }

    public void clickSaveNote(View v) throws Exception {
        if (editNoteEnabled) { // сохранение изменений
            savaNote(this);
        } else { // создание
            MainActivity.db = new DB(this);
            MainActivity.baza = MainActivity.db.getWritableDatabase();
            MainActivity.contentValues = new ContentValues();
            MainActivity.contentValues.clear();
            MainActivity.contentValues.put(DB.getKeyName(), cryptMod.encrypt(nameNoteText, MainActivity.pwd));
            MainActivity.contentValues.put(DB.getKeySource(), cryptMod.encrypt(editNoteText.getText().toString(), MainActivity.pwd));
            MainActivity.baza.insert(DB.getTableNotes(), null, MainActivity.contentValues);
            // update list
            MainActivity.arrayListNotes.add(nameNoteText);
            MainActivity.adapterNotes.notifyDataSetChanged();
            ((ListView) MainActivity.listnotes).setAdapter(MainActivity.adapterNotes);
            Toast.makeText(this, R.string.txtNoteCreateCompl, Toast.LENGTH_SHORT).show();
            MainActivity.db.close();
            editNoteEnabled = false;
            editNote.this.finish();
        }
        }

    static void savaNote(Context context) throws Exception {
            MainActivity.db = new DB(context);
            MainActivity.baza = MainActivity.db.getWritableDatabase();
            MainActivity.baza.execSQL("update " + DB.getTableNotes() + " SET " + DB.getKeySource() + "='" +
                    cryptMod.encrypt(editNoteText.getText().toString(), MainActivity.pwd) + "' WHERE name='" +
                    cryptMod.encrypt(nameNoteText, MainActivity.pwd) + "';");
            MainActivity.db.close();
        Toast.makeText(context, R.string.txtSaveDataMsg, Toast.LENGTH_LONG).show();
        }
    }
