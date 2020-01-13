package dao;

import api.Question;
import impl.QuestionSearchOptions;
import rx.Observable;
import se.fortnox.reactivewizard.CollectionOptions;
import se.fortnox.reactivewizard.db.GeneratedKey;
import se.fortnox.reactivewizard.db.Query;
import se.fortnox.reactivewizard.db.Update;

public interface QuestionDao {

    @Query(
        value =
        "SELECT " +
            "question.id, " +
            "question.question, " +
            "answer_accepted, " +
            "question.title, " +
            "question.bounty, " +
            "question.created_at, " +
            "question.user_id, " +
            "question.slack_id, \"user\".picture, \"user\".name as created_by, " +
            "(SELECT COALESCE(SUM(question_vote.value), 0) FROM question_vote WHERE question_vote.question_id = question.id) AS votes, " +
            "(SELECT COALESCE(jsonb_agg(tag ORDER BY label), '[]') FROM question_tag RIGHT JOIN tag ON question_tag.tag_id = tag.id WHERE question_tag.question_id = question.id) AS tags " +
        "FROM " +
            "question " +
        "INNER JOIN " +
            "\"user\" on \"user\".id = question.user_id WHERE question.user_id=:userId " +
        "ORDER BY " +
            "question.created_at DESC ",
        defaultLimit = 10,
        maxLimit = 50)
    Observable<Question> getQuestions(long userId, CollectionOptions options);

    @Query(
        value =
        "SELECT " +
            "question.id, " +
            "question.question, " +
            "answer_accepted, " +
            "question.title, " +
            "question.bounty, " +
            "question.created_at, " +
            "question.user_id, " +
            "question.slack_id, " +
            "\"user\".picture, " +
            "\"user\".name as created_by, " +
            "(SELECT COALESCE(SUM(question_vote.value), 0) FROM question_vote WHERE question_vote.question_id = question.id) AS votes, " +
            "(SELECT COALESCE(jsonb_agg(tag ORDER BY label), '[]') FROM question_tag RIGHT JOIN tag ON question_tag.tag_id = tag.id WHERE question_tag.question_id = question.id) AS tags " +
            "FROM " +
            "question " +
        "INNER JOIN " +
            "\"user\" on \"user\".id = question.user_id " +
        "ORDER BY " +
            "question.created_at DESC ",
        defaultLimit = 10,
        maxLimit = 50)
    Observable<Question> getLatestQuestions(CollectionOptions options);

    @Query(
        value =
        "SELECT " +
            "question.id, " +
            "question.question, " +
            "answer_accepted, " +
            "question.title, " +
            "question.bounty, " +
            "question.created_at, " +
            "question.user_id, " +
            "question.slack_id, " +
            "\"user\".picture, " +
            "\"user\".name as created_by, " +
            "(SELECT COALESCE(SUM(question_vote.value), 0) FROM question_vote WHERE question_vote.question_id = question.id) AS votes, " +
            "(SELECT COALESCE(jsonb_agg(tag ORDER BY label), '[]') FROM question_tag RIGHT JOIN tag ON question_tag.tag_id = tag.id WHERE question_tag.question_id = question.id) AS tags " +
            "FROM " +
            "question " +
        "INNER JOIN " +
            "\"user\" on \"user\".id = question.user_id " +
        "ORDER BY " +
            "votes DESC NULLS LAST, " +
            "question.created_at DESC ",
        defaultLimit = 10,
        maxLimit = 50)
    Observable<Question> getPopularQuestions(CollectionOptions options);

    @Query(
        value =
        "SELECT " +
            "question.id, " +
            "question.question, " +
            "answer_accepted, " +
            "question.title, " +
            "question.bounty, " +
            "question.created_at, " +
            "question.user_id, " +
            "question.slack_id, " +
            "\"user\".picture, " +
            "\"user\".name as created_by, " +
            "(SELECT COALESCE(SUM(question_vote.value), 0) FROM question_vote WHERE question_vote.question_id = question.id) AS votes, " +
            "(SELECT COALESCE(jsonb_agg(tag ORDER BY label), '[]') FROM question_tag RIGHT JOIN tag ON question_tag.tag_id = tag.id WHERE question_tag.question_id = question.id) AS tags " +
            "FROM " +
            "question " +
        "INNER JOIN " +
            "\"user\" on \"user\".id = question.user_id " +
        "LEFT JOIN " +
            "answer on answer.question_id = question.id " +
        "WHERE " +
            "answer IS NULL " +
        "ORDER BY " +
            "votes DESC NULLS LAST, " +
            "question.created_at DESC ",
        defaultLimit = 10,
        maxLimit = 50)
    Observable<Question> getPopularUnansweredQuestions(CollectionOptions options);

