package impl;

import api.Tag;
import api.TagResource;
import com.google.inject.Inject;
import dao.TagDao;
import rx.Observable;

import java.util.List;

public class TagResourceImpl implements TagResource {
    private final TagDao tagDao;

    @Inject
    public TagResourceImpl(TagDao tagDao) {
        this.tagDao = tagDao;
    }

    public Observable<List<String>> getTags(String searchQuery) {
        return tagDao.getTagsBySearchQuery(searchQuery)
            .map(Tag::getLabel)
            .toList();
    }
}
