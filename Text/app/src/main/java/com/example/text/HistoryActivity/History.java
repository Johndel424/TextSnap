package com.example.text.HistoryActivity;

import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.text.ConvoMessage;
import com.example.text.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


public class History extends Fragment {
    private SearchView searchView;
    private RecyclerView recyclerView;
    private ProgressDialog progressDialog;
    private AlertDialog dialog;
    private HistoryAdapter adapter;
    private ArrayList<HistoryModel> historyList;
    private DatabaseReference databaseReference;
    private ValueEventListener eventListener;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private String currentUid;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        searchView = view.findViewById(R.id.searchh);
        searchView.clearFocus();

        recyclerView = view.findViewById(R.id.recyclerView);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 1);
        recyclerView.setLayoutManager(gridLayoutManager);

        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);

        progressDialog.show();

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        dialog = builder.create();
        dialog.show();

        historyList = new ArrayList<>();
        adapter = new HistoryAdapter(getContext(), historyList);
        recyclerView.setAdapter(adapter);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        currentUid = currentUser.getUid();

        databaseReference = FirebaseDatabase.getInstance().getReference("chatrooms");

        eventListener = databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                historyList.clear();
                for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                    HistoryModel dataClass = itemSnapshot.getValue(HistoryModel.class);
                    if (dataClass != null) {
                        if ((dataClass.getUserUid().equals(currentUid))) {
                            historyList.add(dataClass);
                        }
                    }
                }

                // Sort the chatList by lastMessageTime in descending order (most recent first)
                Collections.sort(historyList, new Comparator<HistoryModel>() {
                    @Override
                    public int compare(HistoryModel o1, HistoryModel o2) {
                        return Long.compare(o2.getTimestamp(), o1.getTimestamp()); // descending order
                    }
                });

                adapter.notifyDataSetChanged();
                progressDialog.dismiss();
                dialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressDialog.dismiss();
                dialog.dismiss();
                Toast.makeText(getContext(), "Failed to load data.", Toast.LENGTH_SHORT).show();
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    adapter.updateList(historyList);
                } else {
                    searchList(newText);
                }
                return true;
            }
        });

        return view;
    }

    public void searchList(String text) {
        ArrayList<HistoryModel> searchList = new ArrayList<>();
        for (HistoryModel dataClass : historyList) {
            if (dataClass.getTitle().toLowerCase().contains(text.toLowerCase())) {
                searchList.add(dataClass);
            }
        }
        adapter.searchDataList(searchList);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (databaseReference != null && eventListener != null) {
            databaseReference.removeEventListener(eventListener);
        }
    }

}