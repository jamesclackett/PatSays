package com.jimboidin.patsays.Social;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jimboidin.patsays.R;


public class FriendsFragment extends Fragment {
    private final String TAG = "FriendsFragment";
    private Context mContext;
    private FirebaseAuth mAuth;
    private DatabaseReference mFriendsDB, mRequestsDB;
    private ValueEventListener mFriendsListener, mRequestsListener;
    private ListView mFriendsListView, mRequestsListView;
    private ArrayAdapter<String> mFriendAdapter, mRequestAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_friends, container, false);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }


    @Override
    public void onStart() {
        super.onStart();
        if (getView() != null){
            mAuth = FirebaseAuth.getInstance();
            mFriendsListView = getView().findViewById(R.id.friends_list_view);
            mRequestsListView = getView().findViewById(R.id.requests_list_view);
            mFriendAdapter = new ArrayAdapter<>(mContext, android.R.layout.simple_list_item_1);
            mRequestAdapter = new ArrayAdapter<>(mContext, android.R.layout.simple_list_item_1);
            mFriendsListView.setAdapter(mFriendAdapter);
            mRequestsListView.setAdapter(mRequestAdapter);
            getFriendsList();
            getRequestsList();

            //TODO - Create addFriend
            //TODO - Create acceptRequest
        }
    }

    private void getFriendsList(){
        mFriendsDB = FirebaseDatabase.getInstance().getReference()
                .child("Users").child(mAuth.getUid()).child("Friends");
        mFriendsListener = mFriendsDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mFriendAdapter.clear();
                if (snapshot.exists())
                    for (DataSnapshot childData : snapshot.getChildren())
                        addToFriendAdapter(childData.getKey());
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void addToFriendAdapter(String name) {
        mFriendAdapter.add(name);
    }

    private void getRequestsList(){
        mRequestsDB = FirebaseDatabase.getInstance().getReference()
                .child("Users").child(mAuth.getUid()).child("Requests");
        mRequestsListener = mRequestsDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mRequestAdapter.clear();
                if (snapshot.exists())
                    for (DataSnapshot childData: snapshot.getChildren())
                        addToRequestAdapter(childData.getKey());
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void addToRequestAdapter(String name){
        mRequestAdapter.add(name);
    }

    private void removeListeners(){
        if (mFriendsListener != null)
            mFriendsDB.removeEventListener(mFriendsListener);
        if (mRequestsListener != null)
            mRequestsDB.removeEventListener(mRequestsListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        removeListeners();
    }
}