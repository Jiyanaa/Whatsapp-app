package com.example.yadhav.whatsapp;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class RequestFragment extends Fragment {
    private View RequestFragmentView;
    private RecyclerView myRequestsList;

    private DatabaseReference ChatRequestsRef, UserRef, ContactsRef;
    private FirebaseAuth mAuth;
    private String currentUserID;

    public RequestFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        RequestFragmentView = inflater.inflate(R.layout.fragment_request, container, false);

        ChatRequestsRef = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        ContactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        myRequestsList = (RecyclerView)RequestFragmentView.findViewById(R.id.chat_requests_list);
        myRequestsList.setLayoutManager(new LinearLayoutManager(getContext()));

        //to add divider after recyclerview item
        DividerItemDecoration horizontalDecoration = new DividerItemDecoration(myRequestsList.getContext(),DividerItemDecoration.VERTICAL);
        Drawable horizontalDivider = ContextCompat.getDrawable(getActivity(),R.drawable.horizontal_divider);
        horizontalDecoration.setDrawable(horizontalDivider);
        myRequestsList.addItemDecoration(horizontalDecoration);

        // Inflate the layout for this fragment

        return RequestFragmentView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                        .setQuery(ChatRequestsRef.child(currentUserID), Contacts.class)
                        .build();

        FirebaseRecyclerAdapter<Contacts,RequestFragment.RequestsViewHolder> adapter =
                new FirebaseRecyclerAdapter<Contacts, RequestsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final RequestFragment.RequestsViewHolder holder, int position, @NonNull Contacts model) {
                        holder.itemView.findViewById(R.id.request_accept_btn).setVisibility(View.VISIBLE);
                        holder.itemView.findViewById(R.id.request_cancel_btn).setVisibility(View.VISIBLE);

                        final String list_user_id = getRef(position).getKey();

                        DatabaseReference getTyperef = getRef(position).child("request_type").getRef();

                        getTyperef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if(dataSnapshot.exists())
                                {
                                    String type = dataSnapshot.getValue().toString();
                                    if(type.equals("received"))
                                    {
                                        UserRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                if(dataSnapshot.hasChild("image"))
                                                {
                                                    String requestUserImage = dataSnapshot.child("image").getValue().toString();

                                                    Picasso.get().load(requestUserImage).placeholder(R.drawable.profile_image).into(holder.profileImage);
                                                }
                                                final String requestUserStatus = dataSnapshot.child("status").getValue().toString();
                                                final String requestUserName= dataSnapshot.child("name").getValue().toString();

                                                holder.userName.setText(requestUserName);
                                                holder.userStatus.setText("wants to connect with you.");

                                                holder.itemView.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        CharSequence options[] = new CharSequence[]
                                                                {
                                                                        "Accept",
                                                                        "Cancel"
                                                                };
                                                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                        builder.setTitle(requestUserName +" Chat Requests");
                                                        builder.setItems(options, new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialogInterface, int i)
                                                            {
                                                                if(i==0)
                                                                {
                                                                    ContactsRef.child(currentUserID).child(list_user_id).child("Contact")
                                                                            .setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if(task.isSuccessful())
                                                                            {
                                                                                ContactsRef.child(list_user_id).child(currentUserID).child("Contact")
                                                                                        .setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                        if(task.isSuccessful())
                                                                                        {
                                                                                            ChatRequestsRef.child(currentUserID).child(list_user_id)
                                                                                                    .removeValue()
                                                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                        @Override
                                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                                            if(task.isSuccessful())
                                                                                                            {
                                                                                                                ChatRequestsRef.child(list_user_id).child(currentUserID)
                                                                                                                        .removeValue()
                                                                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                            @Override
                                                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                                                if(task.isSuccessful())
                                                                                                                                {
                                                                                                                                    Toast.makeText(getContext(), "New Contact is Added", Toast.LENGTH_SHORT).show();

                                                                                                                                }
                                                                                                                            }
                                                                                                                        });
                                                                                                            }
                                                                                                        }
                                                                                                    });

                                                                                        }
                                                                                    }
                                                                                });

                                                                            }
                                                                        }
                                                                    });

                                                                }
                                                                if(i==1)
                                                                {
                                                                    ChatRequestsRef.child(currentUserID).child(list_user_id)
                                                                            .removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if(task.isSuccessful())
                                                                                    {
                                                                                        ChatRequestsRef.child(list_user_id).child(currentUserID)
                                                                                                .removeValue()
                                                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                    @Override
                                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                                        if(task.isSuccessful())
                                                                                                        {
                                                                                                            Toast.makeText(getContext(), "Contact Deleted", Toast.LENGTH_SHORT).show();

                                                                                                        }
                                                                                                    }
                                                                                                });
                                                                                    }
                                                                                }
                                                                            });
                                                                }

                                                            }
                                                        });

                                                        builder.show();
                                                    }
                                                });

                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });

                                    }

                                    else if(type.equals("sent"))
                                    {
                                        Button request_sent_btn = holder.itemView.findViewById(R.id.request_accept_btn);
                                        request_sent_btn.setText("Req Sent");

                                        holder.itemView.findViewById(R.id.request_cancel_btn).setVisibility(View.INVISIBLE);

                                        UserRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                if(dataSnapshot.hasChild("image"))
                                                {
                                                    String requestUserImage = dataSnapshot.child("image").getValue().toString();

                                                    Picasso.get().load(requestUserImage).placeholder(R.drawable.profile_image).into(holder.profileImage);
                                                }
                                                final String requestUserStatus = dataSnapshot.child("status").getValue().toString();
                                                final String requestUserName= dataSnapshot.child("name").getValue().toString();

                                                holder.userName.setText(requestUserName);
                                                holder.userStatus.setText("you have sent a request to " + requestUserName);

                                                holder.itemView.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        CharSequence options[] = new CharSequence[]
                                                                {
                                                                        "Cancel Chat Request"
                                                                };
                                                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                        builder.setTitle("Already sent request.");
                                                        builder.setItems(options, new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialogInterface, int i)
                                                            {
                                                                if(i==0)
                                                                {
                                                                    ChatRequestsRef.child(currentUserID).child(list_user_id)
                                                                            .removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    if(task.isSuccessful())
                                                                                    {
                                                                                        ChatRequestsRef.child(list_user_id).child(currentUserID)
                                                                                                .removeValue()
                                                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                    @Override
                                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                                        if(task.isSuccessful())
                                                                                                        {
                                                                                                            Toast.makeText(getContext(), "you have cancelled the chat request.", Toast.LENGTH_SHORT).show();

                                                                                                        }
                                                                                                    }
                                                                                                });
                                                                                    }
                                                                                }
                                                                            });
                                                                }

                                                            }
                                                        });

                                                        builder.show();
                                                    }
                                                });

                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });
                                    }

                                }

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                    }

                    @NonNull
                    @Override
                    public RequestFragment.RequestsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout,viewGroup,false);
                        RequestFragment.RequestsViewHolder holder = new RequestFragment.RequestsViewHolder(view);
                        return holder;
                    }
                };
        myRequestsList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class RequestsViewHolder extends RecyclerView.ViewHolder{

        TextView userName, userStatus;
        CircleImageView profileImage;
        Button AcceptButton, CancalButton;

        public RequestsViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_status);
            profileImage = itemView.findViewById(R.id.users_profile_image);
            AcceptButton = itemView.findViewById(R.id.request_accept_btn);
            CancalButton = itemView.findViewById(R.id.request_cancel_btn);
        }
    }
}
