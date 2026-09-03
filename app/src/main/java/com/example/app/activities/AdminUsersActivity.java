package com.jobportal.app.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.jobportal.app.R;
import com.jobportal.app.adapters.AdminUserAdapter;
import com.jobportal.app.models.User;
import java.util.ArrayList;
import java.util.List;

public class AdminUsersActivity extends BaseActivity {

    RecyclerView rvUsers;
    AdminUserAdapter userAdapter;
    List<User> userList, allUserList;
    ProgressBar progressBar;
    TextView tvEmpty, tabAll, tabSeekers, tabEmployers;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_users);
        applyWindowInsets(findViewById(android.R.id.content));

        db = FirebaseFirestore.getInstance();

        rvUsers = findViewById(R.id.rvUsers);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);
        tabAll = findViewById(R.id.tabAll);
        tabSeekers = findViewById(R.id.tabSeekers);
        tabEmployers = findViewById(R.id.tabEmployers);

        userList = new ArrayList<>();
        allUserList = new ArrayList<>();
        userAdapter = new AdminUserAdapter(this, userList);
        rvUsers.setLayoutManager(new LinearLayoutManager(this));
        rvUsers.setAdapter(userAdapter);

        loadUsers();

        tabAll.setOnClickListener(v -> filterUsers("all"));
        tabSeekers.setOnClickListener(v -> filterUsers("jobseeker"));
        tabEmployers.setOnClickListener(v -> filterUsers("employer"));
    }

    private void filterUsers(String type) {
        userList.clear();
        if (type.equals("all")) {
            userList.addAll(allUserList);
        } else {
            for (User user : allUserList) {
                if (user.getUserType().equals(type)) {
                    userList.add(user);
                }
            }
        }
        userAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUsers();
    }

    private void loadUsers() {
        progressBar.setVisibility(View.VISIBLE);
        db.collection("users").get()
                .addOnSuccessListener(querySnapshot -> {
                    progressBar.setVisibility(View.GONE);
                    allUserList.clear();
                    userList.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        User user = doc.toObject(User.class);
                        allUserList.add(user);
                        userList.add(user);
                    }
                    if (userList.isEmpty()) {
                        tvEmpty.setVisibility(View.VISIBLE);
                    } else {
                        tvEmpty.setVisibility(View.GONE);
                    }
                    userAdapter.notifyDataSetChanged();
                });
    }
}