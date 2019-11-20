package dao;

import api.Tag;
import rx.Observable;
import se.fortnox.reactivewizard.db.Query;

public interface TagDao {

    @Query(value = "SELECT label FROM tag WHERE label ILIKE ( :search || '%')")
    Observable<Tag> getTagsBySearchQuery(String search);
}
