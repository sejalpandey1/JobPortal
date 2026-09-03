package com.jobportal.app.models;

public class Job {

    private String jobId;
    private String title;
    private String company;
    private String location;
    private String salary;
    private String description;
    private String requirements;
    private String employerId;
    private String jobType;
    private String category;
    private long postedDate;
    private boolean isActive;

    private String approvalStatus; // "pending", "approved", "rejected"

    public Job() {}

    public Job(String jobId, String title, String company, String location,
               String salary, String description, String employerId) {
        this.jobId = jobId;
        this.title = title;
        this.company = company;
        this.location = location;
        this.salary = salary;
        this.description = description;
        this.employerId = employerId;
        this.isActive = true;
        this.postedDate = System.currentTimeMillis();
    }

    public String getJobId() { return jobId; }
    public void setJobId(String jobId) { this.jobId = jobId; }

    public String getApprovalStatus() { return approvalStatus; }
    public void setApprovalStatus(String approvalStatus) {
        this.approvalStatus = approvalStatus;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getSalary() { return salary; }
    public void setSalary(String salary) { this.salary = salary; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getRequirements() { return requirements; }
    public void setRequirements(String requirements) { this.requirements = requirements; }

    public String getEmployerId() { return employerId; }
    public void setEmployerId(String employerId) { this.employerId = employerId; }

    public String getJobType() { return jobType; }
    public void setJobType(String jobType) { this.jobType = jobType; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public long getPostedDate() { return postedDate; }
    public void setPostedDate(long postedDate) { this.postedDate = postedDate; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
}