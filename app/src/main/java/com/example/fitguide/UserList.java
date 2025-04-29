package com.example.fitguide;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class UserList extends AppCompatActivity implements MyAdapter.OnItemClickListener{

    RecyclerView recyclerView;
    ArrayList<User> userArrayList;
    MyAdapter myAdapter;
    FirebaseFirestore fStore;
    Button addUserButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_list);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        recyclerView = findViewById(R.id.recyclerViewUsers);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        addUserButton = findViewById(R.id.button_add_user);
        addUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //startActivity(new Intent(admin_home_page.this, SensorsData.class));                startActivity(new Intent(admin_home_page.this, SensorsData.class));
                startActivity(new Intent(UserList.this, RegisterActivity.class));

            }
        });

        fStore = FirebaseFirestore.getInstance();
        userArrayList = new ArrayList<User>();
        myAdapter = new MyAdapter(UserList.this, userArrayList, this);

        recyclerView.setAdapter(myAdapter);

        EventChangeListener();
    }

    private void EventChangeListener() {
        fStore.collection("users").orderBy("fName", Query.Direction.ASCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {

                        if(error != null){
                            Log.e("Firestore error", error.getMessage());
                        }
                        if (value == null){
                            Log.e("Firestore error", "QuerySnapshot is null");
                            return;
                        }
                        for(DocumentChange dc : value.getDocumentChanges()){
                            if(dc.getType() == DocumentChange.Type.ADDED){
                                Log.d("FirestoreDebug", "Document ID: " + dc.getDocument().getId());
                                Log.d("FirestoreDebug", "Document data: " + dc.getDocument().getData());
                                User user = dc.getDocument().toObject(User.class);
                                user.setId(dc.getDocument().getId());
                                if (user != null) {
                                    userArrayList.add(user);
                                    Log.d("FirestoreDebug", "User added: " + user.getfName()); // Assuming User has getFName()
                                } else {
                                    Log.e("Firestore error", "Document to User conversion failed");
                                }
                            }
                        }
                        myAdapter.notifyDataSetChanged();

                    }
                });
    }
    @Override
    public void onItemClick(User user) {
        Intent intent = new Intent(this, UserProfileActivity.class);
        intent.putExtra("userId", user.getId());
        //Log.d("userIntent", user.toString());
        startActivity(intent);
    }
}