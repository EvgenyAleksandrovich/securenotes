package sectool.securenotes;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import sectool.securenot.R;

import static sectool.securenotes.MainActivity.*;

public class setPass extends AppCompatActivity {

    public EditText pass1, pass2;
    public TextView trebTX1, trebTX2, trebTX3;
    public static boolean resetPass = false;
    static boolean goodPass = false;
    static boolean resetPwd;

    @SuppressLint({"ResourceAsColor", "NewApi"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_pass);
        if (resetPass) setTitle(getString(R.string.txtResetPass)); else setTitle(getString(R.string.txtSetPass));
        pass1 = findViewById(R.id.pass1);
        pass2 = findViewById(R.id.pass2);
        trebTX1 = findViewById(R.id.treb1);
        trebTX2 = findViewById(R.id.treb2);
        trebTX3 = findViewById(R.id.treb3);
        pass1.setTextColor(getColor(R.color.myRed));
        pass2.setTextColor(getColor(R.color.myRed));
        pass1.addTextChangedListener(new TextWatcher() {

            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                ckeckTreb();
            }

            @TargetApi(Build.VERSION_CODES.M)
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                ckeckTreb();
            }

            @TargetApi(Build.VERSION_CODES.M)
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void afterTextChanged(Editable editable) {

            }
        });}


    @RequiresApi(api = Build.VERSION_CODES.M)
    @SuppressLint("ResourceAsColor")
    public void ckeckTreb() {
        boolean[] onoff = new boolean[3];
        // КОЛИЧЕСТВО
        if (pass1.getText().toString().length() >= 6) {
            // совпадает
            trebTX1.setTextColor(getColor(R.color.myGreen));
            onoff[0] = true;
        } else {  // не совпадает
            trebTX1.setTextColor(getColor(R.color.myRed));
            onoff[0] = false;
        }

        // ЗАГЛАВНЫЕ
        for (byte i = 65; i <= 90; i++) {
            if (pass1.getText().toString().contains(String.valueOf((char) i))) {
                trebTX2.setTextColor(getColor(R.color.myGreen));
                onoff[1] = true;
                break;
            } else {
                trebTX2.setTextColor(getColor(R.color.myRed));
                onoff[1] = false;
            }
        }

        // ЦИФРЫ
        for (byte j = 48; j <= 57; j++) {
            if (pass1.getText().toString().contains(String.valueOf((char) j))) {
                trebTX3.setTextColor(getColor(R.color.myGreen));
                onoff[2] = true;
                break;
            } else {
                trebTX3.setTextColor(getColor(R.color.myRed));
                onoff[2] = false;
            }
        }

        if (onoff[0] && onoff[1] && onoff[2]) {goodPass = true;} else goodPass = false;
    }

    @SuppressLint("CommitPrefEdits")
    public void setPassClick(View view) {
        if (goodPass && (pass1.getText().toString().equals(pass2.getText().toString()))) {
            if (resetPwd) {
                sp.edit().putString("5", getDate()).apply();
                sp.edit().putString("4", cryptMod.multiHash(pass1.getText().toString())).apply();
                auth = false;
                startActivity(new Intent(this, MainActivity.class));} else {
            sp.edit().putString("4", cryptMod.multiHash(pass1.getText().toString())).apply();
            sp.edit().putBoolean("0", true).apply();
            sp.edit().putBoolean("1", true).apply();
            sp.edit().putBoolean("2", true).apply();
            sp.edit().putBoolean("3", true).apply();
            startActivity(new Intent(this, MainActivity.class));
        }
        } else {
            Toast.makeText(this, R.string.txtNotEqualsPass, Toast.LENGTH_SHORT).show();
        }
    }
}
