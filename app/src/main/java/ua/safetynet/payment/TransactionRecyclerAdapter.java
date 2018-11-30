package ua.safetynet.payment;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.Timestamp;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import ua.safetynet.Database;
import ua.safetynet.R;
import ua.safetynet.group.GlideApp;
import ua.safetynet.group.Group;
import ua.safetynet.group.GroupRecyclerAdapter;
import ua.safetynet.user.User;
/**
 * @author Jeremy McCormick
 * Recycler Adapter class to show transaction information in a recycler view. Can change how it
 * will show info based on the showType passed in
 * NOIMAGE will display group name without image
 * USER shows user image and user name with transaction
 * GROUP shows group image along with group name
 */
public class TransactionRecyclerAdapter extends RecyclerView.Adapter<TransactionRecyclerAdapter.ViewHolder>{
    private List<Transaction> transactionList;
    private int showType = 2;
    public static final int NOIMAGE = 0;
    public static final int USER = 1;
    public static final int GROUP = 2;


    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView nameText;
        public TextView amountText;
        public ImageView imageView;
        public TextView dateText;
        public View view;
        public ViewHolder(View v) {
            super(v);
            this.view = v;
            this.nameText = v.findViewById(R.id.transaction_recycler_name);
            this.amountText = v.findViewById(R.id.transaction_recycler_amount);
            this.imageView = v.findViewById(R.id.transaction_recycler_image);
            this.dateText = v.findViewById(R.id.transaction_recycler_date);
        }
    }

    public TransactionRecyclerAdapter(List<Transaction> list) {
        this.transactionList = list;
    }

    /**
     * Contructor takes in list of transactions and showType
     * @param list
     * @param showType
     */
    public TransactionRecyclerAdapter(List<Transaction> list, int showType) {
        this.showType = showType;
        this.transactionList = list;
    }
    @NonNull
    @Override
    public TransactionRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.transaction_recyclerview_row, parent, false);
        return new TransactionRecyclerAdapter.ViewHolder(v);
    }
    @Override
    public void onBindViewHolder(final TransactionRecyclerAdapter.ViewHolder holder, int position) {
        final Transaction transaction = transactionList.get(position);
        //Get image based on what we should be showing
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference imageRef = storageRef.child("groupimages/" + transaction.getGroupId() + ".jpg");
        switch(showType) {
            case NOIMAGE: holder.imageView.setVisibility(View.GONE);
                break;
            case GROUP: imageRef = storageRef.child("groupimages/" + transaction.getGroupId() + ".jpg");
                break;
            case USER: imageRef = storageRef.child("userimages/" + transaction.getUserId() + ".jpg");
                break;
        }
        if(showType != NOIMAGE)
            Glide.with(holder.itemView).load(imageRef).apply(new RequestOptions()).into(holder.imageView);
        //Get user name or group name based on what we should be showing
        if(showType == USER) {
        //Check if transaction should be anonymous
            if(transaction.shouldAnonymous())
                holder.nameText.setText("*****");
            else {
                Database db = new Database();
                db.getUser(transaction.getUserId(), new Database.DatabaseUserListener() {
                    @Override
                    public void onUserRetrieval(User user) {
                        holder.nameText.setText(user.getName());
                        Glide.with(holder.itemView).load(R.drawable.defaultuser).into(holder.imageView);
                    }
                });
            }
        }
        else {
            Database db = new Database();
            db.getGroup(transaction.getGroupId(), new Database.DatabaseGroupListener() {
                @Override
                public void onGroupRetrieval(Group group) {
                    holder.nameText.setText(group.getName());
                }
            });
        }
        //Set amount
        NumberFormat format = NumberFormat.getCurrencyInstance(Locale.US);
        Log.d("TransactionAdapter", "Amount is " + transaction.getFunds().toPlainString() + " " + format.format(transaction.getFunds()));
        //If negative set text to red
        if(transaction.getFunds().compareTo(BigDecimal.ZERO)  < 0)
            holder.amountText.setTextColor(Color.RED);
        holder.amountText.setText(format.format(transaction.getFunds()));
        //Set Date

        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.US);
        String dateString = dateFormat.format(transaction.getTimestamp().toDate());
        holder.dateText.setText(dateString);



    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }

}
