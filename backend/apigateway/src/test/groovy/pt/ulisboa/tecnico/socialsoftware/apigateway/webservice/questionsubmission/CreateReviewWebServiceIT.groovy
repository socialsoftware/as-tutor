package pt.ulisboa.tecnico.socialsoftware.apigateway.webservice.questionsubmission

import groovyx.net.http.RESTClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import pt.ulisboa.tecnico.socialsoftware.apigateway.SpockTest
import pt.ulisboa.tecnico.socialsoftware.auth.domain.AuthTecnicoUser
import pt.ulisboa.tecnico.socialsoftware.auth.domain.UserSecurityInfo
import pt.ulisboa.tecnico.socialsoftware.common.dtos.course.CourseType
import pt.ulisboa.tecnico.socialsoftware.common.dtos.question.MultipleChoiceQuestionDto
import pt.ulisboa.tecnico.socialsoftware.common.dtos.question.OptionDto
import pt.ulisboa.tecnico.socialsoftware.common.dtos.question.QuestionDto
import pt.ulisboa.tecnico.socialsoftware.common.dtos.user.Role
import pt.ulisboa.tecnico.socialsoftware.tutor.execution.domain.CourseExecution
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Course
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.questionsubmission.domain.Review
import pt.ulisboa.tecnico.socialsoftware.tutor.questionsubmission.dto.QuestionSubmissionDto
import pt.ulisboa.tecnico.socialsoftware.tutor.questionsubmission.dto.ReviewDto
import pt.ulisboa.tecnico.socialsoftware.tutor.user.domain.User

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CreateReviewWebServiceIT extends SpockTest {
    @LocalServerPort
    private int port

    def course
    def courseExecution
    def teacher
    def authTeacher
    def student
    def authStudent
    def questionSubmission
    def reviewDto
    def response

    def setup() {
        restClient = new RESTClient("http://localhost:" + port)

        course = new Course(COURSE_1_NAME, CourseType.EXTERNAL)
        courseRepository.save(course)
        courseExecution = new CourseExecution(course, COURSE_1_ACRONYM, COURSE_1_ACADEMIC_TERM, CourseType.EXTERNAL, LOCAL_DATE_TOMORROW)
        courseExecutionRepository.save(courseExecution)

        teacher = new User(USER_1_NAME, USER_1_EMAIL, Role.TEACHER, false)
        teacher.setActive(true)
        teacher.addCourse(courseExecution)
        userRepository.save(teacher)
        authTeacher = new AuthTecnicoUser(new UserSecurityInfo(teacher.getId(), USER_1_NAME, Role.TEACHER, false),
                USER_1_EMAIL, USER_1_EMAIL)
        authTeacher.setPassword(passwordEncoder.encode(USER_1_PASSWORD))
        authTeacher.addCourseExecution(courseExecution.getId())
        courseExecution.addUser(teacher)
        authUserRepository.save(authTeacher)

        student = new User(USER_2_NAME, USER_2_EMAIL, Role.STUDENT, false)
        student.setActive(true)
        student.addCourse(courseExecution)
        userRepository.save(student)
        authStudent = new AuthTecnicoUser(new UserSecurityInfo(student.getId(), USER_2_NAME, Role.STUDENT, false),
                USER_2_EMAIL, USER_2_EMAIL)
        authStudent.addCourseExecution(courseExecution.getId())
        courseExecution.addUser(student)
        authUserRepository.save(authStudent)

        def questionDto = new QuestionDto()
        questionDto.setTitle(QUESTION_1_TITLE)
        questionDto.setContent(QUESTION_1_CONTENT)
        questionDto.setStatus(Question.Status.SUBMITTED.name())
        def optionDto = new OptionDto()
        optionDto.setContent(OPTION_1_CONTENT)
        optionDto.setCorrect(true)
        def options = new ArrayList<OptionDto>()
        options.add(optionDto)
        questionDto.setQuestionDetailsDto(new MultipleChoiceQuestionDto())
        questionDto.getQuestionDetailsDto().setOptions(options)

        def questionSubmissionDto = new QuestionSubmissionDto()
        questionSubmissionDto.setCourseExecutionId(courseExecution.getId())
        questionSubmissionDto.setSubmitterId(student.getId())
        questionSubmissionDto.setQuestion(questionDto)

        questionSubmissionService.createQuestionSubmission(questionSubmissionDto)
        questionSubmission = questionSubmissionRepository.findAll().get(0)

        createdUserLogin(USER_1_EMAIL, USER_1_PASSWORD)
    }

    def "create review for question submission"() {
        given: "a reviewDto"
        reviewDto = new ReviewDto()
        reviewDto.setQuestionSubmissionId(questionSubmission.getId())
        reviewDto.setUserId(teacher.getId())
        reviewDto.setComment(REVIEW_1_COMMENT)
        reviewDto.setType(Review.Type.APPROVE.name())

        when:
        response = restClient.post(
                path: '/submissions/'+questionSubmission.getId()+'/reviews',
                body: reviewDto,
                query: ['executionId': courseExecution.getId()],
                requestContentType: 'application/json'
        )

        then: "check the response status"
        response != null
        response.status == 200
        and: "if it responds with the correct review"
        def review = response.data
        review.id != null
        review.comment == REVIEW_1_COMMENT
        review.questionSubmissionId == questionSubmission.getId()
        review.userId == teacher.getId()
        review.type == Review.Type.APPROVE.name()
    }

    def cleanup() {
        userRepository.deleteById(teacher.getId())
        userRepository.deleteById(student.getId())
        courseExecutionRepository.deleteById(courseExecution.getId())
        authUserRepository.deleteById(authStudent.getId())
        authUserRepository.deleteById(authTeacher.getId())
        courseRepository.deleteById(course.getId())
    }
}

