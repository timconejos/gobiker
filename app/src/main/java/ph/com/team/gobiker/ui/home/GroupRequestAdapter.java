package ph.com.team.gobiker.ui.home;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


import de.hdodenhof.circleimageview.CircleImageView;
import ph.com.team.gobiker.R;

public class GroupRequestAdapter extends RecyclerView.Adapter<GroupRequestAdapter.ViewHolder> {

    private List<GroupMemberProfile> listItems;
    private Context context;

    public GroupRequestAdapter(List<GroupMemberProfile> listItems, Context context){
        this.listItems = listItems;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.all_join_requests, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GroupMemberProfile listItem = listItems.get(position);

        if (listItem.getProfileimage().equals(null) || listItem.getProfileimage().equals(""))
            Picasso.with(context).load(R.drawable.profile).into(holder.memberpicture);
        else
            Picasso.with(context).load(listItem.getProfileimage()).placeholder(R.drawable.profile).into(holder.memberpicture);

        holder.membername.setText(listItem.getDescription());

        holder.memberlayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AlertDialogTheme);
                DatabaseReference RootReference = FirebaseDatabase.getInstance().getReference();
                Map requestBody = new HashMap();
                Map requestBodyDetails = new HashMap();
                String member_ref = "Groups/" + listItem.getGid() + "/Members/" + listItem.getUid();

                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                requestBody.put("role", "Member");
                                requestBody.put("status","Accepted");

                                requestBodyDetails.put(member_ref,requestBody);

                                RootReference.updateChildren(requestBodyDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            RootReference.child("Users").child(listItem.getUid()).child("groupsJoined").child(listItem.getGid()).setValue("Accepted");
                                            Toast.makeText(context, "You have successfully APPROVED the join request.", Toast.LENGTH_SHORT).show();
                                            ((GroupDetailsActivity)context).profileSelectedList.clear();
                                            ((GroupDetailsActivity)context).profileadapter.notifyDataSetChanged();
                                            ((GroupDetailsActivity)context).requestSelectedList.clear();
                                            ((GroupDetailsActivity)context).requestadapter.notifyDataSetChanged();
                                            ((GroupDetailsActivity)context).RetrieveUsers();
                                        }else{
                                            Toast.makeText(context, "Something went wrong. Please check internet connection and try again.", Toast.LENGTH_SHORT).show();
                                        }

                                    }
                                });
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                builder.setMessage("Are you sure you want to DISAPPROVE the join request?");
                                RootReference.child(member_ref).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            RootReference.child("Users").child(listItem.getUid()).child("groupsJoined").child(listItem.getGid()).removeValue();
                                            Toast.makeText(context, "You have successfully DISAPPROVED the join request.", Toast.LENGTH_SHORT).show();
                                            ((GroupDetailsActivity)context).profileSelectedList.clear();
                                            ((GroupDetailsActivity)context).profileadapter.notifyDataSetChanged();
                                            ((GroupDetailsActivity)context).requestSelectedList.clear();
                                            ((GroupDetailsActivity)context).requestadapter.notifyDataSetChanged();
                                            ((GroupDetailsActivity)context).RetrieveUsers();
                                        }else{
                                            Toast.makeText(context, "Something went wrong. Please check internet connection and try again.", Toast.LENGTH_SHORT).show();
                                        }

                                    }
                                });
                                break;
                        }
                    }
                };

                builder.setMessage("APPROVE or DISAPPROVE join request of "+listItem.getDescription()+"?");
                builder.setPositiveButton("Approve", dialogClickListener)
                        .setNegativeButton("Disapprove", dialogClickListener);

                AlertDialog alert = builder.create();
                alert.setOnShowListener(arg0 -> {
                    alert.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(context.getResources().getColor(R.color.colorPrimary));
                    alert.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(context.getResources().getColor(R.color.colorPrimaryDark));
                });
                alert.show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return listItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public CircleImageView memberpicture;
        public TextView membername;
        public LinearLayout memberlayout;

        public ViewHolder(View itemView){
            super(itemView);

            memberpicture = (CircleImageView) itemView.findViewById(R.id.memberpicture);
            membername = (TextView) itemView.findViewById(R.id.membername);
            memberlayout = (LinearLayout) itemView.findViewById(R.id.linear_posts);
        }
    }

}
