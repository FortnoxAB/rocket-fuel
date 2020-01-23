package dao;

import api.Tag;
import rx.Observable;
import se.fortnox.reactivewizard.db.Query;
import se.fortnox.reactivewizard.db.Update;

import java.util.List;

public interface TagDao {

    @Query("SELECT label FROM tag WHERE label ILIKE ( '%' || :search || '%')")
    Observable<Tag> getTagsContaining(String search);

    @Update(value = "INSERT INTO question_tag (SELECT DISTINCT :questionId, id FROM tag WHERE label IN (:labels)) ON CONFLICT DO NOTHING", minimumAffected = 0)
    Observable<Integer> associateTagsWithQuestion(Long questionId, List<String> labels);

    @Query("SELECT id, label FROM tag_usage ORDER BY usages DESC LIMIT 10")
    Observable<Tag> getPopularTags();

    @Update(value = "INSERT INTO tag (label) VALUES (:tag) ON CONFLICT DO NOTHING", minimumAffected = 0)
    Observable<Integer> mergeTag(String tag);

    @Update(value = "DELETE FROM tag WHERE id IN (SELECT id FROM tag_usage WHERE usages = 0)", minimumAffected = 0)
    Observable<Integer> deleteUnusedTags();

    @Update(value = "DELETE FROM question_tag WHERE question_id=:id", minimumAffected = 0)
    Observable<Integer> removeTagAssociationFromQuestion(Long id);
}
