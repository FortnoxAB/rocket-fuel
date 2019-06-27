 package api;


 /**
  * A class that defines the shared attributes between a {@link Answer}
  * or {@link Question}.
  */
 public abstract class Post {
    private Long id;

    private String createdBy;

    private Long userId;

    private String picture;

    private String createdAt;

    private Integer votes;

    private String slackId;

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
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
 }
