package com.sun.media.img.ui.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.sun.base.bean.MediaFile;
import com.sun.base.toast.CustomToast;
import com.sun.base.toast.ToastHelper;
import com.sun.base.upload.UpLoadFileHelper;
import com.sun.base.util.CollectionUtil;
import com.sun.base.util.DataUtil;
import com.sun.base.widget.RoundLayout;
import com.sun.media.R;
import com.sun.media.img.ImageLoader;
import com.sun.media.img.MediaSelector;
import com.sun.media.img.ui.activity.ImagePreviewActivity;
import com.sun.media.video.model.SuperPlayerModel;
import com.sun.media.video.ui.activity.VideoEditActivity;
import com.sun.media.video.ui.activity.VideoPlayActivity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Harper
 * @date 2022/7/22
 * note: 选择图片或视频控件
 */
public class MediaSelectorWidget extends FrameLayout {

    private final RecyclerView mRecyclerView;
    private final ArrayList<MediaFile> mModels;
    private MediaSelectorListener listener;
    private final Context mContext;
    private Adapter mAdapter;
    private UpLoadFileHelper mUpLoadFileHelper;

    public MediaSelectorWidget(@NonNull Context context) {
        this(context, null);
    }

    public MediaSelectorWidget(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MediaSelectorWidget(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.view_choose_photo_video, this);
        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setNestedScrollingEnabled(false);
        mModels = new ArrayList<>();
        mContext = context;
        mAdapter = new Adapter();
        mRecyclerView.setAdapter(mAdapter);
    }

    /**
     * 初始化空间时必须调用
     *
     * @param mediaFiles 媒体文件数据
     * @param listener   选择器的监听
     */
    public void initWidgetData(ArrayList<MediaFile> mediaFiles, MediaSelectorListener listener) {
        this.listener = listener;
        int maxCount = MediaSelector.getInstance().config.maxCount;
        ArrayList<MediaFile> models = checkPhotoVideoModels(mediaFiles);
        if (CollectionUtil.notEmpty(mModels)) {
            mModels.clear();
        }
        if (CollectionUtil.isEmpty(models)) {
            mModels.add(new MediaFile(MediaFile.BUTTON_ADD));
        } else {
            if (models.size() < maxCount) {
                mModels.addAll(models);
                mModels.add(new MediaFile(MediaFile.BUTTON_ADD));
            } else {
                for (MediaFile model : models) {
                    if (mModels.size() < maxCount) {
                        mModels.add(model);
                    }
                }
            }
        }
        if (MediaSelector.getInstance().config.showLocal) {
            mAdapter.notifyDataSetChanged();
        } else {
            boolean[] uploadFail = {false};
            int size = mModels.size();
            for (int i = 0; i < size; i++) {
                MediaFile model = mModels.get(i);
                if (!model.fromNet && model.path != null) {
                    if (model.itemType == MediaFile.PHOTO || model.itemType == MediaFile.VIDEO) {
                        if (mUpLoadFileHelper == null) {
                            mUpLoadFileHelper = new UpLoadFileHelper("", "");
                        }
                        int finalI = i;
                        mUpLoadFileHelper.setUploadResultListener(new UpLoadFileHelper.IUploadResultListener() {
                            @Override
                            public void onUploadSuccess(String localPath, String url) {
                                model.setFromNet(true);
                                model.url = url;
                                if (finalI == size - 1 && !uploadFail[0]) {
                                    mAdapter.notifyDataSetChanged();
                                }
                            }

                            @Override
                            public void onUploadFail(String localPath, Exception e, String text) {
                                if (!uploadFail[0]){
                                    ToastHelper.showCustomToast("文件上传失败", CustomToast.WARNING);
                                    uploadFail[0] = true;
                                }
                            }
                        });
                    }
                }
            }
        }
    }

