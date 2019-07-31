package dao;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class AnswerVote {

    private long userId;

    private long answerId;

    @Max(1)
    @Min(-1)
    @NotNull
    int value;

    public AnswerVote() {
    }

    public AnswerVote(long userId, long answerId, int value) {
        this.userId = userId;
        this.answerId = answerId;
        this.value = value;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getAnswerId() {
        return answerId;
    }

    public void setAnswerId(long answerId) {
        this.answerId = answerId;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
