package ph.com.team.gobiker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class SearchAutoCompleteAdapter extends ArrayAdapter<SearchAutoComplete> {
    private List<SearchAutoComplete> searchListFull;

    public SearchAutoCompleteAdapter(@NonNull Context context, @NonNull List<SearchAutoComplete> searchList) {
        super(context, 0, searchList);
        searchListFull = new ArrayList<>(searchList);
    }

    @NonNull
    @Override
    public Filter getFilter(){
        return searchFilter;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent){
        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(
                    R.layout.all_users_display_layout, parent, false
            );
        }

        TextView profileName = convertView.findViewById(R.id.all_users_profile_name);
        CircleImageView profileImage = convertView.findViewById(R.id.all_users_profile_image);

        SearchAutoComplete profileItem = getItem(position);

        if(profileItem != null){
            profileName.setText(profileItem.getProfilename());
            if (profileItem.getProfileimage().equals(null) || profileItem.getProfileimage().equals(""))
                Picasso.with(getContext()).load(R.drawable.profile).into(profileImage);
            else
                Picasso.with(getContext()).load(profileItem.getProfileimage()).placeholder(R.drawable.profile).into(profileImage);
        }
        return convertView;
    }

    private Filter searchFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            List<SearchAutoComplete> suggestions = new ArrayList<>();

            if(constraint == null || constraint.length() == 0){
                suggestions.addAll(searchListFull);
            }else{
                String filterPattern = constraint.toString().toLowerCase().trim();
                for(SearchAutoComplete profile : searchListFull){
                    if(profile.getProfilename().toLowerCase().contains(filterPattern)){
                        suggestions.add(profile);
                    }
                }
            }
            results.values = suggestions;
            results.count = suggestions.size();
            return results;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults results) {
            clear();
            addAll((List) results.values);
            notifyDataSetChanged();
        }

        @Override
        public CharSequence convertResultToString(Object resultValue){
            return ((SearchAutoComplete) resultValue).getProfilename();
        }
    };
}
