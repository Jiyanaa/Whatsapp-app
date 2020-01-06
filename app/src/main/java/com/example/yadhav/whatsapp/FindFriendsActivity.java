package com.example.yadhav.whatsapp;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


public class FindFriendsActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private RecyclerView FindFriendRecyclerList;

    private DatabaseReference UserRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);

        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");

        FindFriendRecyclerList = (RecyclerView) findViewById(R.id.find_friend_recycler_list);
        FindFriendRecyclerList.setLayoutManager(new LinearLayoutManager(this));

        //add divider after recyclerview item
        DividerItemDecoration horizontalDecoration = new DividerItemDecoration(FindFriendRecyclerList.getContext(),DividerItemDecoration.VERTICAL);
        Drawable horizontalDivider = ContextCompat.getDrawable(FindFriendsActivity.this,R.drawable.horizontal_divider);
        horizontalDecoration.setDrawable(horizontalDivider);
        FindFriendRecyclerList.addItemDecoration(horizontalDecoration);

        mToolbar = (Toolbar) findViewById(R.id.find_friend_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Find Friends");

    }
    //search data
    private void firebaseSearch(String searchText)
    {
        Query firebaseSearchQuery = UserRef.orderByChild("name").startAt(searchText).endAt(searchText + "\uf8ff");

        FirebaseRecyclerOptions<Contacts> options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                        .setQuery(firebaseSearchQuery,Contacts.class)
                        .build();

        FirebaseRecyclerAdapter<Contacts, FindFriendsActivity.FindFriendViewHolder> adapter =
                new FirebaseRecyclerAdapter<Contacts, FindFriendsActivity.FindFriendViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull FindFriendViewHolder holder, final int position, @NonNull Contacts model) {

                        holder.userName.setText(model.getName());
                        holder.userStatus.setText(model.getStatus());
                        Picasso.get().load(model.getImage()).placeholder(R.drawable.profile_image).into(holder.profileImage);

                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String visit_user_id = getRef(position).getKey();

                                Intent profileintent = new Intent(FindFriendsActivity.this,ProfileActivity.class);
                                profileintent.putExtra("visit_user_id",visit_user_id);
                                startActivity(profileintent);
                            }
                        });

                    }

                    @NonNull
                    @Override
                    public FindFriendsActivity.FindFriendViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout, viewGroup, false);
                        FindFriendViewHolder viewHolder = new FindFriendViewHolder(view);
                        return viewHolder;
                    }
                };
        FindFriendRecyclerList.setAdapter(adapter);
        adapter.startListening();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //inflate the menu; this adds items to the actions bar if it presents
        getMenuInflater().inflate(R.menu.search_menu,menu);
        MenuItem item = menu.findItem(R.id.main_search_option);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                firebaseSearch(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //filter as you type
                firebaseSearch(newText);
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(UserRef,Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts, FindFriendViewHolder> adapter =
                new FirebaseRecyclerAdapter<Contacts, FindFriendViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull FindFriendViewHolder holder, final int position, @NonNull Contacts model) {

                        holder.userName.setText(model.getName());
                        holder.userStatus.setText(model.getStatus());
                        Picasso.get().load(model.getImage()).placeholder(R.drawable.profile_image).into(holder.profileImage);

                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String visit_user_id = getRef(position).getKey();

                                Intent profileintent = new Intent(FindFriendsActivity.this,ProfileActivity.class);
                                profileintent.putExtra("visit_user_id",visit_user_id);
                                startActivity(profileintent);
                            }
                        });
                    }

                    @NonNull
                    @Override
                    public FindFriendViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout, viewGroup, false);
                        FindFriendViewHolder viewHolder = new FindFriendViewHolder(view);
                        return viewHolder;
                    }
                };
        FindFriendRecyclerList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class FindFriendViewHolder extends RecyclerView.ViewHolder
    {
        View mView;
        TextView userName, userStatus;
        CircleImageView profileImage;

        public FindFriendViewHolder(@NonNull View itemView) {
            super(itemView);

            mView=itemView;

            userName = mView.findViewById(R.id.user_profile_name);
            userStatus = mView.findViewById(R.id.user_status);
            profileImage = mView.findViewById(R.id.users_profile_image);
        }
    }
}
