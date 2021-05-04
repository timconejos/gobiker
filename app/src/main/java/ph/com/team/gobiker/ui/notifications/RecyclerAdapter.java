package ph.com.team.gobiker.ui.notifications;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.Comparator;
import java.util.List;


import de.hdodenhof.circleimageview.CircleImageView;
import ph.com.team.gobiker.ui.posts.CommentsActivity;
import ph.com.team.gobiker.ui.posts.LikesActivity;
import ph.com.team.gobiker.R;
import ph.com.team.gobiker.ui.home.GroupDetailsActivity;
import ph.com.team.gobiker.ui.posts.SpecificPostActivity;
import ph.com.team.gobiker.ui.profile.PersonProfileActivity;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {

    private List<Notifications> listItems;
    private Context context;


    public RecyclerAdapter(List<Notifications> listItems, Context context){
        this.listItems = listItems;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.all_notification_layout, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Notifications listItem = listItems.get(position);

        if (listItem.getProfileimage().equals(null) || listItem.getProfileimage().equals(""))
            Picasso.with(context).load(R.drawable.profile).into(holder.profilepicture);
        else
            Picasso.with(context).load(listItem.getProfileimage()).placeholder(R.drawable.profile).into(holder.profilepicture);

        holder.notif_description.setText(listItem.getDescription());
        holder.notif_time.setText(listItem.getDate()+" "+listItem.getTime());

        if(!listItem.getSeen()){
            holder.notif_layout.setBackgroundColor(Color.LTGRAY);
        }else{
            holder.notif_layout.setBackgroundColor(Color.WHITE);
        }

        holder.notif_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(listItem.getNotiftype().equals("comment")){
                    Intent commentsIntent = new Intent(context, SpecificPostActivity.class);
                    commentsIntent.putExtra("post_id", listItem.getPostid());
                    commentsIntent.putExtra("feed_type", "HomeFeed");
                    context.startActivity(commentsIntent);
                }
                if(listItem.getNotiftype().equals("like")){
                    Intent likesIntent = new Intent(context, SpecificPostActivity.class);
                    likesIntent.putExtra("post_id", listItem.getPostid());
                    likesIntent.putExtra("feed_type", "HomeFeed");
                    context.startActivity(likesIntent);
                }
                if(listItem.getNotiftype().equals("follow")){
                    Intent profileIntent =  new Intent(context, PersonProfileActivity.class);
                    profileIntent.putExtra("visit_user_id",listItem.getUid());
                    context.startActivity(profileIntent);
                }
                if(listItem.getNotiftype().equals("group_comment")){
                    Intent commentsIntent = new Intent(context, SpecificPostActivity.class);
//                    commentsIntent.putExtra("PostKey", listItem.getPostid());
//                    commentsIntent.putExtra("FeedType", "GroupFeed");
                    commentsIntent.putExtra("post_id", listItem.getPostid());
                    commentsIntent.putExtra("feed_type", "GroupFeed");
                    context.startActivity(commentsIntent);
                }
                if(listItem.getNotiftype().equals("group_like")){
                    Intent likesIntent = new Intent(context, SpecificPostActivity.class);
//                    likesIntent.putExtra("PostKey", listItem.getPostid());
//                    likesIntent.putExtra("FeedType", "GroupFeed");
                    likesIntent.putExtra("post_id", listItem.getPostid());
                    likesIntent.putExtra("feed_type", "GroupFeed");
                    context.startActivity(likesIntent);
                }
                if(listItem.getNotiftype().equals("group_join")){
                    Intent groupIntent =  new Intent(context, GroupDetailsActivity.class);
                    groupIntent.putExtra("GroupID",listItem.getUid());
                    groupIntent.putExtra("groupAction","joinrequests");
                    context.startActivity(groupIntent);
                }
                if(listItem.getNotiftype().equals("group_member")){
                    Intent groupIntent =  new Intent(context, GroupDetailsActivity.class);
                    groupIntent.putExtra("GroupID",listItem.getUid());
                    groupIntent.putExtra("groupAction","viewmembers");
                    context.startActivity(groupIntent);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return listItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public CircleImageView profilepicture;
        public TextView notif_description, notif_time;
        public LinearLayout notif_layout;

        public ViewHolder(View itemView){
            super(itemView);

            profilepicture = (CircleImageView) itemView.findViewById(R.id.notification_profile_image);
            notif_description = (TextView) itemView.findViewById(R.id.notification_desc);
            notif_time = (TextView) itemView.findViewById(R.id.notification_time);
            notif_layout = (LinearLayout) itemView.findViewById(R.id.linear_posts);

        }
    }


    public class DateComparator implements Comparator<Notifications>
    {
        @Override
        public int compare(Notifications notifications, Notifications t1) {
            return notifications.date.compareTo(t1.date);
        }
    }
}
