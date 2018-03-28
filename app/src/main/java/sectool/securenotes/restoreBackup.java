package sectool.securenotes;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

import sectool.securenot.R;

public class restoreBackup extends AppCompatActivity {
    public static String action, type, pathImport;
    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restore_backup);
        getSupportActionBar().setTitle(R.string.txtImportData);
            action = getIntent().getAction();
            type = getIntent().getType();
            Uri fileUri = getIntent().getParcelableExtra(Intent.EXTRA_STREAM);
            pathImport = fileUri.getPath();
        }
        @SuppressLint("CommitPrefEdits")
        public void clickGo(View v){
            // обрабатываем входящий файл
                pathImport = pathImport.replace("root", "");
                try {
                    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
                    if (cryptMod.multiHash( ((EditText)findViewById(R.id.editPass)).getText()
                            .toString()).equals(sp.getString("4", ""))) {
                    MainActivity.unzip(pathImport,
                            getFilesDir().getPath() + "/");
                    // Read Settings
                    FileReader fr = new FileReader(getFilesDir().getPath() + "/sp.bak");
                    Scanner sc = new Scanner(fr);
                    String settings = sc.nextLine();
                    sc.close();
                    String[] set = settings.split(" ");
                    try{
                    MainActivity.sp.edit().putString("4", set[0]).apply();
                    if (set[1].equals("true")) MainActivity.sp.edit().putBoolean("0", true).apply();
                    if (set[2].equals("true")) MainActivity.sp.edit().putBoolean("1", true).apply();
                    if (set[3].equals("true")) MainActivity.sp.edit().putBoolean("2", true).apply();
                    if (set[4].equals("true")) MainActivity.sp.edit().putBoolean("3", true).apply();
                    MainActivity.sp.edit().putString("5", set[5]).apply();
                    } catch (Exception e){
                        Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
                    }
                    // Import DB
                    MainActivity.importDB(this, DB.getSecnotesDB(), getFilesDir().getPath() + "/db.bak");
                    Toast.makeText(this, R.string.txtDataRestore, Toast.LENGTH_LONG).show();
                    // restart
                    MainActivity.auth = false;
                    MainActivity.hashPass = "";
                    startActivity(new Intent(this, MainActivity.class));} else {
                        Toast.makeText(this, R.string.txtWrondPass, Toast.LENGTH_SHORT).show();
                    }
            } catch (IOException e) {
                    Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
            }
}
}
