package com.yahoo.ec.core;

public class Course {
    private String teacherId;
    private String courseName;
    private String teacherName;
    private int maxSeats;

    public String getTeacherId() {
        return teacherId;
    }

    public Course setTeacherId(String teacherId) {
        this.teacherId = teacherId;
        return this;
    }

    public String getCourseName() {
        return courseName;
    }

    public Course setCourseName(String courseName) {
        this.courseName = courseName;
        return this;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public Course setTeacherName(String teacherName) {
        this.teacherName = teacherName;
        return this;
    }

    public int getMaxSeats() {
        return maxSeats;
    }

    public Course setMaxSeats(int maxSeats) {
        this.maxSeats = maxSeats;
        return this;
    }
}