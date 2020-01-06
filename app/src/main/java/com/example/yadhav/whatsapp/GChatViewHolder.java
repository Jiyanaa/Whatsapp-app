package com.example.yadhav.whatsapp;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class GChatViewHolder extends RecyclerView.ViewHolder {
    View mView;

    public GChatViewHolder(@NonNull View itemView) {
        super(itemView);
        mView = itemView;

    }

    public void setDetails(Context ctx, String name, String message, final String date, final String time, String image, String type){

        TextView mFrom = mView.findViewById(R.id.user_name);
        TextView mMsg = mView.findViewById(R.id.receiver_messages_text);
        ImageView imageView = mView.findViewById(R.id.message_image_layout);
        TextView mTime = mView.findViewById(R.id.msg_time);
        CircleImageView mImage = mView.findViewById(R.id.message_profile_image);

        String formMessageType = type.toString();
        if(formMessageType.equals("text"))
        {
            mTime.setVisibility(View.INVISIBLE);
            imageView.setVisibility(View.INVISIBLE);
            mFrom.setText(name);
            mMsg.setText(message);
            Picasso.get().load(image).placeholder(R.drawable.profile_image).into(mImage);

            mMsg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TextView mTime = mView.findViewById(R.id.msg_time);
                    mTime.setVisibility(View.VISIBLE);
                    mTime.setText(date+" AT "+time);

                }
            });
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TextView mTime = mView.findViewById(R.id.msg_time);
                    mTime.setVisibility(View.INVISIBLE);
                }
            });


        }
        else {

            mTime.setVisibility(View.INVISIBLE);
            mMsg.setVisibility(View.INVISIBLE);
            mFrom.setText(name);
            Picasso.get().load(image).placeholder(R.drawable.profile_image).into(mImage);
            imageView.setBackgroundResource(R.drawable.receiver_messages_layout);
            Picasso.get().load(message).placeholder(R.drawable.profile_image).into(imageView);

            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TextView mTime = mView.findViewById(R.id.msg_time);
                    mTime.setVisibility(View.VISIBLE);
                    mTime.setText(date+" AT "+time);

                }
            });
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TextView mTime = mView.findViewById(R.id.msg_time);
                    mTime.setVisibility(View.INVISIBLE);
                }
            });

        }
    }

}
