 package api;

 import java.util.List;

 /**
  *  Defines a question. A question is connected to the {@link User} asking it.
  *
  *  A question can be answered if it has a accepted answer.
  *
  *  {@link Answer}s are linked to a question.
  */
 public class Question extends Post {

    private String title;

    private String question;

    private Integer bounty;

    private boolean answerAccepted;

    private String slackThreadId;

    private List<String> tags;

    public boolean isAnswerAccepted() {
        return answerAccepted;
    }

    public void setAnswerAccepted(boolean answerAccepted) {
        this.answerAccepted = answerAccepted;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public Integer getBounty() {
        return bounty;
    }

    public void setBounty(Integer bounty) {
        this.bounty = bounty;
    }

    public String getSlackThreadId() {
       return slackThreadId;
    }

    public void setSlackThreadId(String slackThreadId) {
       this.slackThreadId = slackThreadId;
    }

     public String getTitle() {
         return title;
     }

     public void setTitle(String title) {
         this.title = title;
     }

     public List<String> getTags() {
         return tags;
     }

     public void setTags(List<String> tags) {
         this.tags = tags;
     }
 }
