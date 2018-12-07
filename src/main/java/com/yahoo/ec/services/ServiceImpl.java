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

    private final String TEACHER_ID = "TeacherId";
    private final String COURSE_NAME = "CourseName";
    private final String TEACHER_NAME = "TeacherName";
    private final String MAX_SEATS = "MaxSeats";
    private final String STUDENTS = "Students";

    private final String STUDENT_ID = "StudentId";
    private final String COURSES = "Courses";

    @Override
    public Course createCourse(@Nonnull String teacherId, @Nonnull String courseName, String teacherName, int maxSeats) {
        HashMap<String,AttributeValue> itemValues = new HashMap<String,AttributeValue>();

        itemValues.put(TEACHER_ID, AttributeValue.builder().s(teacherId).build());
        itemValues.put(COURSE_NAME, AttributeValue.builder().s(courseName).build());
        itemValues.put(TEACHER_NAME, AttributeValue.builder().s(teacherName).build());
        itemValues.put(MAX_SEATS, AttributeValue.builder().n(Integer.toString(maxSeats)).build());

        DynamoDbClient ddb = DynamoDbClient.create();
        PutItemRequest request = PutItemRequest.builder().tableName(COURSE_TABLE_NAME).item(itemValues).build();

        try {
            ddb.putItem(request);
        } catch (ResourceNotFoundException e) {
            System.err.format("Error: The table \"%s\" can't be found.\n", COURSE_TABLE_NAME);
            System.err.println("Be sure that it exists and that you've typed its name correctly!");
        } catch (DynamoDbException e) {
            System.err.println(e.getMessage());
        }

        return null;
    }

    @Override
    public List<Course> getCourseByStudentId(@Nonnull String studentId) {
        HashMap<String, AttributeValue> keyToGet = new HashMap<String, AttributeValue>();

        keyToGet.put(STUDENT_ID, AttributeValue.builder().s(studentId).build());

        GetItemRequest request = GetItemRequest.builder().key(keyToGet).tableName(STUDENT_TABLE_NAME).build();

        DynamoDbClient ddb = DynamoDbClient.create();

        List<Course> student_courses = new ArrayList<Course>();
        try {
            Map<String, AttributeValue> returnedItem = ddb.getItem(request).item();

            if (returnedItem != null) {
                List<AttributeValue> attributes = returnedItem.get(COURSES).l();

                for (AttributeValue attribute : attributes) {
                    Map<String, AttributeValue>  fields = attribute.m();
                    Course course = new Course();

                    course.setTeacherId(fields.get(TEACHER_ID).s());
                    course.setCourseName(fields.get(COURSE_NAME).s());

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

                if (map.get(TEACHER_ID) != null) {
                    course.setTeacherId(map.get(TEACHER_ID).s());
                }
                if (map.get(COURSE_NAME) != null) {
                    course.setCourseName(map.get(COURSE_NAME).s());
                }
                if (map.get(TEACHER_NAME) != null) {
                    course.setTeacherName(map.get(TEACHER_NAME).s());
                }
                if (map.get(MAX_SEATS) != null) {
                    course.setMaxSeats(
                            Integer.parseInt(map.get(MAX_SEATS).n()));
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
            courseItemKey.put(TEACHER_ID, AttributeValue.builder().s(teacherId).build());
            courseItemKey.put(COURSE_NAME, AttributeValue.builder().s(courseName).build());

            GetItemRequest getItemReq = GetItemRequest.builder().key(courseItemKey).tableName(COURSE_TABLE_NAME).build();
            Map<String, AttributeValue> item = ddb.getItem(getItemReq).item();

            if (item != null) {
                List<AttributeValue> studentIds = item.get(STUDENTS).l();
                List<String> studentList = new ArrayList<String>();

                for (AttributeValue s : studentIds) {
                    studentList.add(s.s());
                }

                result.setStudents(studentList).setMaxSeats(Integer.parseInt(item.get(MAX_SEATS).n()));
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
            courseItemKey.put(TEACHER_ID, AttributeValue.builder().s(course.getTeacherId()).build());
            courseItemKey.put(COURSE_NAME, AttributeValue.builder().s(course.getCourseName()).build());

            Map<String, AttributeValue> attrValues = new HashMap<String,AttributeValue>();
            attrValues.put(":c", AttributeValue.builder().l(AttributeValue.builder().s(studentId).build()).build());


            // Update Course table.
            UpdateItemRequest updateCourseItemReq = UpdateItemRequest.builder()
                    .tableName(COURSE_TABLE_NAME)
                    .key(courseItemKey)
                    .updateExpression("SET " + STUDENTS + " = list_append(" + STUDENTS + ", :c)")
                    .expressionAttributeValues(attrValues)
                    .build();

            ddb.updateItem(updateCourseItemReq);

            // Update Student table.
            // Could try DynamoDB Stream later
            Map<String, AttributeValue> studentItemKey = new HashMap<String, AttributeValue>();
            studentItemKey.put(STUDENT_ID, AttributeValue.builder().s(studentId).build());

            Map<String, AttributeValue> insertVal = new HashMap<String, AttributeValue>();
            insertVal.put(TEACHER_ID, AttributeValue.builder().s(course.getTeacherId()).build());
            insertVal.put(COURSE_NAME, AttributeValue.builder().s(course.getCourseName()).build());

            attrValues.put(":c", AttributeValue.builder()
                    .l(AttributeValue.builder().m(insertVal).build())
                    .build());

            UpdateItemRequest updateStudentItemReq = UpdateItemRequest.builder()
                    .tableName(STUDENT_TABLE_NAME)
                    .key(studentItemKey)
                    .updateExpression("SET " + COURSES + " = list_append(" + COURSES + ", :c)")
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
                    .keyConditionExpression(TEACHER_ID + " = " + partitionKeyName)
                    .expressionAttributeValues(attrValues)
                    .build();

            QueryResponse queryRes = ddb.query(queryReq);

            // Iterate over every student in each course item.
            for (Map<String, AttributeValue> map : queryRes.items()) {
                List<AttributeValue> students = map.get(STUDENTS).l();

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

