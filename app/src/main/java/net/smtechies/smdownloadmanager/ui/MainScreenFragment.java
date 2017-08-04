package net.smtechies.smdownloadmanager.ui;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadListener;
import com.liulishuo.filedownloader.FileDownloader;
import com.liulishuo.filedownloader.util.FileDownloadUtils;

import net.smtechies.smdownloadmanager.R;
import net.smtechies.smdownloadmanager.adapter.RecyclerViewAdapter;
import net.smtechies.smdownloadmanager.utils.FileDatabase;
import net.smtechies.smdownloadmanager.utils.FileDatabaseUtils;
import net.smtechies.smdownloadmanager.utils.FileModel;
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
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;

/**
 * Created by Faraz on 03/08/2017.
 */

public class MainScreenFragment extends Fragment implements View.OnClickListener {

    private static final String ARG_SECTION_NUMBER = "section_number";
    private String url_down = "http://files.mp3slash.net/download/8951408a5b5ddd596956ec83354c95e9";

    private Context context;


    private LinearLayoutManager lm_all;
    private LinearLayoutManager lm_down;
    private LinearLayoutManager lm_comp;

    private FileDownloadListener fdl_new;
    private FileDownloadListener fdl_old;

    private FileDatabase fileDatabase;
    private SQLiteDatabase db;
    private FileDatabaseUtils fdu;

    private HashMap<String, String>[] files;

    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder notificationBuilder;

    private ActionModeCallback actionModeCallback = new ActionModeCallback();
    private ActionMode actionMode;

    private ArrayList<FileModel> fm_All;
    private ArrayList<FileModel> fm_Downloading;
    private ArrayList<FileModel> fm_Completed;

    private RecyclerView rc_ALL;
    private RecyclerView rc_DOWNLOADING;
    private RecyclerView rc_COMPLETED;

    private RecyclerViewAdapter adapter_all;
    private RecyclerViewAdapter adapter_down;
    private RecyclerViewAdapter adapter_comp;

    public MainScreenFragment() {
    }

    public static MainScreenFragment Instance(int sectionNumber) {
        MainScreenFragment fragment = new MainScreenFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(
                R.layout.recycler_all, container, false);
        View rootView_all = inflater.inflate(R.layout.recycler_all, container, false);
        View rootView_down = inflater.inflate(R.layout.recycler_down, container, false);
        View rootView_comp = inflater.inflate(R.layout.recycler_comp, container, false);

        lm_all = new LinearLayoutManager(getContext());
        lm_down = new LinearLayoutManager(getContext());
        lm_comp = new LinearLayoutManager(getContext());

        rc_ALL = (RecyclerView) rootView_all.findViewById(R.id.recycler_all);
        rc_DOWNLOADING = (RecyclerView) rootView_down.findViewById(R.id.recycler_down);
        rc_COMPLETED = (RecyclerView) rootView_comp.findViewById(R.id.recycler_comp);


        FloatingActionButton fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);
        fab.setOnClickListener(this);
        context = getContext();
        fileDatabase = new FileDatabase(context);
        db = fileDatabase.getWritableDatabase();
        fdu = new FileDatabaseUtils(db);
        Init();
        ListFromDatabase();

        adapter_all = new RecyclerViewAdapter(fm_All, new RecyclerViewAdapter.RCViewHolder.ClickListener() {
            @Override
            public void onItemClicked(int position) {
                if (actionMode != null) {
                    toggleSelection(adapter_all, position);
                } else {
                    listOnClick(fm_All, position);
                }
            }

            @Override
            public boolean onItemLongClicked(int position) {
                if (actionMode == null) {
                    AppCompatActivity activity = (AppCompatActivity) getActivity();
                    //activity.startSupportActionMode(modeCallBack);
                    actionMode = activity.startSupportActionMode(actionModeCallback);

                }

                toggleSelection(adapter_all, position);
                return true;
            }
        });
        adapter_down = new RecyclerViewAdapter(fm_Downloading, new RecyclerViewAdapter.RCViewHolder.ClickListener() {
            @Override
            public void onItemClicked(int position) {
                if (actionMode != null) {
                    toggleSelection(adapter_down, position);
                } else {
                    listOnClick(fm_Downloading, position);
                }


            }

            @Override
            public boolean onItemLongClicked(int position) {
                if (actionMode == null) {
                    AppCompatActivity activity = (AppCompatActivity) getActivity();
                    //activity.startSupportActionMode(modeCallBack);
                    actionMode = activity.startSupportActionMode(actionModeCallback);

                }

                toggleSelection(adapter_down, position);
                return true;
            }
        });
        adapter_comp = new RecyclerViewAdapter(fm_Completed, new RecyclerViewAdapter.RCViewHolder.ClickListener() {
            @Override
            public void onItemClicked(int position) {
                if (actionMode != null) {
                    toggleSelection(adapter_comp, position);
                } else {
                    listOnClick(fm_Completed, position);
                }
            }

            @Override
            public boolean onItemLongClicked(int position) {
                if (actionMode == null) {
                    AppCompatActivity activity = (AppCompatActivity) getActivity();
                    //activity.startSupportActionMode(modeCallBack);
                    actionMode = activity.startSupportActionMode(actionModeCallback);

                }

                toggleSelection(adapter_comp, position);
                return true;
            }
        });


