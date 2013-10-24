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


public class FetchingCoursesTest  {

    static WebClient client;
    static Page page;
    static WebResponse response;
    static JSONArray courses;
    static JSONParser parser;

    static String coursePage = "http://0.0.0.0:10377/app/courses";


    @BeforeClass
    public static void setupClientAndLoadPage() throws IOException {
        client = new WebClient();
        page = client.getPage(coursePage);

        response = page.getWebResponse();
        parser = new JSONParser();

        try {
            courses = (JSONArray) parser.parse(response.getContentAsString());
        } catch (ParseException ex) {
            fail("Response is not json");
        }
    }

    @AfterClass
    public static void closeBrowser() {
        client.closeAllWindows();
    }



    @Test
    public void testTwoCoursesShouldExist() {
        assertEquals(2, courses.size());
    }

    @Test
    public void testUnitTestingCourseShouldExist() {

        boolean found = false;

        Iterator<JSONObject> iterator = courses.iterator();

        while (iterator.hasNext()) {
            JSONObject course = iterator.next();

            if (course.containsKey("name") && course.get("name").equals("unit-test-course")) {
                found = true;
            }
        }

        assertTrue(found);
    }


    @Test
    public void testUnitTestingCourseShouldHave10Students() {

        boolean found = false;

        Iterator<JSONObject> iterator = courses.iterator();

        while (iterator.hasNext()) {
            JSONObject course = iterator.next();

            if (course.containsKey("name") && course.get("name").equals("unit-test-course")) {
                assertEquals(Long.parseLong("10"), course.get("amountOfStudents"));
                found = true;
            }
        }

        assertTrue(found);
    }

}