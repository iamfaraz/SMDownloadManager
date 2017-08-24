package net.smtechies.smdownloadmanager.utils;

import android.provider.BaseColumns;

/**
 * Created by Faraz on 31/07/2017.
 */

public class Rows implements BaseColumns {
    public static final String TABLE_NAME = "FILES";
    public static final String rowId = "rowId";
    public static final String fileId = "fileId";
    public static final String fileName = "fileName";
    public static final String fileProgress = "fileProgress";
    public static final String fileSize = "fileSize";
    public static final String fileStatus = "fileStatus";
    public static final String fileDate = "fileDate";
    public static final String fileUrl = "fileUrl";
    public static final String filePath = "filePath";
    public static final String fileDName = "fileDName";
}