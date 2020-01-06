package com.example.yadhav.whatsapp;


import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatFragment extends Fragment {
    private View PrivateChatsView;
    private RecyclerView chatsList;

    private DatabaseReference ChatsRef, UsersRef;
    private FirebaseAuth mAuth;
    private String currentUserID;

    public ChatFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        PrivateChatsView =  inflater.inflate(R.layout.fragment_chat, container, false);
        mAuth = FirebaseAuth.getInstance();

        //code
        if (mAuth.getCurrentUser() != null) {
            currentUserID = mAuth.getCurrentUser().getUid();
            ChatsRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserID);
            UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");


        }

        chatsList = (RecyclerView) PrivateChatsView.findViewById(R.id.chats_list);
        LinearLayoutManager linearLayoutManager= new LinearLayoutManager(getContext());

        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        chatsList.setHasFixedSize(true);
        chatsList.setLayoutManager(linearLayoutManager);

        // Inflate the layout for this fragment
        return PrivateChatsView;
    }

    @Override
    public void onStart() {
        super.onStart();

        if(mAuth.getCurrentUser()!=null) {
            FirebaseRecyclerOptions<Contacts> options =
                    new FirebaseRecyclerOptions.Builder<Contacts>()
                            .setQuery(ChatsRef, Contacts.class)
                            .build();

            FirebaseRecyclerAdapter<Contacts, ChatFragment.ChatsViewHolder> adapter =
                    new FirebaseRecyclerAdapter<Contacts, ChatsViewHolder>(options) {
                        @Override
                        protected void onBindViewHolder(@NonNull final ChatsViewHolder holder, int position, @NonNull Contacts model) {
                            final String usersIDs = getRef(position).getKey();
                            final String[] retImage = {"default_image"};

                            UsersRef.child(usersIDs).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        if (dataSnapshot.hasChild("image")) {
                                            retImage[0] = dataSnapshot.child("image").getValue().toString();

                                            Picasso.get().load(retImage[0]).placeholder(R.drawable.profile_image).into(holder.profileImage);
                                        }

                                        final String retStatus = dataSnapshot.child("status").getValue().toString();
                                        final String retName = dataSnapshot.child("name").getValue().toString();
                                        Intent intent = new Intent();
                                        intent.putExtra("last status", retStatus);

                                        holder.userName.setText(retName);

                                        if (dataSnapshot.child("userState").hasChild("state")) {
                                            String state = dataSnapshot.child("userState").child("state").getValue().toString();
                                            String date = dataSnapshot.child("userState").child("date").getValue().toString();
                                            String time = dataSnapshot.child("userState").child("time").getValue().toString();

                                            if (state.equals("online")) {
                                                holder.userStatus.setText("online");
                                            } else if (state.equals("offline")) {
                                                holder.userStatus.setText("last seen at \n" + date + " " + time);
                                            }

                                        } else {
                                            holder.userStatus.setText("offline");
                                        }

                                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                                chatIntent.putExtra("visit_user_id", usersIDs);
                                                chatIntent.putExtra("visit_user_name", retName);
                                                chatIntent.putExtra("visit_image", retImage[0]);
                                                startActivity(chatIntent);

                                            }
                                        });

                                        holder.profileImage.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                Intent open_profile = new Intent(getContext(), ProfileActivity.class);
                                                open_profile.putExtra("visit_user_id", usersIDs);
                                                startActivity(open_profile);
                                            }
                                        });

                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                        }

                        @NonNull
                        @Override
                        public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout, viewGroup, false);
                            //ChatFragment.ChatsViewHolder holder = new ChatFragment.ChatsViewHolder(view);
                            return new ChatsViewHolder(view);
                        }
                    };
            chatsList.setAdapter(adapter);
            adapter.startListening();
        }
    }

    public static class ChatsViewHolder extends RecyclerView.ViewHolder {

        TextView userName, userStatus;
        CircleImageView profileImage;

        public ChatsViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_status);
            profileImage = itemView.findViewById(R.id.users_profile_image);
        }
    }
}
