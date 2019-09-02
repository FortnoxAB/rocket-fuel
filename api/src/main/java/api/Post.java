 package api;

 import com.fasterxml.jackson.annotation.JsonIgnore;
 import com.fasterxml.jackson.annotation.JsonProperty;

 import java.time.LocalDateTime;
 import java.time.ZonedDateTime;

 import static java.time.ZoneId.systemDefault;
 import static java.util.Optional.ofNullable;

 /**
  * A class that defines the shared attributes between a {@link Answer}
  * or {@link Question}.
  */
 public abstract class Post {
    private Long id;

    private String createdBy;

    private Long userId;

    private String picture;

    private LocalDateTime createdAt;

    private Integer votes;

    private String slackId;

    private Integer currentUserVote;

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    @JsonProperty("createdAt")
    public ZonedDateTime getCreated() {
        return ofNullable(createdAt)
            .map(time -> time.atZone(systemDefault()))
            .orElse(null);
    }

    @JsonIgnore
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Integer getVotes() {
        return votes;
    }

    public void setVotes(Integer votes) {
        this.votes = votes;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

     public String getSlackId() {
         return slackId;
     }

     public void setSlackId(String slackId) {
         this.slackId = slackId;
     }

     public String getPicture() {
         return picture;
     }

     public void setPicture(String picture) {
         this.picture = picture;
     }

     /**
      * When populated, represents the value of the vote that the "current user" has applied to the post.
      */
     public Integer getCurrentUserVote() {
         return currentUserVote;
     }

     public void setCurrentUserVote(Integer currentUserVote) {
         this.currentUserVote = currentUserVote;
     }
 }
