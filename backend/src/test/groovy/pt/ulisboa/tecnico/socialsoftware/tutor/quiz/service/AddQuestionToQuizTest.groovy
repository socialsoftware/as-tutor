package pt.ulisboa.tecnico.socialsoftware.tutor.quiz.service


import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.BeanConfiguration
import pt.ulisboa.tecnico.socialsoftware.tutor.SpockTest
import pt.ulisboa.tecnico.socialsoftware.tutor.question.domain.Question
import pt.ulisboa.tecnico.socialsoftware.tutor.quiz.domain.Quiz

@DataJpaTest
class AddQuestionToQuizTest extends SpockTest {

    def setup() {
        def quiz = new Quiz()
        quiz.setKey(1)
        quizRepository.save(quiz)
        def question = new Question()
        question.setKey(1)
        question.setTitle("Question title")
        questionRepository.save(question)
    }

    def 'add a question to a quiz' () {
        given:
        def quizId = quizRepository.findAll().get(0).getId()
        def questionId = questionRepository.findAll().get(0).getId()

        when:
        quizService.addQuestionToQuiz(questionId, quizId)

        then:
        quizQuestionRepository.findAll().size() == 1
        def quizQuestion = quizQuestionRepository.findAll().get(0)
        quizQuestion.getId() != null
        quizQuestion.getSequence() == 0
        quizQuestion.getQuiz() != null
        quizQuestion.getQuiz().getQuizQuestions().size() == 1
        quizQuestion.getQuiz().getQuizQuestions().contains(quizQuestion)
        quizQuestion.getQuestion() != null
        quizQuestion.getQuestion().getQuizQuestions().size() == 1
        quizQuestion.getQuestion().getQuizQuestions().contains(quizQuestion)
    }

    @TestConfiguration
    static class LocalBeanConfiguration extends BeanConfiguration {}
}
