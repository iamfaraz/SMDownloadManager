package net.smtechies.smdownloadmanager.ui;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
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
import android.provider.MediaStore;
import android.support.design.widget.BottomSheetDialog;
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
import android.widget.ListView;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadListener;
import com.liulishuo.filedownloader.FileDownloadQueueSet;
import com.liulishuo.filedownloader.FileDownloader;
import com.liulishuo.filedownloader.model.FileDownloadStatus;
import com.liulishuo.filedownloader.util.FileDownloadSerialQueue;
import com.liulishuo.filedownloader.util.FileDownloadUtils;

import net.smtechies.smdownloadmanager.R;
import net.smtechies.smdownloadmanager.adapter.AboutListAdapter;
import net.smtechies.smdownloadmanager.adapter.FileItem;
import net.smtechies.smdownloadmanager.utils.FileDatabase;
import net.smtechies.smdownloadmanager.utils.FileDatabaseUtils;
import net.smtechies.smdownloadmanager.utils.Rows;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.SelectableAdapter.Mode;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {


    private final String PATH = Environment.
            getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
    private final int PERMISSIONS_WRITE_EXTERNAL_STORAGE = 101;

    private FileDownloadListener fdl;

    private FileDownloadListener fdl_youtube;

    private LayoutInflater layoutInflater;
    private View fileDialogView;

    private TextView fd_FileName, fd_FilePath, fd_FileDate;
    private Button cfid_FileOpenBTN, cfid_FileDeleteBTN, cfid_HideBTN;

    private NavigationView navigationView;
    private ImageButton ib_Play;
    private String url_down =
            //"http://files.funmaza.info/download/2ed3ab958afb3f78f9bac1fa3708ffeb";
            "http://videos.funmaza.info/storage/0517/Vitamin%20D%201080p%20-%20Ludacris%20feat." +
                    "%20Ty%20Dolla%20Sign%20FunmazaHD.mp4";


    private int fileId = 0;
    private Context context;
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
    private int currentScreen = 0;
    private Toolbar toolbar;

    private List<BaseDownloadTask> youtubeTasks;
    private List<String> youtubeTaskNames;

    private BaseDownloadTask task1;
    private BaseDownloadTask task2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("All");
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

        Intenter(savedInstanceState, getIntent());
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

    private void Init() {

        fi_ALL = new ArrayList<>();
        fi_DOWN = new ArrayList<>();
        fi_COMP = new ArrayList<>();

        youtubeTasks = new ArrayList<>();
        youtubeTaskNames = new ArrayList<>();

        layoutInflater = LayoutInflater.from(context);

        onItemClickListener = new FlexibleAdapter.OnItemClickListener() {
            @Override
            public boolean onItemClick(int position) {
                if (actionMode != null && position != RecyclerView.NO_POSITION) {
                    // Mark the position selected
                    toggleSelection(position);
                    return true;
                } else {
                    FileItem fi = adapter.getItem(position);
                    if (fi.getFileStatus() == FileDownloadStatus.completed
                            || fi.getFileStatus() == FileDownloadStatus.blockComplete) {
                        ShowFileInfoDialog(fi);
                    } else if (fi.getFileStatus() == FileDownloadStatus.paused
                            || fi.getFileStatus() == FileDownloadStatus.error) {
                        Snacked("Resuming " + fi.getFileName(), 0);
                        FileDownloader.getImpl()
                                .create(fi.getFileUrl())
                                .setForceReDownload(false)
                                .setPath(fi.getFilePath() + File.separator + fi.getFileName())
                                .setMinIntervalUpdateSpeed(1000)
                                .setListener(fdl)
                                .start();
                        //ToggleDownload(fi.getFileUrl(), fi.getFilePath(), fi.getFileName(), false, true);
                    } else if (fi.getFileStatus() == FileDownloadStatus.pending ||
                            fi.getFileStatus() == FileDownloadStatus.started ||
                            fi.getFileStatus() == FileDownloadStatus.connected ||
                            fi.getFileStatus() == FileDownloadStatus.progress ||
                            fi.getFileStatus() == FileDownloadStatus.retry) {
                        Snacked(fi.getFileName() + " Paused", 0);
                        FileDownloader.getImpl().pause((int) fi.getFileId());

                    }
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
                        new FileItem(task.getId(), task.getFilename(),
                                currentProgress(soFarBytes, totalBytes), task.getStatus(), "ETA",
                                GetSizeFromBytes(soFarBytes) + "/" + GetSizeFromBytes(totalBytes), "Speed", "",
                                task.getUrl(), task.getPath(), task.getFilename()), false);


                fi.setFileName(task.getFilename());
                fi.setFileStatus(task.getStatus());
                fi.setFileDate(fi.getFileDate());
                fi.setFileSize(GetSizeFromBytes(soFarBytes) + "/" + GetSizeFromBytes(totalBytes));
                fi.setFileDName(task.getFilename());
                fi.setFileSpeed("-");
                fi.setFileUrl(task.getUrl());
                String path = task.getPath().substring(0, task.getPath().lastIndexOf("/"));
                fi.setFilePath(path);


                UpdateCurrentList();


                String[] columns = {Rows.fileName, Rows.fileStatus, Rows.fileUrl, Rows.filePath, Rows.fileDName};
                String[] columnVals = {fi.getFileName(), "" + fi.getFileStatus(),
                        fi.getFileUrl(), fi.getFilePath(), task.getFilename()};

                String whereColumn = Rows.fileId;
                String[] whereArgs = {fi.getFileId() + ""};
                fdu.UpdateFileinDatabase(columns, columnVals, whereColumn, whereArgs);
            }

            @Override
            protected void started(BaseDownloadTask task) {
                FileItem fi = GetFileModelByModel(
                        new FileItem(task.getId(), task.getFilename(),
                                0, task.getStatus(), "ETA",
                                "---/---", "Speed", getDateTime(System.currentTimeMillis()),
                                task.getUrl(), task.getPath(), task.getFilename()), false);

                fi.setFileName(task.getFilename());
                fi.setFileStatus(task.getStatus());
                fi.setFileSpeed("-");

                String[] columns = {Rows.fileName, Rows.fileStatus, Rows.fileUrl, Rows.filePath};
                String[] columnVals = {fi.getFileName(), "" + fi.getFileStatus(), fi.getFileUrl(), fi.getFilePath()};

                String whereColumn = Rows.fileId;
                String[] whereArgs = {fi.getFileId() + ""};
                fdu.UpdateFileinDatabase(columns, columnVals, whereColumn, whereArgs);
            }

            @Override
            protected void connected(BaseDownloadTask task, String etag, boolean isContinue,
                                     int soFarBytes, int totalBytes) {
                FileItem fi = GetFileModelByModel(
                        new FileItem(task.getId(), task.getFilename(), currentProgress(soFarBytes, totalBytes),
                                task.getStatus(), "ETA",
                                GetSizeFromBytes(soFarBytes) + "/" + GetSizeFromBytes(totalBytes), "Speed",
                                getDateTime(System.currentTimeMillis()),
                                task.getUrl(), task.getPath(), task.getFilename()), false);

                fi.setFileName(task.getFilename());
                fi.setFileStatus(task.getStatus());
                fi.setFileSpeed("-");
                fi.setFileSize(GetSizeFromBytes(soFarBytes) + "/" + GetSizeFromBytes(totalBytes));


                String[] columns = {Rows.fileName, Rows.fileStatus, Rows.fileSize};
                String[] columnVals = {fi.getFileName(), "" + fi.getFileStatus(),
                        GetSizeFromBytes(soFarBytes) + "/" + GetSizeFromBytes(totalBytes)};

                String whereColumn = Rows.fileId;
                String[] whereArgs = {fi.getFileId() + ""};
                fdu.UpdateFileinDatabase(columns, columnVals, whereColumn, whereArgs);
            }


            @Override
            protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                FileItem fi = GetFileModelByModel(
                        new FileItem(task.getId(), task.getFilename(),
                                currentProgress(soFarBytes, totalBytes), task.getStatus(), "ETA",
                                GetSizeFromBytes(soFarBytes) + "/" + GetSizeFromBytes(totalBytes), "Speed",
                                getDateTime(System.currentTimeMillis()),
                                task.getUrl(), task.getPath(), task.getFilename()), false);

                fi.setFileName(task.getFilename());
                fi.setFileProgress(currentProgress(soFarBytes, totalBytes));
                fi.setFileStatus(task.getStatus());
                fi.setFileSize(GetSizeFromBytes(soFarBytes) + "/" + GetSizeFromBytes(totalBytes));
                fi.setFileSpeed(ConvertSpeed(task.getSpeed()));

                UpdateCurrentList();

                String[] columns = {Rows.fileName, Rows.fileProgress, Rows.fileStatus, Rows.fileSize};
                String[] columnVals = {fi.getFileName(), currentProgress(soFarBytes, totalBytes) + "",
                        "" + fi.getFileStatus(), GetSizeFromBytes(soFarBytes) + "/" + GetSizeFromBytes(totalBytes)};

                String whereColumn = Rows.fileId;
                String[] whereArgs = {fi.getFileId() + ""};
                fdu.UpdateFileinDatabase(columns, columnVals, whereColumn, whereArgs);
            }

            @Override
            protected void retry(BaseDownloadTask task, Throwable ex, int retryingTimes, int soFarBytes) {
                FileItem fi = GetFileModelByModel(
                        new FileItem(task.getId(), task.getFilename(),
                                0, task.getStatus(), "ETA",
                                GetSizeFromBytes(soFarBytes) + "/" + "---", "Speed", getDateTime(System.currentTimeMillis()),
                                task.getUrl(), task.getPath(), task.getFilename()), false);

                fi.setFileName(task.getFilename());

                fi.setFileStatus(task.getStatus());
                fi.setFileDate(getDateTime(System.currentTimeMillis()));
                fi.setFileSize(GetSizeFromBytes(soFarBytes) + "/" + "---");
                fi.setFileSpeed("-");

                UpdateCurrentList();
            }

            @Override
            protected void completed(BaseDownloadTask task) {
                FileItem fi = GetFileModelByModel(
                        new FileItem(task.getId(), task.getFilename(),
                                100, task.getStatus(), "ETA",
                                GetSizeFromBytes(task.getSmallFileTotalBytes()), "Speed", getDateTime(System.currentTimeMillis()),
                                task.getUrl(), task.getPath(), task.getFilename()), false);

                fi.setFileName(task.getFilename());
                fi.setFileProgress(100);
                fi.setFileStatus(task.getStatus());
                fi.setFileDate(getDateTime(System.currentTimeMillis()));
                fi.setFileSize(GetSizeFromBytes(task.getSmallFileTotalBytes()));
                fi.setFileSpeed("-");

                fi_DOWN.remove(fi);
                fi_COMP.add(fi);

                UpdateCurrentList();

                String[] columns = {Rows.fileName, Rows.fileProgress, Rows.fileStatus, Rows.fileSize, Rows.fileDate};
                String[] columnVals = {fi.getFileName(), 100 + "", "" + task.getStatus(),
                        fi.getFileSize(), System.currentTimeMillis() + ""};

                String whereColumn = Rows.fileId;
                String[] whereArgs = {fi.getFileId() + ""};
                fdu.UpdateFileinDatabase(columns, columnVals, whereColumn, whereArgs);
                ShowFileInfoDialog(fi);

            }

            @Override
            protected void paused(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                FileItem fi = GetFileModelByModel(
                        new FileItem(task.getId(), task.getFilename(),
                                currentProgress(soFarBytes, totalBytes), task.getStatus(), "ETA",
                                GetSizeFromBytes(soFarBytes) + "/" + GetSizeFromBytes(totalBytes), "Speed",
                                getDateTime(System.currentTimeMillis()),
                                task.getUrl(), task.getPath(), task.getFilename()), false);
                if (fi == null)
                    fi = GetFileModelByModel(
                            new FileItem(task.getId(), task.getFilename(),
                                    currentProgress(soFarBytes, totalBytes), task.getStatus(), "ETA",
                                    GetSizeFromBytes(soFarBytes) + "/" + GetSizeFromBytes(totalBytes), "Speed",
                                    getDateTime(System.currentTimeMillis()),
                                    task.getUrl(), task.getPath(), task.getFilename()), true);

                fi.setFileName(task.getFilename());
                fi.setFileSpeed("-");
                fi.setFileStatus(task.getStatus());

                UpdateCurrentList();

                String[] columns = {Rows.fileName, Rows.fileProgress, Rows.fileStatus, Rows.fileSize};
                String[] columnVals = {fi.getFileName(), currentProgress(soFarBytes, totalBytes) + "",
                        "" + fi.getFileStatus(), GetSizeFromBytes(soFarBytes) + "/" + GetSizeFromBytes(totalBytes)};

                String whereColumn = Rows.fileId;
                String[] whereArgs = {fi.getFileId() + ""};
                fdu.UpdateFileinDatabase(columns, columnVals, whereColumn, whereArgs);

            }

            @Override
            protected void error(BaseDownloadTask task, Throwable e) {
                FileItem fi = GetFileModelByModel(
                        new FileItem(task.getId(), task.getFilename(),
                                0, task.getStatus(), "ETA", "Size", "Speed", getDateTime(System.currentTimeMillis()),
                                task.getUrl(), task.getPath(), task.getFilename()), false);
                if (fi == null)
                    fi = GetFileModelByModel(
                            new FileItem(task.getId(), task.getFilename(),
                                    0, task.getStatus(), "ETA",
                                    "---/" + GetSizeFromBytes(task.getSmallFileTotalBytes()), "Speed",
                                    getDateTime(System.currentTimeMillis()),
                                    task.getUrl(), task.getPath(), task.getFilename()), true);

                fi.setFileName(task.getFilename());
                fi.setFileStatus(task.getStatus());
                fi.setFileSpeed("-");

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


        fdl_youtube = new FileDownloadListener() {


            @Override
            protected void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {

                if (task.getFilename().contains("task1")) {
                    FileItem fi = GetFileModelByModel(
                            new FileItem(task.getId(), task.getFilename(),
                                    currentProgress(soFarBytes, totalBytes), task.getStatus(), "ETA",
                                    GetSizeFromBytes(soFarBytes) + "/" + GetSizeFromBytes(totalBytes), "Speed", "",
                                    task.getUrl(), task.getPath(), task.getFilename()), false);


                    fi.setFileName(task.getFilename());
                    fi.setFileStatus(task.getStatus());
                    fi.setFileDate(fi.getFileDate());
                    fi.setFileSize(GetSizeFromBytes(soFarBytes) + "/" + GetSizeFromBytes(totalBytes));
                    fi.setFileDName(task.getFilename());
                    fi.setFileSpeed("-");
                    fi.setFileUrl(task.getUrl());
                    String path = task.getPath().substring(0, task.getPath().lastIndexOf("/"));
                    fi.setFilePath(path);


                    UpdateCurrentList();


                    String[] columns = {Rows.fileName, Rows.fileStatus, Rows.fileUrl, Rows.filePath, Rows.fileDName};
                    String[] columnVals = {fi.getFileName(), "" + fi.getFileStatus(),
                            fi.getFileUrl(), fi.getFilePath(), task.getFilename()};

                    String whereColumn = Rows.fileId;
                    String[] whereArgs = {fi.getFileId() + ""};
                    fdu.UpdateFileinDatabase(columns, columnVals, whereColumn, whereArgs);
                }

                if (task.getFilename().contains("task2")) {

                }
            }

            @Override
            protected void started(BaseDownloadTask task) {
                FileItem fi = GetFileModelByModel(
                        new FileItem(task.getId(), task.getFilename(),
                                0, task.getStatus(), "ETA",
                                "---/---", "Speed", getDateTime(System.currentTimeMillis()),
                                task.getUrl(), task.getPath(), task.getFilename()), false);

                fi.setFileName(task.getFilename());
                fi.setFileStatus(task.getStatus());
                fi.setFileSpeed("-");

                String[] columns = {Rows.fileName, Rows.fileStatus, Rows.fileUrl, Rows.filePath};
                String[] columnVals = {fi.getFileName(), "" + fi.getFileStatus(), fi.getFileUrl(), fi.getFilePath()};

                String whereColumn = Rows.fileId;
                String[] whereArgs = {fi.getFileId() + ""};
                fdu.UpdateFileinDatabase(columns, columnVals, whereColumn, whereArgs);
            }

            @Override
            protected void connected(BaseDownloadTask task, String etag, boolean isContinue,
                                     int soFarBytes, int totalBytes) {
                FileItem fi = GetFileModelByModel(
                        new FileItem(task.getId(), task.getFilename(), currentProgress(soFarBytes, totalBytes),
                                task.getStatus(), "ETA",
                                GetSizeFromBytes(soFarBytes) + "/" + GetSizeFromBytes(totalBytes), "Speed",
                                getDateTime(System.currentTimeMillis()),
                                task.getUrl(), task.getPath(), task.getFilename()), false);

                fi.setFileName(task.getFilename());
                fi.setFileStatus(task.getStatus());
                fi.setFileSpeed("-");
                fi.setFileSize(GetSizeFromBytes(soFarBytes) + "/" + GetSizeFromBytes(totalBytes));


                String[] columns = {Rows.fileName, Rows.fileStatus, Rows.fileSize};
                String[] columnVals = {fi.getFileName(), "" + fi.getFileStatus(),
                        GetSizeFromBytes(soFarBytes) + "/" + GetSizeFromBytes(totalBytes)};

                String whereColumn = Rows.fileId;
                String[] whereArgs = {fi.getFileId() + ""};
                fdu.UpdateFileinDatabase(columns, columnVals, whereColumn, whereArgs);
            }

            @Override
            protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                FileItem fi = GetFileModelByModel(
                        new FileItem(task.getId(), task.getFilename(),
                                currentProgress(soFarBytes, totalBytes), task.getStatus(), "ETA",
                                GetSizeFromBytes(soFarBytes) + "/" + GetSizeFromBytes(totalBytes), "Speed",
                                getDateTime(System.currentTimeMillis()),
                                task.getUrl(), task.getPath(), task.getFilename()), false);

                fi.setFileName(task.getFilename());
                fi.setFileProgress(currentProgress(soFarBytes, totalBytes));
                fi.setFileStatus(task.getStatus());
                fi.setFileSize(GetSizeFromBytes(soFarBytes) + "/" + GetSizeFromBytes(totalBytes));
                fi.setFileSpeed(ConvertSpeed(task.getSpeed()));

                UpdateCurrentList();

                String[] columns = {Rows.fileName, Rows.fileProgress, Rows.fileStatus, Rows.fileSize};
                String[] columnVals = {fi.getFileName(), currentProgress(soFarBytes, totalBytes) + "",
                        "" + fi.getFileStatus(), GetSizeFromBytes(soFarBytes) + "/" + GetSizeFromBytes(totalBytes)};

                String whereColumn = Rows.fileId;
                String[] whereArgs = {fi.getFileId() + ""};
                fdu.UpdateFileinDatabase(columns, columnVals, whereColumn, whereArgs);
            }

            @Override
            protected void retry(BaseDownloadTask task, Throwable ex, int retryingTimes, int soFarBytes) {
                FileItem fi = GetFileModelByModel(
                        new FileItem(task.getId(), task.getFilename(),
                                0, task.getStatus(), "ETA",
                                GetSizeFromBytes(soFarBytes) + "/" + "---", "Speed", getDateTime(System.currentTimeMillis()),
                                task.getUrl(), task.getPath(), task.getFilename()), false);

                fi.setFileName(task.getFilename());

                fi.setFileStatus(task.getStatus());
                fi.setFileDate(getDateTime(System.currentTimeMillis()));
                fi.setFileSize(GetSizeFromBytes(soFarBytes) + "/" + "---");
                fi.setFileSpeed("-");

                UpdateCurrentList();
            }

            @Override
            protected void completed(BaseDownloadTask task) {
                FileItem fi = GetFileModelByModel(
                        new FileItem(task.getId(), task.getFilename(),
                                100, task.getStatus(), "ETA",
                                GetSizeFromBytes(task.getSmallFileTotalBytes()), "Speed", getDateTime(System.currentTimeMillis()),
                                task.getUrl(), task.getPath(), task.getFilename()), false);

                fi.setFileName(task.getFilename());
                fi.setFileProgress(100);
                fi.setFileStatus(task.getStatus());
                fi.setFileDate(getDateTime(System.currentTimeMillis()));
                fi.setFileSize(GetSizeFromBytes(task.getSmallFileTotalBytes()));
                fi.setFileSpeed("-");

                fi_DOWN.remove(fi);
                fi_COMP.add(fi);

                UpdateCurrentList();

                String[] columns = {Rows.fileName, Rows.fileProgress, Rows.fileStatus, Rows.fileSize, Rows.fileDate};
                String[] columnVals = {fi.getFileName(), 100 + "", "" + task.getStatus(),
                        fi.getFileSize(), System.currentTimeMillis() + ""};

                String whereColumn = Rows.fileId;
                String[] whereArgs = {fi.getFileId() + ""};
                fdu.UpdateFileinDatabase(columns, columnVals, whereColumn, whereArgs);
                ShowFileInfoDialog(fi);

            }

            @Override
            protected void paused(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                FileItem fi = GetFileModelByModel(
                        new FileItem(task.getId(), task.getFilename(),
                                currentProgress(soFarBytes, totalBytes), task.getStatus(), "ETA",
                                GetSizeFromBytes(soFarBytes) + "/" + GetSizeFromBytes(totalBytes), "Speed",
                                getDateTime(System.currentTimeMillis()),
                                task.getUrl(), task.getPath(), task.getFilename()), false);
                if (fi == null)
                    fi = GetFileModelByModel(
                            new FileItem(task.getId(), task.getFilename(),
                                    currentProgress(soFarBytes, totalBytes), task.getStatus(), "ETA",
                                    GetSizeFromBytes(soFarBytes) + "/" + GetSizeFromBytes(totalBytes), "Speed",
                                    getDateTime(System.currentTimeMillis()),
                                    task.getUrl(), task.getPath(), task.getFilename()), true);

                fi.setFileName(task.getFilename());
                fi.setFileSpeed("-");
                fi.setFileStatus(task.getStatus());

                UpdateCurrentList();

                String[] columns = {Rows.fileName, Rows.fileProgress, Rows.fileStatus, Rows.fileSize};
                String[] columnVals = {fi.getFileName(), currentProgress(soFarBytes, totalBytes) + "",
                        "" + fi.getFileStatus(), GetSizeFromBytes(soFarBytes) + "/" + GetSizeFromBytes(totalBytes)};

                String whereColumn = Rows.fileId;
                String[] whereArgs = {fi.getFileId() + ""};
                fdu.UpdateFileinDatabase(columns, columnVals, whereColumn, whereArgs);

            }

            @Override
            protected void error(BaseDownloadTask task, Throwable e) {
                FileItem fi = GetFileModelByModel(
                        new FileItem(task.getId(), task.getFilename(),
                                0, task.getStatus(), "ETA", "Size", "Speed", getDateTime(System.currentTimeMillis()),
                                task.getUrl(), task.getPath(), task.getFilename()), false);
                if (fi == null)
                    fi = GetFileModelByModel(
                            new FileItem(task.getId(), task.getFilename(),
                                    0, task.getStatus(), "ETA",
                                    "---/" + GetSizeFromBytes(task.getSmallFileTotalBytes()), "Speed",
                                    getDateTime(System.currentTimeMillis()),
                                    task.getUrl(), task.getPath(), task.getFilename()), true);

                fi.setFileName(task.getFilename());
                fi.setFileStatus(task.getStatus());
                fi.setFileSpeed("-");

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

    private FileDownloadQueueSet StartQueue(String link1, String name1, String name2, String link2) {
        final FileDownloadQueueSet queueSet = new FileDownloadQueueSet(fdl_youtube);

        final List<BaseDownloadTask> tasks = new ArrayList<>();

        tasks.add(FileDownloader.getImpl().
                create(link1)
                .setPath(PATH + File.separator + "SMDM-tmp" + File.separator + name1).setTag("videoFile"));
        tasks.add(FileDownloader.getImpl().
                create(link2)
                .setPath(PATH + File.separator + "SMDM-tmp" + File.separator + name2).setTag("audioFile"));

        queueSet.disableCallbackProgressTimes(); // do not want each task's download progress's callback,
        // we just consider which task will completed.

        // auto retry 1 time if download fail
        queueSet.setAutoRetryTimes(3);

        queueSet.downloadSequentially(tasks);
        queueSet.start();
        return queueSet;
    }

    private void PauseQueue(FileDownloadQueueSet queueSet) {

        BaseDownloadTask task1 = FileDownloader.getImpl().create(url_down).setPath(PATH).setListener(fdl_youtube);
        FileDownloadSerialQueue sd = new FileDownloadSerialQueue();

        sd.enqueue(task1);
        sd.resume();

    }

    private void ShowDialog(String title, String msg) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(title);
        alert.setMessage(msg);
        alert.setNegativeButton("Hide", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        alert.show();
    }

    private void Intenter(Bundle savedInstanceState, Intent in) {
        if (savedInstanceState == null && Intent.ACTION_SEND.equals(getIntent().getAction())
                && getIntent().getType() != null && "text/plain".equals(getIntent().getType())) {

            String videoLink = in.getStringExtra("videoLink");
            String videoName = in.getStringExtra("videoName");
            String audioLink = in.getStringExtra("audioLink");
            String audioName = in.getStringExtra("audioName");

            String ytLink = getIntent().getStringExtra(Intent.EXTRA_TEXT);
            if (!TextUtils.isEmpty(videoLink) && videoLink != null) {
                if (!TextUtils.isEmpty(audioLink) && !audioLink.equals("")) {
//                    ShowDialog(videoName, "VideoLink: "+videoLink+"\n\nVideo: "+videoName+"\n\nAudioLink: "+audioLink+"\n\nAudioName: "+audioName);
                    new GetNameFromUrl().execute("you", videoLink, videoName, audioLink, audioName);
                    return;
                } else {
//                    ShowDialog(videoName, "VideoLink: "+videoLink+"\nVideo: "+videoName);
                    new GetNameFromUrl().execute("you", videoLink, videoName);
                    return;
                }
            } else if (!TextUtils.isEmpty(audioLink) && !audioLink.equals("")) {
//                ShowDialog(audioName, "AudioLink: " + audioLink + "\nAudio: " + audioName);
                new GetNameFromUrl().execute("you", audioLink, audioName);
                return;
            } else {
                if (ytLink != null
                        && (ytLink.contains("://youtu.be/") || ytLink.contains("youtube.com/watch?v="))) {
                    OpenYoutubeVideo(ytLink);
                    return;
                } else {
                    //Snacked(ytLink, 1); //TODO implement webview to open webpages
                }
            }
        }

        if (in == null || in.getData() == null) {
            return;
        }

        Uri data = in.getData();
        if (data.getScheme().equals("http") || data.getScheme().equals("https")) {
            String link = data.getScheme() + "://" + data.getHost() + data.getPath();
            new GetNameFromUrl().execute("out", link, "");
        }
    }

    public String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
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

    public String GetSizeFromBytes(long bytes) {
        int unit = 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = ("KMGTPE").charAt(exp - 1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    public String ConvertSpeed(long KBSpeed) {
        long bytes = KBSpeed * 1024;
        int unit = 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = ("KMGTPE").charAt(exp - 1) + "";
        return String.format("%.1f %sB/s", bytes / Math.pow(unit, exp), pre);
    }

    public void ListFromDatabase() {


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
                FileItem fi = new FileItem(Integer.parseInt(maplist.get(i).get(Rows.fileId)),
                        maplist.get(i).get(Rows.fileName),
                        Integer.parseInt(maplist.get(i).get(Rows.fileProgress)), Byte.parseByte(maplist.get(i).get(Rows.fileStatus)), "-",
                        maplist.get(i).get(Rows.fileSize),
                        "-", getDateTime(Long.parseLong(maplist.get(i).get(Rows.fileDate))),
                        maplist.get(i).get(Rows.fileUrl),
                        maplist.get(i).get(Rows.filePath),
                        maplist.get(i).get(Rows.fileDName));

                if (fi.getFileProgress() < 100) {
                    fi.setFileStatus(FileDownloadStatus.paused);
                } else {
                    fi.setFileStatus(FileDownloadStatus.completed);
                }
                if (fi.getFileStatus() == FileDownloadStatus.completed) {
                    fi_COMP.add(0, fi);
                } else {
                    fi_DOWN.add(0, fi);
                }
                fi_ALL.add(0, fi);
            }
        }
    }

    private String getDateTime(long milliSeconds) {
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
            return GetMonth(askedForDate.get(Calendar.MONTH)) + " " + ChangeDate(askedForDate.get(Calendar.DATE) + "");
        else {
            int am = Calendar.AM;
            int current = askedForDate.get(Calendar.AM_PM);
            String ampm = "";
            if (am == current) ampm = "am";
            else ampm = "pm";
            return ChangeHour(askedForDate.get(Calendar.HOUR) + "") + ":" + ChangeMinute(askedForDate.get(Calendar.MINUTE) + "") + " " + ampm;
        }

    }

    private String ChangeDate(String date) {
        if (date.length() == 2 && date.charAt(0) == '1') {
            return date + "th";
        } else {
            if (date.charAt(date.length() - 1) == '1') {
                return date + "st";
            } else if (date.charAt(date.length() - 1) == '2') {
                return date + "nd";
            } else if (date.charAt(date.length() - 1) == '3') {
                return date + "rd";
            } else {
                return date + "th";
            }
        }
    }

    private String ChangeMinute(String min) {
        String rMin = "";

        if (min.length() == 1) {
            rMin = "0" + min;
            return rMin;
        }
        return min;
    }

    private String ChangeHour(String hour) {
        String rHour;
        if (hour.equals("0")) {
            rHour = "12";
            return rHour;
        } else {
            if (hour.length() == 1) {
                rHour = "0" + hour;
                return rHour;
            }
        }
        return hour;
    }

    private String GetMonth(int month) {
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

    private void showAboutDialog() {
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(context);
        View mView = layoutInflaterAndroid.inflate(R.layout.custom_about, null);

        final AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);

        dialog.setView(mView);

        String version = "";
        int version_code = 0;
        String appName = getResources().getString(R.string.app_name);
        String pkgName = "";
        try {
            PackageInfo pInfo = this.getPackageManager().getPackageInfo(getPackageName(), 0);
            pkgName = pInfo.packageName;
            version = pInfo.versionName;
            version_code = pInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        ListView list = mView.findViewById(R.id.ca_list);
        AboutListAdapter arrayAdapter;

        if (AppInstalled("net.smtechies.youtubedownloader")) {
            String[] titles = {"Application", "Version", "Version Code", "Package", "Programmer",
                    "Email", "FlexibleAdapter by davideas", "FileDownloader by lingochamp",
                    "android-youtubeExtractor by HaarigerHarald"};
            String[] subTitles = {appName, version, version_code + "", pkgName, "Faraz Ahmad",
                    "imfaraz101@gmail.com", "https://github.com/davideas/FlexibleAdapter",
                    "https://github.com/lingochamp/FileDownloader/",
                    "https://github.com/HaarigerHarald/android-youtubeExtractor"};
            arrayAdapter = new AboutListAdapter(
                    this, titles, subTitles);
        } else {
            String[] titles = {"Application", "Version", "Version Code", "Package", "Programmer",
                    "Email", "FlexibleAdapter by davideas", "FileDownloader by lingochamp"};
            String[] subTitles = {appName, version, version_code + "", pkgName, "Faraz Ahmad",
                    "imfaraz101@gmail.com", "https://github.com/davideas/FlexibleAdapter",
                    "https://github.com/lingochamp/FileDownloader/"};
            arrayAdapter = new AboutListAdapter(
                    this, titles, subTitles);
        }
        list.setAdapter(arrayAdapter);

        dialog.setPositiveButton("Hide", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        dialog.show();

    }

    private void showDownloadDialog() {
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(context);
        View mView = layoutInflaterAndroid.inflate(R.layout.custom_dialog, null);

        final BottomSheetDialog dialog = new BottomSheetDialog(MainActivity.this);

        dialog.setContentView(mView);

        final EditText etUrl = mView.findViewById(R.id.etUrl);
        etUrl.setMaxLines(5);
        Button cd_Cancel = mView.findViewById(R.id.cd_cancel_btn);
        Button cd_Down = mView.findViewById(R.id.cd_down_btn);

        cd_Cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        cd_Down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(etUrl.getText().toString())) {
                    Snacked("Link not found", 0);
                } else {
                    if (etUrl.getText().toString() != null
                            && (etUrl.getText().toString().contains("://youtu.be/")
                            || etUrl.getText().toString().contains("youtube.com/watch?v="))) {
                        OpenYoutubeVideo(etUrl.getText().toString());
                    } else {
                        new GetNameFromUrl().execute("in", etUrl.getText().toString(), "");
                        dialog.dismiss();
                    }
                    dialog.dismiss();
                }
            }
        });
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = clipboard.getPrimaryClip();
        if (clip == null || clip.getItemCount() == 0 || clip.getItemCount() > 0
                && clip.getItemAt(0).getText() == null) {
            etUrl.setText("");
        } else {
            etUrl.setText(clip.getItemAt(clip.getItemCount() - 1).getText().toString());
        }


        etUrl.setHint("Enter Download Link");
        dialog.show();

    }

    private void OpenYoutubeVideo(String link) {
        if (AppInstalled("net.smtechies.youtubedownloader")) {
            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setClassName("net.smtechies.youtubedownloader", "net.smtechies.youtubedownloader.DownloadActivity");
            sharingIntent.setType("text/plain");
            sharingIntent.putExtra(Intent.EXTRA_TEXT, link);
            startActivity(sharingIntent);
        } else {
            Snacked("Can't download Youtube Videos", 0);
        }
    }

    private boolean AppInstalled(String uri) {
        PackageManager pm = getPackageManager();
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
        }

        return false;
    }

    private void showDownloadDialog(final String url, final String name, final String size) {
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(context);
        View mView = layoutInflaterAndroid.inflate(R.layout.custom_dialog_name, null);

        final BottomSheetDialog dialog = new BottomSheetDialog(MainActivity.this);
        dialog.setContentView(mView);

        final EditText etLink = mView.findViewById(R.id.cdn_et_link);
        final EditText etName = mView.findViewById(R.id.cdn_etName);
        TextView tvSize = mView.findViewById(R.id.cdn_tv_size);
        final TextView tvPath = mView.findViewById(R.id.cdn_tv_path);

        etLink.setText(url);
        etLink.setEnabled(false);
        tvSize.setText(GetSizeFromBytes(Long.parseLong(size)));
        tvPath.setText(PATH);
        etName.setText(name);

        etName.setMaxLines(5);

        Button cdn_Browse = mView.findViewById(R.id.cdn_browse_btn);
        Button cdn_Cancel = mView.findViewById(R.id.cdn_cancel_btn);
        Button cdn_Down = mView.findViewById(R.id.cdn_down_btn);
        final CheckBox cbn_forceDownload = mView.findViewById(R.id.cdn_forceDownload_cb);

        cdn_Browse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snacked("Browser not implemented yet!!!", 0);
            }
        });
        cdn_Cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        cdn_Down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean forceDownload = cbn_forceDownload.isChecked();
                if (TextUtils.isEmpty(etName.getText().toString())) {
                    Snacked("File Name Empty!\nEnter Name to continue!", 1);
                } else {
                    ToggleDownload(etLink.getText().toString(), tvPath.getText().toString(),
                            etName.getText().toString(), forceDownload, false);
                    dialog.dismiss();
                }
            }
        });

        dialog.show();

    }

    private void ShowFileInfoDialog(FileItem fia) {

        final FileItem fi = GetFileModelByModel(fia, false);
        fileDialogView = layoutInflater.inflate(R.layout.custom_file_info_dialog, null, false);

        final BottomSheetDialog dialog = new BottomSheetDialog(MainActivity.this);
        dialog.setContentView(fileDialogView);

        fd_FileName = fileDialogView.findViewById(R.id.cfid_tv_fileName);
        fd_FilePath = fileDialogView.findViewById(R.id.cfid_tv_Path);
        fd_FileDate = fileDialogView.findViewById(R.id.cfid_tv_Date);

        cfid_FileOpenBTN = fileDialogView.findViewById(R.id.cfid_btn_open);
        cfid_FileDeleteBTN = fileDialogView.findViewById(R.id.cfid_btn_delete);
        cfid_HideBTN = fileDialogView.findViewById(R.id.cfid_btn_hide);


        fd_FileName.setText(fi.getFileName());
        fd_FileDate.setText(fi.getFileDate());
        fd_FilePath.setText(fi.getFilePath());

        cfid_FileOpenBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
                dialog.dismiss();
                startActivity(i);

            }
        });


        cfid_FileDeleteBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File file = new File(fi.getFilePath() + File.separator + fi.getFileName());
                if (file.exists()) {
                    if (file.delete()) {
                        fi_ALL.remove(fi);


                        if (fi_DOWN.contains(fi))
                            fi_DOWN.remove(fi);

                        if (fi_COMP.contains(fi))
                            fi_COMP.remove(fi);

                        String whereColumn = Rows.fileId;
                        String[] whereArgs = {String.valueOf(fi.getFileId())};

                        fdu.DeleteFileFromDatabase(whereColumn, whereArgs);
                        Snacked(fi.getFileName() + " Deleted", 0);
                    } else {
                        Snacked("Deletion failed!", 0);
                    }
                } else {
                    Snacked(fi.getFileName() + " Not Found\n" +
                            "Deleting from list", 0);

                    fi_ALL.remove(fi);

                    String whereColumn = Rows.fileId;
                    String[] whereArgs = {String.valueOf(fi.getFileId())};

                    fdu.DeleteFileFromDatabase(whereColumn, whereArgs);

                    if (fi_DOWN.contains(fi))
                        fi_DOWN.remove(fi);

                    if (fi_COMP.contains(fi))
                        fi_COMP.remove(fi);

                    Snacked(fi.getFileName() + " removed from list", 0);
                }
                dialog.dismiss();
            }
        });

        cfid_HideBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void otherNotification(int id) {
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

    private Notification.Builder ShowNotification() {
        Notification.InboxStyle inboxStyle = new Notification.InboxStyle();
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);


        NotificationManager nManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Notification.Builder builder = new Notification.Builder(context);
        builder.setContentTitle("Title");
        builder.setContentText("ContentText");
        builder.setSmallIcon(R.mipmap.ic_launcher_round);
        builder.setLargeIcon(bitmap);
        builder.setProgress(100, 50, true);
        builder.setAutoCancel(true);
        inboxStyle.setBigContentTitle("BigContent");
        inboxStyle.addLine("Line");
        builder.setStyle(inboxStyle);
        return builder;
    }

    private void Snacked(String Message, int length) {
        if (length <= 0)
            length = Toast.LENGTH_SHORT;
        else
            length = Toast.LENGTH_LONG;

        Toast.makeText(context, Message, length).show();
    }

    private int currentProgress(int soFarBytes, int totalBytes) {
        int prog = 0;
        if (totalBytes != 0)
            prog = (int) (((double) soFarBytes / totalBytes) * 100);
        return prog;
    }

    private boolean hasIdInFileModel(ArrayList<FileItem> fileModel, int id) {
        for (int i = 0; i < fileModel.size(); i++) {
            if (fileModel.get(i).getFileId() == id) {
                return true;
            }
        }
        return false;
    }

    private boolean hasFileModel(ArrayList<FileItem> fileModel, FileItem fm) {
        for (int i = 0; i < fileModel.size(); i++) {
            if (fileModel.get(i).getFileId() == fm.getFileId()) {
                return true;
            }
        }

        return false;
    }

    private boolean UpdateFileModel(FileItem fm) {

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

    private FileItem GetFileModelByModel(FileItem fi, boolean createNewIfNoTFound) {

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

    private FileItem GetFileModelById(int id, boolean createNewIfNoTFound) {

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

    private FileItem CreateNewFileModel(FileItem fi) {
        String[] insertData = {fi.getFileId() + "",
                fi.getFileName(), fi.getFileProgress() + "", fi.getFileSize(), FileDownloadStatus.pending + "",
                "" + System.currentTimeMillis(), fi.getFileUrl(),
                fi.getFilePath(), fi.getFileDName()};
        new InsertDataIntoDB(db).execute(insertData);
        fi_DOWN.add(fi);
        fi_ALL.add(fi);
        return fi;
    }

    private FileItem CreateNewFileModel(int id) {
        String[] insertData = {id + "",
                "", "0", "-", FileDownloadStatus.pending + "",
                "" + System.currentTimeMillis(), "",
                "", ""};
        new InsertDataIntoDB(db).execute(insertData);
        FileItem fm = new FileItem(id, "", 0, FileDownloadStatus.pending, "",
                "", "", getDateTime(System.currentTimeMillis()),
                "", "", "");
        fi_DOWN.add(0, fm);
        fi_ALL.add(0, fm);
        return fm;
    }

    private void ToggleDownload(String fileUrl, String filePath, String fileName,
                                boolean forceDownload, boolean resume) {
        //Snacked(fileName + "\n\n" + fileUrl + "\n\n" + forceDownload + "", 1);

        String path = filePath + File.separator + fileName;

        int id = FileDownloadUtils.generateId(fileUrl, path);
        boolean fileInModel = hasIdInFileModel(fi_ALL, id);
        if (fileInModel)
            return;
        boolean fileExists = FileExists(path);
        if (forceDownload) {
            FileItem fi = GetFileModelById(id, true);
            fi.setFileUrl(fileUrl);
            fi.setFilePath(PATH);
            fi.setFileName(fileName);

            fileId = FileDownloader.getImpl()
                    .create(fi.getFileUrl())
                    .setForceReDownload(forceDownload)
                    .setPath(path)
                    .setMinIntervalUpdateSpeed(1000)
                    .setListener(fdl)
                    .start();

            return;
        } else {
            if (fileExists) {
                Snacked(fileName + "\nalready downloaded!!!", 1);
                return;
            } else {
                if (fileInModel) {
                    FileItem fi = GetFileModelById(id, !fileInModel);
                    byte status = fi.getFileStatus();
                    if (status == FileDownloadStatus.completed || status == FileDownloadStatus.blockComplete) {
                        Snacked("File already downloaded\n" +
                                "To Re-Download, select Force_Download", 1);
                        return;
                    } else if (status == FileDownloadStatus.paused || status == FileDownloadStatus.error) {
                        fileId = FileDownloader.getImpl()
                                .create(fi.getFileUrl())
                                .setForceReDownload(false)
                                .setPath(fi.getFilePath() + File.separator + fi.getFileName())
                                .setMinIntervalUpdateSpeed(1000)
                                .setListener(fdl)
                                .start();
                        return;
                    } else if (status == FileDownloadStatus.pending ||
                            status == FileDownloadStatus.started ||
                            status == FileDownloadStatus.connected ||
                            status == FileDownloadStatus.progress ||
                            status == FileDownloadStatus.retry) {
                        FileDownloader.getImpl().pause(id);
                        return;
                    }
                    return;
                }

                FileItem fi = GetFileModelById(id, true);
                fi.setFileUrl(fileUrl);
                fi.setFilePath(PATH);
                fi.setFileName(fileName);

                fileId = FileDownloader.getImpl()
                        .create(fi.getFileUrl())
                        .setForceReDownload(false)
                        .setPath(path)
                        .setMinIntervalUpdateSpeed(1000)
                        .setListener(fdl)
                        .start();
            }
        }
    }

    private void StartYouTubeDownload(String[] file1, String[] file2, long totalSize) {

        task1 = FileDownloader.getImpl().create(file1[1]).
                setPath(PATH + File.separator + file1[0]).setListener(fdl_youtube).setAutoRetryTimes(3).setTag("[task1]");

        task2 = FileDownloader.getImpl().create(file2[1]).
                setPath(PATH + File.separator + file2[0]).setListener(fdl_youtube).setAutoRetryTimes(3).setTag("[task2]");


        boolean file1Exist = FileExists(PATH + File.separator + file1[0]);
        boolean file2Exist = FileExists(PATH + File.separator + file2[0]);

        if (file1Exist && new File(PATH + File.separator + file1[1]).length() > Long.parseLong(file1[3])) {

        } else {

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

    private boolean FileExists(String path) {
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
                //startActivity(new Intent(this, Settings.class));
                Snacked("Settings not implemented yet!", 1);
                break;
            case R.id.action_exit:
                KillApp();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void KillApp() {
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
            toolbar.setTitle("All");
            UpdateCurrentList();
        } else if (id == R.id.nav_down) {
            currentScreen = 1;
            UpdateCurrentList();
            toolbar.setTitle("Downloading");
        } else if (id == R.id.nav_comp) {
            currentScreen = 2;
            UpdateCurrentList();
            toolbar.setTitle("Completed");
        } else if (id == R.id.nav_feedback) {
            StringBuilder builder = new StringBuilder();
            builder.append("android : ").append(Build.VERSION.RELEASE);

            Field[] fields = Build.VERSION_CODES.class.getFields();
            for (Field field : fields) {
                String fieldName = field.getName();
                int fieldValue = -1;

                try {
                    fieldValue = field.getInt(new Object());
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }

                if (fieldValue == Build.VERSION.SDK_INT) {
                    builder.append(" : ").append(fieldName).append(" : ");
                    builder.append("sdk=").append(fieldValue);
                }
            }

            Intent in = new Intent(Intent.ACTION_SEND);
            in.setType("text/plain");

            in.putExtra(android.content.Intent.EXTRA_EMAIL,
                    new String[]{"feedback@smtechies.me"});

            in.putExtra(android.content.Intent.EXTRA_SUBJECT,
                    "SM Download Manager - Feedback");


            String sdk = builder.toString();
            String device = android.os.Build.DEVICE;
            String model = android.os.Build.MODEL;
            String product = android.os.Build.PRODUCT;

            in.putExtra(android.content.Intent.EXTRA_TEXT,
                    sdk +
                            "\nDEVICE: " + device +
                            "\nMODEL: " + model +
                            "\nPRODUCT: " + product +
                            "\n_____________\n");

            startActivity(Intent.createChooser(
                    in, "Send mail..."));
        } else if (id == R.id.nav_about) {
            showAboutDialog();
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

        private ProgressDialog pd;

        GetNameFromUrl() {
            pd = new ProgressDialog(context);
            pd.setIcon(R.mipmap.ic_launcher);
            pd.setMax(100);
            pd.setMessage("Please wait...");
            pd.setTitle("Getting File Info...");
            pd.show();
        }

        @Override
        protected String[] doInBackground(String... params) {
            try {

                if (params.length == 3)
                    return GetName(params, false);
                else if (params.length == 5)
                    return GetName(params, true);
                return null;
            } catch (NullPointerException e) {
                String[] result = {"Exception", "NullPointerException", e.getMessage()};
                return result;
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            pd.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(String[] result) {
            pd.dismiss();
            if (result != null) {
                String[] fileInfo = result;

                if (fileInfo[0].equals("file")) {
                    if (fileInfo.length == 4) {
                        showDownloadDialog(fileInfo[2], fileInfo[1], fileInfo[3]);
//                        ShowDialog(fileInfo[1], "FileName: " + fileInfo[1] + "\n\nFileLink: " +
// fileInfo[2] + "\n\nFileSize: " + GetSizeFromBytes(Long.parseLong(fileInfo[3])));
                    } else {

                        String[] file1 = {fileInfo[1], fileInfo[2], fileInfo[3], fileInfo[4]};
                        String[] file2 = {fileInfo[5], fileInfo[6], fileInfo[7], fileInfo[8]};
                        long totalSize = Long.parseLong(fileInfo[3]) + Long.parseLong(fileInfo[7]);
                        StartYouTubeDownload(file1, file2, totalSize);
//                        ShowDialog("YouTube",
//                                "FirstFileName: " + fileInfo[1] +
//                                        "\n\nFirstFileLink: " + fileInfo[2] +
//                                        "\n\nFirstFileSize: " + GetSizeFromBytes(Long.parseLong(fileInfo[3])) +
//                                        "\n\nFirstFileExt: " + fileInfo[4] +
//                                        "\n\nSecondFileName: " + fileInfo[5] +
//                                        "\n\nSecondFileLink: " + fileInfo[6] +
//                                        "\n\nSecondFileSize: " + GetSizeFromBytes(Long.parseLong(fileInfo[7])) +
//                                        "\n\nSecondFileExt: " + fileInfo[8] +
//                                        "\n\nTotalSize: " + GetSizeFromBytes(totalSize));
                    }

                } else {
                    Snacked(result[0] + "\n\n" + result[1] + "\n\n" + result[2], 1);
                }

            }
        }

        String[] GetName(String[] params, boolean twoFiles) {
            String firstFileName;
            String firstFileSize;
            String firstFileType;

            String secondFileName = "";
            String secondFileSize = "";
            String secondFileType = "";
            String link = null;
            try {

                String[] firstFile = GettingName(params[1]);

                if (params.length > 3) {
                    link = params[3];
                }
                String[] secondFile = (link != null) ? GettingName(link) : null;
                if (twoFiles) {
                    if (secondFile != null) {
                        secondFileName = params[4];
                        secondFileSize = secondFile[1];
                        secondFileType = secondFile[2];
                    }
                }
                if (!TextUtils.isEmpty(params[2])) {
                    firstFileName = params[2];
                } else {
                    firstFileName = firstFile[0];
                }

                firstFileSize = firstFile[1];
                firstFileType = firstFile[2];

                firstFileName = firstFileName.replace("%20", " ");
                publishProgress(80);
                String firstFileExt = firstFileName.substring(firstFileName.lastIndexOf('.') + 1, firstFileName.length());
                String secondFileExt = (twoFiles) ? secondFileName.substring(secondFileName.lastIndexOf('.') + 1, secondFileName.length()) : "";
                publishProgress(90);
                if (twoFiles) {
                    String[] result = {"file", firstFileName, /*firstFileLink*/ params[1], firstFileSize, firstFileExt, secondFileName, /*secondFileLink*/ link, secondFileSize, secondFileExt};
                    publishProgress(100);
                    return result;
                } else {
                    String[] result = {"file", firstFileName, /*firstFileLink*/ params[1], firstFileSize};
                    publishProgress(100);
                    return result;
                }
            } catch (IOException e) {
                String[] result = {"Exception", "IOException", e.getLocalizedMessage()};
                return result;
            } catch (NullPointerException e) {
                String[] result = {"Exception", "NullPointerException", e.getLocalizedMessage()};
                return result;
            }
        }

        String[] GettingName(String url) throws IOException {
            HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
            publishProgress(10);
            int responseCode = con.getResponseCode();
            String disposition = "";
            String fileName = null;
            String contentType = null;

            int contentLength = 0;
            publishProgress(20);
            if (responseCode == HttpURLConnection.HTTP_OK) {
                disposition = con.getHeaderField("Content-Disposition");
                contentType = con.getContentType();
                contentLength = con.getContentLength();
                publishProgress(60);
                if (disposition != null) {
                    int index = disposition.indexOf("filename=");
                    if (index > 0) {

                        fileName = disposition.substring(index + 10, disposition.length() - 1);
                        publishProgress(70);

                    }
                } else {
                    fileName = url.substring(url.lastIndexOf("/") + 1, url.length());
                    publishProgress(70);
                }
            }
            return new String[]{fileName, contentLength + "", contentType};
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
                        if (selectedList.get(i).getFileStatus() != FileDownloadStatus.completed ||
                                selectedList.get(i).getFileStatus() != FileDownloadStatus.blockComplete ||
                                selectedList.get(i).getFileStatus() != FileDownloadStatus.paused ||
                                selectedList.get(i).getFileStatus() != FileDownloadStatus.error) {

                            FileDownloader.getImpl().pause((int) selectedList.get(i).getFileId());

                            FileDownloader.getImpl().clear((int) selectedList.get(i).getFileId(),
                                    selectedList.get(i).getFilePath() + File.separator + selectedList.get(i).getFileName());

                        }
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