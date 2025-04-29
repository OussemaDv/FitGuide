package com.example.fitguide;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.widget.TextView;

import java.util.ArrayList;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

    @SuppressLint("RestrictedApi")
    Context context;
    ArrayList<User> userArrayList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(User user);
    }

    public MyAdapter(Context context, ArrayList<User> userArrayList) {
        this.context = context;
        this.userArrayList = userArrayList;
    }

    public MyAdapter(Context context, ArrayList<User> userArrayList, OnItemClickListener listener) {
        this.context = context;
        this.userArrayList = userArrayList;
        this.listener = listener;
    }


    @NonNull
    @Override
    public MyAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(context).inflate(R.layout.user_item,parent, false);

        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyAdapter.MyViewHolder holder, int position) {
        User user = userArrayList.get(position);

        holder.fNameText.setText(user.fName);
        holder.roleText.setText(user.role);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(user);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userArrayList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{

        TextView fNameText, emailText,dobText,genderText,phoneText, roleText;



        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            fNameText = itemView.findViewById(R.id.tvfirstname);
            roleText = itemView.findViewById(R.id.tvrole);
        }
    }
}
