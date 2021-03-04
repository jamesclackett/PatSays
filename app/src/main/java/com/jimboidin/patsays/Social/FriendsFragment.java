package com.jimboidin.patsays.Social;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;

import android.text.InputType;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jimboidin.patsays.LobbyActivity;
import com.jimboidin.patsays.R;

import java.util.HashMap;


public class FriendsFragment extends Fragment {
    private final String TAG = "FriendsFragment";
    private Context mContext;
    private FirebaseAuth mAuth;
    private DatabaseReference mFriendsDB, mRequestsDB;
    private ValueEventListener mFriendsListener, mRequestsListener;
    private ListView mFriendsListView, mRequestsListView;
    private ArrayAdapter<User> mFriendAdapter, mRequestAdapter;
    private String mHostName;
    private LobbyListener mLobbyListener;
    private Boolean mInLobby;
    private User myProfile;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_friends, container, false);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
        mLobbyListener = (LobbyListener) context;
    }


    @Override
    public void onStart() {
        super.onStart();
        if (getView() != null){
            mAuth = FirebaseAuth.getInstance();
            mInLobby = mLobbyListener.askIsLobby();

            if (mInLobby)
                mHostName = mLobbyListener.getHostName();

            getMyProfile();
            initializeListViews();
            fillFriendsList();
            fillRequestsList();
            attachContextMenus();

            FloatingActionButton fab = getView().findViewById(R.id.add_friend_fab);
            fab.setOnClickListener(v -> openRequestDialog());
        }
    }

    private void getMyProfile(){
        FirebaseDatabase.getInstance().getReference().child("Users")
                .child(mAuth.getUid()).child("username").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists())
                    myProfile = new User(snapshot.getValue(String.class), mAuth.getUid());
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void initializeListViews(){
        mFriendsListView = getView().findViewById(R.id.friends_list_view);
        mRequestsListView = getView().findViewById(R.id.requests_list_view);
        mFriendAdapter = new ArrayAdapter<>(mContext, android.R.layout.simple_list_item_1);
        mRequestAdapter = new ArrayAdapter<>(mContext, android.R.layout.simple_list_item_1);
        mFriendsListView.setAdapter(mFriendAdapter);
        mRequestsListView.setAdapter(mRequestAdapter);
    }

    private void attachContextMenus() {
        registerForContextMenu(mFriendsListView);
        registerForContextMenu(mRequestsListView);
        mFriendsListView.setOnItemClickListener((parent, view, position, id) -> parent.showContextMenuForChild(view));
        mRequestsListView.setOnItemClickListener((parent, view, position, id) -> parent.showContextMenuForChild(view));
    }

    @Override
    public void onCreateContextMenu(@NonNull ContextMenu menu,
                                    @NonNull View v, @Nullable ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;

        String name = ((TextView) info.targetView).getText().toString();
        menu.setHeaderTitle(name);
        menu.setHeaderIcon(R.drawable.ic_add_friend);

        if (v.getId() == R.id.friends_list_view && mInLobby){
            inflater.inflate(R.menu.friends_menu, menu);
        }
        if (v.getId() == R.id.friends_list_view && !mInLobby){
            inflater.inflate(R.menu.friends_menu_noinv, menu);
        }
        if (v.getId() == R.id.requests_list_view){
            inflater.inflate(R.menu.requests_menu, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        //TODO - Find a better way of doing the below, maybe even user something other than Context Menu

        if (item.getItemId() == R.id.accept_friend){
            User user = (User) mRequestsListView.getAdapter().getItem(info.position);
            User.handleRequest(myProfile, user, true);
        }
        else if (item.getItemId() == R.id.decline_friend){
            User user = (User) mRequestsListView.getAdapter().getItem(info.position);
            User.handleRequest(myProfile, user, false);
        }
        else if (item.getItemId() == R.id.invite_friend){
            User user = (User) mFriendsListView.getAdapter().getItem(info.position);
            User.invite(myProfile, user, mHostName);
        }
        else if (item.getItemId() == R.id.remove_friend){
            User user = (User) mFriendsListView.getAdapter().getItem(info.position);
            User.removeFriend(myProfile, user);
        }
        return super.onContextItemSelected(item);
    }


    private void fillFriendsList(){
        mFriendsDB = FirebaseDatabase.getInstance().getReference()
                .child("Users").child(mAuth.getUid()).child("Friends");
        mFriendsListener = mFriendsDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mFriendAdapter.clear();
                if (snapshot.exists()){
                    getView().findViewById(R.id.friends_title).setVisibility(View.VISIBLE);
                    for (DataSnapshot childData : snapshot.getChildren())
                        appendFriendUser(childData.getKey());
                }
                else
                    getView().findViewById(R.id.friends_title).setVisibility(View.GONE);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void fillRequestsList(){
        mRequestsDB = FirebaseDatabase.getInstance().getReference()
                .child("Users").child(mAuth.getUid()).child("Friend_Requests");
        mRequestsListener = mRequestsDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mRequestAdapter.clear();
                if (snapshot.exists()){
                    getView().findViewById(R.id.requests_title).setVisibility(View.VISIBLE);
                    for (DataSnapshot childData: snapshot.getChildren())
                        appendRequestUser(childData.getKey());
                }
                else
                    getView().findViewById(R.id.requests_title).setVisibility(View.GONE);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void appendRequestUser(String id){
        FirebaseDatabase.getInstance().getReference()
                .child("Users").child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    String username = snapshot.child("username").getValue(String.class);
                    User user = new User(username, id);
                    mRequestAdapter.add(user);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void appendFriendUser(String id){
        FirebaseDatabase.getInstance().getReference()
                .child("Users").child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    String username = snapshot.child("username").getValue(String.class);
                    User user = new User(username, id);
                    mFriendAdapter.add(user);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void openRequestDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("Add Friend");

        final EditText input = new EditText(mContext);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> findAndRequest(input.getText().toString()));
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void findAndRequest(String requestInput) {
        if (!requestInput.equals(myProfile.getUsername())) {
            DatabaseReference usersDB = FirebaseDatabase.getInstance().getReference().child("Users");
            usersDB.orderByChild("username").equalTo(requestInput).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot childData : snapshot.getChildren()) {
                        User user = new User(requestInput, childData.getKey());
                        User.requestFriend(myProfile, user);
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }
        else {
            displayToast("You cannot add yourself as a friend");
        }
    }

    private void removeListeners(){
        if (mFriendsListener != null)
            mFriendsDB.removeEventListener(mFriendsListener);
        if (mRequestsListener != null)
            mRequestsDB.removeEventListener(mRequestsListener);
    }

    private void displayToast(String message) {
        Toast.makeText(mContext, message,
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        removeListeners();
    }
}
