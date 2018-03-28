package sectool.securenotes;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import sectool.securenot.R;

public class MainActivity extends AppCompatActivity {

    public static SharedPreferences sp;
    public static String hashPass, pwd;
    public static final int maxLengthName = 20;
    public static Dialog dialog;
    public static DB db;
    public static SQLiteDatabase baza;
    public static ContentValues contentValues;
    public static ArrayAdapter<String> adapterNotes, adapterPass;
    @SuppressLint("StaticFieldLeak")
    public static View listnotes, listpass;
    public static boolean selectednotes, selectedpass, auth;
    public static ArrayList<String> arrayListNotes, arrayListPass;
    public static Cursor cursor;
    @SuppressLint("StaticFieldLeak")
    static Toolbar toolbar;


    @SuppressLint("InflateParams")
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(R.string.txtNotes);
        // убираем прокрутку
        AppBarLayout.LayoutParams params = (AppBarLayout.LayoutParams) toolbar.getLayoutParams();
        params.setScrollFlags(0);

        /*
          *  0 - Автосохранение
          *  1 - Переодически предлагать смену пароля
          *  2 - Защита от скриншотов
          *  3 - Скрывать пароли под звёздочками
          *  4 - ХЭШ
          *  5 - Дата создания пароля
         **/

        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        ViewPager mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        toolbar.setTitle(R.string.txtNotes);
                        break;
                    case 1:
                        toolbar.setTitle(R.string.txtPass);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        sp = PreferenceManager.getDefaultSharedPreferences(this);
        dialog = new Dialog(this);
        hashPass = sp.getString("4", ""); // читаем пасc

        // антискриншот
        if (sp.getBoolean("2", true))
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        else getWindow().setFlags(WindowManager.LayoutParams.FLAGS_CHANGED, WindowManager.LayoutParams.FLAGS_CHANGED);

