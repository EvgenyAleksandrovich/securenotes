package sectool.securenotes;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import java.io.File;
import java.util.ArrayList;

import sectool.securenot.R;

import static sectool.securenotes.MainActivity.exportSettings;
import static sectool.securenotes.MainActivity.zip;

public class createBackup extends AppCompatActivity {

    static ArrayList<String> dirList;
    static ArrayAdapter<String> adapter;
    private String theDir, enterDir;
    private String[] permission = {
            "android.permission.WRITE_EXTERNAL_STORAGE",
            "android.permission.READ_EXTERNAL_STORAGE"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dir_dialog);
        (findViewById(R.id.okDir)).setVisibility(View.GONE);
        (findViewById(R.id.backButton)).setVisibility(View.GONE);
        getSupportActionBar().setTitle(R.string.txtSelectFolder);
        // ЗАПРОС ПРАВ
        if ((ActivityCompat.checkSelfPermission(this, permission[0]) !=
                PackageManager.PERMISSION_GRANTED)) ActivityCompat.requestPermissions(this, permission, 1);
        else go();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        for (int res : grantResults) {
            if (res != PackageManager.PERMISSION_GRANTED) Toast.makeText(this,
            R.string.txtNotPermission, Toast.LENGTH_SHORT).show();
            else {
                go();
            }
        }
    }

    public void go(){
        (findViewById(R.id.okDir)).setVisibility(View.VISIBLE);
        (findViewById(R.id.backButton)).setVisibility(View.VISIBLE);
        dirList = new ArrayList<>();
        ((ListView)findViewById(R.id.listFolder)).setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {
                getDirs(theDir + dirList.get(pos) + "/");
            }
        });
        getDirs(Environment.getExternalStorageDirectory().getPath() + "/");
    }

    public void backClick(View v){
        getDirs(new File((theDir)).getParent() + "/");
    }

    public void getDirs(String dir){
        dirList.clear();
        if ((!dir.contains(Environment.getExternalStorageDirectory().getPath())))
            dir = Environment.getExternalStorageDirectory().getPath() + "/";
        theDir = dir;

        for (File ff: new File(dir).listFiles()) {
            if (ff.isDirectory()) dirList.add(ff.getPath().replace(ff.getParent(), "").replace("/", ""));
        }

        adapter = new ArrayAdapter<>(this, R.layout.listdirs, dirList);
        ((ListView)findViewById(R.id.listFolder)).setAdapter(adapter);
    }

    public void okClick(View v){
            enterDir = theDir;
            try {
                MainActivity.exportDB(this, enterDir + "db.bak", DB.getSecnotesDB());
                exportSettings(enterDir + "sp.bak");
                zip(new String[]{
                                enterDir + "db.bak",
                                enterDir + "sp.bak"},
                        enterDir + ".SNbackup");
                new File(enterDir + "db.bak").delete();
                new File(enterDir + "sp.bak").delete();
                Toast.makeText(this, "Экспортированно в "
                        + theDir.replace(Environment.getExternalStorageDirectory().getPath(), "")
                        + ".SNbackup", Toast.LENGTH_LONG).show();
                this.finish();
                startActivity(new Intent(this, MainActivity.class));
            } catch (Exception e) {
                Toast.makeText(this, "Ошибка экспорта: " + e.toString(), Toast.LENGTH_LONG).show();
            }
        }
}
