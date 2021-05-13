package pt.ulisboa.tecnico.socialsoftware.tutor

import com.google.common.eventbus.EventBus
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.PropertySource
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.JavaMailSenderImpl
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.ActiveProfiles
import pt.ulisboa.tecnico.socialsoftware.common.utils.Mailer
import pt.ulisboa.tecnico.socialsoftware.tutor.answer.AnswerService
import pt.ulisboa.tecnico.socialsoftware.tutor.demoutils.TutorDemoUtils
import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.DiscussionService
import pt.ulisboa.tecnico.socialsoftware.tutor.execution.AssessmentService
import pt.ulisboa.tecnico.socialsoftware.tutor.execution.CourseExecutionService
import pt.ulisboa.tecnico.socialsoftware.tutor.impexp.domain.AnswersXmlImport
import pt.ulisboa.tecnico.socialsoftware.tutor.question.QuestionService
import pt.ulisboa.tecnico.socialsoftware.tutor.question.TopicService
import pt.ulisboa.tecnico.socialsoftware.tutor.questionsubmission.QuestionSubmissionService
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.QuizService
import pt.ulisboa.tecnico.socialsoftware.tutor.user.UserService

@TestConfiguration
@PropertySource("classpath:application-test.properties")
@ActiveProfiles("test")
class BeanConfiguration {

    @Value('${spring.mail.host}')
    private String host

    @Value('${spring.mail.port}')
    private int port

    @Value('${spring.mail.username}')
    private String username

    @Value('${spring.mail.password}')
    private String password

    @Value('${spring.mail.properties.mail.smtp.auth}')
    private String auth;

    @Value('${spring.mail.properties.mail.smtp.starttls.enable}')
    private String starttls

    @Value('${spring.mail.properties.mail.transport.protocol}')
    private String protocol

    @Value('${spring.mail.properties.mail.debug}')
    private String debug

    @Bean
    QuizService quizService() {
        return new QuizService()
    }

    @Bean
    AnswerService answerService() {
        return new AnswerService()
    }

    @Bean
    UserService userService() {
        return new UserService()
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder()
    }

    @Bean
    QuestionService questionService() {
        return new QuestionService()
    }

    @Bean
    CourseExecutionService courseService() {
        return new CourseExecutionService()
    }

    @Bean
    TopicService topicService() {
        return new TopicService()
    }

    @Bean
    AssessmentService assessmentService() {
        return new AssessmentService()
    }

    @Bean
    DiscussionService discussionService() {
        return new DiscussionService()
    }

    @Bean
    AnswersXmlImport answersXmlImport() {
        return new AnswersXmlImport();
    }

    @Bean
    QuestionSubmissionService questionSubmissionService() {
        return new QuestionSubmissionService()
    }

    @Bean
    TutorDemoUtils demoUtils() {
        return new TutorDemoUtils();
    }

    @Bean
    Mailer mailer() {
        return new Mailer()
    }

    @Bean
    JavaMailSender getJavaMailSender() {
        JavaMailSender mailSender = new JavaMailSenderImpl();
        mailSender.setHost(host);
        mailSender.setPort(port);

        mailSender.setUsername(username);
        mailSender.setPassword(password);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", Boolean.parseBoolean(protocol));
        props.put("mail.smtp.auth", Boolean.parseBoolean(auth));
        props.put("mail.smtp.starttls.enable", starttls);
        props.put("mail.debug", debug);

        return mailSender;
    }

    @Bean
    EventBus eventBus() {
        return new EventBus()
    }
}