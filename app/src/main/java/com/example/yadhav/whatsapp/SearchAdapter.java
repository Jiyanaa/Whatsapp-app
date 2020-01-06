package com.example.yadhav.whatsapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.SearchViewHolder> {
    Context context;

    ArrayList<String> fullNameList;
    ArrayList<String> userStatusList;
    ArrayList<String> profilePicList;
    private DatabaseReference UserRef;

    class SearchViewHolder extends RecyclerView.ViewHolder{
        CircleImageView profileImage;
        TextView full_name, user_status;

        public SearchViewHolder(@NonNull View itemView) {
            super(itemView);

            profileImage = (CircleImageView)itemView.findViewById(R.id.users_profile_image);
            full_name = (TextView)itemView.findViewById(R.id.user_profile_name);
            user_status = (TextView)itemView.findViewById(R.id.user_status);
            UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        }

    }

    public SearchAdapter(Context context, ArrayList<String> fullNameList, ArrayList<String> userStatusList, ArrayList<String> profilePicList) {
        this.context = context;
        this.fullNameList = fullNameList;
        this.userStatusList = userStatusList;
        this.profilePicList = profilePicList;
    }

    @NonNull
    @Override
    public SearchAdapter.SearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.users_display_layout, parent, false);
        return new SearchAdapter.SearchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchViewHolder holder, final int position) {
        holder.full_name.setText(fullNameList.get(position));
        holder.user_status.setText(userStatusList.get(position));

        Picasso.get().load(profilePicList.get(position)).placeholder(R.drawable.profile_image).into(holder.profileImage);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "Go to profile activity", Toast.LENGTH_SHORT).show();


            }
        });
    }


    @Override
    public int getItemCount() {
        return fullNameList.size();
    }
}
