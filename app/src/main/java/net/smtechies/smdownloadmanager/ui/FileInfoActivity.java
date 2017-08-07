package net.smtechies.smdownloadmanager.ui;

import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import net.smtechies.smdownloadmanager.R;
import net.smtechies.smdownloadmanager.adapter.FileItem;
import net.smtechies.smdownloadmanager.utils.Rows;

public class FileInfoActivity extends AppCompatActivity {

    ActionBar actionBar;

    FileItem fileItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_info);
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        String fileName, fileSize, fileDate, fileUrl, filePath, fileDName;
        long fileId;
        int fileProgrss;
        byte fileStatus;

        fileId = getIntent().getLongExtra(Rows.fileId, 0);
        fileName = getIntent().getStringExtra(Rows.fileName);
        fileProgrss = getIntent().getIntExtra(Rows.fileProgress, 0);
        fileStatus = getIntent().getByteExtra(Rows.fileStatus, (byte) 0);
        fileSize = getIntent().getStringExtra(Rows.fileSize);
        fileDate = getIntent().getStringExtra(Rows.fileDate);
        fileUrl = getIntent().getStringExtra(Rows.fileUrl);
        filePath = getIntent().getStringExtra(Rows.filePath);
        fileDName = getIntent().getStringExtra(Rows.fileDName);

        fileItem = new FileItem(fileId, fileName, fileProgrss, fileStatus, "ETA", fileSize, "Speed", fileDate, fileUrl, filePath, fileDName, null);


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
