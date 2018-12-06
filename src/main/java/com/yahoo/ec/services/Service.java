package com.yahoo.ec.services;

import com.yahoo.ec.core.Course;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.List;

public interface Service {
    Course createCourse(@Nonnull String teacherId, @Nonnull String courseName, String teacherName, int maxSeats);
    List<Course> getCourseByStudentId(@Nonnull String studentId);
}