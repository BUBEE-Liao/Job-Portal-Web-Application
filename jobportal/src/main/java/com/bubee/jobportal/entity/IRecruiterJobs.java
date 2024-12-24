package com.bubee.jobportal.entity;

// method name is according to the column name of table
// e.g. totalCantiteates -> get"T"otalCandidates();
// implementation will be done by spring
public interface IRecruiterJobs {

    Long getTotalCandidates();

    int getJob_post_id();

    String getJob_title();

    int getLocationId();

    String getCity();

    String getState();

    String getCountry();

    int getCompanyId();

    String getName();
}
