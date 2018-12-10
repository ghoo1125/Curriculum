package com.yahoo.ec.core;

import com.yahoo.ec.services.Service;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

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
        List<Student> rv = new ArrayList<Student>();
        Set<String> studentSet = new HashSet<String>();

        List<Student> studentList = service.getStudentByTeacherId(teacherId);
        for (Student s : studentList) {
            if (!studentSet.contains(s.getStudentId())) {
                studentSet.add(s.getStudentId());
                rv.add(s);
            }
        }
        return rv;
    }

    public Course registerCourse(String teacherId, String courseName, String studentId) {
        Course target = service.getOneCourseStudents(teacherId, courseName);

        if (target.getStudents().size() < target.getMaxSeats() &&
            !target.getStudents().contains(studentId)) {
            target = service.addStudentToCourse(target, studentId);
        }

        return target;
    }
}