        if (hashPass.length() != 0){
            if (!auth){
                inputDialog(getString(R.string.txtAuth), getString(R.string.txtEnterTxt),
                        getString(R.string.txtEnter), true, true, new heandlerFunct() {
                            @Override
                            public void run(String enterTXT) throws Exception {
                                if (!hashPass.equals(cryptMod.multiHash(enterTXT)))
                                    Toast.makeText(MainActivity.this, R.string.txtAuthFiled,
                                            Toast.LENGTH_SHORT).show();
                                else{ // АВТОРИЗОВАЛИСЬ
                                    pwd = enterTXT;
                                    // автосохранение
                                    if (sp.getBoolean("0", false)) {
                                        editNote.setAutoSave(true);
                                        editPass.setAutoSave(true);
                                    } else {
                                        editNote.setAutoSave(true);
                                        editPass.setAutoSave(true);
                                    }
                                    // напоминалка о смене пароля
                                    if (sp.getBoolean("1", true)) napominalka();

                                    // пароль звёздочки
                                    editPass.setHidePass(sp.getBoolean("3", false));
                                    auth = true;
                                    auth = true;
                                    dialog.cancel();
                                    updateNotes();
                                    updatePass();
                                }
                            }
                        });}
        }else startActivity(new Intent(this, firstStart.class));
    }

    public void updateNotes() throws Exception {
        // загрузим список всех заметок
        db = new DB(this);
        baza = db.getWritableDatabase();
        cursor = baza.query(DB.getTableNotes(), null, null, null, null, null, null);
        int nameIndex = cursor.getColumnIndex(DB.getKeyName());
        arrayListNotes = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do{
                arrayListNotes.add(cryptMod.decrypt(cursor.getString(nameIndex), pwd));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        // устанавливаем адаптер
        adapterNotes = new ArrayAdapter<>(this, R.layout.listnote, arrayListNotes);
        ((ListView)listnotes).setAdapter(adapterNotes);
    }

    public void updatePass() throws Exception {
        // загрузим список всех паролей
        db = new DB(this);
        baza = db.getWritableDatabase();
        cursor = baza.query(DB.getTablePass(), null, null, null, null, null, null);
        arrayListPass = new ArrayList<>();
        if (cursor.moveToFirst()) {
            int nameIndex = cursor.getColumnIndex(DB.getKeyName());
            do{
                arrayListPass.add(cryptMod.decrypt(cursor.getString(nameIndex), pwd));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        // устанавливаем адаптер
        adapterPass = new ArrayAdapter<>(this, R.layout.listpass, arrayListPass);
        ((ListView)listpass).setAdapter(adapterPass);
    }

    // МЕНЮ
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @SuppressLint("CommitPrefEdits")
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.importData:
                startActivity(new Intent(this, importInfo.class));
                return true;
            case R.id.exportData:
                    startActivity(new Intent(this, createBackup.class));
                return true;
            case R.id.resetPass:
                // СМЕНА ПАРОЛЯ
                inputDialog(getString(R.string.txtResetPass), getString(R.string.txtInputOldPass),
                        getString(R.string.txtContinue), true, false, new heandlerFunct() {
                    @Override
                    public void run(String enterTXT) {
                        // если пароль верный, то окно смены пароля
                        if (hashPass.equals(cryptMod.multiHash(enterTXT))) {
                            startActivity(new Intent(MainActivity.this, setPass.class));
                            setPass.resetPass = true;
                            setPass.resetPwd = true;
                        }
                        else Toast.makeText(MainActivity.this, R.string.txtWrondPass, Toast.LENGTH_SHORT).show();
                    }
                });
                return true;
            case R.id.settings:
                // НАСТРОЙКИ
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.information:
                startActivity(new Intent(this, info.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static class notesFragment extends Fragment {

        public notesFragment() {
        }

        public static notesFragment newInstance() {
            notesFragment fragment = new notesFragment();
            Bundle args = new Bundle();
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            listnotes = rootView.findViewById(R.id.listNotes);
            ((ListView)listnotes).setAdapter(adapterNotes);
            registerForContextMenu((rootView.findViewById(R.id.listNotes)));
            ((ListView)listnotes).setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {
                    editNote.setEditNoteEnabled(true);
                    editNote.setNameNoteText(arrayListNotes.get(pos));
                    startActivity(new Intent(getContext(), editNote.class));
                }
            });
            return rootView;
        }
    }

    public static class passFragment extends Fragment {

        public passFragment() {
        }

        public static passFragment newInstance() {
            passFragment fragment = new passFragment();
            Bundle args = new Bundle();
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_pass, container, false);
            ((ListView)rootView.findViewById(R.id.listPass)).setAdapter(adapterPass);
            registerForContextMenu((rootView.findViewById(R.id.listPass)));
            listpass = rootView.findViewById(R.id.listPass);
            ((ListView)listpass).setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {
                    editPass.setEditPwdEnabled(true);
                    editPass.setNamePwd(arrayListPass.get(pos));
                    startActivity(new Intent(getContext(), editPass.class));
                }
            });
            return rootView;
        }
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment result = notesFragment.newInstance();
            if (position == 1) result = passFragment.newInstance();
            return result;
        }

        @Override
        public int getCount() {
            return 2;
        }

    }

    // КОНТЕКСТНОЕ МЕНЮ
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        selectedpass = false; selectednotes = false;
        if (v.getId() == listnotes.getId()) selectednotes = true; else selectedpass = true;
        getMenuInflater().inflate(R.menu.context_menu, menu);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        final AdapterView.AdapterContextMenuInfo listSelected = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        String tmpTableName;
        String selectElement;

        if (selectednotes) {selectElement = arrayListNotes.get(listSelected.position); tmpTableName = DB.getTableNotes(); }
        else { selectElement = arrayListPass.get(listSelected.position); tmpTableName = DB.getTablePass();}

        db = new DB(MainActivity.this);
        baza = db.getWritableDatabase();

        switch (item.getItemId()){
            case R.id.rename:
                final String finalTmpTableName = tmpTableName;
                final String finalSelectElement = selectElement;
                inputDialog(getString(R.string.txtRename), getString(R.string.txtSmallRename) + selectElement + " в:",
                        getString(R.string.txtRename3), false, false, new heandlerFunct() {
                            @Override
                            public void run(String enterTXT) throws Exception {
                                if (enterTXT.length() <= 20) {
                                    baza.execSQL("update " + finalTmpTableName + " SET " + DB.getKeyName() + "='" + enterTXT + "' WHERE " + DB.getKeyName() + " ='" + finalSelectElement + "';");
                                    db.close();
                                    // update list
                                    if (selectednotes) {
                                        if (exsistsNameNote(enterTXT)){
                                        arrayListNotes.set(listSelected.position, enterTXT);
                                        adapterNotes.notifyDataSetChanged();
                                        ((ListView)listnotes).setAdapter(adapterNotes);} else {
                                            Toast.makeText(MainActivity.this, R.string.txtExsistsNotee, Toast.LENGTH_LONG).show();
                                        }
                                    }
                                    else {
                                        if (exsistsNamePass(enterTXT)){
                                        arrayListPass.set(listSelected.position, enterTXT);
                                        adapterPass.notifyDataSetChanged();
                                        ((ListView)listpass).setAdapter(adapterPass);}
                                        else {
                                            Toast.makeText(MainActivity.this, R.string.txtExistsNamePass, Toast.LENGTH_LONG).show();
                                        }
                                    }
                                    selectedpass = false; selectednotes = false;
                                    dialog.cancel();
                                }
                                else  Toast.makeText(MainActivity.this, getString(R.string.txtErrBigName), Toast.LENGTH_SHORT).show();
                            }
                        });
                break;
            case R.id.delete:
                try {
                    baza.execSQL("DELETE FROM " + tmpTableName + " WHERE " + DB.getKeyName() + "='" + cryptMod.encrypt(selectElement, pwd) + "'");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                db.close();
                // update list
                if (selectednotes) {
                    arrayListNotes.remove(listSelected.position);
                    adapterNotes.notifyDataSetChanged();
                    ((ListView)listnotes).setAdapter(adapterNotes);
                }
                else {
                    arrayListPass.remove(listSelected.position);
                    adapterPass.notifyDataSetChanged();
                    ((ListView)listpass).setAdapter(adapterPass);
                }
                selectedpass = false; selectednotes = false;
                dialog.cancel();
                break;
        }
        return super.onContextItemSelected(item);
    }

    public interface heandlerFunct{
        void run(String enterTXT) throws Exception;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @SuppressLint({"ResourceAsColor", "ResourceType"})
    public void inputDialog(String title, String msg, String buttonTXT, boolean pwd,
                            final boolean offCancel, final heandlerFunct funct){

        dialog.setCancelable(false);
        LayoutInflater inflater = getLayoutInflater();
        @SuppressLint("InflateParams") View v = inflater.inflate(R.layout.inputdialog, null, false);
        TextView head = v.findViewById(R.id.dialogHead);
        TextView msgTXT = v.findViewById(R.id.dialogMsg);
        final EditText edit = v.findViewById(R.id.editText2);
        Button btn = v.findViewById(R.id.button2);
        btn.setText(buttonTXT);
        Button btnExit = v.findViewById(R.id.button3);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    funct.run(edit.getText().toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
                if (offCancel) MainActivity.this.finish();
            }
        });
        head.setText(title);
        msgTXT.setText(msg);
        if (pwd) edit.setTransformationMethod(new PasswordTransformationMethod()); else {
            edit.setTransformationMethod(null);
        }
        dialog.setContentView(v); // устанавливаем лайоут в диалог
        dialog.show();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void addNoteClick(View v){
        // ДОБАВИТЬ ЗАМЕТКУ
        inputDialog(getString(R.string.txtNewNote), getString(R.string.txtNewNameNote),  getString(R.string.txtCreate), false, false, new heandlerFunct() {
            @Override
            public void run(String enterTXT) {
                if (enterTXT.length() > maxLengthName) Toast.makeText(MainActivity.this, getString(R.string.txtErrBigName) + maxLengthName + getString(R.string.txtChars), Toast.LENGTH_LONG).show();
                else {
                    if (enterTXT.length() < 1 || enterTXT.length() >  20)
                        Toast.makeText(MainActivity.this, R.string.txtBadName, Toast.LENGTH_SHORT).show();
                    else{
                        if (exsistsNameNote(enterTXT)) {
                    // СОЗДАЁМ ЗАМЕТКУ
                    editNote.setNameNoteText(enterTXT);
                    editNote.setEditNoteEnabled(false);
                    startActivity(new Intent(MainActivity.this, editNote.class));
                    dialog.cancel();} else
                    Toast.makeText(MainActivity.this, R.string.txtExsistsNotee,
                    Toast.LENGTH_LONG).show();
                    }}
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void addPassClick(View v){
        // ДОБАВИТЬ ПАРОЛЬ
        inputDialog(getString(R.string.txtNewPass), getString(R.string.txtNewNamePass), getString(R.string.txtCreate), false, false, new heandlerFunct() {
            @Override
            public void run(String enterTXT) {
                if (enterTXT.length() > maxLengthName) Toast.makeText(MainActivity.this, getString(R.string.txtErrBigName) + maxLengthName + getString(R.string.txtChars), Toast.LENGTH_LONG).show();
                else {
                    if (enterTXT.length() < 1 || enterTXT.length() >  20)
                        Toast.makeText(MainActivity.this, R.string.txtBadName, Toast.LENGTH_SHORT).show();
                    else{
                        if (exsistsNamePass(enterTXT)){
                            // СОЗДАЁМ ПАРОЛЬ
                    editPass.setNamePwd(enterTXT);
                    editPass.setEditPwdEnabled(false);
                    startActivity(new Intent(MainActivity.this, editPass.class));
                    dialog.cancel();} else
                    Toast.makeText(MainActivity.this, R.string.txtExistsNamePass,
                    Toast.LENGTH_LONG).show();
                }}
            }
        });
    }

    public boolean exsistsNameNote(String checkName){
        boolean r = true;
        for (String check: arrayListNotes){
            if (check.equals(checkName)) r = false;
        }
        return r;
    }

    public boolean exsistsNamePass(String checkName){
        boolean r = true;
        for (String check: arrayListPass){
            if (check.equals(checkName)) r = false;
        }
        return r;
    }

    public static long lastDays(String d1, String d2){
        @SuppressLint("SimpleDateFormat") SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
        Date date1 = null;
        Date date2 = null;
        try {
            date1 = format.parse(d1);
            date2 = format.parse(d2);
        } catch (Exception e) {
            e.printStackTrace();
        }
        long difference = date1.getTime() - date2.getTime();
        return difference / (24 * 60 * 60 * 1000);
    }

    public static String getDate(){
        Calendar c = Calendar.getInstance();
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
        return sdf.format(c.getTime());
    }

    public static void napominalka(){
        if (sp.getBoolean("1", false)) {
            if ( lastDays(getDate(), sp.getString("5", getDate())) >= 30) {
                Snackbar.make(MainActivity.toolbar, R.string.txtResetPassOld,
                        Snackbar.LENGTH_LONG).show();
            }
        }
    }

    // копирование файлов
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static boolean copyFile(final String src, final String dst) {
        final File srcFile = new File(src);
        final File dstFile = new File(dst);
        if (srcFile.exists() && srcFile.isFile() && !dstFile.exists()) {
            try (InputStream in = new FileInputStream(srcFile);
                 OutputStream out = new FileOutputStream(dstFile)) {
                byte[] buffer = new byte[1024];
                int bytes;
                while ((bytes = in.read(buffer)) > 0) {
                    out.write(buffer, 0, bytes);
                }
            } catch (FileNotFoundException ex) {
                return false;
            } catch (Exception e) {
                return false;
            }
            return true;
        } else return false;
    }

    public static void exportDB(Context context, String path, String dbName) {
        try {
                File currentDB = context.getDatabasePath(dbName);
                File backupDB = new File(path);
                FileChannel src = new FileInputStream(currentDB).getChannel();
                FileChannel dst = new FileOutputStream(backupDB).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void importDB(Context context, String dbName, String pathBackup) {
        try {
                File currentDB = context.getDatabasePath(dbName);
                File backupDB = new File(pathBackup);
                FileChannel src = new FileInputStream(backupDB).getChannel();
                FileChannel dst = new FileOutputStream(currentDB).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void exportSettings(String path) throws IOException {
        // читаем настройки
        String params = sp.getString("4", "")
                + " " + String.valueOf(sp.getBoolean("0", false))
                + " " + String.valueOf(sp.getBoolean("1", false))
                + " " + String.valueOf(sp.getBoolean("2", false))
                + " " + String.valueOf(sp.getBoolean("3", false))
                + " " + sp.getString("5", "");
        // пишем их в файл
        FileWriter fw = new FileWriter(path);
        fw.write(params);
        fw.close();
    }

    public static void zip(String[] files, String zipFileName) {
        ZipOutputStream out = null;
        try {
            out = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFileName)));
            byte data[] = new byte[80000];
            for (String file : files) {
                processFile(out, data, file);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close(out);
        }
    }

    private static void processFile(ZipOutputStream out, byte[] data, String file) {
        BufferedInputStream origin = null;
        try {
            origin = new BufferedInputStream(new FileInputStream(file), 80000);
            ZipEntry entry = new ZipEntry(file.substring(file.lastIndexOf("/") + 1));
            out.putNextEntry(entry);
            int count;
            while ((count = origin.read(data, 0, 80000)) != -1) {
                out.write(data, 0, count);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close(origin);
        }
    }

    private static void close(Closeable closeable){
        if (null != closeable){
            try {
                closeable.close();
            } catch (IOException ignored) {
            }
        }
    }

    public static void unzip(String pathzip, String pathExtract) throws IOException {
        ZipFile zip = new ZipFile(pathzip);
        Enumeration entries = zip.entries();
        LinkedList<ZipEntry> zfiles = new LinkedList<>();
        while (entries.hasMoreElements()) {
            ZipEntry entry = (ZipEntry) entries.nextElement();
            zfiles.add(entry);
        }
        for (ZipEntry entry : zfiles) {
            InputStream in = zip.getInputStream(entry);
            OutputStream out = new FileOutputStream(pathExtract + "/" + entry.getName());
            byte[] buffer = new byte[1024];
            int len;
            while ((len = in.read(buffer)) >= 0)
                out.write(buffer, 0, len);
            in.close();
            out.close();
        }
        zip.close();
    }
}