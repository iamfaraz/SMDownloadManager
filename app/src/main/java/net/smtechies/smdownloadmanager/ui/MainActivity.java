package net.smtechies.smdownloadmanager.ui;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
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
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.SelectableAdapter.Mode;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {


    private final String PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
    private final int PERMISSIONS_WRITE_EXTERNAL_STORAGE = 101;
    FileDownloadListener fdl;
    private EditText etUrl;
    private CheckBox cb_forceDownload;
    private Button cd_Cancel, cd_Down;
    private LayoutInflater layoutInflater;
    private View fileDialogView;

    private TextView fd_FileName, fd_FilePath, fd_FileDate;
    private Button cfid_FileOpenBTN, cfid_FileDeleteBTN, cfid_HideBTN;

    private NavigationView navigationView;
    private ImageButton ib_Play;
    private String url_down =
            //"http://files.funmaza.info/download/2ed3ab958afb3f78f9bac1fa3708ffeb";
            "http://videos.funmaza.info/storage/0517/Vitamin%20D%201080p%20-%20Ludacris%20feat.%20Ty%20Dolla%20Sign%20FunmazaHD.mp4";
    private String[] fileContent;
    private String fileName;
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

        Intenter(getIntent());
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

    void Init() {

        fi_ALL = new ArrayList<>();
        fi_DOWN = new ArrayList<>();
        fi_COMP = new ArrayList<>();


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
                    if (fi.getFileStatus() == FileDownloadStatus.completed || fi.getFileStatus() == FileDownloadStatus.blockComplete) {
                        ShowFileInfoDialog(fi);
                    } else if (fi.getFileStatus() == FileDownloadStatus.paused || fi.getFileStatus() == FileDownloadStatus.error) {
                        Snacked("Resuming " + fi.getFileName(), 0);
                        FileDownloader.getImpl()
                                .create(fi.getFileUrl())
                                .setForceReDownload(false)
                                .setPath(fi.getFilePath() + File.separator + fi.getFileName())
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
                        new FileItem(task.getId(), TextUtils.isEmpty(fileName) ? task.getFilename() : fileName,
                                currentProgress(soFarBytes, totalBytes), task.getStatus(), "ETA",
                                size(soFarBytes) + "/" + size(totalBytes), "Speed", getDateTime(System.currentTimeMillis()),
                                task.getUrl(), task.getPath(), task.getFilename()), false);


                fi.setFileName(TextUtils.isEmpty(fileName) ? task.getFilename() : fileName);
                fi.setFileStatus(task.getStatus());
                fi.setFileDate(getDateTime(System.currentTimeMillis()));
                fi.setFileSize(size(soFarBytes) + "/" + size(totalBytes));
                fi.setFileDName(task.getFilename());
                fi.setFileSpeed("-");
                fi.setFileUrl(task.getUrl());
                String path = task.getPath().substring(0, task.getPath().lastIndexOf("/"));
                fi.setFilePath(path);


                UpdateCurrentList();


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
                                task.getUrl(), task.getPath(), task.getFilename()), false);

                fi.setFileName(TextUtils.isEmpty(fileName) ? task.getFilename() : fileName);
                fi.setFileStatus(task.getStatus());
                fi.setFileSpeed("-");

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
                                task.getUrl(), task.getPath(), task.getFilename()), false);

                fi.setFileName(TextUtils.isEmpty(fileName) ? task.getFilename() : fileName);
                fi.setFileStatus(task.getStatus());
                fi.setFileSpeed("-");
                fi.setFileSize(size(soFarBytes) + "/" + size(totalBytes));


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
                                task.getUrl(), task.getPath(), task.getFilename()), false);

                fi.setFileName(TextUtils.isEmpty(fileName) ? task.getFilename() : fileName);
                fi.setFileProgress(currentProgress(soFarBytes, totalBytes));
                fi.setFileStatus(task.getStatus());
                fi.setFileSize(size(soFarBytes) + "/" + size(totalBytes));
                fi.setFileSpeed(speed(task.getSpeed()));

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
                                task.getUrl(), task.getPath(), task.getFilename()), false);

                fi.setFileName(TextUtils.isEmpty(fileName) ? task.getFilename() : fileName);

                fi.setFileStatus(task.getStatus());
                fi.setFileDate(getDateTime(System.currentTimeMillis()));
                fi.setFileSize(size(soFarBytes) + "/" + "---");
                fi.setFileSpeed("-");

                UpdateCurrentList();
            }

            @Override
            protected void completed(BaseDownloadTask task) {
                FileItem fi = GetFileModelByModel(
                        new FileItem(task.getId(), TextUtils.isEmpty(fileName) ? task.getFilename() : fileName,
                                100, task.getStatus(), "ETA",
                                size(task.getSmallFileTotalBytes()), "Speed", getDateTime(System.currentTimeMillis()),
                                task.getUrl(), task.getPath(), task.getFilename()), false);

                fi.setFileName(TextUtils.isEmpty(fileName) ? task.getFilename() : fileName);
                fi.setFileProgress(100);
                fi.setFileStatus(task.getStatus());
                fi.setFileDate(getDateTime(System.currentTimeMillis()));
                fi.setFileSize(size(task.getSmallFileTotalBytes()));
                fi.setFileSpeed("-");

                fi_DOWN.remove(fi);
                fi_COMP.add(fi);

                UpdateCurrentList();

                String[] columns = {Rows.fileName, Rows.fileProgress, Rows.fileStatus, Rows.fileSize, Rows.fileDate};
                String[] columnVals = {fi.getFileName(), 100 + "", "" + task.getStatus(), fi.getFileSize(), fi.getFileDate()};

                String whereColumn = Rows.fileId;
                String[] whereArgs = {fi.getFileId() + ""};
                fdu.UpdateFileinDatabase(columns, columnVals, whereColumn, whereArgs);
                ShowFileInfoDialog(fi);

            }

            @Override
            protected void paused(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                FileItem fi = GetFileModelByModel(
                        new FileItem(task.getId(), TextUtils.isEmpty(fileName) ? task.getFilename() : fileName,
                                currentProgress(soFarBytes, totalBytes), task.getStatus(), "ETA",
                                size(soFarBytes) + "/" + size(totalBytes), "Speed", getDateTime(System.currentTimeMillis()),
                                task.getUrl(), task.getPath(), task.getFilename()), false);
                if (fi == null)
                    fi = GetFileModelByModel(
                            new FileItem(task.getId(), TextUtils.isEmpty(fileName) ? task.getFilename() : fileName,
                                    currentProgress(soFarBytes, totalBytes), task.getStatus(), "ETA",
                                    size(soFarBytes) + "/" + size(totalBytes), "Speed", getDateTime(System.currentTimeMillis()),
                                    task.getUrl(), task.getPath(), task.getFilename()), true);

                fi.setFileName(TextUtils.isEmpty(fileName) ? task.getFilename() : fileName);
                fi.setFileSpeed("-");
                fi.setFileStatus(task.getStatus());

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
                                task.getUrl(), task.getPath(), task.getFilename()), false);
                if (fi == null)
                    fi = GetFileModelByModel(
                            new FileItem(task.getId(), TextUtils.isEmpty(fileName) ? task.getFilename() : fileName,
                                    0, task.getStatus(), "ETA",
                                    "---/" + size(task.getSmallFileTotalBytes()), "Speed", getDateTime(System.currentTimeMillis()),
                                    task.getUrl(), task.getPath(), task.getFilename()), true);

                fi.setFileName(TextUtils.isEmpty(fileName) ? task.getFilename() : fileName);
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

    private void Intenter(Intent in) {
        if (in == null || in.getData() == null) {
            return;
        }

        Uri data = in.getData();

        if (data != null) {
            String dataString = "";
            if (data.getScheme().equals("http") || data.getScheme().equals("https")) {
                String url = data.getScheme() + "://" + data.getHost() + data.getPath();
                showDownloadDialog(url);
            }
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
                    FileItem fi = new FileItem(Integer.parseInt(maplist.get(i).get(Rows.fileId)),
                            maplist.get(i).get(Rows.fileName),
                            Integer.parseInt(maplist.get(i).get(Rows.fileProgress)), Byte.parseByte(maplist.get(i).get(Rows.fileStatus)), "-",
                            maplist.get(i).get(Rows.fileSize),
                            "-", getDateTime(Long.parseLong(maplist.get(i).get(Rows.fileDate))),
                            maplist.get(i).get(Rows.fileUrl),
                            maplist.get(i).get(Rows.filePath),
                            maplist.get(i).get(Rows.fileDName));

                    if (fi.getFileStatus() == FileDownloadStatus.completed) {
                        fi_COMP.add(fi);
                    } else {
                        if (fi.getFileStatus() != FileDownloadStatus.error &&
                                fi.getFileStatus() != FileDownloadStatus.paused &&
                                fi.getFileStatus() != FileDownloadStatus.completed &&
                                fi.getFileStatus() != FileDownloadStatus.blockComplete) {
                            fi.setFileStatus(FileDownloadStatus.paused);
                        }
                        fi_DOWN.add(fi);
                    }
                    fi_ALL.add(fi);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Snacked("ListFromDatabase():\n" + e.getLocalizedMessage(), 0);
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

        final BottomSheetDialog dialog = new BottomSheetDialog(MainActivity.this);
        dialog.setContentView(mView);

        etUrl = mView.findViewById(R.id.etUrl);
        etUrl.setMaxLines(5);
        cd_Cancel = mView.findViewById(R.id.cd_cancel_btn);
        cd_Down = mView.findViewById(R.id.cd_down_btn);
        cb_forceDownload = mView.findViewById(R.id.cb_forceDownload);

        cd_Cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        cd_Down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean forceDownload = cb_forceDownload.isChecked();
                new GetNameFromUrl().execute(etUrl.getText().toString(), forceDownload + "");
                dialog.dismiss();
            }
        });

        etUrl.setText(url_down);

        etUrl.setHint("Enter Download Link");
        dialog.show();

    }

    public void showDownloadDialog(String url) {
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(context);
        View mView = layoutInflaterAndroid.inflate(R.layout.custom_dialog, null);

        final BottomSheetDialog dialog = new BottomSheetDialog(MainActivity.this);
        dialog.setContentView(mView);

        etUrl = mView.findViewById(R.id.etUrl);
        etUrl.setMaxLines(5);
        cd_Cancel = mView.findViewById(R.id.cd_cancel_btn);
        cd_Down = mView.findViewById(R.id.cd_down_btn);
        cb_forceDownload = mView.findViewById(R.id.cb_forceDownload);

        cd_Cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        cd_Down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean forceDownload = cb_forceDownload.isChecked();
                new GetNameFromUrl().execute(etUrl.getText().toString(), forceDownload + "");
                dialog.dismiss();
            }
        });

        etUrl.setText(url);

        etUrl.setHint("Enter Download Link");
        dialog.show();

    }

    public void ShowFileInfoDialog(final FileItem fi) {
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
        String path = fi.getFilePath().substring(0, fi.getFilePath().lastIndexOf("/"));
        fd_FilePath.setText(path);

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

                        String whereColumn = Rows.fileId;
                        String[] whereArgs = {String.valueOf(fi.getFileId())};

                        fdu.DeleteFileFromDatabase(whereColumn, whereArgs);

                        if (fi_DOWN.contains(fi))
                            fi_DOWN.remove(fi);

                        if (fi_COMP.contains(fi))
                            fi_COMP.remove(fi);

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
                "", "", "");
        fi_DOWN.add(fm);
        fi_ALL.add(fm);
        return fm;
    }

    public void ToggleDownload(String fileUrl, String filePath, String fileName, boolean forceDownload, boolean resume) {
        //Snacked(fileName + "\n\n" + fileUrl + "\n\n" + forceDownload + "", 1);

        String path = filePath + File.separator + fileName;

        int id = FileDownloadUtils.generateId(fileUrl, path);
        boolean fileInModel = hasIdInFileModel(fi_ALL, id);
        Snacked(fileInModel + "", 0);
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
                        .setListener(fdl)
                        .start();

            }
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

        private ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            pd = new ProgressDialog(context);
            pd.setIcon(R.mipmap.ic_launcher);
            pd.setMax(100);
            pd.setMessage("Please wait.....");
            pd.setTitle("Getting name....");
            pd.show();
        }

        @Override
        protected String[] doInBackground(String... params) {

            String filename = "";
            publishProgress(0);
            try {
                filename = GettingName(params[0]);

                filename = filename.replace("%20", " ");
                publishProgress(80);
                String fileExt = filename.substring(filename.lastIndexOf('.') + 1, filename.length());
                publishProgress(90);
                String[] fileName = {"file", filename, fileExt, params[0], params[1]};
                publishProgress(100);
                return fileName;
            } catch (MalformedURLException e) {
                String[] error1 = {"Exception", "MalformedURLException", e.getMessage()};
                return error1;
            } catch (IOException e) {
                String[] error2 = {"Exception", "IOException", e.getMessage()};
                return error2;
            }

        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            pd.setProgress(values[0]);
        }

        String GettingName(String url) throws IOException {
            HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
            publishProgress(10);
            int responseCode = con.getResponseCode();
            String disposition = "";
            String fileName = null;
            publishProgress(20);
            if (responseCode == HttpURLConnection.HTTP_OK) {
                disposition = con.getHeaderField("Content-Disposition");
                String contentType = con.getContentType();
                int contentLength = con.getContentLength();
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
            return fileName;
        }

        @Override
        protected void onPostExecute(String[] result) {
            pd.dismiss();

            fileContent = result;
            String[] fileInfo = fileContent;
            if (fileInfo[0].equals("file")) {
                fileName = fileInfo[1];
                ToggleDownload(fileInfo[3], PATH, fileInfo[1], Boolean.parseBoolean(fileInfo[4]), false);
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