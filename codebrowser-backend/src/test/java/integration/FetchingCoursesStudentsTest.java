package integration;

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebResponse;
import java.io.IOException;
import java.util.Iterator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.AfterClass;
import org.junit.BeforeClass;


public class FetchingCoursesStudentsTest  {

    static WebClient client;
    static Page page;
    static WebResponse response;
    static JSONArray students;
    static JSONParser parser;

    static String studentsPage = "http://0.0.0.0:10377/app/courses/1/students";


    @BeforeClass
    public static void setupClientAndLoadPage() throws IOException {
        client = new WebClient();
        page = client.getPage(studentsPage);

        response = page.getWebResponse();
        parser = new JSONParser();

        try {
            students = (JSONArray) parser.parse(response.getContentAsString());
        } catch (ParseException ex) {
            fail("Response is not json");
        }
    }

    @AfterClass
    public static void closeBrowser() {
        client.closeAllWindows();
    }



    @Test
    public void testUnitTestingCourseShouldHave10Students() {
        assertEquals(10, students.size());
    }

    @Test
    public void testUnitTestingShouldHaveStudent0001() {

        boolean found = false;

        Iterator<JSONObject> iterator = students.iterator();

        while (iterator.hasNext()) {
            JSONObject student = iterator.next();

            if (student.containsKey("name") && student.get("name").equals("unit-test-course_student_0001")) {
                found = true;
            }
        }

        assertTrue(found);
    }

    @Test
    public void testUnitTestingShouldHaveStudent0006() {

        boolean found = false;

        Iterator<JSONObject> iterator = students.iterator();

        while (iterator.hasNext()) {
            JSONObject student = iterator.next();

            if (student.containsKey("name") && student.get("name").equals("unit-test-course_student_0006")) {
                found = true;
            }
        }

        assertTrue(found);
    }

    @Test
    public void testUnitTestingShouldHaveStudent0010andStudentShouldHaveCourses() {

        boolean found = false;

        Iterator<JSONObject> iterator = students.iterator();

        while (iterator.hasNext()) {
            JSONObject student = iterator.next();

            if (student.containsKey("name") && student.get("name").equals("unit-test-course_student_0010")) {
                found = true;

                assertTrue(student.containsKey("courses"));

                JSONArray courses = (JSONArray) student.get("courses");
                assertEquals(1, courses.size());
            }
        }

        assertTrue(found);
    }



}