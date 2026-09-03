package com.jobportal.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jobportal.app.R;
import com.jobportal.app.models.User;
import java.util.List;

public class AdminUserAdapter extends RecyclerView.Adapter<AdminUserAdapter.ViewHolder> {

    Context context;
    List<User> userList;
    FirebaseFirestore db;

    public AdminUserAdapter(Context context, List<User> userList) {
        this.context = context;
        this.userList = userList;
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_admin_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = userList.get(position);

        holder.tvName.setText(user.getName());
        holder.tvEmail.setText(user.getEmail());
        holder.tvPhone.setText(user.getPhone() != null
                ? user.getPhone() : "N/A");
        holder.tvUserType.setText(user.getUserType().toUpperCase());

        switch (user.getUserType().toLowerCase()) {
            case "jobseeker":
                holder.tvUserType.setBackgroundColor(0xFF1565C0);
                break;
            case "employer":
                holder.tvUserType.setBackgroundColor(0xFF2E7D32);
                break;
            case "admin":
                holder.tvUserType.setBackgroundColor(0xFFB71C1C);
                break;
        }

        holder.btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Delete User")
                    .setMessage("क्या आप इस user को delete करना चाहते हैं?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        db.collection("users")
                                .document(user.getUserId())
                                .delete()
                                .addOnSuccessListener(unused -> {
                                    userList.remove(position);
                                    notifyDataSetChanged();
                                    Toast.makeText(context,
                                            "User deleted!",
                                            Toast.LENGTH_SHORT).show();
                                });
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() { return userList.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvEmail, tvPhone, tvUserType;
        Button btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            tvUserType = itemView.findViewById(R.id.tvUserType);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}