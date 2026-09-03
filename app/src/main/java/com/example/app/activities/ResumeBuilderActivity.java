package com.jobportal.app.activities;

import android.content.ClipData;

import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.content.FileProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.jobportal.app.R;
import java.io.File;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;

public class
ResumeBuilderActivity extends BaseActivity {

    EditText etName, etEmail, etPhone, etSkills,
            etExperience, etEducation, etObjective;
    Button btnGenerate, btnClear, btnShare,
            btnCopy, btnSavePdf, btnSendEmployer;
    TextView tvGeneratedResume;
    ProgressBar progressBar;
    ScrollView scrollResult;
    FirebaseFirestore db;
    FirebaseAuth mAuth;
    String generatedResumeText = "";
    android.widget.Spinner spinnerTemplate;
    String selectedTemplate = "Modern";

    // Groq API — Free + India में काम करता है
    private static final String GROQ_API_KEY = "api key ";
    private static final String API_URL =
            "https://api.groq.com/openai/v1/chat/completions";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resume_builder);
        applyWindowInsets(findViewById(R.id.rootLayout));

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etSkills = findViewById(R.id.etSkills);
        etExperience = findViewById(R.id.etExperience);
        etEducation = findViewById(R.id.etEducation);
        etObjective = findViewById(R.id.etObjective);
        btnGenerate = findViewById(R.id.btnGenerate);
        btnClear = findViewById(R.id.btnClear);
        btnShare = findViewById(R.id.btnShare);
        btnCopy = findViewById(R.id.btnCopy);
        btnSavePdf = findViewById(R.id.btnSavePdf);
        btnSendEmployer = findViewById(R.id.btnSendEmployer);
        tvGeneratedResume = findViewById(R.id.tvGeneratedResume);
        progressBar = findViewById(R.id.progressBar);
        scrollResult = findViewById(R.id.scrollResult);

        spinnerTemplate = findViewById(R.id.spinnerTemplate);
        String[] templates = {"Modern", "Classic", "Minimal",
                "Creative", "Executive"};
        android.widget.ArrayAdapter<String> templateAdapter =
                new android.widget.ArrayAdapter<>(this,
                        android.R.layout.simple_spinner_item, templates);
        templateAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        spinnerTemplate.setAdapter(templateAdapter);
        spinnerTemplate.setOnItemSelectedListener(
                new android.widget.AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(
                            android.widget.AdapterView<?> parent,
                            android.view.View view, int position, long id) {
                        selectedTemplate = templates[position];
                    }
                    @Override
                    public void onNothingSelected(
                            android.widget.AdapterView<?> parent) {}
                });

        loadProfileData();

        btnGenerate.setOnClickListener(v -> generateResume());

        btnClear.setOnClickListener(v -> {
            tvGeneratedResume.setText("");
            generatedResumeText = "";
            scrollResult.setVisibility(View.GONE);
        });

        btnShare.setOnClickListener(v -> shareResume());
        btnCopy.setOnClickListener(v -> copyResume());
        btnSavePdf.setOnClickListener(v -> saveAsPdf());
        btnSendEmployer.setOnClickListener(v -> sendToEmployer());
    }

    private void loadProfileData() {
        String uid = mAuth.getCurrentUser().getUid();
        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.getString("name") != null)
                        etName.setText(doc.getString("name"));
                    if (doc.getString("email") != null)
                        etEmail.setText(doc.getString("email"));
                    if (doc.getString("phone") != null)
                        etPhone.setText(doc.getString("phone"));
                    if (doc.getString("skills") != null)
                        etSkills.setText(doc.getString("skills"));
                    if (doc.getString("experience") != null)
                        etExperience.setText(doc.getString("experience"));
                    if (doc.getString("education") != null)
                        etEducation.setText(doc.getString("education"));
                });
    }

    private void generateResume() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String skills = etSkills.getText().toString().trim();
        String experience = etExperience.getText().toString().trim();
        String education = etEducation.getText().toString().trim();
        String objective = etObjective.getText().toString().trim();

        if (name.isEmpty() || skills.isEmpty()) {
            Toast.makeText(this,
                    "Name और Skills ज़रूरी हैं!",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnGenerate.setEnabled(false);
        scrollResult.setVisibility(View.GONE);

        String templateStyle = "";
        switch (selectedTemplate) {
            case "Modern":
                templateStyle = "Use modern formatting with clear sections, " +
                        "bullet points, and professional language.";
                break;
            case "Classic":
                templateStyle = "Use traditional resume format with " +
                        "formal language and structured sections.";
                break;
            case "Minimal":
                templateStyle = "Use minimal clean format with " +
                        "concise points and white space.";
                break;
            case "Creative":
                templateStyle = "Use creative format with strong " +
                        "action verbs and impressive language.";
                break;
            case "Executive":
                templateStyle = "Use executive level format with " +
                        "leadership focus and achievements.";
                break;
        }

        String prompt = "Create a professional " + selectedTemplate +
                " style resume. " + templateStyle + "\n\n" +
                "Person Details:\n" +
                "Name: " + name + "\n" +
                "Email: " + email + "\n" +
                "Phone: " + phone + "\n" +
                "Career Objective: " + (objective.isEmpty()
                ? "Seeking a challenging position to utilize " +
                "my skills and grow professionally" : objective) + "\n" +
                "Technical Skills: " + skills + "\n" +
                "Work Experience: " + (experience.isEmpty()
                ? "Fresh Graduate" : experience) + "\n" +
                "Education: " + education + "\n\n" +
                "IMPORTANT FORMATTING RULES:\n" +
                "1. Start with candidate name as heading\n" +
                "2. Use ═══════════════ as section dividers\n" +
                "3. Use ► for bullet points\n" +
                "4. Use CAPS for section headings\n" +
                "5. Make it exactly 1 page worth of content\n" +
                "6. Include all 5 sections: CONTACT, OBJECTIVE, " +
                "SKILLS, EXPERIENCE, EDUCATION\n" +
                "7. Make it ATS-friendly and impressive\n" +
                "8. Add relevant skills based on the profile";

        callGroqAPI(prompt);
    }

    private void callGroqAPI(String prompt) {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                .build();

        try {
            JSONObject message = new JSONObject();
            message.put("role", "user");
            message.put("content", prompt);

            JSONArray messages = new JSONArray();
            messages.put(message);

            JSONObject requestBody = new JSONObject();
            requestBody.put("model", "llama-3.3-70b-versatile");
            requestBody.put("messages", messages);
            requestBody.put("max_tokens", 2048);
            requestBody.put("temperature", 0.7);

            android.util.Log.d("RESUME_API",
                    "Groq Request: " + requestBody.toString());

            RequestBody body = RequestBody.create(
                    requestBody.toString(),
                    MediaType.parse("application/json; charset=utf-8"));

            Request request = new Request.Builder()
                    .url(API_URL)
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", "Bearer " + GROQ_API_KEY)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        btnGenerate.setEnabled(true);
                        Toast.makeText(ResumeBuilderActivity.this,
                                "Network Error: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    });
                }

                @Override
                public void onResponse(Call call, Response response)
                        throws IOException {
                    String responseBody = response.body().string();
                    android.util.Log.d("RESUME_API",
                            "Code: " + response.code());
                    android.util.Log.d("RESUME_API",
                            "Response: " + responseBody);

                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        btnGenerate.setEnabled(true);

                        if (!response.isSuccessful()) {
                            try {
                                JSONObject errorJson =
                                        new JSONObject(responseBody);
                                String errorMsg = errorJson
                                        .getJSONObject("error")
                                        .getString("message");
                                Toast.makeText(ResumeBuilderActivity.this,
                                        "Error: " + errorMsg,
                                        Toast.LENGTH_LONG).show();
                            } catch (Exception ex) {
                                Toast.makeText(ResumeBuilderActivity.this,
                                        "HTTP Error: " + response.code(),
                                        Toast.LENGTH_LONG).show();
                            }
                            return;
                        }

                        try {
                            JSONObject json = new JSONObject(responseBody);

                            generatedResumeText = json
                                    .getJSONArray("choices")
                                    .getJSONObject(0)
                                    .getJSONObject("message")
                                    .getString("content");

                            tvGeneratedResume.setText(generatedResumeText);
                            scrollResult.setVisibility(View.VISIBLE);

                            Toast.makeText(ResumeBuilderActivity.this,
                                    "✅ Resume generated!",
                                    Toast.LENGTH_SHORT).show();

                        } catch (Exception e) {
                            android.util.Log.e("RESUME_API",
                                    "Parse Error: " + e.getMessage());
                            Toast.makeText(ResumeBuilderActivity.this,
                                    "Error: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                }
            });

        } catch (Exception e) {
            progressBar.setVisibility(View.GONE);
            btnGenerate.setEnabled(true);
            Toast.makeText(this, "Error: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    private void shareResume() {
        if (generatedResumeText.isEmpty()) {
            Toast.makeText(this, "पहले Resume generate करो!",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT,
                "My Professional Resume");
        shareIntent.putExtra(Intent.EXTRA_TEXT, generatedResumeText);
        startActivity(Intent.createChooser(shareIntent,
                "Share Resume via"));
    }

    private void copyResume() {
        if (generatedResumeText.isEmpty()) {
            Toast.makeText(this, "पहले Resume generate करो!",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        ClipboardManager clipboard = (ClipboardManager)
                getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(
                "Resume", generatedResumeText);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, "✅ Resume copied!",
                Toast.LENGTH_SHORT).show();
    }

    private void saveAsPdf() {
        if (generatedResumeText.isEmpty()) {
            Toast.makeText(this, "पहले Resume generate करो!",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            String fileName = "Resume_" +
                    etName.getText().toString().trim()
                            .replace(" ", "_") + ".pdf";

            // Internal storage use करो
            File pdfDir = new File(getFilesDir(), "resumes");
            if (!pdfDir.exists()) pdfDir.mkdirs();
            File pdfFile = new File(pdfDir, fileName);

            PdfWriter writer = new PdfWriter(pdfFile);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            // Page margins
            document.setMargins(40, 40, 40, 40);

            // Name as Title
            document.add(new Paragraph(
                    etName.getText().toString().trim())
                    .setBold()
                    .setFontSize(22)
                    .setTextAlignment(
                            com.itextpdf.layout.properties
                                    .TextAlignment.CENTER));

            // Contact Info
            String contactLine = etEmail.getText().toString().trim()
                    + "  |  " + etPhone.getText().toString().trim();
            document.add(new Paragraph(contactLine)
                    .setFontSize(10)
                    .setTextAlignment(
                            com.itextpdf.layout.properties
                                    .TextAlignment.CENTER)
                    .setFontColor(
                            com.itextpdf.kernel.colors.ColorConstants.GRAY));

            document.add(new Paragraph(" "));

            // Resume Content
            String[] lines = generatedResumeText.split("\n");
            for (String line : lines) {
                if (line.trim().isEmpty()) {
                    document.add(new Paragraph(" ").setFontSize(6));
                } else if (line.contains("═") || line.contains("─")) {
                    document.add(new Paragraph(line)
                            .setFontSize(8)
                            .setFontColor(
                                    com.itextpdf.kernel.colors
                                            .ColorConstants.LIGHT_GRAY));
                } else if (line.toUpperCase().equals(line.trim())
                        && line.trim().length() > 3
                        && !line.contains("►")) {
                    document.add(new Paragraph(line.trim())
                            .setBold()
                            .setFontSize(12)
                            .setFontColor(new com.itextpdf.kernel.colors
                                    .DeviceRgb(21, 101, 192)));
                } else if (line.contains("►")) {
                    document.add(new Paragraph(line.trim())
                            .setFontSize(10)
                            .setMarginLeft(15));
                } else {
                    document.add(new Paragraph(line.trim())
                            .setFontSize(10));
                }
            }

            document.close();

            Toast.makeText(this,
                    "✅ PDF saved successfully!",
                    Toast.LENGTH_LONG).show();

            // PDF open करो
            Uri uri = FileProvider.getUriForFile(this,
                    getPackageName() + ".provider", pdfFile);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/pdf");
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, "Open PDF"));

        } catch (Exception e) {
            Toast.makeText(this,
                    "PDF Error: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    private void sendToEmployer() {
        if (generatedResumeText.isEmpty()) {
            Toast.makeText(this, "पहले Resume generate करो!",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("text/plain");
        emailIntent.putExtra(Intent.EXTRA_SUBJECT,
                "Job Application - " +
                        etName.getText().toString().trim());
        emailIntent.putExtra(Intent.EXTRA_TEXT,
                "Dear Hiring Manager,\n\n" +
                        "Please find my resume below:\n\n" +
                        generatedResumeText +
                        "\n\nThank you.\n\nRegards,\n" +
                        etName.getText().toString().trim());
        startActivity(Intent.createChooser(emailIntent,
                "Send Resume to Employer"));
    }
}