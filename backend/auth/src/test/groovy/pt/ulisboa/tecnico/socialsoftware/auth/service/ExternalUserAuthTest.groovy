package pt.ulisboa.tecnico.socialsoftware.auth.service

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.auth.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.auth.SpockTest
import pt.ulisboa.tecnico.socialsoftware.auth.domain.AuthUser
import pt.ulisboa.tecnico.socialsoftware.common.dtos.auth.AuthUserType
import pt.ulisboa.tecnico.socialsoftware.common.dtos.course.CourseType
import pt.ulisboa.tecnico.socialsoftware.common.dtos.user.Role
import pt.ulisboa.tecnico.socialsoftware.common.exceptions.ErrorMessage
import pt.ulisboa.tecnico.socialsoftware.common.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.execution.domain.CourseExecution
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.user.domain.User

@DataJpaTest
class ExternalUserAuthTest extends SpockTest {

    User user
    AuthUser authUser
    Course course
    CourseExecution courseExecution

	def setup(){
        course = new Course(COURSE_1_NAME, CourseType.EXTERNAL)
        courseRepository.save(course)
        courseExecution = new CourseExecution(course, COURSE_1_ACRONYM, COURSE_1_ACADEMIC_TERM, CourseType.EXTERNAL, LOCAL_DATE_TOMORROW)
        courseExecutionRepository.save(courseExecution)

        authUser = authUserService.createUserWithAuth(USER_1_NAME, USER_1_EMAIL, USER_1_EMAIL, Role.STUDENT, AuthUserType.EXTERNAL)
        user = userRepository.findAll().get(0)
        user.addCourse(courseExecution)
        user.setActive(true)
        courseExecution.addUser(user)
        authUser.setPassword(passwordEncoder.encode(USER_1_PASSWORD))
    }

    def "user logins successfully" () {
        when:
        def result = authUserService.externalUserAuth(USER_1_EMAIL, USER_1_PASSWORD)

        then:
        result.user.username == USER_1_EMAIL
    }

    def "login fails, given values are invalid" () {
        when:
        authUserService.externalUserAuth(username, password)

        then:
        def error = thrown(TutorException)
        error.getErrorMessage() == errorMessage

        where:
        username     | password        || errorMessage
        null         | USER_1_PASSWORD || ErrorMessage.EXTERNAL_USER_NOT_FOUND
        USER_2_EMAIL | USER_1_PASSWORD || ErrorMessage.EXTERNAL_USER_NOT_FOUND
        USER_1_EMAIL | USER_2_PASSWORD || ErrorMessage.INVALID_PASSWORD
        USER_1_EMAIL | null            || ErrorMessage.INVALID_PASSWORD
    }
    

    @TestConfiguration
    static class LocalBeanConfiguration extends BeanConfiguration {}
}