        rc_ALL.setLayoutManager(lm_all);
        rc_DOWNLOADING.setLayoutManager(lm_down);
        rc_COMPLETED.setLayoutManager(lm_comp);

        fm_All.add(new FileModel(1, "Faraz", 25, "Downloaded", "-", "7.5MB", "-", getDateTime(System.currentTimeMillis()), "", "", ""));
        fm_All.add(new FileModel(1, "Faraz", 25, "Downloaded", "-", "7.5MB", "-", getDateTime(System.currentTimeMillis()), "", "", ""));
        fm_All.add(new FileModel(1, "Faraz", 25, "Downloaded", "-", "7.5MB", "-", getDateTime(System.currentTimeMillis()), "", "", ""));
        fm_All.add(new FileModel(1, "Faraz", 25, "Downloaded", "-", "7.5MB", "-", getDateTime(System.currentTimeMillis()), "", "", ""));
        fm_All.add(new FileModel(1, "Faraz", 25, "Downloaded", "-", "7.5MB", "-", getDateTime(System.currentTimeMillis()), "", "", ""));
        fm_All.add(new FileModel(1, "Faraz", 25, "Downloaded", "-", "7.5MB", "-", getDateTime(System.currentTimeMillis()), "", "", ""));
        fm_All.add(new FileModel(1, "Faraz", 25, "Downloaded", "-", "7.5MB", "-", getDateTime(System.currentTimeMillis()), "", "", ""));
        fm_All.add(new FileModel(1, "Faraz", 25, "Downloaded", "-", "7.5MB", "-", getDateTime(System.currentTimeMillis()), "", "", ""));
        fm_All.add(new FileModel(1, "Faraz", 25, "Downloaded", "-", "7.5MB", "-", getDateTime(System.currentTimeMillis()), "", "", ""));
        fm_All.add(new FileModel(1, "Faraz", 25, "Downloaded", "-", "7.5MB", "-", getDateTime(System.currentTimeMillis()), "", "", ""));
        fm_All.add(new FileModel(1, "Faraz", 25, "Downloaded", "-", "7.5MB", "-", getDateTime(System.currentTimeMillis()), "", "", ""));
        fm_All.add(new FileModel(1, "Faraz", 25, "Downloaded", "-", "7.5MB", "-", getDateTime(System.currentTimeMillis()), "", "", ""));

        fm_Downloading.add(new FileModel(1, "Faraz", 25, "Downloaded", "-", "7.5MB", "-", getDateTime(System.currentTimeMillis()), "", "", ""));
        fm_Downloading.add(new FileModel(1, "Faraz", 25, "Downloaded", "-", "7.5MB", "-", getDateTime(System.currentTimeMillis()), "", "", ""));
        fm_Downloading.add(new FileModel(1, "Faraz", 25, "Downloaded", "-", "7.5MB", "-", getDateTime(System.currentTimeMillis()), "", "", ""));
        fm_Downloading.add(new FileModel(1, "Faraz", 25, "Downloaded", "-", "7.5MB", "-", getDateTime(System.currentTimeMillis()), "", "", ""));
        fm_Downloading.add(new FileModel(1, "Faraz", 25, "Downloaded", "-", "7.5MB", "-", getDateTime(System.currentTimeMillis()), "", "", ""));
        fm_Downloading.add(new FileModel(1, "Faraz", 25, "Downloaded", "-", "7.5MB", "-", getDateTime(System.currentTimeMillis()), "", "", ""));
        fm_Downloading.add(new FileModel(1, "Faraz", 25, "Downloaded", "-", "7.5MB", "-", getDateTime(System.currentTimeMillis()), "", "", ""));
        fm_Downloading.add(new FileModel(1, "Faraz", 25, "Downloaded", "-", "7.5MB", "-", getDateTime(System.currentTimeMillis()), "", "", ""));
        fm_Downloading.add(new FileModel(1, "Faraz", 25, "Downloaded", "-", "7.5MB", "-", getDateTime(System.currentTimeMillis()), "", "", ""));
        fm_Downloading.add(new FileModel(1, "Faraz", 25, "Downloaded", "-", "7.5MB", "-", getDateTime(System.currentTimeMillis()), "", "", ""));
        fm_Downloading.add(new FileModel(1, "Faraz", 25, "Downloaded", "-", "7.5MB", "-", getDateTime(System.currentTimeMillis()), "", "", ""));
        fm_Downloading.add(new FileModel(1, "Faraz", 25, "Downloaded", "-", "7.5MB", "-", getDateTime(System.currentTimeMillis()), "", "", ""));

