package dao;

import api.Answer;
import api.Question;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class AnswerInternal extends Answer {

    private Question question;

    @JsonIgnore
    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }
}
