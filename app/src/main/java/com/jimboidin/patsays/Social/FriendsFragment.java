package com.jimboidin.patsays.Social;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.InputType;
import android.view.ContextMenu;
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
import com.jimboidin.patsays.R;


/*
    A Fragment to contain Friends and Friend Requests
    Has two different behaviors depending on whether user is in a lobby or not:
        > If not in lobby, users can only remove a friend but not invite
        > If in a lobby, users can remove friends but also invite them to the lobby
    Uses ListViews & Adapters to display friends and friends requests
    Uses Context Menus to offer users options when they click on a friend/request
 */
public class FriendsFragment extends Fragment {
    private final String TAG = "FriendsFragment";
    private Context mContext;
    private FirebaseAuth mAuth;
    private DatabaseReference mFriendsDB, mRequestsDB;
    private ValueEventListener mFriendsListener, mRequestsListener;
    private ListView mFriendsListView, mRequestsListView;
    private ArrayAdapter<User> mFriendAdapter, mRequestAdapter;
    private LobbyListener mLobbyListener;
    private Boolean mInLobby;
    private User myProfile;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_friends, container, false);
    }

    // When the fragment is attached to its parent activity (SocialActivity), now we have
    // context to give to the LobbyListener interface. This allows interfacing with SocialActivity
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
        mLobbyListener = (LobbyListener) context; //
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getView() != null){
            mAuth = FirebaseAuth.getInstance();
            mInLobby = mLobbyListener.askIsLobby(); // interface call to SocialActivity

            getMyProfile();
            initializeListViews();
            attachContextMenus();

            FloatingActionButton fab = getView().findViewById(R.id.add_friend_fab);
            fab.setOnClickListener(v -> openRequestDialog());
        }
    }

    // Instantiates myProfile variable with current user details
    // I would like to make this a static method in User class to avoid repetition, however it was not
    // possible to return a value when the method uses the asynchronous 'onDataChange()'
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
        fillFriendsList();
        fillRequestsList();
    }

    // Populates the friends list with data from database.
    // mFriendAdapter is cleared and then a call to appendFriendUser is made with the relevant data
    // mFriendsListener will run in its own thread, updating the friends list as required until it is stopped
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

    // This method works the same as fillFriendsList
    // mRequestsList also runs in background until it is terminated
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

    // Simply creates a User object with the data received from fillFriendsList and adds this to mFriendAdapter
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

    // Same behaviour as appendFriendUser, only for the requests list instead
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


    // Two different context menus are needed for this fragment. One for the friends list and one for
    // requests list. Clicking on an item opens the context menu
    private void attachContextMenus() {
        registerForContextMenu(mFriendsListView);
        registerForContextMenu(mRequestsListView);
        mFriendsListView.setOnItemClickListener((parent, view, position, id) -> parent.showContextMenuForChild(view));
        mRequestsListView.setOnItemClickListener((parent, view, position, id) -> parent.showContextMenuForChild(view));
    }

    // Handles the logic of what menu to show. Menu layouts are stored in Res
    // friends list has two different layouts depending on whether user is in lobby or not
    // requests list has only one layout.
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

    // Handles the logic of what action to take when a menu item is clicked on.
    // takes the id of the menu item and calls the relevant static User method
    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

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
            User.invite(myProfile, user);
        }
        else if (item.getItemId() == R.id.remove_friend){
            User user = (User) mFriendsListView.getAdapter().getItem(info.position);
            User.removeFriend(myProfile, user);
        }
        return super.onContextItemSelected(item);
    }

    // triggered when user clicks on the FAB.
    // Opens a dialog that allows the user to enter a name and calls findAndRequest() with that name
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

    // called by the requestDialog. Searches for the input name in the database and sends off a
    // friend request if that user exists. Currently, if they do not exist then nothing happens
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

    // terminates the two listeners so that they do not continue to run after the fragment has been stopped
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

    // standard behaviour aside from call to removeListeners
    @Override
    public void onDestroy() {
        super.onDestroy();
        removeListeners();
    }
}
