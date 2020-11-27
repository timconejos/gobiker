package ph.com.team.gobiker.ui.home;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;


import de.hdodenhof.circleimageview.CircleImageView;
import ph.com.team.gobiker.R;

public class GroupsRecyclerAdapter extends RecyclerView.Adapter<GroupsRecyclerAdapter.ViewHolder> {

    private List<Groups> listItems;
    private Context context;

    public GroupsRecyclerAdapter(List<Groups> listItems, Context context){
        this.listItems = listItems;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.all_groups_layout, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Groups listItem = listItems.get(position);

        if (listItem.getGroup_picture().equals(null) || listItem.getGroup_picture().equals(""))
            Picasso.with(context).load(R.drawable.profile).into(holder.grouppicture);
        else
            Picasso.with(context).load(listItem.getGroup_picture()).placeholder(R.drawable.profile).into(holder.grouppicture);

        holder.groupname.setText(listItem.getGroup_name());
        holder.grouptype.setText(listItem.getGroup_type());
        holder.groupjoined.setText(listItem.getCurr_user_joined());

        holder.grouplayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent groupDetailsIntent = new Intent(context, GroupDetailsActivity.class);
                groupDetailsIntent.putExtra("GroupID",listItem.getGroup_id());
                groupDetailsIntent.putExtra("groupAction","none");
                context.startActivity(groupDetailsIntent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public CircleImageView grouppicture;
        public TextView grouptype, groupname, groupjoined;
        public RelativeLayout grouplayout;

        public ViewHolder(View itemView){
            super(itemView);

            grouppicture = (CircleImageView) itemView.findViewById(R.id.group_pic);
            grouptype = (TextView) itemView.findViewById(R.id.group_type);
            groupname = (TextView) itemView.findViewById(R.id.group_name);
            groupjoined = (TextView) itemView.findViewById(R.id.group_joined);
            grouplayout = (RelativeLayout) itemView.findViewById(R.id.relativeLayout);

        }
    }

}
