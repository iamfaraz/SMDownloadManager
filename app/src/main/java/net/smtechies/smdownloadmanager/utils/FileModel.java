package net.smtechies.smdownloadmanager.utils;

import android.widget.ImageButton;

/**
 * Created by Faraz on 30/07/2017.
 */

public class FileModel {

    private int fileId;
    private ImageButton statusButton;
    private String fileName;
    private int fileProgress;
    private String fileStatus;
    private String fileETA;
    private String fileSize;
    private String fileSpeed;
    private String fileDate;
    private String fileUrl;
    private String filePath;
    private String fileDName;

    public FileModel(int fileId, String fileName, int fileProgress,
                     String fileStatus, String fileETA, String fileSize,
                     String fileSpeed, String fileDate, String fileUrl,
                     String filePath, String fileDName) {
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

    }

    public String getFileDName() {
        return fileDName;
    }

    public void setFileDName(String fileDName) {
        this.fileDName = fileDName;
    }

    public int getFileId() {
        return fileId;
    }

    public void setFileId(int fileId) {
        this.fileId = fileId;
    }

    public ImageButton getStatusButton() {
        return statusButton;
    }

    public void setStatusButton(ImageButton statusButton) {
        this.statusButton = statusButton;
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

    public String getFileStatus() {
        return fileStatus;
    }

    public void setFileStatus(String fileStatus) {
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
}