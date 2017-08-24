package net.smtechies.smdownloadmanager.utils;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Faraz on 30/07/2017.
 */

public class FileDatabaseUtils {

    SQLiteDatabase db;

    public FileDatabaseUtils(SQLiteDatabase db) {
        this.db = db;
    }

    public void InsertDataIntoTable(String fileId, String fileName,
                                    String fileProgress, String fileSize, String fileStatus, String fileDate,
                                    String fileUrl, String filePath, String fileDName) {


        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(Rows.fileId, fileId);
        values.put(Rows.fileName, fileName);
        values.put(Rows.fileProgress, fileProgress);
        values.put(Rows.fileSize, fileSize);
        values.put(Rows.fileStatus, fileStatus);
        values.put(Rows.fileDate, fileDate);
        values.put(Rows.fileUrl, fileUrl);
        values.put(Rows.filePath, filePath);
        values.put(Rows.fileDName, fileDName);

        // Insert the new row, returning the primary key value of the new row
//        long newRowId =
        db.insert(Rows.TABLE_NAME, null, values);
    }

    public Cursor getAllData() {

        Cursor cursor = db.rawQuery("select * from " + Rows.TABLE_NAME, null);

        return cursor;
    }

    public Cursor getDatabyColumn(String column, String[] columnArgs, String[] columnsToShow) {

        if (columnsToShow == null) {
            String[] proj = {
                    Rows.rowId,
                    Rows.fileId,
                    Rows.fileName,
                    Rows.fileProgress,
                    Rows.fileSize,
                    Rows.fileStatus,
                    Rows.fileDate,
                    Rows.fileUrl,
                    Rows.filePath,
                    Rows.fileDName
            };

            columnsToShow = proj;
        }

        // selection is Column name and selectionArgs is search argument
        column = column + " = ?";

        // How you want the results sorted in the resulting Cursor
        String sortOrder = Rows.fileDate + " DESC";

        Cursor cursor = db.query(
                Rows.TABLE_NAME,                     // The table to query
                columnsToShow,                               // The columns to return
                column,                                // The columns for the WHERE clause
                columnArgs,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );

        return cursor;
    }

    public void DeleteTable(String tableName) {
        db.delete(tableName, null, null);
        db.execSQL("vacuum");
        Log.d("FDU: Delete Table", "deleted");
    }

    public void DeleteFileFromDatabase(String column, String[] columnArgs) {
        // Define 'where' part of query.
        column = column + " LIKE ?";
        // Issue SQL statement.
        db.delete(Rows.TABLE_NAME, column, columnArgs);
        Log.d("FDU: Delete Entry", "deleted");
    }

    public void UpdateFileinDatabase(String[] columnsToUpdate, String[] columnValues, String whereColumn, String[] whereArgs) {
        if (columnsToUpdate.length != columnValues.length)
            return;
        ContentValues values = new ContentValues();
        for (int i = 0; i < columnsToUpdate.length; i++) {
            values.put(columnsToUpdate[i], columnValues[i]);
        }


        // Which row to update, based on the column
        whereColumn = whereColumn + " LIKE ?";

        int count = db.update(
                Rows.TABLE_NAME,
                values,
                whereColumn,
                whereArgs);
    }

    public boolean IdInDB(int id) {
        try {
            Cursor cursor = getAllData();

            ArrayList<HashMap<String, String>> maplist = new ArrayList<HashMap<String, String>>();
            // looping through all rows and adding to list

            if (cursor.moveToFirst()) {
                do {
                    HashMap<String, String> map = new HashMap<String, String>();
                    for (int i = 0; i < cursor.getColumnCount(); i++) {
                        map.put(cursor.getColumnName(i), cursor.getString(i));
                    }

                    maplist.add(map);
                } while (cursor.moveToNext());
            }

            for (int i = 0; i < maplist.size(); i++) {
                if (maplist.get(i).get(Rows.fileId) == String.valueOf(id)) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("FDU: IdinDB Exception", e.getLocalizedMessage());
        }
        return false;
    }

}