    public void refreshWidgetData(ArrayList<MediaFile> mediaFiles) {
        ArrayList<MediaFile> models = checkPhotoVideoModels(mediaFiles);
        if (CollectionUtil.isEmpty(models)) {
            return;
        }
        if (MediaSelector.getInstance().config.showLocal) {
            reallyRefreshData(models);
        } else {
            boolean[] uploadFail = {false};
            int size = models.size();
            for (int i = 0; i < size; i++) {
                MediaFile model = models.get(i);
                if (!model.fromNet && model.path != null){
                    if (model.itemType == MediaFile.PHOTO || model.itemType == MediaFile.VIDEO){
                        if (mUpLoadFileHelper == null) {
                            mUpLoadFileHelper = new UpLoadFileHelper("", "");
                        }
                        int finalI = i;
                        mUpLoadFileHelper.setUploadResultListener(new UpLoadFileHelper.IUploadResultListener() {
                            @Override
                            public void onUploadSuccess(String localPath, String url) {
                                model.setFromNet(true);
                                model.url = url;
                                if (finalI == size - 1 && !uploadFail[0]) {
                                    reallyRefreshData(models);
                                }
                            }

                            @Override
                            public void onUploadFail(String localPath, Exception e, String text) {
                                if (!uploadFail[0]){
                                    ToastHelper.showCustomToast("文件上传失败", CustomToast.WARNING);
                                    uploadFail[0] = true;
                                }
                            }
                        });
                    }
                }
            }
        }
    }

    private void reallyRefreshData(ArrayList<MediaFile> models) {
        int maxCount = MediaSelector.getInstance().config.maxCount;
        if (CollectionUtil.isEmpty(mModels)) {
            if (models.size() < maxCount) {
                mModels.addAll(models);
                mModels.add(new MediaFile(MediaFile.BUTTON_ADD));
            } else {
                for (MediaFile model : models) {
                    if (mModels.size() < maxCount) {
                        mModels.add(model);
                    }
                }
            }
        } else {
            for (MediaFile model : mModels) {
                if (model == null) {
                    mModels.remove(null);
                } else {
                    if (model.itemType == MediaFile.BUTTON_ADD) {
                        mModels.remove(model);
                        break;
                    }
                }
            }
            if (mModels.size() + models.size() < maxCount) {
                mModels.addAll(models);
                mModels.add(new MediaFile(MediaFile.BUTTON_ADD));
            } else {
                for (MediaFile model : models) {
                    if (mModels.size() < maxCount) {
                        mModels.add(model);
                    }
                }
            }
        }
        mAdapter.notifyDataSetChanged();
    }

    private ArrayList<MediaFile> checkPhotoVideoModels(ArrayList<MediaFile> models) {
        if (CollectionUtil.notEmpty(models)) {
            Iterator<MediaFile> iterator = models.iterator();
            while (iterator.hasNext()) {
                MediaFile model = iterator.next();
                if (model == null) {
                    iterator.remove();
                } else {
                    if (model.fromNet) {
                        if (TextUtils.isEmpty(model.url)) {
                            iterator.remove();
                        }
                    } else {
                        if (TextUtils.isEmpty(model.path)) {
                            iterator.remove();
                        }
                    }
                }
            }
        }
        return models;
    }

