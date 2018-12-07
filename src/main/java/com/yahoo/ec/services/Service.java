package com.yahoo.ec.services;

import com.yahoo.ec.core.Course;
import com.yahoo.ec.core.Student;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.List;

public interface Service {
    Course createCourse(@Nonnull String teacherId, @Nonnull String courseName, String teacherName, int maxSeats);
    List<Course> getCourseByStudentId(@Nonnull String studentId);
    List<Course> getCoursesAll();
    List<Student> getStudentByTeacherId(@Nonnull String teacherId);
    Course getOneCourseStudents(@Nonnull String teacherId, @Nonnull String courseName);
    Course addStudentToCourse(@Nonnull Course course,  String studentId);
}