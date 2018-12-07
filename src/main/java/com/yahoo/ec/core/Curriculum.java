package com.yahoo.ec.core;

import com.yahoo.ec.services.Service;

import java.util.List;

public class Curriculum {
    private Service service;

    public Curriculum(Service service) {
        this.service = service;
    }


    public Course createCourse(Course course) {
        return service.createCourse(course.getTeacherId(), course.getCourseName(), course.getTeacherName(), course.getMaxSeats());
    }

    public List<Course> getCourseByStudentId(String studentId) {
        return service.getCourseByStudentId(studentId);
    }

    public List<Course> getCoursesAll() {
        return service.getCoursesAll();
    }

    public List<Student> getStudentByTeacherId(String teacherId) {
        return service.getStudentByTeacherId(teacherId);
    }

    public Course registerCourse(String teacherId, String courseName, String studentId) {
        Course target = service.getOneCourseStudents(teacherId, courseName);

        if (target.getStudents().size() < target.getMaxSeats()) {
            target = service.addStudentToCourse(target,  studentId);
        }

        return target;
    }
}