    @Query(
        value =
        "SELECT " +
            "question.id, " +
            "question.question, " +
            "answer_accepted, " +
            "question.title, " +
            "question.bounty, " +
            "question.created_at, " +
            "question.user_id, " +
            "question.slack_id, " +
            "\"user\".picture, " +
            "\"user\".name as created_by, " +
            "(SELECT COALESCE(SUM(question_vote.value), 0) FROM question_vote WHERE question_vote.question_id = question.id) AS votes, " +
            "(SELECT COALESCE(jsonb_agg(tag ORDER BY label), '[]') FROM question_tag RIGHT JOIN tag ON question_tag.tag_id = tag.id WHERE question_tag.question_id = question.id) AS tags " +
            "FROM " +
            "question " +
        "INNER JOIN " +
            "\"user\" on \"user\".id = question.user_id " +
        "LEFT JOIN " +
            "answer on answer.question_id = question.id " +
        "WHERE " +
            "answer.accepted_at IS NOT NULL " +
        "ORDER BY " +
            "answer.accepted_at DESC NULLS LAST ",
        defaultLimit = 10,
        maxLimit = 50)
    Observable<Question> getRecentlyAcceptedQuestions(CollectionOptions options);

    @Update(
        "INSERT INTO " +
            "question (" +
            "question, " +
            "title, " +
            "bounty, " +
            "created_at, " +
            "user_id, " +
            "slack_id) " +
      " VALUES" +
            "(" +
            ":question.question, " +
            ":question.title, " +
            ":question.bounty, " +
            "NOW(), " +
            ":userId, " +
            ":question.slackId" +
            ")")
    Observable<GeneratedKey<Long>> addQuestion(long userId, Question question);

    @Update("UPDATE question " +
            "SET question=:question.question, title=:question.title " +
            "WHERE question.id=:questionId AND question.user_id=:userId")
    Observable<Integer> updateQuestion(long userId, long questionId, Question question);

    @Query(
        "SELECT " +
            "question.id, " +
            "question, " +
            "title, " +
            "bounty, " +
            "answer_accepted, " +
            "created_at, " +
            "\"user\".name as created_by, " +
            "user_id, " +
            "slack_id, " +
            "\"user\".picture, " +
            "(SELECT COALESCE(SUM(question_vote.value), 0) FROM question_vote WHERE question_vote.question_id = question.id) AS votes, " +
            "(SELECT COALESCE(SUM(question_vote.value), 0) " +
                "FROM question_vote " +
                "WHERE question_vote.question_id = question.id AND question_vote.user_id = :userId) AS current_user_vote, " +
            "(SELECT COALESCE(jsonb_agg(tag ORDER BY label), '[]') FROM question_tag RIGHT JOIN tag ON question_tag.tag_id = tag.id WHERE question_tag.question_id = question.id) AS tags " +
            "FROM " +
            "question " +
        "INNER JOIN " +
            "\"user\" on \"user\".id = question.user_id " +
        "WHERE " +
            "question.id=:questionId")
    Observable<Question> getQuestion(long userId, long questionId);

    @Query(
        "SELECT " +
            "question.id, " +
            "question, " +
            "title, " +
            "bounty, " +
            "answer_accepted, " +
            "created_at, " +
            "user_id, " +
            "slack_id, " +
            "u.id AS user_id, " +
            "u.name as created_by, " +
            "u.picture as picture, " +
            "(SELECT COALESCE(SUM(question_vote.value), 0) FROM question_vote WHERE question_vote.question_id = question.id) AS votes, " +
            "(SELECT COALESCE(jsonb_agg(tag ORDER BY label), '[]') FROM question_tag RIGHT JOIN tag ON question_tag.tag_id = tag.id WHERE question_tag.question_id = question.id) AS tags " +
        "FROM " +
            "question " +
        "LEFT JOIN " +
            "\"user\" u on u.id = question.user_id " +
        "WHERE " +
            "question.id=:questionId")
    Observable<Question> getQuestion(long questionId);

