package ph.com.team.gobiker.ui.chat;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;
import ph.com.team.gobiker.ClickPostActivity;
import ph.com.team.gobiker.CommentsActivity;
import ph.com.team.gobiker.FindFriends;
import ph.com.team.gobiker.FindFriendsActivity;
import ph.com.team.gobiker.PersonProfileActivity;
import ph.com.team.gobiker.R;
import ph.com.team.gobiker.ui.home.HomeFragment;
import ph.com.team.gobiker.ui.home.Posts;

public class ChatFragment extends Fragment {

    private ChatViewModel chatViewModel;
    private FirebaseAuth mAuth;
    private RecyclerView postList;
    private DatabaseReference MessagesRef,UsersRef;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        chatViewModel =
                ViewModelProviders.of(this).get(ChatViewModel.class);
        View root = inflater.inflate(R.layout.fragment_chat, container, false);
        final TextView textView = root.findViewById(R.id.text_chat);
        chatViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });

        mAuth = FirebaseAuth.getInstance();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        MessagesRef = FirebaseDatabase.getInstance().getReference().child("Messages").child(mAuth.getCurrentUser().getUid());

        postList = root.findViewById(R.id.all_users_msgs_list);
        postList.setHasFixedSize(true);
        postList.setLayoutManager(new LinearLayoutManager(getActivity()));

        DisplayAllUsersMsgs();

        return root;
    }

    private void DisplayAllUsersMsgs() {
        FirebaseRecyclerAdapter<FindChat, FindChatViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<FindChat, FindChatViewHolder>(
                FindChat.class,
                R.layout.all_users_display_layout,
                FindChatViewHolder.class,
                UsersRef
        ) {
            @Override
            protected void populateViewHolder(final FindChatViewHolder findChatViewHolder, final FindChat findChat, int i) {
                findChatViewHolder.setFullname(findChat.getFullname());

                findChatViewHolder.setProfileimage(getActivity().getApplicationContext(), findChat.getProfileimage());
            }
        };
        postList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class FindChatViewHolder extends RecyclerView.ViewHolder{
        View mView;
        public FindChatViewHolder(View itemView){
            super(itemView);
            mView = itemView;
        }

        public void setProfileimage(Context ctx, String profileimage){
            CircleImageView myImage = mView.findViewById(R.id.all_users_profile_image);
            Picasso.with(ctx).load(profileimage).placeholder(R.drawable.profile).into(myImage);
        }

        public void setFullname(String fullname) {
            TextView myName = mView.findViewById(R.id.all_users_profile_name);
            myName.setText(fullname);
        }
    }
}
