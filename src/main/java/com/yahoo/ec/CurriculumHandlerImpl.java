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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

import java.net.URISyntaxException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * CurriculumHandlerImpl is interface implementation that implement CurriculumHandler interface.
 */
public class CurriculumHandlerImpl implements CurriculumHandler {
    private final String COURSE_TABLE_NAME = "Course";
    private final String LIST_COURSES_INDEX_NAME = "ListAllCourses";

    @Override
    public CoursesResponse getCoursesAll(ResourceContext context) {
        CoursesResponse res = new CoursesResponse();

        try {
            // FIXME Should setup DynamoDB with region when deploy to AWS.
            DynamoDbClient ddb = DynamoDbClient.builder().endpointOverride(
                new URI("http://localhost:8000")).build();

            ScanRequest scanReq = ScanRequest.builder()
                .tableName(COURSE_TABLE_NAME)
                .indexName(LIST_COURSES_INDEX_NAME)
                .build();
            ScanResponse scanRes = ddb.scan(scanReq);

            List<Course> courseList = new ArrayList<Course>();

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

            res.setCourses(courseList);
            res.setResultsTotal(courseList.size());
        } catch (DynamoDbException e) {
            System.out.println(e.getMessage());
            return null;
        } catch(URISyntaxException e) {
            System.out.println(e.getMessage());
            return null;
        }

        return res;
    }

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
    public StudentsResponse getStudents(ResourceContext context, String teacherId) {
        StudentsResponse res = new StudentsResponse();

        try {
            // FIXME Should setup DynamoDB with region when deploy to AWS.
            DynamoDbClient ddb = DynamoDbClient.builder().endpointOverride(
                new URI("http://localhost:8000")).build();

            // Set up mapping of the partition name with the value.
            String partitionKeyName = ":tid";
            HashMap<String, AttributeValue> attrValues =
                    new HashMap<String,AttributeValue>();
            attrValues.put(partitionKeyName,
                AttributeValue.builder().s(teacherId.toString()).build());

            QueryRequest queryReq = QueryRequest.builder()
                .tableName(COURSE_TABLE_NAME)
                .keyConditionExpression("TeacherId = " + partitionKeyName)
                .expressionAttributeValues(attrValues)
                .build();
            QueryResponse queryRes = ddb.query(queryReq);

            List<Student> studentList = new ArrayList<Student>();
            Set<String> studentSet = new HashSet<String>();

            // Iterate over every student in each course item.
            for (Map<String, AttributeValue> map : queryRes.items()) {
                List<AttributeValue> students = map.get("Students").l();

                // Remove duplicate students using HashSet.
                for (AttributeValue s : students) {
                    studentSet.add(s.s());
                }
            }

            for (String s : studentSet) {
                Student student = new Student();
                student.setStudentId(s);
                studentList.add(student);
            }

            res.setStudents(studentList);
            res.setResultsTotal(studentList.size());
        } catch (DynamoDbException e) {
            System.out.println(e.getMessage());
            return null;
        } catch(URISyntaxException e) {
            System.out.println(e.getMessage());
            return null;
        }

        return res;
    }

    @Override
    public ResourceContext newResourceContext(HttpServletRequest request, HttpServletResponse response) {
        return new DefaultResourceContext(request, response);
    }
}
