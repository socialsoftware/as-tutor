package pt.ulisboa.tecnico.socialsoftware.tutor.auth;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.fenixedu.sdk.ApplicationConfiguration;
import org.fenixedu.sdk.FenixEduClientImpl;
import org.fenixedu.sdk.FenixEduUserDetails;
import org.fenixedu.sdk.exception.FenixEduClientException;
import pt.ulisboa.tecnico.socialsoftware.tutor.course.CourseDto;
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.FENIX_CONFIGURATION_ERROR;
import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.FENIX_ERROR;


public class FenixEduInterface {
    private FenixEduClientImpl client;
    private FenixEduUserDetails userDetails;
    private JsonObject person;
    private JsonObject courses;

    public FenixEduInterface(String baseUrl, String oauthConsumerId, String oauthConsumerSecret, String callbackUrl) {
        ApplicationConfiguration config = new ApplicationConfiguration(baseUrl, oauthConsumerId, oauthConsumerSecret, callbackUrl);
        try {
            client = new FenixEduClientImpl(config);
        } catch (
                FenixEduClientException e) {
            throw new TutorException(FENIX_CONFIGURATION_ERROR);
        }
    }

    public void authenticate(String code) {
        try {
            userDetails = client.getUserDetailsFromCode(code);
        } catch (Exception e) {
            throw new TutorException(FENIX_ERROR);
        }

        person = client.getPerson(userDetails.getAuthorization());
    }

    public String getPersonName() {
        return String.valueOf(person.get("name")).replaceAll("^\"|\"$", "");
    }

    public String getPersonUsername() {
        return String.valueOf(person.get("username")).replaceAll("^\"|\"$", "");
    }

    private JsonObject getPersonCourses() {
        String academicTerm = client.getAbout().get("currentAcademicTerm").getAsString();
        Matcher currentYearMatcher = Pattern.compile("([0-9]+/[0-9]+)").matcher(academicTerm);
        currentYearMatcher.find();
        String currentYear = currentYearMatcher.group(1);
        return client.getPersonCourses(userDetails.getAuthorization(), currentYear);
    }

    public List<CourseDto> getPersonAttendingCourses() {
        if (courses == null) {
            courses = getPersonCourses();
        }
        return getCourses(courses.get("attending").getAsJsonArray());
    }

    public List<CourseDto> getPersonTeachingCourses() {
        if (courses == null) {
            courses = getPersonCourses();
        }
        return getCourses(courses.get("teaching").getAsJsonArray());
    }

    private List<CourseDto> getCourses(JsonArray coursesJson) {
        List<CourseDto> result = new ArrayList<>();
        for (JsonElement courseJson : coursesJson) {
            result.add(new CourseDto(courseJson.getAsJsonObject().get("name").getAsString(),
                    courseJson.getAsJsonObject().get("acronym").getAsString(),
                    courseJson.getAsJsonObject().get("academicTerm").getAsString()));
        }
        return result;
    }
}
