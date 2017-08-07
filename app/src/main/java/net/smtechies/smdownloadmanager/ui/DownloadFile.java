package net.smtechies.smdownloadmanager.ui;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import net.smtechies.smdownloadmanager.R;

public class DownloadFile extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_file);

        Intent in = getIntent();

        Uri data = in.getData();

        String dataString = "";
        if (in != null) {
            if (data.getHost().equals("media")) {
                dataString = "Scheme: " + data.getScheme()
                        + "\nHost: " + data.getHost() + "\nPath: "
                        + getRealPathFromURI(this, data);
            } else {
                dataString = "Scheme: " + data.getScheme()
                        + "\nHost: " + data.getHost() + "\nPath: "
                        + data.getPath() + "\nLastPath: " + data.getLastPathSegment();
            }
        } else
            dataString = "NULL";
        final String result = dataString;
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
            }
        });
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
}
