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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;


import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;

/**
 * CurriculumHandlerImpl is interface implementation that implement CurriculumHandler interface.
 */
public class CurriculumHandlerImpl implements CurriculumHandler {

    @Override
    public CoursesResponse getCourses(ResourceContext context, String studentId) {
        CoursesResponse response = new CoursesResponse();

        HashMap<String, AttributeValue> key_to_get = new HashMap<String, AttributeValue>();

        key_to_get.put("studentId", AttributeValue.builder().s(studentId).build());

        GetItemRequest request = GetItemRequest.builder().key(key_to_get).tableName("Student").build();

        DynamoDbClient ddb = DynamoDbClient.create();

        try {
            Map<String, AttributeValue> returned_item = ddb.getItem(request).item();

            if (returned_item != null) {
                List<AttributeValue> attributes = returned_item.get("courses").l();
                List<Course> student_courses = new ArrayList<Course>();

                for (AttributeValue attribute : attributes) {
                    Map<String, AttributeValue>  fields = attribute.m();
                    Course course = new Course();

                    course.setTeacherId(fields.get("teacherId").s());
                    course.setCourseName(fields.get("courseName").s());

                    student_courses.add(course);
                }

                response.setCourses(student_courses);
                response.setResultsTotal(student_courses.size());
            }
        } catch (DynamoDbException e) {
            System.err.println(e.getMessage());
        }

        return response;
    }

    @Override
    public Course postCourses(ResourceContext context, Course course) {
        HashMap<String,AttributeValue> item_values = new HashMap<String,AttributeValue>();

        item_values.put("teacherId", AttributeValue.builder().s(course.getTeacherId()).build());
        item_values.put("courseName", AttributeValue.builder().s(course.getCourseName()).build());
        item_values.put("teacherName", AttributeValue.builder().s(course.getTeacherName()).build());
        item_values.put("maxSeats", AttributeValue.builder().n(Integer.toString(course.getMaxSeats())).build());

        DynamoDbClient ddb = DynamoDbClient.create();
        PutItemRequest request = PutItemRequest.builder().tableName("Courses").item(item_values).build();

        try {
            ddb.putItem(request);
        } catch (ResourceNotFoundException e) {
            System.err.format("Error: The table \"%s\" can't be found.\n", "Courses");
            System.err.println("Be sure that it exists and that you've typed its name correctly!");
        } catch (DynamoDbException e) {
            System.err.println(e.getMessage());
        }

        return course;
    }

    @Override
    public ResourceContext newResourceContext(HttpServletRequest request, HttpServletResponse response) {
        return new DefaultResourceContext(request, response);
    }
}
