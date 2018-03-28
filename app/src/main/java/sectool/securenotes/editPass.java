package sectool.securenotes;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import sectool.securenot.R;

import static sectool.securenotes.MainActivity.*;

public class editPass extends AppCompatActivity {

    public static void setNamePwd(String namePwd) {
        editPass.namePwd = namePwd;
    }

    public static void setHidePass(boolean hidePass) {
        editPass.hidePass = hidePass;
    }

    public static void setEditPwdEnabled(boolean editPwdEnabled) {
        editPass.editPwdEnabled = editPwdEnabled;
    }

    public static void setAutoSave(boolean autoSave) {
        editPass.autoSave = autoSave;
    }

    private static String namePwd;
    private static boolean hidePass;
    private static boolean editPwdEnabled;
    private static boolean autoSave;
    private static TextView name;
    private static EditText url;
    private static EditText info;
    private static EditText login;
    private static EditText pass;
    private static EditText selEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_edit_pass);

            name = findViewById(R.id.textName);
            url = findViewById(R.id.url);
            info = findViewById(R.id.info);
            login = findViewById(R.id.login);
            pass = findViewById(R.id.pass);
            name.setText(namePwd);

            if (!editPwdEnabled) setTitle(getString(R.string.txtCreatePass));
            else { // создание
                setTitle(getString(R.string.txtEditPass)); // редактирование
                name.setText(namePwd);
                if (hidePass) pass.setTransformationMethod(new PasswordTransformationMethod());
                else pass.setTransformationMethod(null);
                db = new DB(this);
                baza = db.getWritableDatabase();
                cursor = baza.query(DB.getTablePass(), null, DB.getKeyName() + "='" +
                cryptMod.encrypt(namePwd, pwd) + "'", null, null, null, null);
                cursor.moveToFirst();
                url.setText(cryptMod.decrypt(cursor.getString(cursor.getColumnIndex(DB.getKeyUrl())), pwd));
                info.setText(cryptMod.decrypt(cursor.getString(cursor.getColumnIndex(DB.getKeyInfo())), pwd));
                login.setText(cryptMod.decrypt(cursor.getString(cursor.getColumnIndex(DB.getKeyLogin())), pwd));
                pass.setText(cryptMod.decrypt(cursor.getString(cursor.getColumnIndex(DB.getKeyPass())), pwd));
                baza.close();
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        selEdit = url;

        info.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                selEdit = info;
            }
        });

        login.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                selEdit = login;
            }
        });

        pass.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                selEdit = pass;
            }
        });

        url.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                selEdit = url;
            }
        });
        }

    public void onStop(){
        if (autoSave && editPwdEnabled){
            try {savePass(this);} catch (Exception e) {
                e.printStackTrace();}
        }
        super.onStop();
    }

    public String genPass(){
        String buff = "";
        for (byte i = 0; i < 3; i++){
            buff += String.valueOf((char)(int)(97 + (Math.random() * 25)))
                    + String.valueOf((char)(int)(48 + (Math.random() * 9)))
                    + String.valueOf((char)(int)(65 + (Math.random() * 25)));
        }
        return buff;
    }

    public void clickGenPass(View v){
        EditText tmp = selEdit;
        pass.setText(genPass());
        if (hidePass) Toast.makeText(this, getString(R.string.txtGenPass) + pass.getText(),
        Toast.LENGTH_LONG).show();
        selEdit = tmp;
    }

    public void clickSavePass(View v) throws Exception {

        if (editPwdEnabled) { // редактирование
            savePass(this);
        } else { // создание
            db = new DB(this);
            baza = db.getWritableDatabase();
            contentValues = new ContentValues();
            contentValues.clear();
            contentValues.put(DB.getKeyName(), cryptMod.encrypt(namePwd, pwd));
            contentValues.put(DB.getKeyUrl(), cryptMod.encrypt(url.getText().toString(), pwd));
            contentValues.put(DB.getKeyInfo(), cryptMod.encrypt(info.getText().toString(), pwd));
            contentValues.put(DB.getKeyLogin(), cryptMod.encrypt(login.getText().toString(), pwd));
            contentValues.put(DB.getKeyPass(), cryptMod.encrypt(pass.getText().toString(), pwd));
            baza.insert(DB.getTablePass(), null, contentValues);
            db.close();
            Toast.makeText(this, R.string.txtCreatePassCompl, Toast.LENGTH_SHORT).show();
            // update list
            arrayListPass.add(namePwd);
            adapterPass.notifyDataSetChanged();
            ((ListView) listpass).setAdapter(adapterPass);
        }
        editPwdEnabled = false;
        editPass.this.finish();
    }

    public void clickCopy(View v){
        try{
        ClipboardManager clipboard = (ClipboardManager) this.getSystemService(Context.CLIPBOARD_SERVICE);
        clipboard.setPrimaryClip(ClipData.newPlainText("", selEdit.getText().toString()));
        Toast.makeText(this, R.string.txtCopyInBuff, Toast.LENGTH_LONG).show();
        new Thread(new delay()).start();} catch (Exception e){
            Toast.makeText(this, "", Toast.LENGTH_SHORT).show();
        }
    }

    static void savePass(Context context) throws Exception {
        db = new DB(context);
        baza = db.getWritableDatabase();
        baza.execSQL("UPDATE " + DB.getTablePass() + " SET " + DB.getKeyName() + "='" +
                cryptMod.encrypt(url.getText().toString(), pwd) + "', " + DB.getKeyUrl() + "='" +
                cryptMod.encrypt(info.getText().toString(), pwd) + "', " + DB.getKeyInfo()  + "='" +
                cryptMod.encrypt(login.getText().toString(), pwd) + "', " + DB.getKeyLogin() + "='" +
                cryptMod.encrypt(pass.getText().toString(), pwd) + "' WHERE " + DB.getKeyName() + "='" +
                cryptMod.encrypt(namePwd, pwd) + "';");
        db.close();
        Toast.makeText(context, R.string.txtSaveDataMsg, Toast.LENGTH_LONG).show();
    }

    class delay implements Runnable{

        @Override
        public void run() {
            try {
                Thread.sleep(25000);
                ClipboardManager clipboard = (ClipboardManager) editPass.this.getSystemService(Context.CLIPBOARD_SERVICE);
                clipboard.setPrimaryClip(ClipData.newPlainText("", ""));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
