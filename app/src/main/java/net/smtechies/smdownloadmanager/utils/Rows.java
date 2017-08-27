package net.smtechies.smdownloadmanager.utils;

import android.provider.BaseColumns;

/**
 * Created by Faraz on 31/07/2017.
 */

public class Rows implements BaseColumns {
    public static final String TABLE_FILES_NAME = "FILES";
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


    public static final String TABLE_YT_NAME = "Youtube_FILES";
    public static final String ytRowId = "ytRowId";
    public static final String ytFileId = "ytFileId";
    public static final String ytFileName = "ytFileName";
    public static final String ytFilePath = "ytPATH";
    public static final String ytVideoLink = "ytVideoLink";
    public static final String ytVideoStatus = "ytVideoStatus";
    public static final String ytAudioLink = "ytAudioLink";
    public static final String ytAudioStatus = "ytAudioStatus";
}