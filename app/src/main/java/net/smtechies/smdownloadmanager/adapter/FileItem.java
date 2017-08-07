package net.smtechies.smdownloadmanager.adapter;

/**
 * Created by Faraz on 05/08/2017.
 */

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.liulishuo.filedownloader.FileDownloadListener;
import com.liulishuo.filedownloader.model.FileDownloadStatus;

import net.smtechies.smdownloadmanager.R;

import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.flexibleadapter.utils.DrawableUtils;
import eu.davidea.viewholders.FlexibleViewHolder;

/**
 * Where AbstractFlexibleItem implements IFlexible!
 */
public class FileItem extends AbstractFlexibleItem<FileItem.ItemHolder> {
    public FileItem fileModel;

    private int lastPosition = -1;

    private long fileId;
    private String fileName;
    private int fileProgress;
    private byte fileStatus;
    private String fileETA;
    private String fileSize;
    private String fileSpeed;
    private String fileDate;
    private String fileUrl;
    private String filePath;
    private String fileDName;

    private FileDownloadListener fileDownloadListener;

    public FileItem(long fileId, String fileName, int fileProgress,
                    byte fileStatus, String fileETA, String fileSize,
                    String fileSpeed, String fileDate, String fileUrl,
                    String filePath, String fileDName, FileDownloadListener fileDownloadListener) {
        this.fileId = fileId;
        this.fileName = fileName;
        this.fileProgress = fileProgress;
        this.fileSize = fileSize;
        this.fileStatus = fileStatus;
        this.fileSpeed = fileSpeed;
        this.fileDate = fileDate;
        this.fileETA = fileETA;
        this.fileUrl = fileUrl;
        this.filePath = filePath;
        this.fileDName = fileDName;
        this.fileModel = this;
        this.fileDownloadListener = fileDownloadListener;
    }

    public String getFileDName() {
        return fileDName;
    }

    public void setFileDName(String fileDName) {
        this.fileDName = fileDName;
    }

    public long getFileId() {
        return fileId;
    }

    public void setFileId(long fileId) {
        this.fileId = fileId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileETA() {
        return fileETA;
    }

    public void setFileETA(String fileETA) {
        this.fileETA = fileETA;
    }

    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    public int getFileProgress() {
        return fileProgress;
    }

    public void setFileProgress(int fileProgress) {
        this.fileProgress = fileProgress;
    }

    public byte getFileStatus() {
        return fileStatus;
    }

    public void setFileStatus(byte fileStatus) {
        this.fileStatus = fileStatus;
    }

    public String getFileSpeed() {
        return fileSpeed;
    }

    public void setFileSpeed(String fileSpeed) {
        this.fileSpeed = fileSpeed;
    }

    public String getFileDate() {
        return fileDate;
    }

    public void setFileDate(String fileDate) {
        this.fileDate = fileDate;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public FileDownloadListener getFileDownloadListener() {
        return fileDownloadListener;
    }

    public void setFileDownloadListener(FileDownloadListener fileDownloadListener) {
        this.fileDownloadListener = fileDownloadListener;
    }

    /**
     * When an item is equals to another?
     * Write your own concept of equals, mandatory to implement or use
     * default java implementation (return this == o;) if you don't have unique IDs!
     * This will be explained in the "Item interfaces" Wiki page.
     */
    @Override
    public boolean equals(Object inObject) {
        if (inObject instanceof FileItem) {
            FileItem inItem = (FileItem) inObject;
            return this.fileId == inItem.fileId;
        }
        return false;
    }

    /**
     * You should implement also this method if equals() is implemented.
     * This method, if implemented, has several implications that Adapter handles better:
     * - The Hash, increases performance in big list during Update & Filter operations.
     * - You might want to activate stable ids via Constructor for RV, if your id
     * is unique (read more in the wiki page: "Setting Up Advanced") you will benefit
     * of the animations also if notifyDataSetChanged() is invoked.
     */
    @Override
    public int hashCode() {

        return (fileId + "").hashCode();
    }

    /**
     * For the item type we need an int value: the layoutResID is sufficient.
     */
    @Override
    public int getLayoutRes() {
        return R.layout.row_item_ui;
    }

    /**
     * Delegates the creation of the ViewHolder to the user (AutoMap).
     * The infladed view is already provided as well as the Adapter.
     */
    @Override
    public ItemHolder createViewHolder(View view, FlexibleAdapter adapter) {
        return new ItemHolder(view, adapter);
    }

    /**
     * The Adapter and the Payload are provided to perform and get more specific information.
     */
    @Override
    public void bindViewHolder(FlexibleAdapter adapter, final ItemHolder viewHolder, int position,
                               List payloads) {

        viewHolder.fileId_int = getFileId();
        viewHolder.playButton_ib.setTag(position);
        viewHolder.fileName_txt.setText(getFileName());
        viewHolder.fileProgress_pb.setProgress(getFileProgress());
        viewHolder.fileSize_txt.setText(getFileSize());

        if (viewHolder.fileProgress_pb.getProgress() == 100) {
            viewHolder.playButton_ib.setBackgroundResource(R.drawable.complete_btn);
            viewHolder.playButton_ib.setImageResource(R.drawable.complete_btn);
        }
        viewHolder.filePerecnt_txt.setText(viewHolder.fileProgress_pb.getProgress() + "%");
        viewHolder.fileETA_txt.setText(getFileETA());

        if (getFileStatus() == FileDownloadStatus.completed) {
            viewHolder.fileStatus_txt.setText("Downloaded");
        } else if (getFileStatus() == FileDownloadStatus.pending) {
            viewHolder.fileStatus_txt.setText("Pending");
        } else if (getFileStatus() == FileDownloadStatus.error) {
            viewHolder.fileStatus_txt.setText("Error");
        } else if (getFileStatus() == FileDownloadStatus.blockComplete) {
            viewHolder.fileStatus_txt.setText("Block Completed");
        } else if (getFileStatus() == FileDownloadStatus.connected) {
            viewHolder.fileStatus_txt.setText("Connected");
        } else if (getFileStatus() == FileDownloadStatus.paused) {
            viewHolder.fileStatus_txt.setText("Paused");
        } else if (getFileStatus() == FileDownloadStatus.progress) {
            viewHolder.fileStatus_txt.setText("Downloading");
        } else if (getFileStatus() == FileDownloadStatus.started) {
            viewHolder.fileStatus_txt.setText("Started");
        } else if (getFileStatus() == FileDownloadStatus.retry) {
            viewHolder.fileStatus_txt.setText("Retrying");
        }
        viewHolder.fileSpeed_txt.setText(getFileSpeed());
        viewHolder.fileDateTIme_txt.setText(getFileDate());
        viewHolder.selectedOverlay.setAlpha(0.2f);
        viewHolder.selectedOverlay.setVisibility(View.VISIBLE);
        final Context context = viewHolder.itemView.getContext();

        viewHolder.playButton_ib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                /*if (viewHolder.fileStatus_txt.getText().toString().equals("Downloading")) {
                    FileDownloader.getImpl().pause((int) getFileId());
                    viewHolder.playButton_ib.setBackgroundResource(R.drawable.play_btn);
                    viewHolder.playButton_ib.setImageResource(R.drawable.play_btn);
                    Toast.makeText(context, fileModel.getFileUrl() + fileModel.getFilePath(), Toast.LENGTH_LONG).show();

                } else {
                    FileDownloader.getImpl().create(fileModel.getFileUrl()).setPath(fileModel.getFilePath(), true).setListener(getFileDownloadListener()).start();
                    viewHolder.playButton_ib.setBackgroundResource(R.drawable.pause_btn);
                    viewHolder.playButton_ib.setImageResource(R.drawable.pause_btn);
                    Toast.makeText(context, fileModel.getFileUrl() + fileModel.getFilePath(), Toast.LENGTH_LONG).show();
                }*/
            }
        });

