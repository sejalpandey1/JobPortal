package com.jobportal.app.models;

public class Application {

    private String applicationId;
    private String jobId;
    private String seekerId;
    private String employerId;
    private String seekerName;
    private String seekerEmail;
    private String jobTitle;
    private String status;
    private long appliedDate;
    private String coverLetter;

    public Application() {}

    public Application(String applicationId, String jobId, String seekerId,
                       String employerId, String seekerName,
                       String seekerEmail, String jobTitle) {
        this.applicationId = applicationId;
        this.jobId = jobId;
        this.seekerId = seekerId;
        this.employerId = employerId;
        this.seekerName = seekerName;
        this.seekerEmail = seekerEmail;
        this.jobTitle = jobTitle;
        this.status = "pending";
        this.appliedDate = System.currentTimeMillis();
    }

    public String getApplicationId() { return applicationId; }
    public void setApplicationId(String applicationId) { this.applicationId = applicationId; }

    public String getJobId() { return jobId; }
    public void setJobId(String jobId) { this.jobId = jobId; }

    public String getSeekerId() { return seekerId; }
    public void setSeekerId(String seekerId) { this.seekerId = seekerId; }

    public String getEmployerId() { return employerId; }
    public void setEmployerId(String employerId) { this.employerId = employerId; }

    public String getSeekerName() { return seekerName; }
    public void setSeekerName(String seekerName) { this.seekerName = seekerName; }

    public String getSeekerEmail() { return seekerEmail; }
    public void setSeekerEmail(String seekerEmail) { this.seekerEmail = seekerEmail; }

    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public long getAppliedDate() { return appliedDate; }
    public void setAppliedDate(long appliedDate) { this.appliedDate = appliedDate; }

    public String getCoverLetter() { return coverLetter; }
    public void setCoverLetter(String coverLetter) { this.coverLetter = coverLetter; }
}