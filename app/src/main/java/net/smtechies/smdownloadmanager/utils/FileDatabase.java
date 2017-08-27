package net.smtechies.smdownloadmanager.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Faraz on 30/07/2017.
 */

public class FileDatabase extends SQLiteOpenHelper {

    private static final String SQL_CREATE_FILES_TABLES = "create table "
            + Rows.TABLE_FILES_NAME + "( "
            + Rows.rowId + " integer primary key autoincrement, "
            + Rows.fileId + " text not null, "
            + Rows.fileName + " text, "
            + Rows.fileProgress + " text, "
            + Rows.fileSize + " text, "
            + Rows.fileStatus + " text, "
            + Rows.fileDate + " text, "
            + Rows.fileUrl + " text, "
            + Rows.filePath + " text, "
            + Rows.fileDName + " text"
            + ");";

    private static final String SQL_CREATE_YT_ENTRIES = "create table "
            + Rows.TABLE_YT_NAME + "( "
            + Rows.ytRowId + " integer primary key autoincrement, "
            + Rows.ytFileId + " text not null, "
            + Rows.ytFileName + " text, "
            + Rows.ytFilePath + " text, "
            + Rows.ytVideoLink + " text, "
            + Rows.ytVideoStatus + " text, "
            + Rows.ytAudioLink + " text, "
            + Rows.ytAudioStatus + " text"
            + ");";

    private static final String SQL_DELETE_FILES_TABLE =
            "DROP TABLE IF EXISTS " +
                    Rows.TABLE_FILES_NAME;

    private static final String SQL_DELETE_YT_TABLE =
            "DROP TABLE IF EXISTS " +
                    Rows.TABLE_YT_NAME;

    private static final String DATABASE_NAME = "FILESDATABASE.db";
    private static final int DATABASE_VERSION = 1;

    public FileDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_FILES_TABLES);
        db.execSQL(SQL_CREATE_YT_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_FILES_TABLE);
        db.execSQL(SQL_DELETE_YT_TABLE);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
