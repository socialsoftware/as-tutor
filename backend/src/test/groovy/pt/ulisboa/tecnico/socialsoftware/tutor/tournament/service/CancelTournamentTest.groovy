package pt.ulisboa.tecnico.socialsoftware.tutor.tournament.service

import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.TutorException
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Assessment
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User

import static pt.ulisboa.tecnico.socialsoftware.tutor.exceptions.ErrorMessage.*

@DataJpaTest
class CancelTournamentTest extends TournamentTest {
    def setup() {
        createAssessmentWithTopicConjunction(ASSESSMENT_1_TITLE, Assessment.Status.AVAILABLE, externalCourseExecution)

        def question1 = createQuestion(LOCAL_DATE_TODAY, QUESTION_1_CONTENT, QUESTION_1_TITLE, Question.Status.AVAILABLE, externalCourse)

        createOption(OPTION_1_CONTENT, question1)
    }

    def "user that created tournament cancels it"() {
        given:
        def tournamentDto = createTournament(user1, STRING_DATE_TOMORROW, STRING_DATE_LATER, NUMBER_OF_QUESTIONS, false)

        when:
        tournamentService.cancelTournament(user1.getId(), tournamentDto.getId())

        then:
        tournamentRepository.count() == 1L
        def result = tournamentRepository.findAll().get(0)
        result.isCanceled()
    }

    def "user that created an open tournament tries to cancel it"() {
        given:
        def tournamentDto = createTournament(user1, STRING_DATE_TODAY, STRING_DATE_LATER, NUMBER_OF_QUESTIONS, false)

        when:
        tournamentService.cancelTournament(user1.getId(), tournamentDto.getId())

        then:
        def exception = thrown(TutorException)
        exception.getErrorMessage() == TOURNAMENT_IS_OPEN
        tournamentRepository.count() == 1L
    }

    def "user that created tournament tries to cancel it after has ended with no answers"() {
        given:
        def tournamentDto = createTournament(user1, STRING_DATE_TODAY, STRING_DATE_TODAY, NUMBER_OF_QUESTIONS, false)

        when:
        tournamentService.cancelTournament(user1.getId(), tournamentDto.getId())

        then:
        tournamentRepository.count() == 1L
        def result = tournamentRepository.findAll().get(0)
        result.isCanceled()
    }

    def "user that created tournament tries to cancel it with answers"() {
        given:
        def tournamentDto = createTournament(user1, STRING_DATE_TODAY, STRING_DATE_LATER, NUMBER_OF_QUESTIONS, false)
        and: "join a tournament"
        tournamentRepository.findById(tournamentDto.getId()).orElse(null).addParticipant(user1, "")
        and: "solve a tournament"
        tournamentService.solveQuiz(user1.getId(), tournamentDto.getId())
        and: "is now closed"
        tournamentDto.setEndTime(STRING_DATE_TODAY)

        when:
        tournamentService.cancelTournament(user1.getId(), tournamentDto.getId())

        then:
        def exception = thrown(TutorException)
        exception.getErrorMessage() == TOURNAMENT_IS_OPEN
        tournamentRepository.count() == 1L
    }

    def "user that did not created tournament cancels it"() {
        given:
        def tournamentDto = createTournament(user1, STRING_DATE_TOMORROW, STRING_DATE_LATER, NUMBER_OF_QUESTIONS, false)
        and: "a new user"
        def user2 = createUser(USER_2_NAME, USER_2_USERNAME, USER_2_EMAIL, User.Role.STUDENT, externalCourseExecution)

        when:
        tournamentService.cancelTournament(user2.getId(), tournamentDto.getId())

        then:
        def exception = thrown(TutorException)
        exception.getErrorMessage() == TOURNAMENT_CREATOR
        and:
        tournamentRepository.count() == 1L
        def result = tournamentRepository.findAll().get(0)
        !result.isCanceled()
    }

    @TestConfiguration
    static class LocalBeanConfiguration extends BeanConfiguration {}
}