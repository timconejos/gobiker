package ph.com.team.gobiker.ui.home;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.squareup.picasso.Picasso;

import java.security.acl.Group;
import java.util.List;


import de.hdodenhof.circleimageview.CircleImageView;
import ph.com.team.gobiker.R;
import ph.com.team.gobiker.ui.chat.ChatProfile;

public class GroupSearchAdapter extends RecyclerView.Adapter<GroupSearchAdapter.ViewHolder> {

    private List<GroupMemberProfile> listItems;
    private Context context;
    private DatabaseReference GroupsRef;

    public GroupSearchAdapter(List<GroupMemberProfile> listItems, Context context, DatabaseReference GroupsRef){
        this.listItems = listItems;
        this.context = context;
        this.GroupsRef = GroupsRef;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.all_group_members, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GroupMemberProfile listItem = listItems.get(position);

        if (listItem.getProfileimage().equals(null) || listItem.getProfileimage().equals(""))
            Picasso.with(context).load(R.drawable.profile).into(holder.profilepicture);
        else
            Picasso.with(context).load(listItem.getProfileimage()).placeholder(R.drawable.profile).into(holder.profilepicture);

        holder.notif_description.setText(listItem.getDescription());
        holder.notif_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AdminOptionsDialog alert = new AdminOptionsDialog();
                alert.showDialog(context, listItem.getGid(), listItem.getUid(), listItem.getDescription(), listItem.getProfileimage(), listItem.getCurrentrole());
            }
        });

        holder.notif_role.setText(listItem.getCurrentrole());
    }

    @Override
    public int getItemCount() {
        return listItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public CircleImageView profilepicture;
        public TextView notif_description, notif_role;
        public LinearLayout notif_layout;

        public ViewHolder(View itemView){
            super(itemView);

            notif_layout = (LinearLayout) itemView.findViewById(R.id.linear_posts);
            notif_description = (TextView) itemView.findViewById(R.id.all_users_profile_name);
            profilepicture = (CircleImageView) itemView.findViewById(R.id.all_users_profile_image);
            notif_role = (TextView) itemView.findViewById(R.id.member_role);
        }
    }

    public class AdminOptionsDialog {
        public void showDialog(Context activity, String gid, String uid, String membername, String memberimage, String memberrole){
            final Dialog admindialog = new Dialog(activity);
            admindialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            admindialog.setCancelable(false);
            admindialog.setContentView(R.layout.dialog_members_options);

            Toolbar toolbar = (Toolbar) admindialog.findViewById(R.id.toolbar);
            toolbar.setTitle("Member Details");
            ((AppCompatActivity)activity).setSupportActionBar(toolbar);
            ((AppCompatActivity)activity).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            ((AppCompatActivity)activity).getSupportActionBar().setDisplayShowHomeEnabled(true);

            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    admindialog.dismiss();
                }
            });


            CircleImageView member_pic = (CircleImageView) admindialog.findViewById(R.id.member_pic);
            TextView member_name = (TextView) admindialog.findViewById(R.id.member_name);

            if (memberimage.equals(null) || memberimage.equals(""))
                Picasso.with(context).load(R.drawable.profile).into(member_pic);
            else
                Picasso.with(context).load(memberimage).placeholder(R.drawable.profile).into(member_pic);

            member_name.setText(membername);
            Button make_admin_btn = (Button) admindialog.findViewById(R.id.make_admin_btn);
            Button kick_btn = (Button) admindialog.findViewById(R.id.kick_btn);

            if(memberrole.equals("Member")){
                make_admin_btn.setText("Promote");
            }else if(memberrole.equals("Admin")){
                make_admin_btn.setText("Demote");
            }

            make_admin_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which){
                                case DialogInterface.BUTTON_POSITIVE:
                                    if(make_admin_btn.getText().equals("Promote")){
                                        GroupsRef.child(gid).child("Members").child(uid).child("role").setValue("Admin").addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    Toast.makeText(activity, "You have successfully promoted this member.", Toast.LENGTH_SHORT).show();
                                                    admindialog.dismiss();
                                                    ((GroupDetailsActivity)context).profileSelectedList.clear();
                                                    ((GroupDetailsActivity)context).profileadapter.notifyDataSetChanged();
                                                    ((GroupDetailsActivity)context).RetrieveUsers();
                                                }else{
                                                    Toast.makeText(activity, "Something went wrong. Please check internet connection and try again.", Toast.LENGTH_SHORT).show();
                                                    admindialog.dismiss();
                                                }

                                            }
                                        });
                                    }else if(make_admin_btn.getText().equals("Demote")){
                                        GroupsRef.child(gid).child("Members").child(uid).child("role").setValue("Member").addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    Toast.makeText(activity, "You have successfully demoted this member.", Toast.LENGTH_SHORT).show();
                                                    admindialog.dismiss();
                                                    ((GroupDetailsActivity)context).profileSelectedList.clear();
                                                    ((GroupDetailsActivity)context).profileadapter.notifyDataSetChanged();
                                                    ((GroupDetailsActivity)context).RetrieveUsers();
                                                }else{
                                                    Toast.makeText(activity, "Something went wrong. Please check internet connection and try again.", Toast.LENGTH_SHORT).show();
                                                    admindialog.dismiss();
                                                }
                                            }
                                        });
                                    }

                                    break;

                                case DialogInterface.BUTTON_NEGATIVE:
                                    //No button clicked
                                    break;
                            }
                        }
                    };


                    AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AlertDialogTheme);
                    builder.setMessage("Are you sure you want to "+make_admin_btn.getText()+" this member?")
                            .setPositiveButton("Yes", dialogClickListener)
                            .setNegativeButton("No", dialogClickListener);

                    AlertDialog alert = builder.create();
                    alert.setOnShowListener(arg0 -> {
                        alert.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(activity.getResources().getColor(R.color.colorPrimary));
                        alert.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(activity.getResources().getColor(R.color.colorPrimaryDark));
                    });
                    alert.show();

                }
            });

            kick_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which){
                                case DialogInterface.BUTTON_POSITIVE:
                                    GroupsRef.child(gid).child("Members").child(uid).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                Toast.makeText(activity, "You have successfully kicked this member out of the group.", Toast.LENGTH_SHORT).show();
                                                admindialog.dismiss();
                                                ((GroupDetailsActivity)context).profileSelectedList.clear();
                                                ((GroupDetailsActivity)context).profileadapter.notifyDataSetChanged();
                                                ((GroupDetailsActivity)context).RetrieveUsers();
                                            }else{
                                                Toast.makeText(activity, "Something went wrong. Please check internet connection and try again.", Toast.LENGTH_SHORT).show();
                                                admindialog.dismiss();
                                            }

                                        }
                                    });
                                    break;

                                case DialogInterface.BUTTON_NEGATIVE:
                                    //No button clicked
                                    break;
                            }
                        }
                    };

                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setMessage("Are you sure you want to kick this member from the group?")
                            .setPositiveButton("Yes", dialogClickListener)
                            .setNegativeButton("No", dialogClickListener);

                    AlertDialog alert = builder.create();
                    alert.setOnShowListener(arg0 -> {
                        alert.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(activity.getResources().getColor(R.color.colorPrimary));
                        alert.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(activity.getResources().getColor(R.color.colorPrimaryDark));
                    });
                    alert.show();

                }
            });
            admindialog.show();
        }
    }

}
