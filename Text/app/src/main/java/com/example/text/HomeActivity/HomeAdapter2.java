package com.example.text.HomeActivity;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.text.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeAdapter2 extends RecyclerView.Adapter<HomeAdapter2.HomeViewHolder2> {
    private Context context;
    private List<HomeModel> messageList;
    private String chatRoomId;

    public HomeAdapter2(Context context, List<HomeModel> messageList, String chatRoomId) {
        this.context = context;
        this.messageList = messageList;
        this.chatRoomId = chatRoomId;
    }

    @NonNull
    @Override
    public HomeViewHolder2 onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_message_item2, parent, false);
        return new HomeViewHolder2(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HomeViewHolder2 holder, int position) {
        HomeModel message = messageList.get(position);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String currentUserUID = mAuth.getCurrentUser().getUid();



        if (message.getSender().equals("user")) {
            holder.userLayout.setVisibility(View.VISIBLE);
            holder.otherLayout.setVisibility(View.GONE);
            holder.otherSettings.setVisibility(View.GONE);
            holder.userText.setText(message.getMessage());
        } else {
            holder.otherLayout.setVisibility(View.VISIBLE);
            holder.otherSettings.setVisibility(View.VISIBLE);
            holder.userLayout.setVisibility(View.GONE);
            holder.otherText.setText(message.getMessage());
        }

        holder.copy.setOnClickListener(v -> {
            copyToClipboard(message.getMessage());
        });
    }




    @Override
    public int getItemCount() {
        return messageList.size();
    }
    private void copyToClipboard(String text) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Copied Text", text);
        clipboard.setPrimaryClip(clip);

        Toast.makeText(context, "Text copied to clipboard", Toast.LENGTH_SHORT).show();
    }
    private void deleteMessage(String messageId) {
        DatabaseReference messageRef = FirebaseDatabase.getInstance().getReference("convo")
                .child(chatRoomId)
                .child(messageId);

        messageRef.removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(context, "Message deleted successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Failed to delete message: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    static class HomeViewHolder2 extends RecyclerView.ViewHolder {

        RelativeLayout userLayout,otherLayout,otherSettings;
        TextView userText,otherText;
        ImageButton copy,audio;

        public HomeViewHolder2(@NonNull View itemView) {
            super(itemView);

            userLayout = itemView.findViewById(R.id.userLayout);
            userText = itemView.findViewById(R.id.userText);
            otherLayout= itemView.findViewById(R.id.otherLayout);
            otherText= itemView.findViewById(R.id.otherText);
            otherSettings= itemView.findViewById(R.id.otherSettings);
            copy= itemView.findViewById(R.id.copy);
            audio = itemView.findViewById(R.id.audio);
        }
    }
}