    @Update(
        "UPDATE " +
            "question " +
        "SET " +
            "answer_accepted=true " +
        "WHERE " +
            "question.id=:questionId AND question.user_id=:userId")
    Observable<Integer> markAsAnswered(long userId, long questionId);

    @Query(
        "SELECT " +
            "id, question, " +
            "title, bounty, " +
            "answer_accepted, " +
            "created_at, user_id, " +
            "slack_id, " +
            "(SELECT COALESCE(SUM(question_vote.value), 0) FROM question_vote WHERE question_vote.question_id = question.id) AS votes " +
        "FROM " +
            "question " +
        "WHERE slack_id = :slackId")
    Observable<Question> getQuestionBySlackThreadId(String slackId);

    @Update(
        "DELETE FROM " +
            "question " +
        "WHERE " +
            "question.user_id = :userId AND question.id = :questionId")
    Observable<Integer> deleteQuestion(long userId, long questionId);

    @Query(
        value =
        "WITH cte AS ( " +
            "SELECT DISTINCT " +
            "question.id, " +
            "answer_accepted, " +
            "question.question, " +
            "question.title, " +
            "question.bounty, " +
            "question.created_at, " +
            "question.user_id, " +
            "\"user\".name as created_by, " +
            "(SELECT COALESCE(SUM(question_vote.value), 0) FROM question_vote WHERE question_vote.question_id = question.id) AS votes, " +
            "(SELECT COALESCE(jsonb_agg(tag ORDER BY label), '[]') FROM question_tag RIGHT JOIN tag ON question_tag.tag_id = tag.id WHERE question_tag.question_id = question.id) AS tags, " +
            "ARRAY(SELECT tag.label FROM question_tag RIGHT JOIN tag ON question_tag.tag_id = tag.id WHERE question_tag.question_id = question.id ORDER BY tag.label) AS tag_labels, " +
            "answer.answer " +
            "FROM " +
            "question " +
            "INNER JOIN " +
            "\"user\" on \"user\".id = question.user_id " +
            "LEFT JOIN " +
            "answer on answer.question_id = question.id " +
            ") " +
            "SELECT " +
            "cte.id, " +
            "cte.answer_accepted, " +
            "cte.title, " +
            "cte.bounty, " +
            "cte.created_at, " +
            "cte.question, " +
            "cte.user_id, " +
            "cte.created_by, " +
            "cte.votes, " +
            "cte.tags " +
            "FROM " +
            "cte " +
            "WHERE " +
            "( " +
            ":questionSearchOptions.contentSearch = '' AND tag_labels @> :questionSearchOptions.tags " +
            ") " +
            "OR " +
            "( " +
            ":questionSearchOptions.contentSearch != '' AND '{}' = :questionSearchOptions.tags AND ( " +
            "title ILIKE ('%' || :questionSearchOptions.contentSearch || '%') OR " +
            "question ILIKE ('%' || :questionSearchOptions.contentSearch || '%') OR " +
            "answer  ILIKE ('%' || :questionSearchOptions.contentSearch || '%') " +
            ") " +
            ") " +
            "OR " +
            "( " +
            ":questionSearchOptions.contentSearch != '' AND tag_labels @> :questionSearchOptions.tags AND ( " +
            "title ILIKE ('%' || :questionSearchOptions.contentSearch || '%') OR " +
            "question ILIKE ('%' || :questionSearchOptions.contentSearch || '%') OR " +
            "answer  ILIKE ('%' || :questionSearchOptions.contentSearch || '%') " +
            ") " +
            ") " +
            "ORDER BY " +
            "votes desc, " +
            "created_at desc",
        defaultLimit = 50,
        maxLimit = 50)
    Observable<Question> getQuestions(QuestionSearchOptions questionSearchOptions, CollectionOptions options);
}
