package pt.ulisboa.tecnico.socialsoftware.tutor.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import pt.ulisboa.tecnico.socialsoftware.common.dtos.execution.CourseExecutionDto;
import pt.ulisboa.tecnico.socialsoftware.common.dtos.user.Role;
import pt.ulisboa.tecnico.socialsoftware.common.exceptions.TutorException;
import pt.ulisboa.tecnico.socialsoftware.common.security.token.UserInfo;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.domain.QuestionAnswer;
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.repository.QuestionAnswerRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.domain.Discussion;
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.domain.Reply;
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.repository.DiscussionRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.repository.ReplyRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.execution.CourseExecutionService;
import pt.ulisboa.tecnico.socialsoftware.tutor.execution.repository.AssessmentRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Topic;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.CourseRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.QuestionRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.repository.TopicRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.questionsubmission.domain.QuestionSubmission;
import pt.ulisboa.tecnico.socialsoftware.tutor.questionsubmission.repository.QuestionSubmissionRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.repository.QuizRepository;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.repository.UserRepository;

import java.io.Serializable;

import static pt.ulisboa.tecnico.socialsoftware.common.exceptions.ErrorMessage.COURSE_NOT_FOUND;

@Component
public class TutorPermissionEvaluator implements PermissionEvaluator {

    private static final Logger logger = LoggerFactory.getLogger(TutorPermissionEvaluator.class);

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private QuestionSubmissionRepository questionSubmissionRepository;

    @Autowired
    private TopicRepository topicRepository;

    @Autowired
    private AssessmentRepository assessmentRepository;

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private CourseExecutionService courseExecutionService;

    @Autowired
    private QuestionAnswerRepository questionAnswerRepository;

    @Autowired
    private DiscussionRepository discussionRepository;

    @Autowired
    private ReplyRepository replyRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        UserInfo userInfo = ((UserInfo) authentication.getPrincipal());
        int userId = userInfo.getId();

        if (targetDomainObject instanceof CourseExecutionDto) {
            CourseExecutionDto courseExecutionDto = (CourseExecutionDto) targetDomainObject;
            String permissionValue = (String) permission;
            switch (permissionValue) {
                case "EXECUTION.CREATE":
                    return userInfo.getEnrolledCourseAcronyms().contains(courseExecutionDto.getAcronym() + courseExecutionDto.getAcademicTerm());
                case "DEMO.ACCESS":
                    return courseExecutionDto.getName().equals("Demo Course");
                default:
                    return false;
            }
        }

        if (targetDomainObject instanceof Integer) {
            int id = (int) targetDomainObject;
            String permissionValue = (String) permission;
            switch (permissionValue) {
                case "DEMO.ACCESS":
                    CourseExecutionDto courseExecutionDto = courseExecutionService.getCourseExecutionById(id);
                    return courseExecutionDto.getName().equals("Demo Course");
                case "COURSE.ACCESS":
                    return userHasAnExecutionOfCourse(userInfo, id);
                case "EXECUTION.ACCESS":
                    return userHasThisExecution(userInfo, id);
                case "QUESTION.ACCESS":
                    Question question = questionRepository.findQuestionWithCourseById(id).orElse(null);
                    if (question != null) {
                        return userHasAnExecutionOfCourse(userInfo, question.getCourse().getId());
                    }
                    return false;
                case "TOPIC.ACCESS":
                    Topic topic = topicRepository.findTopicWithCourseById(id).orElse(null);
                    if (topic != null) {
                        return userHasAnExecutionOfCourse(userInfo, topic.getCourse().getId());
                    }
                    return false;
                case "ASSESSMENT.ACCESS":
                    Integer courseExecutionId = assessmentRepository.findCourseExecutionIdById(id).orElse(null);
                    if (courseExecutionId != null) {
                        return userHasThisExecution(userInfo, courseExecutionId);
                    }
                    return false;
                case "QUIZ.ACCESS":
                    courseExecutionId = quizRepository.findCourseExecutionIdById(id).orElse(null);
                    if (courseExecutionId != null) {
                        return userHasThisExecution(userInfo, courseExecutionId);
                    }
                    return false;
                case "SUBMISSION.ACCESS":
                    QuestionSubmission questionSubmission = questionSubmissionRepository.findById(id).orElse(null);
                    if (questionSubmission != null) {
                        boolean hasCourseExecutionAccess = userHasThisExecution(userInfo, questionSubmission.getCourseExecution().getId());
                        if (userInfo.getRole() == Role.STUDENT) {
                            return hasCourseExecutionAccess && questionSubmission.getSubmitter().getId() == userId;
                        } else {
                            return hasCourseExecutionAccess;
                        }
                    }
                    return false;
                case "QUESTION_ANSWER.ACCESS":
                    QuestionAnswer questionAnswer = questionAnswerRepository.findById(id).orElse(null);
                    return questionAnswer != null && questionAnswer.getQuizAnswer().getUser().getId().equals(userId);
                case "DISCUSSION.OWNER":
                    Discussion discussion = discussionRepository.findById(id).orElse(null);
                    return discussion != null && discussion.getUser().getId().equals(userId);
                case "DISCUSSION.ACCESS":
                    discussion = discussionRepository.findById(id).orElse(null);
                    return discussion != null && userInfo.isTeacher() && userHasThisExecution(userInfo, discussion.getCourseExecution().getId());
                case "REPLY.ACCESS":
                    Reply reply = replyRepository.findById(id).orElse(null);
                    return reply != null && userHasThisExecution(userInfo, reply.getDiscussion().getCourseExecution().getId());
                default: return false;
            }
        }

        return false;
    }

    public boolean userHasThisExecution(UserInfo userInfo, int courseExecutionId) {
        return userInfo.getCourseExecutions().contains(courseExecutionId);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public boolean userHasAnExecutionOfCourse(UserInfo userInfo, int courseId) {
        return courseRepository.findCourseWithCourseExecutionsById(courseId).orElseThrow(() -> new TutorException(COURSE_NOT_FOUND, courseId))
                .getCourseExecutions()
                .stream()
                .anyMatch(courseExecution ->  userHasThisExecution(userInfo, courseExecution.getId()));
                //.anyMatch(courseExecution ->  userRepository.countUserCourseExecutionsPairById(userInfo.getId(), courseExecution.getId()) == 1);

    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable serializable, String s, Object o) {
        return false;
    }
}
