package impl;

import api.Tag;
import api.TagResource;
import com.google.inject.Inject;
import dao.TagDao;
import rx.Observable;

import javax.inject.Singleton;
import java.util.List;

@Singleton
public class TagResourceImpl implements TagResource {
    private final TagDao tagDao;

    @Inject
    public TagResourceImpl(TagDao tagDao) {
        this.tagDao = tagDao;
    }

    public Observable<List<Tag>> queryTags(String searchQuery) {
        return tagDao.getTagsContaining(searchQuery)
            .toList();
    }

    @Override
    public Observable<List<Tag>> getPopularTags() {
        return tagDao.getPopularTags()
            .toList();
    }
}
