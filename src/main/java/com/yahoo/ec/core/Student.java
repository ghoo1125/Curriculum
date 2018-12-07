package com.yahoo.ec.core;

public class Student {
    private String studentId;
    private String studentname;

    public String getStudentId() {
        return studentId;
    }

    public Student setStudentId(String studentId) {
        this.studentId = studentId;
        return this;
    }

    public String getStudentname() {
        return studentname;
    }

    public Student setStudentname(String studentname) {
        this.studentname = studentname;
        return this;
    }
}