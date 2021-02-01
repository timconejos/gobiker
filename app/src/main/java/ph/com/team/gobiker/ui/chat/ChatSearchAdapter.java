package ph.com.team.gobiker.ui.chat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;


import de.hdodenhof.circleimageview.CircleImageView;
import ph.com.team.gobiker.R;

public class ChatSearchAdapter extends RecyclerView.Adapter<ChatSearchAdapter.ViewHolder> {

    private List<ChatProfile> listItems;
    private Context context;

    public ChatSearchAdapter(List<ChatProfile> listItems, Context context){
        this.listItems = listItems;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.all_profile_chat_search, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatProfile listItem = listItems.get(position);

        if (listItem.getProfileimage().equals(null) || listItem.getProfileimage().equals(""))
            Picasso.with(context).load(R.drawable.profile).into(holder.profilepicture);
        else
            Picasso.with(context).load(listItem.getProfileimage()).placeholder(R.drawable.profile).into(holder.profilepicture);

        holder.notif_description.setText(listItem.getDescription());
        holder.notif_remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listItems.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, listItems.size());
            }
        });
    }

    @Override
    public int getItemCount() {
        return listItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public CircleImageView profilepicture;
        public TextView notif_description;
        public LinearLayout notif_layout;
        public Button notif_remove;

        public ViewHolder(View itemView){
            super(itemView);

            notif_layout = (LinearLayout) itemView.findViewById(R.id.linear_posts);
            notif_description = (TextView) itemView.findViewById(R.id.all_users_profile_name);
            profilepicture = (CircleImageView) itemView.findViewById(R.id.all_users_profile_image);
            notif_remove = (Button) itemView.findViewById(R.id.remove_profile);

        }
    }

}
