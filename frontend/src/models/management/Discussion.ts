import Question from '@/models/management/Question';
import Reply from '@/models/management/Reply';
import { ISOtoString } from '@/services/ConvertDateService';

export default class Discussion {
  id!: number;
  userId!: number;
  questionId!: number;
  userName!: string;
  message!: string;
  question!: Question;
  replies!: Reply[] | null;
  date!: string | null;
  available!: boolean;
  courseExecutionId!: number;
  closed!: boolean;
  lastReplyDate!: string | null;

  constructor(jsonObj?: Discussion) {
    if (jsonObj) {
      this.id = jsonObj.id;
      this.userId = jsonObj.userId;
      this.questionId = jsonObj.questionId;
      this.userName = jsonObj.userName;
      this.message = jsonObj.message;
      this.question = new Question(jsonObj.question);
      this.date = ISOtoString(jsonObj.date);
      this.available = jsonObj.available;
      this.courseExecutionId = jsonObj.courseExecutionId;
      this.closed = jsonObj.closed;

      if (jsonObj.replies !== null) {
        this.replies = jsonObj.replies.map((reply: any) => {
          return new Reply(reply);
        });
        this.lastReplyDate = this.replies[this.replies.length - 1].date;
      } else {
        this.replies = null;
        this.lastReplyDate = '-';
      }
    }
  }

  getId() {
    return this.id;
  }
}