package impl;

import api.Question;
import api.QuestionSearchResource;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import impl.dao.QuestionSearchDao;
import rx.Observable;

import java.util.List;

@Singleton
public class QuestionSearchResourceImpl implements QuestionSearchResource {

    private final QuestionSearchDao questionSearchDao;
    @Inject
    public QuestionSearchResourceImpl(QuestionSearchDao questionSearchDao){
        this.questionSearchDao = questionSearchDao;
    }

    @Override
    public Observable<List<Question>> getQuestionsBySearchQuery(String searchQuery) {

        validateQuery(searchQuery);
        return questionSearchDao.getQuestions(searchQuery).toList();
    }

    static void validateQuery(String questionQuery) {
        //TODO: add validation for the search query
    }
}
