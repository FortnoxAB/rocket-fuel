package api;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Objects;

import static java.util.Objects.nonNull;

public class Answer extends Post {

    private String answer;

    private LocalDateTime acceptedAt;

    private long questionId;

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public boolean isAccepted() {
        return nonNull(acceptedAt);
    }

    public long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(long questionId) {
        this.questionId = questionId;
    }

    public LocalDateTime getAcceptedAt() {
        return acceptedAt;
    }

    public void setAcceptedAt(LocalDateTime acceptedAt) {
        this.acceptedAt = acceptedAt;
    }
}
