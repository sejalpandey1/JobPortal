package com.jobportal.app.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.jobportal.app.R;
import de.hdodenhof.circleimageview.CircleImageView;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends BaseActivity {

    EditText etName, etPhone, etSkills, etExperience, etEducation;
    Button btnSave, btnUploadResume, btnViewResume;
    CircleImageView ivProfile;
    TextView tvResumeStatus, tvChangePhoto;
    ProgressBar progressBar;
    FirebaseFirestore db;
    FirebaseAuth mAuth;
    Uri resumeUri = null;
    String existingResumeBase64 = null;
    String photoBase64 = null;

    ActivityResultLauncher<Intent> resumePicker = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK
                        && result.getData() != null) {
                    resumeUri = result.getData().getData();
                    tvResumeStatus.setText("Resume selected ✓");
                    tvResumeStatus.setTextColor(
                            getColor(android.R.color.holo_green_dark));
                }
            });

    ActivityResultLauncher<Intent> photoPicker = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK
                        && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    try {
                        InputStream inputStream =
                                getContentResolver().openInputStream(imageUri);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        Bitmap resized = Bitmap.createScaledBitmap(
                                bitmap, 300, 300, true);
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        resized.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                        photoBase64 = Base64.encodeToString(
                                baos.toByteArray(), Base64.DEFAULT);
                        ivProfile.setImageBitmap(resized);
                    } catch (Exception e) {
                        Toast.makeText(this, "Error: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        applyWindowInsets(findViewById(android.R.id.content));

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        etName = findViewById(R.id.etName);
        etPhone = findViewById(R.id.etPhone);
        etSkills = findViewById(R.id.etSkills);
        etExperience = findViewById(R.id.etExperience);
        etEducation = findViewById(R.id.etEducation);
        btnSave = findViewById(R.id.btnSave);
        btnUploadResume = findViewById(R.id.btnUploadResume);
        btnViewResume = findViewById(R.id.btnViewResume);
        tvResumeStatus = findViewById(R.id.tvResumeStatus);
        tvChangePhoto = findViewById(R.id.tvChangePhoto);
        ivProfile = findViewById(R.id.ivProfile);
        progressBar = findViewById(R.id.progressBar);

        loadExistingData();

        ivProfile.setOnClickListener(v -> pickPhoto());
        tvChangePhoto.setOnClickListener(v -> pickPhoto());

        btnUploadResume.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("application/pdf");
            resumePicker.launch(intent);
        });

        btnViewResume.setOnClickListener(v -> viewResume());
        btnSave.setOnClickListener(v -> saveProfile());
    }

    private void pickPhoto() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        photoPicker.launch(intent);
    }

    private void loadExistingData() {
        String uid = mAuth.getCurrentUser().getUid();
        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    etName.setText(doc.getString("name"));
                    etPhone.setText(doc.getString("phone"));
                    etSkills.setText(doc.getString("skills"));
                    etExperience.setText(doc.getString("experience"));
                    etEducation.setText(doc.getString("education"));

                    String profilePhoto = doc.getString("profilePhoto");
                    if (profilePhoto != null && !profilePhoto.isEmpty()) {
                        byte[] photoBytes = Base64.decode(
                                profilePhoto, Base64.DEFAULT);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(
                                photoBytes, 0, photoBytes.length);
                        ivProfile.setImageBitmap(bitmap);
                    }

                    existingResumeBase64 = doc.getString("resumeBase64");
                    if (existingResumeBase64 != null
                            && !existingResumeBase64.isEmpty()) {
                        tvResumeStatus.setText("Resume uploaded ✓");
                        tvResumeStatus.setTextColor(
                                getColor(android.R.color.holo_green_dark));
                        btnViewResume.setVisibility(View.VISIBLE);
                    }
                });
    }

    private void viewResume() {
        if (existingResumeBase64 == null || existingResumeBase64.isEmpty()) {
            Toast.makeText(this, "No resume found!", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            byte[] pdfBytes = Base64.decode(existingResumeBase64, Base64.DEFAULT);
            java.io.File file = new java.io.File(getCacheDir(), "resume.pdf");
            java.io.FileOutputStream fos = new java.io.FileOutputStream(file);
            fos.write(pdfBytes);
            fos.close();

            Uri uri = androidx.core.content.FileProvider.getUriForFile(
                    this, getPackageName() + ".provider", file);

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/pdf");
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, "Open PDF"));
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void saveProfile() {
        String name = etName.getText().toString().trim();
        if (name.isEmpty()) { etName.setError("नाम डालो"); return; }

        progressBar.setVisibility(View.VISIBLE);
        btnSave.setEnabled(false);

        String uid = mAuth.getCurrentUser().getUid();
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("phone", etPhone.getText().toString().trim());
        updates.put("skills", etSkills.getText().toString().trim());
        updates.put("experience", etExperience.getText().toString().trim());
        updates.put("education", etEducation.getText().toString().trim());

        if (photoBase64 != null) {
            updates.put("profilePhoto", photoBase64);
        }

        if (resumeUri != null) {
            try {
                InputStream inputStream =
                        getContentResolver().openInputStream(resumeUri);
                ByteArrayOutputStream byteArrayOutputStream =
                        new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) != -1) {
                    byteArrayOutputStream.write(buffer, 0, length);
                }
                String base64 = Base64.encodeToString(
                        byteArrayOutputStream.toByteArray(), Base64.DEFAULT);
                updates.put("resumeBase64", base64);
            } catch (Exception e) {
                Toast.makeText(this, "Resume error: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        }

        db.collection("users").document(uid)
                .update(updates)
                .addOnSuccessListener(unused -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Profile saved!",
                            Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnSave.setEnabled(true);
                    Toast.makeText(this, "Error: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }
}