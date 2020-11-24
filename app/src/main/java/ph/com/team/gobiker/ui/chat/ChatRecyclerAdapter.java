package ph.com.team.gobiker.ui.chat;

import android.app.LauncherActivity;
import android.content.Context;
import android.content.Intent;
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
import ph.com.team.gobiker.CommentsActivity;
import ph.com.team.gobiker.FindFriendsActivity;
import ph.com.team.gobiker.LikesActivity;
import ph.com.team.gobiker.PersonProfileActivity;
import ph.com.team.gobiker.R;

public class ChatRecyclerAdapter extends RecyclerView.Adapter<ChatRecyclerAdapter.ViewHolder> {

    private List<FindChat> listItems;
    private Context context;

    public ChatRecyclerAdapter(List<FindChat> listItems, Context context){
        this.listItems = listItems;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.all_chat_layout, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FindChat listItem = listItems.get(position);

        if (listItem.getProfileimage().equals(null) || listItem.getProfileimage().equals(""))
            Picasso.with(context).load(R.drawable.profile).into(holder.profilepicture);
        else
            Picasso.with(context).load(listItem.getProfileimage()).placeholder(R.drawable.profile).into(holder.profilepicture);

        holder.chat_name.setText(listItem.getFullname());
        holder.chat_time.setText(listItem.getDatetime());
        holder.chat_message.setText(listItem.getMessage());
        holder.chat_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(listItem.getChattype().equals("single")){
                    Intent profileIntent =  new Intent(context,ChatActivity.class);
                    profileIntent.putExtra("visit_user_id",listItem.getId());
                    context.startActivity(profileIntent);
                }else if(listItem.getChattype().equals("group")){
                    Intent profileIntent =  new Intent(context,ChatGroupActivity.class);
                    profileIntent.putExtra("gcKey",listItem.getId());
                    context.startActivity(profileIntent);
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
        public TextView chat_name, chat_time, chat_message;
        public LinearLayout chat_layout;

        public ViewHolder(View itemView){
            super(itemView);

            profilepicture = (CircleImageView) itemView.findViewById(R.id.all_chat_profile_image);
            chat_name = (TextView) itemView.findViewById(R.id.all_chat_profile_name);
            chat_time = (TextView) itemView.findViewById(R.id.all_chat_time);
            chat_message = (TextView) itemView.findViewById(R.id.all_chat_text);
            chat_layout = (LinearLayout) itemView.findViewById(R.id.chat_linearlayout);

        }
    }

}
