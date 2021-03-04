package com.jimboidin.patsays.Social;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jimboidin.patsays.R;

import javax.crypto.Cipher;


public class RecentPlayersFragment extends Fragment {
    private LobbyListener mLobbyListener;
    private Context mContext;
    private final String TAG = "RecentPlayersFragment";
    private FirebaseAuth mAuth;
    private Boolean mInLobby;
    private String mHostName, mUsername;
    private ListView mRecentListView;
    private ArrayAdapter<User> mRecentAdapter;
    private DatabaseReference mRecentDB;
    private ValueEventListener mRecentListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recent_players, container, false);
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
            if (mInLobby){
                Log.i(TAG, "interface worked!");
                mHostName = mLobbyListener.getHostName();
            }
            getMyUsername();
            initializeListView();
            fillRecentList();
            attachContextMenu();
        }
    }


    private void getMyUsername(){
        FirebaseDatabase.getInstance().getReference().child("Users")
                .child(mAuth.getUid()).child("username").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists())
                    mUsername = snapshot.getValue(String.class);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void initializeListView() {
        mRecentListView = getView().findViewById(R.id.recent_list_view);
        mRecentAdapter = new ArrayAdapter<>(mContext, android.R.layout.simple_list_item_1);
        mRecentListView.setAdapter(mRecentAdapter);
    }


    private void attachContextMenu() {
        registerForContextMenu(mRecentListView);
        mRecentListView.setOnItemClickListener((parent, view, position, id) -> parent.showContextMenuForChild(view));
    }

    private void fillRecentList() {
        //TODO - Complete this method (fill from DB to create User Objects)
        mRecentDB = FirebaseDatabase.getInstance().getReference()
                .child("Users").child(mAuth.getUid()).child("Recent_Players");
        mRecentListener = mRecentDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mRecentAdapter.clear();
                if (snapshot.exists())
                    for (DataSnapshot childData : snapshot.getChildren()){
                        String id = childData.getKey();
                        getPlayerUsername(id);
                    }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void getPlayerUsername(String id){
        FirebaseDatabase.getInstance().getReference()
                .child("Users").child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    String username = snapshot.child("username").getValue(String.class);
                    User user = new User(username, id);
                    mRecentAdapter.add(user);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
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

        if (mInLobby)
            inflater.inflate(R.menu.recent_menu, menu);
        if (!mInLobby)
            inflater.inflate(R.menu.recent_menu_noinv, menu);
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        if (item.getItemId() == R.id.add_recent){
            User user = (User) mRecentListView.getAdapter().getItem(info.position);
            sendFriendRequest(user);
        }
        if (item.getItemId() == R.id.invite_recent){
            User user = (User) mRecentListView.getAdapter().getItem(info.position);
            inviteToGame(user);
        }
        return super.onContextItemSelected(item);
    }

    private void inviteToGame(User user) {
        if (user != null && mHostName != null && mUsername != null){
            DatabaseReference invitesDB = FirebaseDatabase.getInstance().getReference()
                    .child("Users").child(user.getId()).child("Invitations");
            invitesDB.child(mAuth.getUid()).child("host").setValue(mHostName);
            invitesDB.child(mAuth.getUid()).child("invitee").setValue(mUsername);
        }
    }

    private void sendFriendRequest(User user) {
        String id = user.getId();
        DatabaseReference usersDB = FirebaseDatabase.getInstance().getReference().child("Users");
        usersDB.child(id).child("Friend_Requests").child(mAuth.getUid()).setValue(true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        removeListeners();
    }

    private void removeListeners() {
        if (mRecentListener !=  null)
            mRecentDB.removeEventListener(mRecentListener);
    }
}