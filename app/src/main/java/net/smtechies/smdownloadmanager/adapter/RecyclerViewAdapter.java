package net.smtechies.smdownloadmanager.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.liulishuo.filedownloader.FileDownloader;

import net.smtechies.smdownloadmanager.R;
import net.smtechies.smdownloadmanager.utils.FileModel;

import java.util.ArrayList;

/**
 * Created by Faraz on 03/08/2017.
 */

public class RecyclerViewAdapter extends SelectableAdapter<RecyclerViewAdapter.RCViewHolder> implements View.OnClickListener {

    private ArrayList<FileModel> fileModel;
    private int lastPosition = -1;
    private RCViewHolder.ClickListener clickListener;

    public RecyclerViewAdapter(ArrayList<FileModel> fileModel, RCViewHolder.ClickListener clickListener) {
        this.fileModel = fileModel;
        this.clickListener = clickListener;
    }

    @Override
    public void onClick(View v) {
        int position = (Integer) v.getTag();
        FileModel fm = fileModel.get(position);

        switch (v.getId()) {
            case R.id.playBtn:
                if (!fm.getFileStatus().equals("Downloaded")) {
                    if (fm.getFileStatus().equals("Paused") || fm.getFileStatus().equals("Error")){
                        //FileDownloader.getImpl().create(fm.getFileUrl()).setPath();

                    } else {
                        FileDownloader.getImpl().pause(fm.getFileId());
                    }
                }
                break;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerViewAdapter.RCViewHolder viewHolder, int position) {
        FileModel fm = this.fileModel.get(position);
        lastPosition = position;

        viewHolder.fileId_int = fm.getFileId();
        viewHolder.playButton_ib.setOnClickListener(this);
        viewHolder.playButton_ib.setTag(position);
        viewHolder.fileName_txt.setText(fm.getFileName());
        viewHolder.fileProgress_pb.setProgress(fm.getFileProgress());
        viewHolder.fileSize_txt.setText(fm.getFileSize());
        if (viewHolder.fileProgress_pb.getProgress() == 100) {
            viewHolder.playButton_ib.setBackgroundResource(R.drawable.complete_btn);
            viewHolder.playButton_ib.setImageResource(R.drawable.complete_btn);
        }
        viewHolder.filePerecnt_txt.setText(viewHolder.fileProgress_pb.getProgress() + "%");
        viewHolder.fileETA_txt.setText(fm.getFileETA());
        viewHolder.fileStatus_txt.setText(fm.getFileStatus());
        viewHolder.fileSpeed_txt.setText(fm.getFileSpeed());
        viewHolder.fileDateTIme_txt.setText(fm.getFileDate());
        viewHolder.selectedOverlay.setAlpha(0.2f);


        viewHolder.selectedOverlay.setVisibility(isSelected(position) ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public int getItemCount() {
        return this.fileModel.size();
    }

    @Override
    public RCViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_item_ui, parent, false);
        return new RCViewHolder(v, clickListener);
    }

    /**
     * View holder class
     */
    public static class RCViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        int fileId_int;
        ImageButton playButton_ib;
        TextView fileName_txt;
        ProgressBar fileProgress_pb;
        TextView fileStatus_txt;
        TextView filePerecnt_txt;
        TextView fileETA_txt;
        TextView fileSize_txt;
        TextView fileSpeed_txt;
        TextView fileDateTIme_txt;
        View selectedOverlay;


        private ClickListener listener;

        public RCViewHolder(View view, ClickListener listener) {
            super(view);
            fileId_int = -1;
            playButton_ib = (ImageButton) view.findViewById(R.id.playBtn);
            fileName_txt = (TextView) view.findViewById(R.id.fileName);
            fileProgress_pb = (ProgressBar) view.findViewById(R.id.fileProgressBar);
            fileSize_txt = (TextView) view.findViewById(R.id.fileSize);
            fileStatus_txt = (TextView) view.findViewById(R.id.fileStatus);
            filePerecnt_txt = (TextView) view.findViewById(R.id.filePercent);
            fileETA_txt = (TextView) view.findViewById(R.id.fileETA);
            fileSpeed_txt = (TextView) view.findViewById(R.id.fileSpeed);
            fileDateTIme_txt = (TextView) view.findViewById(R.id.fileDate);
            selectedOverlay = view.findViewById(R.id.selected_overlay);

            this.listener = listener;

            view.setOnClickListener(this);
            view.setOnLongClickListener(this);
        }


        @Override
        public void onClick(View v) {
            if (listener != null) {
                listener.onItemClicked(getPosition());
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if (listener != null) {
                return listener.onItemLongClicked(getPosition());
            }

            return false;
        }

        public interface ClickListener {
            public void onItemClicked(int position);

            public boolean onItemLongClicked(int position);
        }
    }
}