        fm_Completed.add(new FileModel(1, "Faraz", 25, "Downloaded", "-", "7.5MB", "-", getDateTime(System.currentTimeMillis()), "", "", ""));
        fm_Completed.add(new FileModel(1, "Faraz", 25, "Downloaded", "-", "7.5MB", "-", getDateTime(System.currentTimeMillis()), "", "", ""));
        fm_Completed.add(new FileModel(1, "Faraz", 25, "Downloaded", "-", "7.5MB", "-", getDateTime(System.currentTimeMillis()), "", "", ""));
        fm_Completed.add(new FileModel(1, "Faraz", 25, "Downloaded", "-", "7.5MB", "-", getDateTime(System.currentTimeMillis()), "", "", ""));
        fm_Completed.add(new FileModel(1, "Faraz", 25, "Downloaded", "-", "7.5MB", "-", getDateTime(System.currentTimeMillis()), "", "", ""));
        fm_Completed.add(new FileModel(1, "Faraz", 25, "Downloaded", "-", "7.5MB", "-", getDateTime(System.currentTimeMillis()), "", "", ""));
        fm_Completed.add(new FileModel(1, "Faraz", 25, "Downloaded", "-", "7.5MB", "-", getDateTime(System.currentTimeMillis()), "", "", ""));
        fm_Completed.add(new FileModel(1, "Faraz", 25, "Downloaded", "-", "7.5MB", "-", getDateTime(System.currentTimeMillis()), "", "", ""));
        fm_Completed.add(new FileModel(1, "Faraz", 25, "Downloaded", "-", "7.5MB", "-", getDateTime(System.currentTimeMillis()), "", "", ""));
        fm_Completed.add(new FileModel(1, "Faraz", 25, "Downloaded", "-", "7.5MB", "-", getDateTime(System.currentTimeMillis()), "", "", ""));
        fm_Completed.add(new FileModel(1, "Faraz", 25, "Downloaded", "-", "7.5MB", "-", getDateTime(System.currentTimeMillis()), "", "", ""));
        fm_Completed.add(new FileModel(1, "Faraz", 25, "Downloaded", "-", "7.5MB", "-", getDateTime(System.currentTimeMillis()), "", "", ""));
        fm_Completed.add(new FileModel(1, "Faraz", 25, "Downloaded", "-", "7.5MB", "-", getDateTime(System.currentTimeMillis()), "", "", ""));
        fm_Completed.add(new FileModel(1, "Faraz", 25, "Downloaded", "-", "7.5MB", "-", getDateTime(System.currentTimeMillis()), "", "", ""));


        rc_ALL.setAdapter(adapter_all);
        rc_DOWNLOADING.setAdapter(adapter_down);
        rc_COMPLETED.setAdapter(adapter_comp);


        if (getArguments().getInt(ARG_SECTION_NUMBER) == 1) {
            return rootView_all;
        } else if (getArguments().getInt(ARG_SECTION_NUMBER) == 2) {
            return rootView_down;
        } else if (getArguments().getInt(ARG_SECTION_NUMBER) == 3) {
            return rootView_comp;
        }


