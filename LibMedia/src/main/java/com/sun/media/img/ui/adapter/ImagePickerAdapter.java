package com.sun.media.img.ui.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sun.base.bean.MediaFile;
import com.sun.base.manager.SelectionManager;
import com.sun.base.toast.ToastHelper;
import com.sun.base.util.MediaFileUtil;
import com.sun.base.util.TimeHelp;
import com.sun.media.R;
import com.sun.media.img.manager.ConfigManager;
import com.sun.media.img.model.bean.ItemType;
import com.sun.media.img.ui.view.SquareImageView;
import com.sun.media.img.ui.view.SquareRelativeLayout;

import java.util.List;

/**
 * @author: Harper
 * @date: 2022/7/19
 * @note: 列表适配器
 */
public class ImagePickerAdapter extends RecyclerView.Adapter<ImagePickerAdapter.BaseHolder> {

    private final Context mContext;
    private final List<MediaFile> mMediaFileList;
    private final boolean isShowCamera;
    private static final long SELECT_TIME_LENGTH = 301 * 1000L;

    public ImagePickerAdapter(Context context, List<MediaFile> mediaFiles) {
        this.mContext = context;
        this.mMediaFileList = mediaFiles;
        this.isShowCamera = true;
    }

    @Override
    public int getItemViewType(int position) {
        if (isShowCamera) {
            if (position == 0) {
                return ItemType.ITEM_TYPE_CAMERA;
            }
            //如果有相机存在，position位置需要-1
            position--;
        }
        if (MediaFileUtil.isVideoFileType(mMediaFileList.get(position).getPath())) {
            return ItemType.ITEM_TYPE_VIDEO;
        } else {
            return ItemType.ITEM_TYPE_IMAGE;
        }
    }

    @Override
    public int getItemCount() {
        if (mMediaFileList == null) {
            return 0;
        }
        return isShowCamera ? mMediaFileList.size() + 1 : mMediaFileList.size();
    }

    /**
     * 获取item所对应的数据源
     *
     * @param position
     * @return
     */
    public MediaFile getMediaFile(int position) {
        if (isShowCamera) {
            if (position == 0) {
                return null;
            }
            return mMediaFileList.get(position - 1);
        }
        return mMediaFileList.get(position);
    }


