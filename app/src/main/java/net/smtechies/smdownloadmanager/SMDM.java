package net.smtechies.smdownloadmanager;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;

import com.liulishuo.filedownloader.FileDownloader;
import com.liulishuo.filedownloader.util.FileDownloadUtils;

import net.smtechies.smdownloadmanager.utils.PrefsKeys;

/**
 * Created by Faraz on 31/07/2017.
 */

public class SMDM extends Application {
    final String DEFAULT_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
    SharedPreferences.Editor editor;
    SharedPreferences settings;

    @Override
    public void onCreate() {
        settings = getApplicationContext().getSharedPreferences(PrefsKeys.PREF_NAME, Context.MODE_PRIVATE); //1
        editor = settings.edit();

        if (!settings.contains(PrefsKeys.FIRST_LAUNCH))
            InitiateApp();

        FileDownloader.setupOnApplicationOnCreate(this);
        FileDownloadUtils.setDefaultSaveRootPath(settings.getString(PrefsKeys.DEFAULT_PATH, null));


        super.onCreate();
    }

    void InitiateApp() {
        String version;
        int version_code;
        try {
            PackageInfo pInfo = this.getPackageManager().getPackageInfo(getPackageName(), 0);
            version = pInfo.versionName;
            version_code = pInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        editor.putString(PrefsKeys.DEFAULT_PATH, DEFAULT_PATH);
        editor.putBoolean(PrefsKeys.FIRST_LAUNCH, false);
        editor.putString(PrefsKeys.APP_VER, DEFAULT_PATH);
        editor.putString(PrefsKeys.DEFAULT_PATH, DEFAULT_PATH);
        editor.putString(PrefsKeys.DEFAULT_PATH, DEFAULT_PATH);
        editor.putString(PrefsKeys.DEFAULT_PATH, DEFAULT_PATH);
        editor.putString(PrefsKeys.DEFAULT_PATH, DEFAULT_PATH);
        editor.putString(PrefsKeys.DEFAULT_PATH, DEFAULT_PATH);
        editor.putString(PrefsKeys.DEFAULT_PATH, DEFAULT_PATH);
        editor.putString(PrefsKeys.DEFAULT_PATH, DEFAULT_PATH);
        editor.putString(PrefsKeys.DEFAULT_PATH, DEFAULT_PATH);
        editor.putString(PrefsKeys.DEFAULT_PATH, DEFAULT_PATH);
    }
}

