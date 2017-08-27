package net.smtechies.smdownloadmanager.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import net.smtechies.smdownloadmanager.R;

/**
 * Created by Faraz on 17/08/2017.
 */

public class AboutListAdapter extends ArrayAdapter<String> {

    private final Activity context;
    private final String[] titles;
    private final String[] subTitles;

    public AboutListAdapter(Activity context,
                            String[] titles, String[] subTitles) {
        super(context, R.layout.custom_about_item, titles);
        this.context = context;
        this.titles = titles;
        this.subTitles = subTitles;

    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.custom_about_item, null, true);
        TextView txtTitle = rowView.findViewById(R.id.cai_title);
        TextView txtSubTitle = rowView.findViewById(R.id.cai_sub);

        txtTitle.setText(titles[position]);
        txtSubTitle.setText(subTitles[position]);

        return rowView;
    }
}