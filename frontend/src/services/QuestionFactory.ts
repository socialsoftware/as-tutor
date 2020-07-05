import Question from '@/models/management/Question';
import MultipleChoiceQuestion from '@/models/management/multiple-choice/MultipleChoiceQuestion';
import CodeFillInQuestion from '@/models/management/code-fill-in/CodeFillInQuestion';
import StatementQuestionCodeFillIn from '@/models/statement/code-fill-in/StatementQuestionCodeFillIn';
import StatementQuestionMultipleChoice from '@/models/statement/multiple-choice/StatementQuestionMultipleChoice';
import StatementQuestion from '@/models/statement/StatementQuestion';
import StatementAnswerMultipleChoice from '@/models/statement/multiple-choice/StatementAnswerMultipleChoice';
import StatementAnswerCodeFillIn from '@/models/statement/code-fill-in/StatementAnswerCodeFillIn';
import StatementAnswer from '@/models/statement/StatementAnswer';
import StatementCorrectAnswer from '@/models/statement/StatementCorrectAnswer';
import StatementCorrectAnswerMultipleChoice from '@/models/statement/multiple-choice/StatementCorrectAnswerMultipleChoice';
import StatementCorrectAnswerCodeFillIn from '@/models/statement/code-fill-in/StatementCorrectAnswerCodeFillIn';

export class QuestionFactory {
  static createQuestion(question: any): Question {
    if (question.type === 'multiple_choice') {
      return new MultipleChoiceQuestion(question);
    } else if (question.type === 'code_fill_in') {
      return new CodeFillInQuestion(question);
    } else {
      throw new Error('Unknown question type.');
    }
  }

  static createStatementQuestion(question: any): StatementQuestion {
    if (question.type === 'multiple_choice') {
      return new StatementQuestionMultipleChoice(question);
    } else if (question.type === 'code_fill_in') {
      return new StatementQuestionCodeFillIn(question);
    } else {
      throw new Error('Unknown question type.');
    }
  }

  static createStatementAnswer(answer: any): StatementAnswer {
    if (answer.type === 'multiple_choice') {
      return new StatementAnswerMultipleChoice(answer);
    } else if (answer.type === 'code_fill_in') {
      return new StatementAnswerCodeFillIn(answer);
    } else {
      throw new Error('Unknown question type.');
    }
  }

  static createCorrectAnswer(answer: any): StatementCorrectAnswer {
    if (answer.type === 'multiple_choice') {
      return new StatementCorrectAnswerMultipleChoice(answer);
    } else if (answer.type === 'code_fill_in') {
      return new StatementCorrectAnswerCodeFillIn(answer);
    } else {
      throw new Error('Unknown question type.');
    }
  }
}
