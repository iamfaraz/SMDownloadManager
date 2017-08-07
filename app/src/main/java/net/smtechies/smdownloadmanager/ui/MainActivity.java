package net.smtechies.smdownloadmanager.ui;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadListener;
import com.liulishuo.filedownloader.FileDownloader;
import com.liulishuo.filedownloader.model.FileDownloadStatus;
import com.liulishuo.filedownloader.util.FileDownloadUtils;

import net.smtechies.smdownloadmanager.R;
import net.smtechies.smdownloadmanager.adapter.FileItem;
import net.smtechies.smdownloadmanager.utils.FileDatabase;
import net.smtechies.smdownloadmanager.utils.FileDatabaseUtils;
import net.smtechies.smdownloadmanager.utils.Rows;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.SelectableAdapter.Mode;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {


    private final String DEFAULT_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
    private final int PERMISSIONS_WRITE_EXTERNAL_STORAGE = 101;

    private EditText etUrl;
    private CheckBox cb_forceDownload;

    private LayoutInflater layoutInflater;
    private View fileDialogView;

    private TextView fd_FileName, fd_FileStatus, fd_FileUrl,
            fd_FilePath, fd_FileSize, fd_FileSpeed, fd_FileId,
            fd_FileDate, fd_FileETA;
    private Button fd_FileStatusBTN, fd_FileDeleteBTN;
    private ProgressBar fd_FileProgressBar;

    private NavigationView navigationView;

    private ImageButton ib_Play;

    private String url_down = "http://files.funmaza.info/download/2ed3ab958afb3f78f9bac1fa3708ffeb";
    //            "http://videos.funmaza.info/storage/0517/Vitamin%20D%201080p%20-%20Ludacris%20feat.%20Ty%20Dolla%20Sign%20FunmazaHD.mp4";
    private String[] fileContent;

    private String fileName;
    private int fileId = 0;

    private Context context;
    private FileDownloadListener fdl;

    private FileDatabase fileDatabase;
    private SQLiteDatabase db;
    private FileDatabaseUtils fdu;

    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder notificationBuilder;

    private ActionModeCallback actionModeCallback = new ActionModeCallback();
    private ActionMode actionMode;

    private ArrayList<FileItem> fi_ALL;
    private ArrayList<FileItem> fi_DOWN;
    private ArrayList<FileItem> fi_COMP;


    private ArrayList<BaseDownloadTask> runningTask;

    private ArrayList<FileItem> currentList;

    private RecyclerView rc_list;

    private LinearLayoutManager layoutManager;
    private FlexibleAdapter<FileItem> adapter;

    private FlexibleAdapter.OnItemClickListener onItemClickListener;
    private FlexibleAdapter.OnItemLongClickListener onItemLongClickListener;

    private int lastScreen = 0, currentScreen = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        RequestPremissions();
        context = this;

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(this);
        layoutManager = new LinearLayoutManager(this);

        rc_list = (RecyclerView) findViewById(R.id.recycler_list);
        rc_list.setLayoutManager(layoutManager);

        fileDatabase = new FileDatabase(context);
        db = fileDatabase.getWritableDatabase();
        fdu = new FileDatabaseUtils(db);
        Init();
        ListFromDatabase();

        currentList = fi_ALL;
        adapter = new FlexibleAdapter<>(currentList);
        adapter.setMode(Mode.MULTI);

        rc_list.invalidate();
        rc_list.setAdapter(adapter);
        rc_list.setLayoutManager(new LinearLayoutManager(this));

        adapter.mItemClickListener = onItemClickListener;
        adapter.mItemLongClickListener = onItemLongClickListener;

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);

        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().getItem(0).setChecked(true);
    }

    private void itemOnClick(FileItem fileItem) {

        Intent in = new Intent(this, FileInfoActivity.class);

        in.putExtra(Rows.fileId, fileItem.getFileId());
        in.putExtra(Rows.fileName, fileItem.getFileName());
        in.putExtra(Rows.fileProgress, fileItem.getFileProgress());
        in.putExtra(Rows.fileStatus, fileItem.getFileStatus());
        in.putExtra("fileETA", fileItem.getFileETA());
        in.putExtra(Rows.fileSize, fileItem.getFileSize());
        in.putExtra("fileSpeed", fileItem.getFileSpeed());
        in.putExtra(Rows.fileDate, fileItem.getFileDate());
        in.putExtra(Rows.fileUrl, fileItem.getFileUrl());
        in.putExtra(Rows.filePath, fileItem.getFilePath());
        in.putExtra(Rows.fileDName, fileItem.getFileDName());

        startActivity(in);
        if (!FileExists(fileItem.getFilePath() + "/" + fileItem.getFileName())) {
            Snacked("File Not Found\nRe-Download It", 1);
        } else {
            Snacked(fileItem.getFileName() + " exists", 1);
                /*
                if (fm.getFileStatus().equals("Paused") || fm.getFileStatus().equals("Error")) {
                    //starterDownload(dataModel.getFileUrl(), dataModel.getFilePath()+"/"+dataModel.getFileName(), false, false, true);
                } else if (fm.getFileStatus().equals("Downloading")) {
                    FileDownloader.getImpl().pause(fm.getFileId());
                    Snacked(fm.getFileName() + "\nPaused", 0);
                }*/
        }
    }

    private void RequestPremissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Here, thisActivity is the current activity
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        PERMISSIONS_WRITE_EXTERNAL_STORAGE);
            }
        }
    }

    private void GetName(String url) {
        GetNameFromUrl fromUrl = new GetNameFromUrl();
        fromUrl.execute(url);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Toast.makeText(this, "Permission Granted.", Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(this, "Permission Denied.\nGrant Permission", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent();
                    intent.setAction(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    void Init() {

        fi_ALL = new ArrayList<>();
        fi_DOWN = new ArrayList<>();
        fi_COMP = new ArrayList<>();


        layoutInflater = LayoutInflater.from(context);
        fileDialogView = layoutInflater.inflate(R.layout.custom_file_info_dialog, null);

        fd_FileName = fileDialogView.findViewById(R.id.fd_tv_fileName);
        fd_FileStatus = fileDialogView.findViewById(R.id.fd_tv_fileStatus);
        fd_FileUrl = fileDialogView.findViewById(R.id.fd_tv_fileUrl);
        fd_FilePath = fileDialogView.findViewById(R.id.fd_tv_filePath);
        fd_FileSize = fileDialogView.findViewById(R.id.fd_tv_fileSize);
        fd_FileSpeed = fileDialogView.findViewById(R.id.fd_tv_fileSpeed);
        fd_FileId = fileDialogView.findViewById(R.id.fd_tv_fileId);
        fd_FileDate = fileDialogView.findViewById(R.id.fd_tv_fileDate);
        fd_FileETA = fileDialogView.findViewById(R.id.fd_tv_fileETA);

        fd_FileDeleteBTN = fileDialogView.findViewById(R.id.fd_bt_fileDelete);

        fd_FileProgressBar = fileDialogView.findViewById(R.id.fd_pb_fileProgress);

        fd_FileStatusBTN = fileDialogView.findViewById(R.id.fd_bt_fileStatus);


        onItemClickListener = new FlexibleAdapter.OnItemClickListener() {
            @Override
            public boolean onItemClick(int position) {
                if (actionMode != null && position != RecyclerView.NO_POSITION) {
                    // Mark the position selected
                    toggleSelection(position);
                    return true;
                } else {
                    FileItem fi = adapter.getItem(position);
                    ShowFileInfoDialog(fi);
                    //Snacked(fi.getFileName(), 0);
                    return false;
                }
            }
        };

        onItemLongClickListener = new FlexibleAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(int position) {
                if (actionMode == null) {
                    actionMode = startSupportActionMode(actionModeCallback);
                    toggleSelection(position);
                }

            }
        };

        fdl = new FileDownloadListener() {


            @Override
            protected void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {

                FileItem fi = GetFileModelByModel(
                        new FileItem(task.getId(), TextUtils.isEmpty(fileName) ? task.getFilename() : fileName,
                                currentProgress(soFarBytes, totalBytes), task.getStatus(), "ETA",
                                size(soFarBytes) + "/" + size(totalBytes), "Speed", getDateTime(System.currentTimeMillis()),
                                task.getUrl(), task.getPath(), task.getFilename(), fdl), false);


                fi.setFileName(TextUtils.isEmpty(fileName) ? task.getFilename() : fileName);
                fi.setFileProgress(currentProgress(soFarBytes, totalBytes));
                fi.setFileStatus(task.getStatus());
                fi.setFileDate(getDateTime(System.currentTimeMillis()));
                fi.setFileSize(size(soFarBytes) + "/" + size(totalBytes));
                fi.setFileDName(task.getFilename());
                fi.setFileSpeed("-");
                fi.setFileUrl(task.getUrl());
                fi.setFilePath(task.getPath());


                UpdateCurrentList();

                UpdateFileInfoDialog(fi);

                String[] columns = {Rows.fileName, Rows.fileStatus, Rows.fileUrl, Rows.filePath, Rows.fileDName};
                String[] columnVals = {fi.getFileName(), "" + fi.getFileStatus(), fi.getFileUrl(), fi.getFilePath(), task.getFilename()};

                String whereColumn = Rows.fileId;
                String[] whereArgs = {fi.getFileId() + ""};
                fdu.UpdateFileinDatabase(columns, columnVals, whereColumn, whereArgs);
            }

            @Override
            protected void started(BaseDownloadTask task) {
                FileItem fi = GetFileModelByModel(
                        new FileItem(task.getId(), TextUtils.isEmpty(fileName) ? task.getFilename() : fileName,
                                0, task.getStatus(), "ETA",
                                "---/---", "Speed", getDateTime(System.currentTimeMillis()),
                                task.getUrl(), task.getPath(), task.getFilename(), fdl), false);

                fi.setFileName(TextUtils.isEmpty(fileName) ? task.getFilename() : fileName);
                fi.setFileStatus(task.getStatus());
                fi.setFileSpeed("-");
                UpdateFileInfoDialog(fi);

                String[] columns = {Rows.fileName, Rows.fileStatus, Rows.fileUrl, Rows.filePath};
                String[] columnVals = {fi.getFileName(), "" + fi.getFileStatus(), fi.getFileUrl(), fi.getFilePath()};

                String whereColumn = Rows.fileId;
                String[] whereArgs = {fi.getFileId() + ""};
                fdu.UpdateFileinDatabase(columns, columnVals, whereColumn, whereArgs);
            }

            @Override
            protected void connected(BaseDownloadTask task, String etag, boolean isContinue, int soFarBytes, int totalBytes) {
                FileItem fi = GetFileModelByModel(
                        new FileItem(task.getId(), TextUtils.isEmpty(fileName) ? task.getFilename() : fileName,
                                currentProgress(soFarBytes, totalBytes), task.getStatus(), "ETA",
                                size(soFarBytes) + "/" + size(totalBytes), "Speed", getDateTime(System.currentTimeMillis()),
                                task.getUrl(), task.getPath(), task.getFilename(), fdl), false);

                fi.setFileName(TextUtils.isEmpty(fileName) ? task.getFilename() : fileName);
                fi.setFileProgress(currentProgress(soFarBytes, totalBytes));
                fi.setFileStatus(task.getStatus());
                fi.setFileSpeed("-");
                fi.setFileSize(size(soFarBytes) + "/" + size(totalBytes));
                UpdateFileInfoDialog(fi);


                String[] columns = {Rows.fileName, Rows.fileStatus, Rows.fileSize};
                String[] columnVals = {fi.getFileName(), "" + fi.getFileStatus(), size(soFarBytes) + "/" + size(totalBytes)};

                String whereColumn = Rows.fileId;
                String[] whereArgs = {fi.getFileId() + ""};
                fdu.UpdateFileinDatabase(columns, columnVals, whereColumn, whereArgs);
            }


            @Override
            protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                FileItem fi = GetFileModelByModel(
                        new FileItem(task.getId(), TextUtils.isEmpty(fileName) ? task.getFilename() : fileName,
                                currentProgress(soFarBytes, totalBytes), task.getStatus(), "ETA",
                                size(soFarBytes) + "/" + size(totalBytes), "Speed", getDateTime(System.currentTimeMillis()),
                                task.getUrl(), task.getPath(), task.getFilename(), fdl), false);

                fi.setFileName(TextUtils.isEmpty(fileName) ? task.getFilename() : fileName);
                fi.setFileProgress(currentProgress(soFarBytes, totalBytes));
                fi.setFileStatus(task.getStatus());
                fi.setFileSize(size(soFarBytes) + "/" + size(totalBytes));
                fi.setFileSpeed(speed(task.getSpeed()));
                UpdateFileInfoDialog(fi);

                UpdateCurrentList();

                String[] columns = {Rows.fileName, Rows.fileProgress, Rows.fileStatus, Rows.fileSize};
                String[] columnVals = {fi.getFileName(), currentProgress(soFarBytes, totalBytes) + "", "" + fi.getFileStatus(), size(soFarBytes) + "/" + size(totalBytes)};

                String whereColumn = Rows.fileId;
                String[] whereArgs = {fi.getFileId() + ""};
                fdu.UpdateFileinDatabase(columns, columnVals, whereColumn, whereArgs);
            }

            @Override
            protected void retry(BaseDownloadTask task, Throwable ex, int retryingTimes, int soFarBytes) {
                FileItem fi = GetFileModelByModel(
                        new FileItem(task.getId(), TextUtils.isEmpty(fileName) ? task.getFilename() : fileName,
                                0, task.getStatus(), "ETA",
                                size(soFarBytes) + "/" + "---", "Speed", getDateTime(System.currentTimeMillis()),
                                task.getUrl(), task.getPath(), task.getFilename(), fdl), false);

                fi.setFileName(TextUtils.isEmpty(fileName) ? task.getFilename() : fileName);

                fi.setFileStatus(task.getStatus());
                fi.setFileDate(getDateTime(System.currentTimeMillis()));
                fi.setFileSize(size(soFarBytes) + "/" + "---");
                fi.setFileSpeed("-");
                UpdateFileInfoDialog(fi);

                UpdateCurrentList();
            }

            @Override
            protected void completed(BaseDownloadTask task) {
                FileItem fi = GetFileModelByModel(
                        new FileItem(task.getId(), TextUtils.isEmpty(fileName) ? task.getFilename() : fileName,
                                100, task.getStatus(), "ETA",
                                size(task.getSmallFileTotalBytes()), "Speed", getDateTime(System.currentTimeMillis()),
                                task.getUrl(), task.getPath(), task.getFilename(), fdl), false);

                fi.setFileName(TextUtils.isEmpty(fileName) ? task.getFilename() : fileName);
                fi.setFileProgress(100);
                fi.setFileStatus(task.getStatus());
                fi.setFileDate(getDateTime(System.currentTimeMillis()));
                fi.setFileSize(size(task.getSmallFileTotalBytes()));
                fi.setFileSpeed("-");
                UpdateFileInfoDialog(fi);

                fi_DOWN.remove(fi);
                fi_COMP.add(fi);

                File from = new File(task.getPath(), task.getFilename());
                File to = new File(task.getPath(), fileName);

                if (FileExists(from.getAbsolutePath()) && !from.getName().equals(to.getName()))
                    from.renameTo(to);

                UpdateCurrentList();

                String[] columns = {Rows.fileName, Rows.fileProgress, Rows.fileStatus, Rows.fileSize};
                String[] columnVals = {fi.getFileName(), 100 + "", "" + task.getStatus(), fi.getFileSize()};

                String whereColumn = Rows.fileId;
                String[] whereArgs = {fi.getFileId() + ""};
                fdu.UpdateFileinDatabase(columns, columnVals, whereColumn, whereArgs);

            }

            @Override
            protected void paused(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                FileItem fi = GetFileModelByModel(
                        new FileItem(task.getId(), TextUtils.isEmpty(fileName) ? task.getFilename() : fileName,
                                currentProgress(soFarBytes, totalBytes), task.getStatus(), "ETA",
                                size(soFarBytes) + "/" + size(totalBytes), "Speed", getDateTime(System.currentTimeMillis()),
                                task.getUrl(), task.getPath(), task.getFilename(), fdl), false);
                if (fi == null)
                    fi = GetFileModelByModel(
                            new FileItem(task.getId(), TextUtils.isEmpty(fileName) ? task.getFilename() : fileName,
                                    currentProgress(soFarBytes, totalBytes), task.getStatus(), "ETA",
                                    size(soFarBytes) + "/" + size(totalBytes), "Speed", getDateTime(System.currentTimeMillis()),
                                    task.getUrl(), task.getPath(), task.getFilename(), fdl), true);

                fi.setFileName(TextUtils.isEmpty(fileName) ? task.getFilename() : fileName);
                fi.setFileSpeed("-");
                fi.setFileStatus(task.getStatus());
                UpdateFileInfoDialog(fi);

                UpdateCurrentList();

                String[] columns = {Rows.fileName, Rows.fileProgress, Rows.fileStatus, Rows.fileSize};
                String[] columnVals = {fi.getFileName(), currentProgress(soFarBytes, totalBytes) + "", "" + fi.getFileStatus(), size(soFarBytes) + "/" + size(totalBytes)};

                String whereColumn = Rows.fileId;
                String[] whereArgs = {fi.getFileId() + ""};
                fdu.UpdateFileinDatabase(columns, columnVals, whereColumn, whereArgs);

            }

            @Override
            protected void error(BaseDownloadTask task, Throwable e) {
                FileItem fi = GetFileModelByModel(
                        new FileItem(task.getId(), TextUtils.isEmpty(fileName) ? task.getFilename() : fileName,
                                0, task.getStatus(), "ETA", "Size", "Speed", getDateTime(System.currentTimeMillis()),
                                task.getUrl(), task.getPath(), task.getFilename(), fdl), false);
                if (fi == null)
                    fi = GetFileModelByModel(
                            new FileItem(task.getId(), TextUtils.isEmpty(fileName) ? task.getFilename() : fileName,
                                    0, task.getStatus(), "ETA",
                                    "---/" + size(task.getSmallFileTotalBytes()), "Speed", getDateTime(System.currentTimeMillis()),
                                    task.getUrl(), task.getPath(), task.getFilename(), fdl), true);

                fi.setFileName(TextUtils.isEmpty(fileName) ? task.getFilename() : fileName);
                fi.setFileStatus(task.getStatus());
                fi.setFileSpeed("-");
                UpdateFileInfoDialog(fi);

                UpdateCurrentList();

                Snacked(task.getFilename() + "\n" + e.getMessage(), 0);
                String[] columns = {Rows.fileName, Rows.fileStatus};
                String[] columnVals = {fi.getFileName(), "" + fi.getFileStatus()};

                String whereColumn = Rows.fileId;
                String[] whereArgs = {fi.getFileId() + ""};

                fdu.UpdateFileinDatabase(columns, columnVals, whereColumn, whereArgs);

            }

            @Override
            protected void warn(BaseDownloadTask task) {
                Snacked("Warning:\n" + task.getStatus(), 0);
            }
        };
    }

    private void UpdateFileInfoDialog(FileItem fi) {
        fd_FileName.setText(fi.getFileName());
        if (fi.getFileStatus() == FileDownloadStatus.completed) {
            fd_FileStatus.setText("Downloaded");
        } else if (fi.getFileStatus() == FileDownloadStatus.pending) {
            fd_FileStatus.setText("Pending");
        } else if (fi.getFileStatus() == FileDownloadStatus.error) {
            fd_FileStatus.setText("Error");
        } else if (fi.getFileStatus() == FileDownloadStatus.blockComplete) {
            fd_FileStatus.setText("Block Completed");
        } else if (fi.getFileStatus() == FileDownloadStatus.connected) {
            fd_FileStatus.setText("Connected");
        } else if (fi.getFileStatus() == FileDownloadStatus.paused) {
            fd_FileStatus.setText("Paused");
        } else if (fi.getFileStatus() == FileDownloadStatus.progress) {
            fd_FileStatus.setText("Downloading");
        } else if (fi.getFileStatus() == FileDownloadStatus.started) {
            fd_FileStatus.setText("Started");
        } else if (fi.getFileStatus() == FileDownloadStatus.retry) {
            fd_FileStatus.setText("Retrying");
        }
        fd_FileUrl.setText("Url: " + fi.getFileUrl());
        fd_FilePath.setText("Path: " + fi.getFilePath());
        fd_FileSize.setText("Size: " + fi.getFileSize());
        fd_FileSpeed.setText("Speed: " + fi.getFileSpeed());
        fd_FileId.setText("ID: " + fi.getFileId());
        fd_FileDate.setText("Date: " + fi.getFileDate());
        fd_FileETA.setText("ETA: -");

        fd_FileProgressBar.setProgress(fi.getFileProgress());
    }

    private void toggleSelection(int position) {
        // Mark the position selected
        adapter.toggleSelection(position);

        int count = adapter.getSelectedItemCount();

        if (count == 0) {
            actionMode.finish();
        } else {
            setContextTitle(count);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        adapter.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // Restore previous state
        if (savedInstanceState != null) {
            // Selection
            adapter.onRestoreInstanceState(savedInstanceState);
            if (adapter.getSelectedItemCount() > 0) {
                actionMode = startSupportActionMode(actionModeCallback);
                setContextTitle(adapter.getSelectedItemCount());
            }
        }
    }

    private void setContextTitle(int count) {
        actionMode.setTitle(String.valueOf(count) + " " + (count == 1 ? "item selected" : "items selected"));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab:
                showDownloadDialog();
                break;
        }
    }

    public String size(long bytes) {
        int unit = 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = ("KMGTPE").charAt(exp - 1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    public String speed(long KBSpeed) {
        long bytes = KBSpeed * 1024;
        int unit = 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = ("KMGTPE").charAt(exp - 1) + "";
        return String.format("%.1f %sB/s", bytes / Math.pow(unit, exp), pre);
    }

    public void ListFromDatabase() {
        try {
            Cursor cursor = fdu.getAllData();

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
            if (maplist.size() != 0) {
                for (int i = 0; i < maplist.size(); i++) {
                    FileItem fm = new FileItem(Integer.parseInt(maplist.get(i).get(Rows.fileId)),
                            maplist.get(i).get(Rows.fileName),
                            Integer.parseInt(maplist.get(i).get(Rows.fileProgress)), Byte.parseByte(maplist.get(i).get(Rows.fileStatus)), "-",
                            maplist.get(i).get(Rows.fileSize),
                            "-", getDateTime(Long.parseLong(maplist.get(i).get(Rows.fileDate))),
                            maplist.get(i).get(Rows.fileUrl),
                            maplist.get(i).get(Rows.filePath),
                            maplist.get(i).get(Rows.fileDName), fdl);

                    if (fm.getFileStatus() == FileDownloadStatus.completed) {
                        fi_COMP.add(fm);
                    } else {
                        fi_DOWN.add(fm);
                    }
                    fi_ALL.add(fm);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Snacked("GETNAMEFROMDB():\n" + e.getLocalizedMessage(), 0);
        }
    }

    String getDateTime(long milliSeconds) {
        long currentMilliSeonds = System.currentTimeMillis();

        Calendar currentDate = Calendar.getInstance();
        Calendar askedForDate = Calendar.getInstance();

        currentDate.setTimeInMillis(currentMilliSeonds);
        askedForDate.setTimeInMillis(milliSeconds);

        if (askedForDate.get(Calendar.YEAR) != currentDate.get(Calendar.YEAR))
            return askedForDate.get(Calendar.YEAR) + "";
        else if ((askedForDate.get(Calendar.MONTH) != currentDate.get(Calendar.MONTH)))
            return GetMonth(askedForDate.get(Calendar.MONTH)) + " " + askedForDate.get(Calendar.YEAR);
        else if (askedForDate.get(Calendar.DATE) != currentDate.get(Calendar.DATE))
            return askedForDate.get(Calendar.DATE) + " " + GetMonth(askedForDate.get(Calendar.MONTH));
        else {
            int am = Calendar.AM;
            int current = askedForDate.get(Calendar.AM_PM);
            String ampm = "";
            if (am == current)
                ampm = "am";
            else
                ampm = "pm";
            return askedForDate.get(Calendar.HOUR) + ":" + askedForDate.get(Calendar.MINUTE) + ampm;
        }

    }

    String GetMonth(int month) {
        switch (month) {
            case 1:
                return "Jan";
            case 2:
                return "Feb";
            case 3:
                return "Mar";
            case 4:
                return "Apr";
            case 5:
                return "May";
            case 6:
                return "Jun";
            case 7:
                return "Jul";
            case 8:
                return "Aug";
            case 9:
                return "Sep";
            case 10:
                return "Oct";
            case 11:
                return "Nov";
            case 12:
                return "Dec";
        }
        return null;
    }

    public void showDownloadDialog() {
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(context);
        View mView = layoutInflaterAndroid.inflate(R.layout.custom_dialog, null);

        final AlertDialog.Builder alert = new AlertDialog.Builder(context);

        etUrl = mView.findViewById(R.id.etUrl);
        etUrl.setMaxLines(5);

        cb_forceDownload = mView.findViewById(R.id.cb_forceDownload);

        etUrl.setText(url_down);
        //alert.setIcon(); TODO add icon
        etUrl.setHint("Enter Download Link");
        alert.setTitle("Download File");
        alert.setView(mView);


        alert.setPositiveButton("Download", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                boolean force_download = cb_forceDownload.isChecked();
                ToggleDownload(etUrl.getText().toString(), DEFAULT_PATH, cb_forceDownload.isChecked(), false);
            }
        });
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

            }
        });

        alert.show();
    }

    public void ShowFileInfoDialog(final FileItem fi) {
        fileDialogView = layoutInflater.inflate(R.layout.custom_file_info_dialog, null, false);

        fd_FileName = fileDialogView.findViewById(R.id.fd_tv_fileName);
        fd_FileStatus = fileDialogView.findViewById(R.id.fd_tv_fileStatus);
        fd_FileUrl = fileDialogView.findViewById(R.id.fd_tv_fileUrl);
        fd_FilePath = fileDialogView.findViewById(R.id.fd_tv_filePath);
        fd_FileSize = fileDialogView.findViewById(R.id.fd_tv_fileSize);
        fd_FileSpeed = fileDialogView.findViewById(R.id.fd_tv_fileSpeed);
        fd_FileId = fileDialogView.findViewById(R.id.fd_tv_fileId);
        fd_FileDate = fileDialogView.findViewById(R.id.fd_tv_fileDate);
        fd_FileETA = fileDialogView.findViewById(R.id.fd_tv_fileETA);

        fd_FileDeleteBTN = fileDialogView.findViewById(R.id.fd_bt_fileDelete);

        fd_FileProgressBar = fileDialogView.findViewById(R.id.fd_pb_fileProgress);

        fd_FileStatusBTN = fileDialogView.findViewById(R.id.fd_bt_fileStatus);

        UpdateFileInfoDialog(fi);
        if (fi.getFileStatus() == FileDownloadStatus.completed) {
            fd_FileStatusBTN.setText("Open");
        } else if (fi.getFileStatus() == FileDownloadStatus.pending ||
                fi.getFileStatus() == FileDownloadStatus.retry ||
                fi.getFileStatus() == FileDownloadStatus.connected ||
                fi.getFileStatus() == FileDownloadStatus.started ||
                fi.getFileStatus() == FileDownloadStatus.progress) {
            fd_FileStatusBTN.setText("Pause");
        } else if (fi.getFileStatus() == FileDownloadStatus.error || fi.getFileStatus() == FileDownloadStatus.paused) {
            fd_FileStatusBTN.setText("Resume");
        }

        fd_FileStatusBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (fi.getFileStatus() == FileDownloadStatus.completed) {

                    //file is completed
                    File file = new File(fi.getFilePath() + File.separator + fi.getFileName());
                    Uri fileUri;
                    if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)) {
                        fileUri = FileProvider.getUriForFile(context,
                                context.getApplicationContext().getPackageName() + ".net.smtechies.smdownloadmanager.provider",
                                file);
                    } else {
                        fileUri = Uri.fromFile(file);
                    }

                    //Snacked(fileUri.toString(), 0);
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(fileUri);
                    i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(i);
                } else if (fi.getFileStatus() == FileDownloadStatus.error || fi.getFileStatus() == FileDownloadStatus.paused) {
                    //file is pause and let's start it
                    FileDownloader.getImpl().create(fi.getFileUrl()).setPath(fi.getFilePath(), true).setListener(fdl).start();
                    fd_FileStatusBTN.setText("Pause");
                } else {
                    //file is active
                    FileDownloader.getImpl().pause((int) fi.getFileId());
                    fd_FileStatusBTN.setText("Resume");
                }
            }
        });


        UpdateFileInfoDialog(fi);

        final AlertDialog.Builder alert = new AlertDialog.Builder(context);

        alert.setTitle(fi.getFileName());
        alert.setView(fileDialogView);


        alert.setPositiveButton("Hide", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {


            }
        });

        alert.show();
    }

    public void otherNotification(int id) {
        //int icon = getResources().getInteger() R.mipmap.ic_launcher_round;
        long when = System.currentTimeMillis();

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        Notification notification = new Notification(R.mipmap.ic_launcher_round, "Custom Notification", when);

        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.custom_notification);
        notification.contentView = contentView;

        Intent notificationIntent = new Intent(context, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
        notification.contentIntent = contentIntent;

/*
        notification.flags |= Notification.FLAG_NO_CLEAR; //Do not clear the notification
        notification.defaults |= Notification.DEFAULT_LIGHTS; // LED
        notification.defaults |= Notification.DEFAULT_VIBRATE; //Vibration
        notification.defaults |= Notification.DEFAULT_SOUND; // Sound

        mNotificationManager.notify(id, notification);
*/

        notificationBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setOngoing(true)
                .setProgress(100, 50, false)
                .setWhen(when);

        mNotificationManager.notify(id, notificationBuilder.build());
    }

    public void ShowNotification(String title, String contentText, String bigContent, String line, int NOTIFICATION_ID) {
        Notification.InboxStyle inboxStyle = new Notification.InboxStyle();
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);


        NotificationManager nManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Notification.Builder builder = new Notification.Builder(context);
        builder.setContentTitle(title);
        builder.setContentText(contentText);
        builder.setSmallIcon(R.mipmap.ic_launcher_round);
        builder.setLargeIcon(bitmap);
        builder.setProgress(100, 50, true);
        builder.setAutoCancel(true);
        inboxStyle.setBigContentTitle(bigContent);
        inboxStyle.addLine(line);
        builder.setStyle(inboxStyle);
        builder.setOngoing(true);
        nManager.notify(getResources().getString(R.string.app_name), NOTIFICATION_ID, builder.build());
    }

    void Snacked(String Message, int length) {
        if (length <= 0)
            length = Toast.LENGTH_SHORT;
        else
            length = Toast.LENGTH_LONG;

        Toast.makeText(context, Message, length).show();
    }

    int currentProgress(int soFarBytes, int totalBytes) {
        int prog = 0;
        if (totalBytes != 0)
            prog = (int) (((double) soFarBytes / totalBytes) * 100);
        return prog;
    }

    public boolean hasIdInFileModel(ArrayList<FileItem> fileModel, int id) {
        for (int i = 0; i < fileModel.size(); i++) {
            if (fileModel.get(i).getFileId() == id) {
                return true;
            }
        }
        return false;
    }

    public boolean hasFileModel(ArrayList<FileItem> fileModel, FileItem fm) {
        for (int i = 0; i < fileModel.size(); i++) {
            if (fileModel.get(i).getFileId() == fm.getFileId()) {
                return true;
            }
        }

        return false;
    }

    public boolean UpdateFileModel(FileItem fm) {

        if (hasFileModel(fi_ALL, fm)) {
            byte status;
            for (int i = 0; i < fi_ALL.size(); i++) {
                if (fi_ALL.get(i).getFileId() == fm.getFileId()) {
                    fi_ALL.set(i, fm);
                }
            }

            status = fm.getFileStatus();
            if (status == FileDownloadStatus.completed) {
                for (int i = 0; i < fi_COMP.size(); i++) {
                    if (fi_COMP.get(i).getFileId() == fm.getFileId()) {
                        fi_COMP.set(i, fm);
                        return true;
                    }
                }
            } else {
                for (int i = 0; i < fi_DOWN.size(); i++) {
                    if (fi_DOWN.get(i).getFileId() == fm.getFileId()) {
                        fi_DOWN.set(i, fm);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public FileItem GetFileModelByModel(FileItem fi, boolean createNewIfNoTFound) {

        for (int i = 0; i < fi_ALL.size(); i++) {
            if (fi_ALL.get(i).getFileId() == fi.getFileId()) {
                return fi_ALL.get(i);
            }
        }
        if (!createNewIfNoTFound) {
            return null;
        } else {
            for (int i = 0; i < fi_ALL.size(); i++) {
                if (fi_ALL.get(i).getFileId() == fi.getFileId()) {
                    return fi_ALL.get(i);
                }
            }

            return CreateNewFileModel(fi);
        }
    }

    public FileItem GetFileModelById(int id, boolean createNewIfNoTFound) {

        for (int i = 0; i < fi_ALL.size(); i++) {
            if (fi_ALL.get(i).getFileId() == id) {
                return fi_ALL.get(i);
            }
        }
        if (!createNewIfNoTFound) {
            return null;
        } else {
            return CreateNewFileModel(id);
        }
    }

    public FileItem CreateNewFileModel(FileItem fi) {
        String[] insertData = {fi.getFileId() + "",
                fi.getFileName(), fi.getFileProgress() + "", fi.getFileSize(), FileDownloadStatus.pending + "",
                "" + System.currentTimeMillis(), fi.getFileUrl(),
                fi.getFilePath(), fi.getFileDName()};
        new InsertDataIntoDB(db).execute(insertData);
        fi_DOWN.add(fi);
        fi_ALL.add(fi);
        return fi;
    }

    public FileItem CreateNewFileModel(int id) {
        String[] insertData = {id + "",
                "", "0", "-", FileDownloadStatus.pending + "",
                "" + System.currentTimeMillis(), "",
                "", ""};
        new InsertDataIntoDB(db).execute(insertData);
        FileItem fm = new FileItem(id, "", 0, FileDownloadStatus.pending, "",
                "", "", getDateTime(System.currentTimeMillis()),
                "", "", "", fdl);
        fi_DOWN.add(fm);
        fi_ALL.add(fm);
        return fm;
    }

    public void ToggleDownload(String fileUrl, String filePath, boolean forceDownload, boolean resume) {

        boolean fileExists = false;
        int id = FileDownloadUtils.generateId(fileUrl, filePath);

        FileItem fm = GetFileModelById(id, true);
        fm.setFileUrl(fileUrl);
        fm.setFilePath(filePath);

        boolean fileInModel = hasIdInFileModel(fi_ALL, id);

        byte status;
        if (fileInModel) {
            status = fm.getFileStatus();
            if (status == FileDownloadStatus.completed || status == FileDownloadStatus.blockComplete) {
                Snacked("File already downloaded", 0);
                fileExists = FileExists(fm.getFilePath() + File.separator + fm.getFileName());
                if (!forceDownload)
                    return;
            } else if (status == FileDownloadStatus.paused || status == FileDownloadStatus.error) {
                fileExists = false;
            }
        }

        if (forceDownload) {
            GetName(fm.getFileUrl());
            fileId = FileDownloader.getImpl()
                    .create(fm.getFileUrl())
                    .setForceReDownload(forceDownload)
                    .setPath(filePath, true)
                    .setListener(fdl)
                    .start();
        } else if (!fileExists) {
            GetName(fileUrl);
            fileId = FileDownloader.getImpl()
                    .create(fileUrl)
                    .setForceReDownload(false)
                    .setPath(filePath, true)
                    .setListener(fdl)
                    .start();
        }
    }

    @Override
    public void onDestroy() {
        for (int i = 0; i < fi_ALL.size(); i++) {
            if (fi_ALL.get(i).getFileStatus() != FileDownloadStatus.completed ||
                    fi_ALL.get(i).getFileStatus() != FileDownloadStatus.paused ||
                    fi_ALL.get(i).getFileStatus() != FileDownloadStatus.error) {
                fi_ALL.get(i).setFileStatus(FileDownloadStatus.paused);

                String[] columns = {Rows.fileStatus};
                String[] columnVals = {"" + fi_ALL.get(i).getFileStatus()};

                String whereColumn = Rows.fileId;
                String[] whereArgs = {fi_ALL.get(i).getFileId() + ""};
                fdu.UpdateFileinDatabase(columns, columnVals, whereColumn, whereArgs);
            }
        }
        FileDownloader.getImpl().pauseAll();
        FileDownloader.getImpl().unBindService();
        db.close();
        fileDatabase.close();
        super.onDestroy();
    }

    public boolean FileExists(String path) {
        File file = new File(path);
        return file.exists();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            moveTaskToBack(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(this, Settings.class));
                break;
            case R.id.action_exit:
                KillApp();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    void KillApp() {
        onDestroy();
        System.exit(0);
    }

    @Override
    public void finish() {
        super.finish();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_all) {
            currentScreen = 0;
            UpdateCurrentList();
        } else if (id == R.id.nav_down) {
            currentScreen = 1;
            UpdateCurrentList();
        } else if (id == R.id.nav_comp) {
            currentScreen = 2;
            UpdateCurrentList();
        } else if (id == R.id.nav_feedback) {
            Snacked("Feedback", 0);
        } else if (id == R.id.nav_about) {
            Snacked("About", 0);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void ChangeList(ArrayList<FileItem> list) {
        adapter.updateDataSet(list);
    }

    private void UpdateCurrentList() {
        switch (currentScreen) {
            case 0:
                currentList = fi_ALL;
                ChangeList(currentList);
                break;
            case 1:
                currentList = fi_DOWN;
                ChangeList(currentList);
                break;
            case 2:
                currentList = fi_COMP;
                ChangeList(currentList);
                break;
        }
    }

    public class GetNameFromUrl extends AsyncTask<String, Integer, String[]> {

        @Override
        protected String[] doInBackground(String... params) {

            String filename = "";
            try {
                URL newUrl = new URL(params[0]);

                if (!filename.equals("") || !filename.equals(null)) {
                    URLConnection con = new URL(params[0]).openConnection();

                    con.connect();

                    InputStream is = con.getInputStream();
                    newUrl = con.getURL();
                    Log.d("Url", "" + newUrl);
                    is.close();
                    filename = GetName(newUrl);

                }

                if (filename.equals("") || filename.equals(null))
                    filename = newUrl.getFile().substring(newUrl.getFile().lastIndexOf('/') + 1, newUrl.getFile().length());

                filename = filename.replace("%20", " ");
                String fileExt = filename.substring(filename.lastIndexOf('.') + 1, filename.length());
                String[] fileName = {"file", filename, fileExt};
                return fileName;
            } catch (MalformedURLException e) {
                String[] error1 = {"Exception", "MalformedURLException", e.getMessage()};
                return error1;
            } catch (IOException e) {
                String[] error2 = {"Exception", "IOException", e.getMessage()};
                return error2;
            }

        }

        String GetName(URL url) throws IOException {


            String filename = "";

            URLConnection con = url.openConnection();
            Map map = con.getHeaderFields();
            if (map.get("Content-Disposition") != null) {
                String raw = map.get("Content-Disposition").toString();
                // raw = "attachment; filename=abc.jpg"
                if (raw != null && raw.indexOf("=") != -1) {
                    filename = raw.split("=")[1]; // getting value after '='
                    filename = filename.replaceAll("\"", "").replaceAll("]", "");
                }
            }

            return filename;
        }

        @Override
        protected void onPostExecute(String[] result) {

            fileContent = result;
            String[] fileInfo = fileContent;
            if (fileInfo[0].equals("file")) {
                fileName = fileInfo[1];
            } else {

            }

        }
    }

    private class ActionModeCallback implements ActionMode.Callback {
        @SuppressWarnings("unused")
        private final String TAG = ActionModeCallback.class.getSimpleName();

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            if (adapter.getSelectedItemCount() > 1) {
                menu.getItem(0).setVisible(false);
            }
            mode.getMenuInflater().inflate(R.menu.selected_menu, menu);
            adapter.setMode(Mode.MULTI);
            for (int i = 0; i < menu.size(); i++) {
                Drawable drawable = menu.getItem(i).getIcon();
                if (drawable != null) {
                    drawable.mutate();
                    drawable.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
                }
            }
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.selected_delete:
                    // TODO: actually remove items
                    List<Integer> list = adapter.getSelectedPositions();
                    ArrayList<FileItem> selectedList = new ArrayList<FileItem>();


                    for (int i = 0; i < list.size(); i++) {
                        selectedList.add(adapter.getItem(list.get(i)));
                    }

                    for (int i = 0; i < selectedList.size(); i++) {
                        fi_ALL.remove(selectedList.get(i));

                        String whereColumn = Rows.fileId;
                        String[] whereArgs = {String.valueOf(selectedList.get(i).getFileId())};

                        fdu.DeleteFileFromDatabase(whereColumn, whereArgs);

                        if (fi_DOWN.contains(selectedList.get(i)))
                            fi_DOWN.remove(selectedList.get(i));

                        if (fi_COMP.contains(selectedList.get(i)))
                            fi_COMP.remove(selectedList.get(i));
                    }

                    UpdateCurrentList();

                    ChangeList(currentList);
                    //ShowNotification("Test", "ContentText ", "BigContent", "Lines ", new Random().nextInt());
                    //otherNotification(new Random().nextInt());
                    mode.finish();
                    return true;
                case R.id.selected_playpause:
                    // TODO: actually play and pause items
                    Log.d(TAG, "Play or Pause");
                    mode.finish();
                    return true;
                case R.id.selected_select_all:
                    // TODO: actually select all items
                    Log.d(TAG, "menu_remove");
                    mode.finish();
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            adapter.setMode(Mode.IDLE);
            adapter.clearSelection();
            actionMode = null;
        }
    }

    public class InsertDataIntoDB extends AsyncTask<String, Integer, Boolean> {

        FileDatabaseUtils fdu;

        public InsertDataIntoDB(SQLiteDatabase db) {
            this.fdu = new FileDatabaseUtils(db);
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
        }

        @Override
        protected Boolean doInBackground(String... params) {
            this.fdu.InsertDataIntoTable(params[0], params[1], params[2], params[3], params[4], params[5], params[6], params[7], params[8]);
            return true;
        }
    }
}
