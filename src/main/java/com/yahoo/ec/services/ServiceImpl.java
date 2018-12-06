package com.yahoo.ec.services;

import com.yahoo.ec.core.Course;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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

public class ServiceImpl implements Service {

    @Override
    public Course createCourse(@Nonnull String teacherId, @Nonnull String courseName, String teacherName, int maxSeats) {
        HashMap<String,AttributeValue> item_values = new HashMap<String,AttributeValue>();

        item_values.put("teacherId", AttributeValue.builder().s(teacherId).build());
        item_values.put("courseName", AttributeValue.builder().s(courseName).build());
        item_values.put("teacherName", AttributeValue.builder().s(teacherName).build());
        item_values.put("maxSeats", AttributeValue.builder().n(Integer.toString(maxSeats)).build());

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

        return null;
    }

    @Override
    public List<Course> getCourseByStudentId(@Nonnull String studentId) {
        HashMap<String, AttributeValue> key_to_get = new HashMap<String, AttributeValue>();

        key_to_get.put("studentId", AttributeValue.builder().s(studentId).build());

        GetItemRequest request = GetItemRequest.builder().key(key_to_get).tableName("Student").build();

        DynamoDbClient ddb = DynamoDbClient.create();

        List<Course> student_courses = new ArrayList<Course>();
        try {
            Map<String, AttributeValue> returned_item = ddb.getItem(request).item();

            if (returned_item != null) {
                List<AttributeValue> attributes = returned_item.get("courses").l();

                for (AttributeValue attribute : attributes) {
                    Map<String, AttributeValue>  fields = attribute.m();
                    Course course = new Course();

                    course.setTeacherId(fields.get("teacherId").s());
                    course.setCourseName(fields.get("courseName").s());

                    student_courses.add(course);
                }
            }
        } catch (DynamoDbException e) {
            System.err.println(e.getMessage());
        }

        return student_courses;
    }
}