        return null;

    }

    void listOnClick(ArrayList<FileModel> fileModel, int position) {
        FileModel fm = fileModel.get(position);

        if (!FileExists(fm.getFilePath() + "/" + fm.getFileName())) {
            Snacked("File Not Found\nRe-Download It", 1);
        } else {
            Snacked(fm.getFileName() + " exists", 1);
                /*
                if (fm.getFileStatus().equals("Paused") || fm.getFileStatus().equals("Error")) {
                    //starterDownload(dataModel.getFileUrl(), dataModel.getFilePath()+"/"+dataModel.getFileName(), false, false, true);
                } else if (fm.getFileStatus().equals("Downloading")) {
                    FileDownloader.getImpl().pause(fm.getFileId());
                    Snacked(fm.getFileName() + "\nPaused", 0);
                }*/
        }
    }



    void Init() {

        fm_All = new ArrayList<>();
        fm_Downloading = new ArrayList<>();
        fm_Completed = new ArrayList<>();


        fdl_new = new FileDownloadListener() {

            FileModel fm;

            @Override
            protected void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                fm = GetFileModel(fm_Downloading, task.getId(), task.getUrl(), task.getPath(), true);
                fm.setFileId(task.getId());
                fm.setFileProgress(currentProgress(soFarBytes, totalBytes));
                fm.setFileName(task.getFilename());
                fm.setFileSize(size(soFarBytes) + "/" + size(totalBytes));
                fm.setFileStatus("Pending");
                fm.setFileDate(getDateTime(System.currentTimeMillis()));
                adapter_down.notifyDataSetChanged();
                adapter_all.notifyDataSetChanged();
            }

            @Override
            protected void started(BaseDownloadTask task) {
                fm.setFileStatus("Started");
                fm.setFileName(task.getFilename());
                fm.setFileSpeed(speed(task.getSpeed()));
                adapter_down.notifyDataSetChanged();
                adapter_all.notifyDataSetChanged();


                String[] columns = {Rows.fileName, Rows.fileStatus, Rows.fileUrl, Rows.filePath};
                String[] columnVals = {task.getFilename(), "Started", task.getUrl(), task.getPath()};

                String whereColumn = Rows.fileId;
                String[] whereArgs = {task.getId() + ""};
                fdu.UpdateFileinDatabase(columns, columnVals, whereColumn, whereArgs);
            }

            @Override
            protected void connected(BaseDownloadTask task, String etag, boolean isContinue, int soFarBytes, int totalBytes) {
                fm.setFileStatus("Connected");
                fm.setFileName(task.getFilename());
                fm.setFileSize(size(soFarBytes) + "/" + size(totalBytes));
                fm.setFileSpeed(speed(task.getSpeed()));
                adapter_down.notifyDataSetChanged();
                adapter_all.notifyDataSetChanged();
                String[] columns = {Rows.fileName, Rows.fileStatus, Rows.fileSize};
                String[] columnVals = {task.getFilename(), "Connected", size(soFarBytes) + "/" + size(totalBytes)};

                String whereColumn = Rows.fileId;
                String[] whereArgs = {task.getId() + ""};
                fdu.UpdateFileinDatabase(columns, columnVals, whereColumn, whereArgs);
            }

            @Override
            protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                if (FileExists(task.getFilename())) {
                    Snacked("File Already Downloaded", 0);
                    completed(task);
                    return;
                }
                fm.setFileStatus("Downloading");
                fm.setFileName(task.getFilename());
                fm.setFileProgress(currentProgress(soFarBytes, totalBytes));
                fm.setFileSize(size(soFarBytes) + "/" + size(totalBytes));

                fm.setFileSpeed(speed(task.getSpeed()));
                adapter_down.notifyDataSetChanged();
                adapter_all.notifyDataSetChanged();

                String[] columns = {Rows.fileName, Rows.fileProgress, Rows.fileStatus, Rows.fileSize};
                String[] columnVals = {task.getFilename(), currentProgress(soFarBytes, totalBytes) + "", "Downloading", size(soFarBytes) + "/" + size(totalBytes)};

                String whereColumn = Rows.fileId;
                String[] whereArgs = {task.getId() + ""};
                fdu.UpdateFileinDatabase(columns, columnVals, whereColumn, whereArgs);
            }

            @Override
            protected void blockComplete(BaseDownloadTask task) {
                //Snacked("blockComplete", 0);
            }

            @Override
            protected void retry(final BaseDownloadTask task, final Throwable ex, final int retryingTimes, final int soFarBytes) {
                fm.setFileStatus("Retrying");
                fm.setFileName(task.getFilename());


                adapter_down.notifyDataSetChanged();
                adapter_all.notifyDataSetChanged();
            }

            @Override
            protected void completed(BaseDownloadTask task) {
                fm.setFileStatus("Downloaded");
                fm.setFileName(task.getFilename());
                fm.setFileSpeed("-");
                fm.setFileProgress(100);
                fm.setFileSize(size(task.getLargeFileTotalBytes()));
                fm_Downloading.remove(fm);
                fm_Completed.add(fm);
                adapter_comp.notifyDataSetChanged();
                adapter_down.notifyDataSetChanged();
                adapter_all.notifyDataSetChanged();


                String[] columns = {Rows.fileProgress, Rows.fileStatus, Rows.fileSize};
                String[] columnVals = {100 + "", "Downloaded", size(task.getLargeFileTotalBytes())};

                String whereColumn = Rows.fileId;
                String[] whereArgs = {task.getId() + ""};
                fdu.UpdateFileinDatabase(columns, columnVals, whereColumn, whereArgs);
            }

            @Override
            protected void paused(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                fm.setFileStatus("Paused");
                fm.setFileName(task.getFilename());
                fm.setFileProgress(currentProgress(soFarBytes, totalBytes));
                fm.setFileSize(size(soFarBytes) + "/" + size(totalBytes));
                fm.setFileSpeed("-");
                adapter_down.notifyDataSetChanged();
                adapter_all.notifyDataSetChanged();

                String[] columns = {Rows.fileProgress, Rows.fileStatus, Rows.fileSize};
                String[] columnVals = {currentProgress(soFarBytes, totalBytes) + "", "Paused", size(soFarBytes) + "/" + size(totalBytes)};

                String whereColumn = Rows.fileId;
                String[] whereArgs = {task.getId() + ""};
                fdu.UpdateFileinDatabase(columns, columnVals, whereColumn, whereArgs);
            }

            @Override
            protected void error(BaseDownloadTask task, Throwable e) {
                if (fm != null) {
                    fm.setFileStatus("Error");
                    fm.setFileName(task.getFilename());
                    Snacked(task.getFilename() + "\n" + e.getMessage(), 0);
                    String[] columns = {Rows.fileStatus};
                    String[] columnVals = {"Error"};

                    String whereColumn = Rows.fileId;
                    String[] whereArgs = {task.getId() + ""};

                    fdu.UpdateFileinDatabase(columns, columnVals, whereColumn, whereArgs);
                    fm.setFileSpeed("-");
                    adapter_down.notifyDataSetChanged();
                    adapter_all.notifyDataSetChanged();
                } else {
                    Snacked(task.getFilename() + "\nError: " + e.getMessage(), 0);
                }
            }

            @Override
            protected void warn(BaseDownloadTask task) {
                if (fm == null) {
                    Snacked("Warning", 0);
                }
            }
        };

        fdl_old = new FileDownloadListener() {

            FileModel fm;

            @Override
            protected void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                //fm = GetFileModel(task.getId(), task.getUrl(), task.getPath(), true);
                fm.setFileId(task.getId());
                fm.setFileProgress(currentProgress(soFarBytes, totalBytes));
                fm.setFileName(task.getFilename());
                fm.setFileSize(size(soFarBytes) + "/" + size(totalBytes));
                fm.setFileStatus("Pending");
                fm.setFileDate(getDateTime(System.currentTimeMillis()));
                adapter_down.notifyDataSetChanged();
                adapter_all.notifyDataSetChanged();
            }

            @Override
            protected void started(BaseDownloadTask task) {
                fm.setFileStatus("Started");
                fm.setFileName(task.getFilename());
                fm.setFileSpeed(speed(task.getSpeed()));
                adapter_down.notifyDataSetChanged();
                adapter_all.notifyDataSetChanged();


                String[] columns = {Rows.fileName, Rows.fileStatus, Rows.fileUrl, Rows.filePath};
                String[] columnVals = {task.getFilename(), "Started", task.getUrl(), task.getPath()};

                String whereColumn = Rows.fileId;
                String[] whereArgs = {task.getId() + ""};
                fdu.UpdateFileinDatabase(columns, columnVals, whereColumn, whereArgs);
            }

            @Override
            protected void connected(BaseDownloadTask task, String etag, boolean isContinue, int soFarBytes, int totalBytes) {
                fm.setFileStatus("Connected");
                fm.setFileName(task.getFilename());
                fm.setFileSize(size(soFarBytes) + "/" + size(totalBytes));
                fm.setFileSpeed(speed(task.getSpeed()));
                adapter_down.notifyDataSetChanged();
                adapter_all.notifyDataSetChanged();
                String[] columns = {Rows.fileName, Rows.fileStatus, Rows.fileSize};
                String[] columnVals = {task.getFilename(), "Connected", size(soFarBytes) + "/" + size(totalBytes)};

                String whereColumn = Rows.fileId;
                String[] whereArgs = {task.getId() + ""};
                fdu.UpdateFileinDatabase(columns, columnVals, whereColumn, whereArgs);
            }

            @Override
            protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                if (FileExists(task.getFilename())) {
                    Snacked("File Already Downloaded", 0);
                    completed(task);
                    return;
                }
                fm.setFileStatus("Downloading");
                fm.setFileName(task.getFilename());
                fm.setFileProgress(currentProgress(soFarBytes, totalBytes));
                fm.setFileSize(size(soFarBytes) + "/" + size(totalBytes));

                fm.setFileSpeed(speed(task.getSpeed()));
                adapter_down.notifyDataSetChanged();
                adapter_all.notifyDataSetChanged();

                String[] columns = {Rows.fileName, Rows.fileProgress, Rows.fileStatus, Rows.fileSize};
                String[] columnVals = {task.getFilename(), currentProgress(soFarBytes, totalBytes) + "", "Downloading", size(soFarBytes) + "/" + size(totalBytes)};

                String whereColumn = Rows.fileId;
                String[] whereArgs = {task.getId() + ""};
                fdu.UpdateFileinDatabase(columns, columnVals, whereColumn, whereArgs);
            }

            @Override
            protected void blockComplete(BaseDownloadTask task) {
                //Snacked("blockComplete", 0);
            }

            @Override
            protected void retry(final BaseDownloadTask task, final Throwable ex, final int retryingTimes, final int soFarBytes) {
                fm.setFileStatus("Retrying");
                fm.setFileName(task.getFilename());


                adapter_down.notifyDataSetChanged();
                adapter_all.notifyDataSetChanged();
            }

            @Override
            protected void completed(BaseDownloadTask task) {
                fm.setFileStatus("Downloaded");
                fm.setFileName(task.getFilename());
                fm.setFileSpeed("-");
                fm.setFileProgress(100);
                fm.setFileSize(size(task.getLargeFileTotalBytes()));
                fm_Downloading.remove(fm);
                fm_Completed.add(fm);
                adapter_comp.notifyDataSetChanged();
                adapter_down.notifyDataSetChanged();
                adapter_all.notifyDataSetChanged();


                String[] columns = {Rows.fileProgress, Rows.fileStatus, Rows.fileSize};
                String[] columnVals = {100 + "", "Downloaded", size(task.getLargeFileTotalBytes())};

                String whereColumn = Rows.fileId;
                String[] whereArgs = {task.getId() + ""};
                fdu.UpdateFileinDatabase(columns, columnVals, whereColumn, whereArgs);
            }

            @Override
            protected void paused(BaseDownloadTask task, int soFarBytes, int totalBytes) {
                fm.setFileStatus("Paused");
                fm.setFileName(task.getFilename());
                fm.setFileProgress(currentProgress(soFarBytes, totalBytes));
                fm.setFileSize(size(soFarBytes) + "/" + size(totalBytes));
                fm.setFileSpeed("-");
                adapter_down.notifyDataSetChanged();
                adapter_all.notifyDataSetChanged();

                String[] columns = {Rows.fileProgress, Rows.fileStatus, Rows.fileSize};
                String[] columnVals = {currentProgress(soFarBytes, totalBytes) + "", "Paused", size(soFarBytes) + "/" + size(totalBytes)};

                String whereColumn = Rows.fileId;
                String[] whereArgs = {task.getId() + ""};
                fdu.UpdateFileinDatabase(columns, columnVals, whereColumn, whereArgs);
            }

            @Override
            protected void error(BaseDownloadTask task, Throwable e) {
                if (fm != null) {
                    fm.setFileStatus("Error");
                    fm.setFileName(task.getFilename());
                    Snacked(task.getFilename() + "\n" + e.getMessage(), 0);
                    String[] columns = {Rows.fileStatus};
                    String[] columnVals = {"Error"};

                    String whereColumn = Rows.fileId;
                    String[] whereArgs = {task.getId() + ""};

                    fdu.UpdateFileinDatabase(columns, columnVals, whereColumn, whereArgs);
                    fm.setFileSpeed("-");
                    adapter_down.notifyDataSetChanged();
                    adapter_all.notifyDataSetChanged();
                } else {
                    Snacked(task.getFilename() + "\nError: " + e.getMessage(), 0);
                }
            }

            @Override
            protected void warn(BaseDownloadTask task) {
                if (fm == null) {
                    Snacked("Warning", 0);
                }
            }
        };
    }

    private void toggleSelection(RecyclerViewAdapter adapter, int position) {
        adapter.toggleSelection(position);
        int count = adapter.getSelectedItemCount();

        if (count == 0) {
            actionMode.finish();
        } else {
            actionMode.setTitle(String.valueOf(count));

            actionMode.invalidate();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab:
                ShowDialog();
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

    String[] GetNameFromURL(String url) {
        GetNameFromUrl fromUrl = new GetNameFromUrl();
        try {
            String[] result = fromUrl.execute(url).get();
            return result;
        } catch (InterruptedException e) {
            String[] error = {"Exception", "InterruptedException", e.getMessage()};
            return error;
        } catch (ExecutionException e) {
            String[] error = {"Exception", "ExecutionException", e.getMessage()};
            return error;
        }
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
                    FileModel fm = new FileModel(Integer.parseInt(maplist.get(i).get(Rows.fileId)),
                            maplist.get(i).get(Rows.fileName),
                            Integer.parseInt(maplist.get(i).get(Rows.fileProgress)), maplist.get(i).get(Rows.fileStatus), "-",
                            maplist.get(i).get(Rows.fileSize),
                            "-", getDateTime(Long.parseLong(maplist.get(i).get(Rows.fileDate))),
                            maplist.get(i).get(Rows.fileUrl),
                            maplist.get(i).get(Rows.filePath),
                            maplist.get(i).get(Rows.fileDName));

                    if (fm.getFileStatus().equals("Downloading") ||
                            fm.getFileStatus().equals("Paused") ||
                            fm.getFileStatus().equals("Retrying") ||
                            fm.getFileStatus().equals("Error") ||
                            fm.getFileStatus().equals("Started") ||
                            fm.getFileStatus().equals("Connected")) {
                        fm_Downloading.add(fm);
                    } else if (fm.getFileStatus().equals("Downloaded")) {
                        fm_Completed.add(fm);
                    }


                    fm_All.add(fm);
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

    public void ShowDialog() {
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(context);
        View mView = layoutInflaterAndroid.inflate(R.layout.custom_dialog, null);

        final AlertDialog.Builder alert = new AlertDialog.Builder(context);

        final EditText etUrl = (EditText) mView.findViewById(R.id.etUrl);
        final EditText etFileName = (EditText) mView.findViewById(R.id.etUrlFileName);
        final Button getFileNameBTN = (Button) mView.findViewById(R.id.getFileNameBTN);
        final CheckBox cb_forceDownload = (CheckBox) mView.findViewById(R.id.cb_forceDownload);

        etUrl.setText(url_down);
        //alert.setIcon(); TODO add icon
        etUrl.setHint("Enter Download Link");
        alert.setTitle("Download File");
        alert.setView(mView);

        etUrl.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                etFileName.setText("");
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        getFileNameBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(etUrl.getWindowToken(), 0);
                String url = etUrl.getText().toString();
                String[] fileInfo = GetNameFromURL(url);
                if (fileInfo[0].equals("file")) {
                    etFileName.setText(fileInfo[1]);
                } else {
                    Snacked(fileInfo[1] + ":\n" + fileInfo[2], 1);
                    Snacked("Couldn't Fetch the Name.\nEnter It", 1);
                    getFileNameBTN.setClickable(false);
                    int color = Color.GRAY;
                    getFileNameBTN.setTextColor(color);
                }
            }
        });
        alert.setPositiveButton("Download", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                boolean force_download = cb_forceDownload.isChecked();
                //ToggleDownload();
            }
        });
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
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

        mNotificationManager = (NotificationManager) getActivity().getSystemService(getActivity().NOTIFICATION_SERVICE);

        RemoteViews contentView = new RemoteViews(getActivity().getPackageName(), R.layout.custom_notification);
        notification.contentView = contentView;

        Intent notificationIntent = new Intent(context, MainScreen.class);
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


        NotificationManager nManager = (NotificationManager) getActivity().getSystemService(getActivity().NOTIFICATION_SERVICE);
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

    public boolean IdInFileModel(ArrayList<FileModel> fileModel, int id) {
        for (int i = 0; i < fileModel.size(); i++) {
            if (fileModel.get(i).getFileId() == id) {
                return true;
            }
        }
        return false;
    }

    public boolean IdInDB(int id) {
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

            for (int i = 0; i < maplist.size(); i++) {
                if (maplist.get(i).get(Rows.fileId) == String.valueOf(id)) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Snacked(e.getMessage().toString(), 0);
        }
        return false;
    }

    public FileModel UpdateFileModel(FileModel fm){
        FileModel fm_all, fm_down, fm_comp;

        for (int i = 0; i < fm_All.size(); i++) {
            if (fm_All.get(i).getFileId() == fm.getFileId()) {
                fm_all = fm_All.get(i);
            }
        }
        return null;
    }

    public FileModel GetFileModel(ArrayList<FileModel> fileModel, int id, String url, String path, boolean createNewIfNotFound) {
        if (!createNewIfNotFound) {
            for (int i = 0; i < fileModel.size(); i++) {
                if (fileModel.get(i).getFileId() == id) {
                    return fileModel.get(i);
                }
            }
            return null;
        } else {
            for (int i = 0; i < fileModel.size(); i++) {
                if (fileModel.get(i).getFileId() == id) {
                    return fileModel.get(i);
                }
            }
            String[] insertData = {id + "", "NoName", "0", "-", "Pending", "" + System.currentTimeMillis(), url, path, FileDownloadUtils.generateFileName(url)};
            new InsertDataIntoDB(db).execute(insertData);
            FileModel fm = new FileModel(id, "NoName", 0, "Pending", "ETA",
                    "Size", "Speed", getDateTime(System.currentTimeMillis()),
                    url, path, FileDownloadUtils.generateFileName(url));
            fm_Downloading.add(fm);
            fm_All.add(fm);
            adapter_down.notifyDataSetChanged();
            adapter_all.notifyDataSetChanged();
            return fm;
        }

    }

    public void ToggleDownload(String fileName, String fileUrl, String filePath, boolean forceDownload) {

        String fullFileName = filePath + File.separator + fileName;

        if(!FileExists(fullFileName)){
            FileDownloader.getImpl()
                    .create(fileUrl)
                    .setForceReDownload(forceDownload)
                    .setPath(fullFileName)
                    .setListener(fdl_new)
                    .start();
        }
    }

    /*
    public void starterDownload(String url, String path, boolean pathAsDirectory, String fileName, boolean forceDownload, boolean resume) {
        if (!pathAsDirectory) {
            //path = path + fileName
        }
        fileName = path.substring(path.lastIndexOf('/') + 1, path.length());
        //TODO calculate ETA
//        speed=speedNow*0.5+speedLastHalfMinute*0.3+speedLastMinute*0.2
        if (!FileExists(fileName)) {
            if (!IdInDB(FileDownloadUtils.generateId(url, path, true)) && !IdInFileModel(FileDownloadUtils.generateId(url, path, true))) {
                FileDownloader.getImpl().create(url)
                        .setPath(path, pathAsDirectory)
                        .setListener(fdl).setForceReDownload(forceDownload).start();
            } else {
                FileModel fileModel = GetFileModel(FileDownloadUtils.generateId(url, path, true), url, path, false);
                if (fileModel.getFileStatus().equals("Paused") || fileModel.getFileStatus().equals("Error")) {
                    FileDownloader.getImpl().create(fileModel.getFileUrl())
                            .setPath(fileModel.getFilePath(), pathAsDirectory)
                            .setListener(fdl).setForceReDownload(false).start();
                    Snacked("Downloading\n" + fileModel.getFileName(), 0);
                } else if (fileModel.getFileStatus().equals("Downloaded")) {
                    FileDownloader.getImpl().create(url)
                            .setPath(path, pathAsDirectory)
                            .setListener(fdl).setForceReDownload(forceDownload).start();
                    Snacked(fileModel.getFileName() + ":\nRe-Downloading", 1);
                } else {
                    Snacked(fileModel.getFileName() + ":\n" + fileModel.getFileStatus(), 1);
                }
            }
        } else {
            Snacked("File Downloaded\nTo Redownload Check Force Download", 0);
        }
    }
*/
    @Override
    public void onDestroy() {
        db.close();
        fileDatabase.close();

        super.onDestroy();
    }

    public boolean FileExists(String path) {
        File file = new File(path);
        if (file.exists())
            return true;
        else
            return false;
    }

    private class ActionModeCallback implements ActionMode.Callback {
        @SuppressWarnings("unused")
        private final String TAG = ActionModeCallback.class.getSimpleName();

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.selected_menu, menu);
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
                    //ShowNotification("Test", "ContentText ", "BigContent", "Lines ", new Random().nextInt());
                    otherNotification(new Random().nextInt());
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
            adapter_all.clearSelection();
            adapter_comp.clearSelection();
            adapter_down.clearSelection();
            actionMode = null;
        }
    }

    public class GetNameFromUrl extends AsyncTask<String, String, String[]> {

        ProgressDialog progressDialog = new ProgressDialog(getActivity());

        public GetNameFromUrl() {

        }

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
                String[] error = {"Exception", "MalformedURLException", e.getMessage()};
                return error;
            } catch (IOException e) {
                String[] error = {"Exception", "IOException", e.getMessage()};
                return error;
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
        protected void onPreExecute() {
            progressDialog.setMessage("Please wait");
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(String[] result) {
            progressDialog.dismiss();
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
        }
    }

    public class InsertDataIntoDB extends AsyncTask<String, Integer, Boolean> {
        FileDatabaseUtils fdu;

        public InsertDataIntoDB(SQLiteDatabase db) {
            fdu = new FileDatabaseUtils(db);
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
        }

        @Override
        protected Boolean doInBackground(String... params) {
            fdu.InsertDataIntoTable(params[0], params[1], params[2], params[3], params[4], params[5], params[6], params[7], params[8]);
            return true;
        }
    }

}
