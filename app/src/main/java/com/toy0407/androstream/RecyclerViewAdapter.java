package com.toy0407.androstream;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.Viewholder> {

    private Context context;
    private ArrayList<VideoClass> VideoClassArrayList;
    private OnVideoListener mOnVideoListener;

    // Constructor
    public RecyclerViewAdapter(Context context, ArrayList<VideoClass> courseModelArrayList,OnVideoListener onVideoListener) {
        this.context = context;
        this.VideoClassArrayList = courseModelArrayList;
        this.mOnVideoListener=onVideoListener;
    }

    @NonNull
    @Override
    public RecyclerViewAdapter.Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // to inflate the layout for each item of recycler view.
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclercard, parent, false);
        return new Viewholder(view,mOnVideoListener);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewAdapter.Viewholder holder, int position) {
        // to set data to textview and imageview of each card layout
        VideoClass model = VideoClassArrayList.get(position);
        holder.name.setText(model.getName());

        String url = VideoClassArrayList.get(position).streamlink;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(url, new HashMap<String, String>());
        Bitmap bitmap = retriever.getFrameAtTime(10000000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
        holder.thumbnail.setImageBitmap(bitmap);
    }

    @Override
    public int getItemCount() {
        return VideoClassArrayList.size();
    }

    public class Viewholder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView thumbnail;
        private TextView name;
        OnVideoListener onVideoListener;

        public Viewholder(@NonNull View itemView,OnVideoListener onVideoListener) {
            super(itemView);
            name = itemView.findViewById(R.id.videoName);
            thumbnail = itemView.findViewById(R.id.videoThumbnail);
            this.onVideoListener=onVideoListener;

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onVideoListener.onVideoClick(getAdapterPosition());
        }
    }

    public interface OnVideoListener{
        void onVideoClick(int position);
    }
}
