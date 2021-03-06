//
// This file is generated by parsec-rdl-gen (development version)
// WILL NOT be auto-generated if file has already existed.
//
package com.yahoo.ec;

import com.yahoo.ec.parsec_generated.Student;
import com.yahoo.ec.parsec_generated.Course;
import com.yahoo.ec.parsec_generated.ResourceContext;
import com.yahoo.ec.parsec_generated.CurriculumHandler;
import com.yahoo.ec.parsec_generated.CoursesResponse;
import com.yahoo.ec.parsec_generated.StudentsResponse;
import com.yahoo.ec.parsec_generated.CourseRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.List;
import java.util.ArrayList;

import com.yahoo.ec.core.Curriculum;
import com.yahoo.ec.services.ServiceImpl;

/**
 * CurriculumHandlerImpl is interface implementation that implement CurriculumHandler interface.
 */
public class CurriculumHandlerImpl implements CurriculumHandler {
    private Curriculum curriculum;

    public CurriculumHandlerImpl() {
        this.curriculum = new Curriculum(new ServiceImpl());
    }


    @Override
    public CoursesResponse getCoursesAll(ResourceContext context) {
        CoursesResponse res = new CoursesResponse();

        List<com.yahoo.ec.core.Course> courses = curriculum.getCoursesAll();
        List<Course> endpointCourses = new ArrayList<Course>();

        for (com.yahoo.ec.core.Course course : courses) {
            Course endpointCourse = new Course();

            endpointCourse.setTeacherId(course.getTeacherId())
                    .setCourseName(course.getCourseName())
                    .setTeacherName(course.getTeacherName())
                    .setMaxSeats(course.getMaxSeats());

            endpointCourses.add(endpointCourse);
        }

        res.setCourses(endpointCourses);
        res.setResultsTotal(endpointCourses.size());

        return res;
    }

    @Override
    public CoursesResponse getCourses(ResourceContext context, String studentId) {
        CoursesResponse response = new CoursesResponse();

        List<com.yahoo.ec.core.Course> courses = curriculum.getCourseByStudentId(studentId);
        List<Course> endpointCourses = new ArrayList<Course>();

        for (com.yahoo.ec.core.Course course : courses) {
            Course endpointCourse = new Course();

            endpointCourse.setTeacherId(course.getTeacherId())
                    .setCourseName(course.getCourseName())
                    .setTeacherName(course.getTeacherName())
                    .setMaxSeats(course.getMaxSeats());

            endpointCourses.add(endpointCourse);
        }

        response.setCourses(endpointCourses);
        response.setResultsTotal(endpointCourses.size());

        return response;
    }

    @Override
    public Course postCourses(ResourceContext context, Course course) {
        com.yahoo.ec.core.Course coreCourse = new com.yahoo.ec.core.Course();

        coreCourse.setTeacherId(course.getTeacherId())
                .setCourseName(course.getCourseName())
                .setTeacherName(course.getTeacherName())
                .setMaxSeats(course.getMaxSeats());

        curriculum.createCourse(coreCourse);

        return course;
    }

    @Override
    public StudentsResponse getStudents(ResourceContext context, String teacherId) {
        StudentsResponse res = new StudentsResponse();

        List<com.yahoo.ec.core.Student> students = curriculum.getStudentByTeacherId(teacherId);
        List<Student> endpointStudents = new ArrayList<Student>();

        for (com.yahoo.ec.core.Student student : students) {
            Student endpointStudent = new Student();

            endpointStudent.setStudentId(student.getStudentId())
                    .setStudentName(student.getStudentName());

            endpointStudents.add(endpointStudent);
        }

        res.setStudents(endpointStudents).setResultsTotal(endpointStudents.size());

        return res;
    }

    @Override
    public Course putCoursesByTeacherId(
            ResourceContext context,
            String teacherId,
            CourseRequest request
    ) {
        Course course = new Course();

        com.yahoo.ec.core.Course coreCourse = curriculum.registerCourse(teacherId, request.getCourseName(), request.getStudentId());

        course.setTeacherId(coreCourse.getTeacherId())
                .setCourseName(coreCourse.getCourseName())
                .setTeacherName(coreCourse.getTeacherName())
                .setMaxSeats(coreCourse.getMaxSeats());

        return course;
    }

    @Override
    public ResourceContext newResourceContext(HttpServletRequest request, HttpServletResponse response) {
        return new DefaultResourceContext(request, response);
    }
}
