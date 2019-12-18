package dao;

import api.Tag;
import rx.Observable;
import se.fortnox.reactivewizard.db.GeneratedKey;
import se.fortnox.reactivewizard.db.Query;
import se.fortnox.reactivewizard.db.Update;

import java.util.List;

public interface TagDao {

    @Query(value = "SELECT label FROM tag WHERE label ILIKE ( :search || '%')")
    Observable<Tag> getTagsBySearchQuery(String search);

    @Query("SELECT id, label FROM tag WHERE label IN (:labels)")
    Observable<Tag> getTagsByLabels(List<String> labels);

    @Update("INSERT INTO tag (label) VALUES (:label) RETURNING id, label")
    Observable<GeneratedKey<Tag>> createTag(String label);

    @Update("INSERT INTO question_tag (question_id, tag_id) VALUES (:questionId, :tagId)")
    Observable<Void> associateTagsWithQuestion(Long questionId, Long tagId);

    @Update(value = "DELETE FROM question_tag WHERE question_id = :questionId", minimumAffected = 0)
    Observable<Void> removeTagsFromQuestion(Long questionId);
}
