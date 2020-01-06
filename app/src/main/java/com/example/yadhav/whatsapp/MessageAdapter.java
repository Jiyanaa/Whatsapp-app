package com.example.yadhav.whatsapp;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> implements
        ActivityCompat.OnRequestPermissionsResultCallback {

    private List<Messages> usermessagesList;
    private FirebaseAuth mAuth;
    private DatabaseReference userRef;

    private static final int WRITE_EXTERNAL_STORAGE_CODE= 1;//save image

    public MessageAdapter(List<Messages> usermessagesList)
    {
        this.usermessagesList = usermessagesList;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.single_messages_layout,viewGroup,false);

        mAuth = FirebaseAuth.getInstance();

        return new MessageViewHolder(view);
    }


    public class MessageViewHolder extends RecyclerView.ViewHolder {

        public TextView senderMessageText, receiverMessageText, senderMessageTime, receiverMessageTime;
        public CircleImageView receiverProfileImage;
        //public ImageButton messageImage;
        public ImageView senderMessageImage, receiverMessageImage;

        private Context context;


        public MessageViewHolder(@NonNull final View itemView) {
            super(itemView);
            context = itemView.getContext();

            senderMessageText = (TextView) itemView.findViewById(R.id.sender_messages_text);
            receiverMessageText = (TextView) itemView.findViewById(R.id.receiver_messages_text);
            senderMessageTime = (TextView) itemView.findViewById(R.id.sender_messages_time);
            receiverMessageTime = (TextView) itemView.findViewById(R.id.receiver_messages_time);
            receiverProfileImage = (CircleImageView) itemView.findViewById(R.id.message_profile_image);
            //messageImage = (ImageButton) itemView.findViewById(R.id.message_image_layout);
            senderMessageImage = (ImageView) itemView.findViewById(R.id.sender_messages_img);
            receiverMessageImage = (ImageView) itemView.findViewById(R.id.receiver_messages_img);

        }
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder messageViewHolder, final int i) {
        String messageSenderID = mAuth.getCurrentUser().getUid();
        Messages messages = usermessagesList.get(i);

        String fromUserID = messages.getFrom();
        String formMessageType = messages.getType();

        userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserID);

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild("image"))
                {
                    String receiverImage = dataSnapshot.child("image").getValue().toString();
                    Picasso.get().load(receiverImage).placeholder(R.drawable.profile_image).into(messageViewHolder.receiverProfileImage);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        if(formMessageType.equals("text"))
        {
            messageViewHolder.receiverProfileImage.setVisibility(View.INVISIBLE);
            messageViewHolder.receiverMessageTime.setVisibility(View.INVISIBLE);
            messageViewHolder.receiverMessageText.setVisibility(View.INVISIBLE);
            messageViewHolder.receiverMessageImage.setVisibility(View.INVISIBLE);
            messageViewHolder.senderMessageImage.setVisibility(View.INVISIBLE);
            messageViewHolder.senderMessageTime.setVisibility(View.INVISIBLE);
            messageViewHolder.senderMessageText.setVisibility(View.INVISIBLE);


            if(fromUserID.equals(messageSenderID))
            {
                messageViewHolder.senderMessageTime.setVisibility(View.VISIBLE);
                messageViewHolder.senderMessageText.setVisibility(View.VISIBLE);
                messageViewHolder.senderMessageText.setBackgroundResource(R.drawable.sender_messages_layout);
                messageViewHolder.senderMessageText.setTextColor(Color.BLACK);
                messageViewHolder.senderMessageText.setText(messages.getMessage());
                messageViewHolder.senderMessageTime.setText(messages.getTime());
            }
            else
            {
                messageViewHolder.receiverMessageTime.setVisibility(View.VISIBLE);
                messageViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);
                messageViewHolder.receiverMessageText.setVisibility(View.VISIBLE);

                messageViewHolder.receiverMessageText.setBackgroundResource(R.drawable.receiver_messages_layout);
                messageViewHolder.receiverMessageText.setTextColor(Color.BLACK);
                messageViewHolder.receiverMessageText.setText(messages.getMessage());
                messageViewHolder.receiverMessageTime.setText(messages.getTime());
            }
        }
        else if(formMessageType.equals("image")) {
            messageViewHolder.receiverProfileImage.setVisibility(View.INVISIBLE);
            messageViewHolder.receiverMessageTime.setVisibility(View.INVISIBLE);
            messageViewHolder.receiverMessageText.setVisibility(View.INVISIBLE);
            messageViewHolder.receiverMessageImage.setVisibility(View.INVISIBLE);
            messageViewHolder.senderMessageTime.setVisibility(View.INVISIBLE);
            messageViewHolder.senderMessageText.setVisibility(View.INVISIBLE);
            messageViewHolder.senderMessageImage.setVisibility(View.INVISIBLE);

            //for sender and receiver add img
            if (fromUserID.equals(messageSenderID)) {
                messageViewHolder.senderMessageTime.setVisibility(View.VISIBLE);
                messageViewHolder.senderMessageImage.setVisibility(View.VISIBLE);
                messageViewHolder.senderMessageImage.setBackgroundResource(R.drawable.sender_messages_layout);
                messageViewHolder.senderMessageTime.setText(messages.getTime());
                Picasso.get().load(messages.getMessage())
                        .placeholder(R.drawable.profile_image).resize(800,800).onlyScaleDown().into(messageViewHolder.senderMessageImage);
            } else {
                messageViewHolder.receiverMessageTime.setVisibility(View.VISIBLE);
                messageViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);
                messageViewHolder.receiverMessageImage.setVisibility(View.VISIBLE);
                messageViewHolder.receiverMessageImage.setBackgroundResource(R.drawable.receiver_messages_layout);
                messageViewHolder.receiverMessageTime.setText(messages.getTime());
                Picasso.get().load(messages.getMessage())
                        .placeholder(R.drawable.profile_image).resize(800,800).onlyScaleDown().into(messageViewHolder.receiverMessageImage);


                //to download image long click on image
                messageViewHolder.receiverMessageImage.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(final View view) {
                        Drawable mDrawable = messageViewHolder.receiverMessageImage.getDrawable();
                        Bitmap mBitmap = ((BitmapDrawable) mDrawable).getBitmap();
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        mBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                        byte[] bytes = stream.toByteArray();
                        final Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                        messageViewHolder.receiverMessageImage.setImageBitmap(bmp);

                        CharSequence options[] = new CharSequence[]{"Download", "Share"};

                        final AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());

                        builder.setTitle("Select Options");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                //Click Event for each item.
                                if (i == 0) {

                                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                                        if(view.getContext().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)==
                                                PackageManager.PERMISSION_DENIED){
                                            String[] permission = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                                            //show popup to grant permission
                                            ActivityCompat.requestPermissions((Activity)view.getContext(),permission, WRITE_EXTERNAL_STORAGE_CODE);

                                        }
                                        else {
                                            //permission already granted, save image
                                            saveImage();
                                        }
                                    }
                                    else{
                                        //system os < marshmallow , save image
                                        saveImage();
                                    }
                                }

                                if (i == 1) {
                                    shareImage();
                                }
                            }

                            private void saveImage() {
                                //timestamp for image name
                                String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss" ,
                                        Locale.getDefault()).format(System.currentTimeMillis());

                                //path to external storage
                                File path = Environment.getExternalStorageDirectory();
                                //craete folder name firebase
                                File dir = new File(path+"/ChatApp/received_images");
                                dir.mkdirs();
                                //image name
                                String imageName = timestamp + ".PNG";
                                File file = new File(dir, imageName);
                                OutputStream out;
                                try {
                                    out = new FileOutputStream(file);
                                    bmp.compress(Bitmap.CompressFormat.PNG, 100, out);
                                    out.flush();
                                    out.close();
                                    Log.e("click", imageName + " saved to " + dir);
                                    Toast.makeText(view.getContext(), imageName+" saved to +"+dir, Toast.LENGTH_SHORT).show();
                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                    Log.e("click", "Error:"+e.getMessage());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }

                            //to share image
                            private void shareImage() {
                                try {
                                    File file = new File(view.getContext().getExternalCacheDir(), "sample.png");
                                    FileOutputStream fOut = new FileOutputStream(file);
                                    bmp.compress(Bitmap.CompressFormat.PNG, 100, fOut);
                                    fOut.flush();
                                    fOut.close();
                                    file.setReadable(true, false);

                                    //intent to share image
                                    Intent intent = new Intent(Intent.ACTION_SEND);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
                                    intent.setType("image/png");
                                    view.getContext().startActivity(Intent.createChooser(intent, "Share Via"));
                                    Toast.makeText(view.getContext(), "share image", Toast.LENGTH_SHORT).show();

                                }
                                catch (Exception e){
                                    Log.e("click", "Error:"+e.getMessage());
                                }
                            }
                        });
                        builder.show();
                        return true;
                    }
                });
            }
        }
    }

    @Override
    public int getItemCount() {
        return usermessagesList.size();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] strings, @NonNull int[] grantResults) {

        switch (requestCode) {

            case WRITE_EXTERNAL_STORAGE_CODE:

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Log.e("click", "PERMISSION_GRANTED");
                    //permission is granted, save image

                } else {

                    Log.e("click", "NOT PERMISSION_GRANTED");
                    //permission denied
                }
                break;
        }
    }

}
