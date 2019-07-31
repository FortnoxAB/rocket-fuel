import ApiFetch from '../components/utils/apifetch';

export function answerQuestion(answer, questionId) {

    const options = {
        url: `/api/questions/${questionId}/answers`,
        method: 'POST',
        body: answer
    };

    return ApiFetch(options);

}

export function getAnswersByQuestionId(id) {
    const options = {
        url: `/api/questions/${id}/answers`,
        method: 'GET'
    };

    return ApiFetch(options);
}

export function acceptAnswer(id) {
    const options = {
        url: `/api/answers/${id}/accept`,
        method: 'PATCH'
    };

    return ApiFetch(options);
}

export function updateAnswer(answerId, body) {
    const options = {
        url: `/api/answers/${answerId}`,
        method: 'PUT',
        body: body
    };

    return ApiFetch(options);
}

export function deleteAnswer(answerId) {
    const options = {
        url: `/api/answers/${answerId}`,
        method: 'DELETE'
    };

    return ApiFetch(options);
}

export function upVoteAnswer(answerId) {
    const options = {
        url: `/api/answers/${answerId}/upvote`,
        method: 'POST'
    };

    return ApiFetch(options);
}

export function downVoteAnswer(answerId) {
    const options = {
        url: `/api/answers/${answerId}/downvote`,
        method: 'POST'
    };

    return ApiFetch(options);
}
