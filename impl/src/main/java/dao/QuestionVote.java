package dao;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class QuestionVote {

    private long userId;

    private long questionId;

    @Max(1)
    @Min(-1)
    @NotNull
    int value;

    public QuestionVote() {
    }

    public QuestionVote(long userId, long questionId, int value) {
        this.userId = userId;
        this.questionId = questionId;
        this.value = value;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(long questionId) {
        this.questionId = questionId;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
