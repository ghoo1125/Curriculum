package com.yahoo.ec.services;

import com.yahoo.ec.core.Course;
import com.yahoo.ec.core.Student;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;
import software.amazon.awssdk.services.dynamodb.model.ConditionalOperator;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;

import java.net.URI;
import java.net.URISyntaxException;

public class ServiceImpl implements Service {
    private final String COURSE_TABLE_NAME = "Course";
    private final String STUDENT_TABLE_NAME = "Student";
    private final String LIST_COURSES_INDEX_NAME = "ListAllCourses";

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

    @Override
    public List<Course> getCoursesAll() {
        List<Course> courseList = new ArrayList<Course>();

        try {
            // FIXME Should setup DynamoDB with region when deploy to AWS.
            DynamoDbClient ddb = DynamoDbClient.builder().endpointOverride(
                    new URI("http://localhost:8000")).build();

            ScanRequest scanReq = ScanRequest.builder()
                    .tableName(COURSE_TABLE_NAME)
                    .indexName(LIST_COURSES_INDEX_NAME)
                    .build();
            ScanResponse scanRes = ddb.scan(scanReq);

            for (Map<String, AttributeValue> map : scanRes.items()) {
                Course course = new Course();

                if (map.get("TeacherId") != null) {
                    course.setTeacherId(map.get("TeacherId").s());
                }
                if (map.get("CourseName") != null) {
                    course.setCourseName(map.get("CourseName").s());
                }
                if (map.get("TeacherName") != null) {
                    course.setTeacherName(map.get("TeacherName").s());
                }
                if (map.get("MaxSeats") != null) {
                    course.setMaxSeats(
                            Integer.parseInt(map.get("MaxSeats").n()));
                }

                courseList.add(course);
            }

        } catch (DynamoDbException e) {
            System.out.println(e.getMessage());
            return null;
        } catch (URISyntaxException e) {
            System.out.println(e.getMessage());
            return null;
        }

        return courseList;
    }

    @Override
    public Course getOneCourseStudents(@Nonnull String teacherId, @Nonnull String courseName) {
        Course result = new Course();

        try {
            DynamoDbClient ddb = DynamoDbClient.builder().endpointOverride(new URI("http://localhost:8000")).build();

            Map<String, AttributeValue> courseItemKey = new HashMap<String, AttributeValue>();
            courseItemKey.put("TeacherId", AttributeValue.builder().s(teacherId).build());
            courseItemKey.put("CourseName", AttributeValue.builder().s(courseName).build());

            GetItemRequest getItemReq = GetItemRequest.builder().key(courseItemKey).tableName(COURSE_TABLE_NAME).build();
            Map<String, AttributeValue> item = ddb.getItem(getItemReq).item();

            if (item != null) {
                List<AttributeValue> studentIds = item.get("Students").l();
                List<String> studentList = new ArrayList<String>();

                for (AttributeValue s : studentIds) {
                    studentList.add(s.s());
                }

                result.setStudents(studentList).setMaxSeats(Integer.parseInt(item.get("MaxSeats").n()));
            }
        } catch (DynamoDbException e) {
            System.out.println(e.getMessage());
        } catch (URISyntaxException e) {
            System.out.println(e.getMessage());
        }

        return result;
    }

    @Override
    public Course addStudentToCourse(@Nonnull Course course, String studentId) {
        try {
            // FIXME Should setup DynamoDB with region when deploy to AWS.
            DynamoDbClient ddb = DynamoDbClient.builder().endpointOverride(new URI("http://localhost:8000")).build();

            // Set up primary key.
            Map<String, AttributeValue> courseItemKey = new HashMap<String, AttributeValue>();
            courseItemKey.put("TeacherId", AttributeValue.builder().s(course.getTeacherId()).build());
            courseItemKey.put("CourseName", AttributeValue.builder().s(course.getCourseName()).build());

            Map<String, AttributeValue> attrValues = new HashMap<String,AttributeValue>();
            attrValues.put(":c", AttributeValue.builder().l(AttributeValue.builder().s(studentId).build()).build());


            // Update Course table.
            UpdateItemRequest updateCourseItemReq = UpdateItemRequest.builder()
                    .tableName(COURSE_TABLE_NAME)
                    .key(courseItemKey)
                    .updateExpression("SET Students = list_append(Students, :c)")
                    .expressionAttributeValues(attrValues)
                    .build();

            ddb.updateItem(updateCourseItemReq);

            // Update Student table.
            // Could try DynamoDB Stream later
            Map<String, AttributeValue> studentItemKey = new HashMap<String, AttributeValue>();
            studentItemKey.put("StudentId", AttributeValue.builder().s(studentId).build());

            Map<String, AttributeValue> insertVal = new HashMap<String, AttributeValue>();
            insertVal.put("TeacherId", AttributeValue.builder().s(course.getTeacherId()).build());
            insertVal.put("CourseName", AttributeValue.builder().s(course.getCourseName()).build());

            attrValues.put(":c", AttributeValue.builder()
                    .l(AttributeValue.builder().m(insertVal).build())
                    .build());

            UpdateItemRequest updateStudentItemReq = UpdateItemRequest.builder()
                    .tableName(STUDENT_TABLE_NAME)
                    .key(studentItemKey)
                    .updateExpression("SET Courses = list_append(Courses, :c)")
                    .expressionAttributeValues(attrValues)
                    .build();

            ddb.updateItem(updateStudentItemReq);

        } catch (DynamoDbException e) {
            System.out.println(e.getMessage());
            return null;
        } catch (URISyntaxException e) {
            System.out.println(e.getMessage());
            return null;
        }

        return null;
    }

    @Override
    public List<Student> getStudentByTeacherId(@Nonnull String teacherId) {
        List<Student> studentList = new ArrayList<Student>();

        try {
            // FIXME Should setup DynamoDB with region when deploy to AWS.
            DynamoDbClient ddb = DynamoDbClient.builder().endpointOverride(new URI("http://localhost:8000")).build();

            // Set up mapping of the partition name with the value.
            String partitionKeyName = ":tid";
            HashMap<String, AttributeValue> attrValues = new HashMap<String,AttributeValue>();
            attrValues.put(partitionKeyName, AttributeValue.builder().s(teacherId).build());

            QueryRequest queryReq = QueryRequest.builder()
                    .tableName(COURSE_TABLE_NAME)
                    .keyConditionExpression("TeacherId = " + partitionKeyName)
                    .expressionAttributeValues(attrValues)
                    .build();

            QueryResponse queryRes = ddb.query(queryReq);

            // Iterate over every student in each course item.
            for (Map<String, AttributeValue> map : queryRes.items()) {
                List<AttributeValue> students = map.get("Students").l();

                for (AttributeValue s : students) {
                    Student student = new Student();
                    student.setStudentId(s.s());
                    studentList.add(student);
                }
            }
        } catch (DynamoDbException e) {
            System.out.println(e.getMessage());
            return null;
        } catch (URISyntaxException e) {
            System.out.println(e.getMessage());
            return null;
        }

        return studentList;
    }
}