        Drawable drawable = DrawableUtils.getSelectableBackgroundCompat(
                Color.TRANSPARENT,                // normal background
                viewHolder.primaryColor,                // pressed background
                Color.LTGRAY);              // ripple color
        DrawableUtils.setBackgroundCompat(viewHolder.selectedOverlay, drawable);


        //viewHolder.selectedOverlay.setVisibility(isSelected(position) ? View.VISIBLE : View.INVISIBLE);
    }

    /**
     * The ViewHolder used by this item.
     * Extending from FlexibleViewHolder is recommended especially when you will use
     * more advanced features.
     */
    public class ItemHolder extends FlexibleViewHolder {

        Context context;
        long fileId_int;
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
        int primaryColor;

        public ItemHolder(View view, FlexibleAdapter adapter) {
            super(view, adapter);
            fileId_int = -1;
            playButton_ib = view.findViewById(R.id.playBtn);
            fileName_txt = view.findViewById(R.id.fileName);
            fileProgress_pb = view.findViewById(R.id.fileProgressBar);
            fileSize_txt = view.findViewById(R.id.fileSize);
            fileStatus_txt = view.findViewById(R.id.fileStatus);
            filePerecnt_txt = view.findViewById(R.id.filePercent);
            fileETA_txt = view.findViewById(R.id.fileETA);
            fileSpeed_txt = view.findViewById(R.id.fileSpeed);
            fileDateTIme_txt = view.findViewById(R.id.fileDate);
            selectedOverlay = view.findViewById(R.id.selected_overlay);

            context = view.getContext();
            primaryColor = fetchPrimaryDarkColor();

        }

        private int fetchPrimaryDarkColor() {
            TypedValue typedValue = new TypedValue();

            TypedArray a = context.obtainStyledAttributes(typedValue.data, new int[]{R.attr.colorPrimaryDark});
            int color = a.getColor(0, 0);

            a.recycle();

            return color;
        }

        public ImageButton getPlayButton_ib() {
            return playButton_ib;
        }
    }
}