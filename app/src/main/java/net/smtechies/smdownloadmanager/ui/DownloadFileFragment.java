package net.smtechies.smdownloadmanager.ui;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.smtechies.smdownloadmanager.R;

/**
 * A placeholder fragment containing a simple view.
 */
public class DownloadFileFragment extends Fragment {

    public DownloadFileFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_download_file, container, false);
    }
}
