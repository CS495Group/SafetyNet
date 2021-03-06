package ua.safetynet.group;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import ua.safetynet.R;

/**
 * @author Jeremy McCormick
 * Adapter for group to list it in recylcer view with its image, name, and balance
 * Uses Firebase Storage UI and Glide libraries to populate image
 */
public class GroupRecyclerAdapter extends RecyclerView.Adapter<GroupRecyclerAdapter.ViewHolder> {
    private List<Group> groupList;
    private GroupClicked clickedListener;
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView mPicture;
        public TextView  mGroupName;
        public TextView  mAmount;
        public View layout;
        public ViewHolder(View v) {
            super(v);
            layout = v;
            mPicture = v.findViewById(R.id.main_recyclerview_icon);
            mGroupName = v.findViewById(R.id.main_recyclerview_group_name);
            mAmount = v.findViewById(R.id.main_recyclerview_group_amount);
        }
    }

    public GroupRecyclerAdapter( List<Group> list, GroupClicked clicked) {
        this.clickedListener = clicked;
        this.groupList = list;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public GroupRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.main_recyclerview_row, parent, false);
        return new ViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)

    /**
     * Replace contents of a view with groups values. Binds image view to
     * groups image storage reference in Firebase
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        final Group group = groupList.get(position);

        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference groupImageRef = storageRef.child("groupimages/" + group.getGroupId() + ".jpg");
        Glide.with(holder.itemView).load(groupImageRef).apply(new RequestOptions()).into(holder.mPicture);

        holder.mGroupName.setText(group.getName());
        NumberFormat format = NumberFormat.getCurrencyInstance(Locale.US);
        holder.mAmount.setText(format.format(group.getFunds()));

        //Transfers to appropriate group activity when element is clicked
        holder.itemView.setOnClickListener( (view) ->
            clickedListener.onItemClicked(group)
        );
    }

    @Override
    public int getItemCount() {
        return groupList.size();
    }

    public void goToGroupHome(Group group) {
        GroupHomeFragment fragment = GroupHomeFragment.newInstance(group);

    }

    public interface GroupClicked {
        void onItemClicked(Group group);
    }
}

