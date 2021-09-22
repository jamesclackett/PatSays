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

/*
    A Fragment that shows the user their list of recent players.
    Allows the user to send recent players a friend request or to invite them
    to a game (if in lobby)
    Again similar in functionality to FriendsFragment.
    Uses ListViews & Adapters to present the data to user and context menu's to interact with it
    Please refer to FriendsFragment for explanation on the below methods. They differ only in
    their names and how they access database data, but their logic is very similar.
*/

public class RecentPlayersFragment extends Fragment {
    private final String TAG = "RecentPlayersFragment";
    private LobbyListener mLobbyListener;
    private Context mContext;
    private FirebaseAuth mAuth;
    private Boolean mInLobby;
    private ListView mRecentListView;
    private ArrayAdapter<User> mRecentAdapter;
    private DatabaseReference mRecentDB;
    private ValueEventListener mRecentListener;
    private User myProfile;

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

            getMyProfile();
            initializeListView();
            attachContextMenu();
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

    private void initializeListView() {
        mRecentListView = getView().findViewById(R.id.recent_list_view);
        mRecentAdapter = new ArrayAdapter<>(mContext, android.R.layout.simple_list_item_1);
        mRecentListView.setAdapter(mRecentAdapter);
        fillRecentList();
    }


    private void attachContextMenu() {
        registerForContextMenu(mRecentListView);
        mRecentListView.setOnItemClickListener((parent, view, position, id) -> parent.showContextMenuForChild(view));
    }

    private void fillRecentList() {
        mRecentDB = FirebaseDatabase.getInstance().getReference()
                .child("Users").child(mAuth.getUid()).child("Recent_Players");
        mRecentListener = mRecentDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mRecentAdapter.clear();
                if (snapshot.exists())
                    for (DataSnapshot childData : snapshot.getChildren()){
                        String id = childData.getKey();
                        appendUserProfile(id);
                    }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void appendUserProfile(String id){
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
            User.requestFriend(myProfile, user);
        }
        if (item.getItemId() == R.id.invite_recent){
            User user = (User) mRecentListView.getAdapter().getItem(info.position);
            User.invite(myProfile, user);
        }
        return super.onContextItemSelected(item);
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