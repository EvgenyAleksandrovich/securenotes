package sectool.securenotes;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.WindowManager;

import sectool.securenot.R;

    /*
     * *  0 - Автосохранение
     * *  1 - Переодически предлагать смену пароля
     * *  2 - Защита от скриншотов
     * *  3 - Скрывать пароли под звёздочками
     * *  4 - ХЭШ
     */

public class SettingsActivity extends AppCompatPreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefences);
        setTitle(getString(R.string.txtSettings));
        SharedPreferences spp = PreferenceManager.getDefaultSharedPreferences(this);
        spp.registerOnSharedPreferenceChangeListener(this);
    }

    // отслеживание измененеий
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {
        switch (key) {
            case "0": // автосохранение
                    editNote.setAutoSave(MainActivity.sp.getBoolean("0", false));
                    editPass.setAutoSave(MainActivity.sp.getBoolean("0", false));
                break;
            case "2": // антискрин
                if (MainActivity.sp.getBoolean(key, false))
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
                else getWindow().setFlags(WindowManager.LayoutParams.FLAGS_CHANGED, WindowManager.LayoutParams.FLAGS_CHANGED);
                break;
            case "3": // скрытие паролей
                   editPass.setHidePass(MainActivity.sp.getBoolean(key, false));
                break;
        }

    }
}
