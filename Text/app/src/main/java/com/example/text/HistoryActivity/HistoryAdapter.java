package com.example.text.HistoryActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.text.ConvoMessage;
import com.example.text.HomeActivity.HomeGenerator;
import com.example.text.HomeActivity.ReviewGenerator;
import com.example.text.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<MyHistoryListViewHolder> {
    private Context context;
    private List<HistoryModel> dataList;
    public HistoryAdapter(Context context, List<HistoryModel> dataList) {
        this.context = context;
        this.dataList = dataList;
    }
    @NonNull
    @Override
    public MyHistoryListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_item, parent, false);
        return new MyHistoryListViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull MyHistoryListViewHolder holder, int position) {
        HistoryModel chatRoom = dataList.get(position);
        String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Long lastMessageTimestamp = chatRoom.getTimestamp();

        // Check if lastMessageTimestamp is not null
        if (lastMessageTimestamp != null) {
            // Get the current time in milliseconds
            long currentTime = System.currentTimeMillis();

            // Calculate the difference in milliseconds
            long timeDifference = currentTime - lastMessageTimestamp;

            // Convert the time difference into days
            long daysPassed = timeDifference / (1000 * 60 * 60 * 24);  // 1 day in milliseconds

            // If the message is from the same day, show the time
            if (daysPassed == 0) {
                // Get the time and format it
                SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                String time = sdf.format(new Date(lastMessageTimestamp));
                holder.timestamp.setText(time);
            } else {
                // Display the number of days passed
                holder.timestamp.setText(daysPassed + " days ago");
            }
        }


        String title = chatRoom.getTitle();

        if (title.length() > 20) {
            title = title.substring(0, 20) + "...";
        }

        holder.name.setText(title);


        String otherId = chatRoom.getChatRoomId();

        holder.recCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(context, HomeGenerator.class);
                intent.putExtra("chatRoomId", otherId);
                context.startActivity(intent);

            }
        });
        holder.delete.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Delete Confirmation")
                    .setMessage("Are you sure you want to delete this item?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        // Reference to the specific item to delete
                        DatabaseReference itemRef = FirebaseDatabase.getInstance().getReference("chatrooms").child(otherId);

                        // Remove the item from Firebase
                        itemRef.removeValue().addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(context, "Item deleted successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(context, "Failed to delete item", Toast.LENGTH_SHORT).show();
                            }
                        });
                    })
                    .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                    .show();
        });

    }
    @Override
    public int getItemCount() {
        return dataList.size();
    }
    public void searchDataList(ArrayList<HistoryModel> searchList){
        dataList = searchList;
        notifyDataSetChanged();
    }
    public void updateList(List<HistoryModel> newList) {
        dataList.clear();
        dataList.addAll(newList);
        notifyDataSetChanged();
    }

    private void deleteChatRoom(String chatRoomId, int position) {
        // Firebase reference sa chatRooms node
        DatabaseReference chatRoomRef = FirebaseDatabase.getInstance().getReference("chatrooms").child(chatRoomId);

        // I-delete ang chat room gamit ang chatRoomId
        chatRoomRef.removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {

                // Pwede mo ring i-refresh ang RecyclerView o i-update ang UI pagkatapos ng deletion
                dataList.remove(position);
                notifyItemRemoved(position);
            } else {
                Toast.makeText(context, "Failed to delete chat room", Toast.LENGTH_SHORT).show();
            }
        });
    }


}
class MyHistoryListViewHolder extends RecyclerView.ViewHolder{
    ImageView delete;
    TextView name,timestamp ;
    LinearLayout recCard;
    public MyHistoryListViewHolder(@NonNull View itemView) {
        super(itemView);


        recCard = itemView.findViewById(R.id.recCard);
        name = itemView.findViewById(R.id.name);
        delete = itemView.findViewById(R.id.delete);
        timestamp = itemView.findViewById(R.id.timestamp);

    }
}