    class Adapter extends RecyclerView.Adapter<Adapter.Holder> {

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_choose_photo_video, parent, false);
            return new Holder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {
            MediaFile model = mModels.get(position);
            if (model.itemType == MediaFile.BUTTON_ADD) {
                //添加按钮
                holder.rlAdd.setVisibility(VISIBLE);
                holder.rlImg.setVisibility(GONE);
                holder.rlAdd.setOnClickListener(v -> {
                    if (listener != null) {
                        MediaSelector.getInstance().setSelectedFiles(getPaths());
                        listener.onAddMedia();
                    }
                });
            } else {
                holder.rlAdd.setVisibility(GONE);
                holder.rlImg.setVisibility(VISIBLE);
                holder.img.setVisibility(VISIBLE);
                //删除按钮
                boolean showDelete = MediaSelector.getInstance().config.showDelete;
                if (showDelete) {
                    holder.ivDelete.setVisibility(VISIBLE);
                    holder.ivDelete.setOnClickListener(v -> remover(position));
                } else {
                    holder.ivDelete.setVisibility(GONE);
                }
                if (model.itemType == MediaFile.VIDEO) {
                    //显示视频
                    holder.ivPlay.setVisibility(VISIBLE);
                    String url = model.fromNet ? model.url : model.path;
                    ImageLoader.getInstance().loadVideo(url, holder.img);
                    holder.ivPlay.setOnClickListener(v -> {
                        if (!TextUtils.isEmpty(url)) {
                            if (model.fromNet) {
                                VideoPlayActivity.start(mContext, new SuperPlayerModel("", url, ""));
                            } else {
                                ArrayList<MediaFile> mediaFiles = new ArrayList<>();
                                mediaFiles.add(model);
                                DataUtil.getInstance().setMediaData(mediaFiles);
                                VideoEditActivity.start(mContext,true);
                            }
                        }
                    });
                } else if (model.itemType == MediaFile.PHOTO) {
                    //显示图片
                    holder.ivPlay.setVisibility(GONE);
                    String url = model.fromNet ? model.url : model.path;
                    ImageLoader.getInstance().loadImage(url, holder.img);
                    holder.img.setOnClickListener(v -> {
                        if (!TextUtils.isEmpty(url)) {
                            List<String> urls = new ArrayList<>();
                            for (MediaFile photoFile : mModels) {
                                if (photoFile != null) {
                                    if (photoFile.fromNet) {
                                        if (!TextUtils.isEmpty(photoFile.url)) {
                                            urls.add(photoFile.url);
                                        }
                                    } else {
                                        if (!TextUtils.isEmpty(photoFile.path)) {
                                            urls.add(photoFile.path);
                                        }
                                    }
                                }
                            }
                            if (CollectionUtil.notEmpty(urls)) {
                                ImagePreviewActivity.start(mContext, position, urls);
                            }
                        }
                    });
                }
            }
        }

        private ArrayList<MediaFile> getPaths() {
            if (CollectionUtil.notEmpty(mModels)) {
                Iterator<MediaFile> iterator = mModels.iterator();
                while (iterator.hasNext()) {
                    MediaFile model = iterator.next();
                    if (model == null) {
                        iterator.remove();
                    } else {
                        if (model.fromNet) {
                            if (TextUtils.isEmpty(model.url)) {
                                iterator.remove();
                            }
                        } else {
                            if (TextUtils.isEmpty(model.path)) {
                                iterator.remove();
                            }
                        }
                    }
                }
            }
            return mModels;
        }

        /**
         * 点击删除按钮
         *
         * @param position 位置
         */
        private void remover(int position) {
            if (hasAddButton()) {
                mModels.remove(position);
            } else {
                mModels.remove(position);
                mModels.add(new MediaFile(MediaFile.BUTTON_ADD));
            }
            notifyDataSetChanged();
        }

        private boolean hasAddButton() {
            return mModels.get(mModels.size() - 1).itemType == MediaFile.BUTTON_ADD;
        }

        @Override
        public void onViewRecycled(@NonNull Holder holder) {
            super.onViewRecycled(holder);
        }

        @Override
        public int getItemCount() {
            return mModels.size();
        }

        class Holder extends RecyclerView.ViewHolder {

            RoundLayout rlImg;
            ImageView img;
            ImageView ivDelete;
            ImageView ivPlay;
            RoundLayout rlAdd;

            public Holder(@NonNull View itemView) {
                super(itemView);
                rlImg = itemView.findViewById(R.id.rl_img);
                img = itemView.findViewById(R.id.img);
                ivDelete = itemView.findViewById(R.id.iv_delete);
                ivPlay = itemView.findViewById(R.id.iv_play);
                rlAdd = itemView.findViewById(R.id.rl_add);
            }
        }
    }

    /**
     * 选择图片或视频控件的监听
     */
    public interface MediaSelectorListener {
        /**
         * 点击添加按钮
         */
        void onAddMedia();
    }
}
