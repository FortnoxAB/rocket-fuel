import ApiFetch from '../components/utils/apifetch';

export function answerQuestion(answer, questionId) {

    const options = {
        url: `/api/answers/question/${questionId}`,
        method: 'POST',
        body: answer
    };

    return ApiFetch(options);

}

export function getAnswersByQuestionId(id) {
    const options = {
        url: `/api/answers/question/${id}`,
        method: 'GET'
    };

    return ApiFetch(options);
}

export function acceptAnswer(id) {
    const options = {
        url: `/api/answers/accept/${id}`,
        method: 'PATCH'
    };

    return ApiFetch(options);
}

export function updateAnswer(answerId, body) {
    const options = {
        url: `/api/users/me/answers/${answerId}`,
        method: 'PUT',
        body: body
    };

    return ApiFetch(options);
}

export function deleteAnswer(answerId) {
    const options = {
        url: `/api/users/me/answers/${answerId}`,
        method: 'DELETE'
    };

    return ApiFetch(options);
}

export function upVoteAnswer(answerId) {
    const options = {
        url: `/api/users/me/answers/${answerId}/upvote`,
        method: 'POST'
    };

    return ApiFetch(options);
}

export function downVoteAnswer(answerId) {
    const options = {
        url: `/api/users/me/answers/${answerId}/downvote`,
        method: 'POST'
    };

    return ApiFetch(options);
}
