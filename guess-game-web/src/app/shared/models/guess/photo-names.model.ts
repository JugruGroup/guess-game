import { QuestionAnswers } from './question-answers.model';

export class PhotoNames extends QuestionAnswers {
  constructor(
    public photoFileName?: string,
    public name0?: string,
    public name1?: string,
    public name2?: string,
    public name3?: string
  ) {
    super();
  }
}
