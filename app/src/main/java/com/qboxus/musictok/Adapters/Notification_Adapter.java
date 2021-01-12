package com.qboxus.musictok.Adapters;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.qboxus.musictok.Models.Notification_Get_Set;
import com.qboxus.musictok.R;
import com.facebook.drawee.view.SimpleDraweeView;

import java.util.ArrayList;

/**
 * Created by AQEEL on 3/20/2018.
 */

public class Notification_Adapter extends RecyclerView.Adapter<Notification_Adapter.CustomViewHolder > {
    public Context context;

    ArrayList<Notification_Get_Set> datalist;
    public interface OnItemClickListener {
        void onItemClick(View view, int postion, Notification_Get_Set item);
    }

    public Notification_Adapter.OnItemClickListener listener;

    public Notification_Adapter(Context context, ArrayList<Notification_Get_Set> arrayList, Notification_Adapter.OnItemClickListener listener) {
        this.context = context;
        datalist= arrayList;
        this.listener=listener;
    }

    @Override
    public Notification_Adapter.CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewtype) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_notification,viewGroup,false);
        view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
        Notification_Adapter.CustomViewHolder viewHolder = new Notification_Adapter.CustomViewHolder(view);
        return viewHolder;
    }

    @Override
    public int getItemCount() {
       return datalist.size();
    }

    class CustomViewHolder extends RecyclerView.ViewHolder {

        SimpleDraweeView user_image;

        TextView username,message,watch_btn;


        public CustomViewHolder(View view) {
            super(view);
            user_image=view.findViewById(R.id.user_image);
            username=view.findViewById(R.id.username);
            message=view.findViewById(R.id.message);
            watch_btn=view.findViewById(R.id.watch_btn);


        }

        public void bind(final int pos , final Notification_Get_Set item, final Notification_Adapter.OnItemClickListener listener) {

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(v,pos,item);
                }
            });

            watch_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(v,pos,item);
                }
            });

        }


    }

    @Override
    public void onBindViewHolder(final Notification_Adapter.CustomViewHolder holder, final int i) {
        holder.setIsRecyclable(false);

        final Notification_Get_Set item=datalist.get(i);
        holder.username.setText(item.username);

        if(item.profile_pic!=null && !item.profile_pic.equals("")) {
            Uri uri = Uri.parse(item.profile_pic);
            holder.user_image.setImageURI(uri);
        }

        holder.message.setText(item.string);

        if(item.type.equalsIgnoreCase("video_comment")){
            holder.watch_btn.setVisibility(View.VISIBLE);
        }

        else if(item.type.equalsIgnoreCase("video_like")) {
            holder.watch_btn.setVisibility(View.VISIBLE);
        }

        else if(item.type.equalsIgnoreCase("following")) {
            holder.watch_btn.setVisibility(View.GONE);
        }


        holder.bind(i,datalist.get(i),listener);

}

}