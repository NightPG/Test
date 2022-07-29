package com.sun.media.img.task;

import android.content.Context;

import com.sun.base.bean.MediaFile;
import com.sun.media.img.i.IMediaLoadCallback;
import com.sun.media.img.loader.MediaHandler;
import com.sun.media.img.loader.VideoScanner;

import java.util.ArrayList;

/**
 * @author: Harper
 * @date: 2022/7/19
 * @note: 媒体库扫描任务（视频）
 */
public class VideoLoadTask implements Runnable {

    private Context mContext;
    private VideoScanner mVideoScanner;
    private IMediaLoadCallback mIMediaLoadCallback;

    public VideoLoadTask(Context context, IMediaLoadCallback IMediaLoadCallback) {
        this.mContext = context;
        this.mIMediaLoadCallback = IMediaLoadCallback;
        mVideoScanner = new VideoScanner(context);
    }

    @Override
    public void run() {

        //存放所有视频
        ArrayList<MediaFile> videoFileList = new ArrayList<>();

        if (mVideoScanner != null) {
            videoFileList = mVideoScanner.queryMedia();
        }

        if (mIMediaLoadCallback != null) {
            mIMediaLoadCallback.loadMediaSuccess(MediaHandler.getVideoFolder(mContext, videoFileList));
        }


    }

}