    @NonNull
    @Override
    public BaseHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == ItemType.ITEM_TYPE_CAMERA) {
            view = LayoutInflater.from(mContext).inflate(R.layout.item_recyclerview_camera, null);
            return new BaseHolder(view);
        }
        if (viewType == ItemType.ITEM_TYPE_IMAGE) {
            view = LayoutInflater.from(mContext).inflate(R.layout.item_recyclerview_image, null);
            return new ImageHolder(view);
        }
        if (viewType == ItemType.ITEM_TYPE_VIDEO) {
            view = LayoutInflater.from(mContext).inflate(R.layout.item_recyclerview_video, null);
            return new VideoHolder(view);
        }
        return null;
    }


    @Override
    public void onBindViewHolder(@NonNull BaseHolder holder, @SuppressLint("RecyclerView") final int position) {
        int itemType = getItemViewType(position);
        final MediaFile mediaFile = getMediaFile(position);
        switch (itemType) {
            //图片、视频Item
            case ItemType.ITEM_TYPE_IMAGE:
            case ItemType.ITEM_TYPE_VIDEO:
                MediaHolder mediaHolder = (MediaHolder) holder;
                bindMedia(mediaHolder, mediaFile);
                break;
            //相机Item
            default:
                break;
        }
        //设置点击事件监听
        if (mOnItemClickListener != null) {
            holder.mSquareRelativeLayout.setOnClickListener(view -> {
                if (mediaFile.getDuration() >= SELECT_TIME_LENGTH) {
                    ToastHelper.showToast(R.string.choose_video_tips);
                    return;
                }
                mOnItemClickListener.onMediaClick(view, position);
            });

            if (holder instanceof MediaHolder) {
                ((MediaHolder) holder).mViewCheck.setOnClickListener(view -> {
                    if (mediaFile.getDuration() >= SELECT_TIME_LENGTH) {
                        ToastHelper.showToast(R.string.choose_video_tips);
                        return;
                    }
                    mOnItemClickListener.onMediaCheck(view, position);
                });
            }
        }
    }


    /**
     * 绑定数据（图片、视频）
     *
     * @param mediaHolder
     * @param mediaFile
     */
    private void bindMedia(MediaHolder mediaHolder, MediaFile mediaFile) {
        String imagePath = mediaFile.getPath();
        if (!TextUtils.isEmpty(imagePath)) {
            //选择状态（仅是UI表现，真正数据交给SelectionManager管理）
            if (SelectionManager.getInstance().isImageSelect(imagePath)) {
                mediaHolder.mViewCheck.setBackgroundResource(R.drawable.shape_oval_write_yellow_4px);
                mediaHolder.mTvCheck.setText(SelectionManager.getInstance().getImageSelectPosition(imagePath) + 1 + "");
            } else {
                if (mediaFile.getDuration() > SELECT_TIME_LENGTH) {
                    mediaHolder.mViewCheck.setBackgroundResource(R.drawable.shape_oval_gray_4px);
                } else {
                    mediaHolder.mViewCheck.setBackgroundResource(R.drawable.shape_oval_write_4px);
                }
                mediaHolder.mTvCheck.setText("");
            }
            try {
                ConfigManager.getInstance().getImageLoader().loadImage(mediaHolder.mImageView, imagePath);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (mediaHolder instanceof ImageHolder) {
                //如果是gif图，显示gif标识
                String suffix = imagePath.substring(imagePath.lastIndexOf(".") + 1);
                if (suffix.toUpperCase().equals("GIF")) {
                    ((ImageHolder) mediaHolder).mImageGif.setVisibility(View.VISIBLE);
                } else {
                    ((ImageHolder) mediaHolder).mImageGif.setVisibility(View.GONE);
                }
            }
            if (mediaHolder instanceof VideoHolder) {
                //如果是视频，需要显示视频时长
                String duration = TimeHelp.getVideoDuration(mediaFile.getDuration());
                ((VideoHolder) mediaHolder).mVideoDuration.setText(duration);
            }
        }
    }

    /**
     * 图片Item
     */
    class ImageHolder extends MediaHolder {

        public ImageView mImageGif;

        public ImageHolder(View itemView) {
            super(itemView);
            mImageGif = itemView.findViewById(R.id.iv_item_gif);
        }
    }

    /**
     * 视频Item
     */
    class VideoHolder extends MediaHolder {

        TextView mVideoDuration;

        VideoHolder(View itemView) {
            super(itemView);
            mVideoDuration = itemView.findViewById(R.id.tv_item_videoDuration);
        }
    }

    /**
     * 媒体Item
     */
    class MediaHolder extends BaseHolder {

        SquareImageView mImageView;
        View mViewCheck;
        TextView mTvCheck;

        MediaHolder(View itemView) {
            super(itemView);
            mImageView = itemView.findViewById(R.id.iv_item_image);
            mViewCheck = itemView.findViewById(R.id.frame_checked);
            mTvCheck = itemView.findViewById(R.id.tv_checked);
        }
    }

    /**
     * 基础Item
     */
    class BaseHolder extends RecyclerView.ViewHolder {

        SquareRelativeLayout mSquareRelativeLayout;

        BaseHolder(View itemView) {
            super(itemView);
            mSquareRelativeLayout = itemView.findViewById(R.id.srl_item);
        }
    }


    /**
     * 接口回调，将点击事件向外抛
     */
    private OnItemClickListener mOnItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onMediaClick(View view, int position);

        void onMediaCheck(View view, int position);
    }
}