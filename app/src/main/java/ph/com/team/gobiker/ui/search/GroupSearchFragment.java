package ph.com.team.gobiker.ui.search;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;
import ph.com.team.gobiker.R;
import ph.com.team.gobiker.ui.home.GroupDetailsActivity;
import ph.com.team.gobiker.ui.home.HomeViewModel;
import ph.com.team.gobiker.ui.profile.PersonProfileActivity;

public class GroupSearchFragment extends Fragment {
    private View root;
    private RecyclerView SearchResultList;
    private DatabaseReference allGroupsDatabaseRef;
    private String searchTag;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.fragment_search_results, container, false);

        allGroupsDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Groups");

        SearchResultList = root.findViewById(R.id.search_result_list);
        SearchResultList.setHasFixedSize(true);
        SearchResultList.setLayoutManager(new LinearLayoutManager(getActivity()));

        searchTag = getArguments().getString("searchTag");

        SearchPeopleAndFriends(searchTag);

        return root;
    }

    private void SearchPeopleAndFriends(String searchBoxInput) {
        Query searchGroupsQuery = allGroupsDatabaseRef.orderByChild("group_name")
                .startAt(searchBoxInput).endAt(searchBoxInput + "\uf8ff");

        FirebaseRecyclerAdapter<FindGroups, FindFriendsViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<FindGroups, FindFriendsViewHolder>(
                FindGroups.class,
                R.layout.all_users_display_layout,
                FindFriendsViewHolder.class,
                searchGroupsQuery
        ) {
            @Override
            protected void populateViewHolder(FindFriendsViewHolder findFriendsViewHolder, FindGroups findGroups, int i) {
                findFriendsViewHolder.setFullname(findGroups.getGroup_name());

                findFriendsViewHolder.setProfileimage(getActivity(), findGroups.getGroup_picture());
                final String group_id = getRef(i).getKey();

                findFriendsViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent groupDetailsIntent =  new Intent(getActivity(), GroupDetailsActivity.class);
                        groupDetailsIntent.putExtra("GroupID",group_id);
                        groupDetailsIntent.putExtra("groupAction","none");
                        startActivity(groupDetailsIntent);
                    }
                });
            }
        };
        SearchResultList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class FindFriendsViewHolder extends RecyclerView.ViewHolder{
        View mView;
        public FindFriendsViewHolder(View itemView){
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
