import ApiFetch from '../components/utils/apifetch';

export function answerQuestion(answer, questionId, token) {

    const options = {
        url: `/api/answers/question/${questionId}`,
        method: 'POST',
        body: answer,
        headers: {
            authorizationToken: token
        }
    };

    return ApiFetch(options);

}

export function getAnswersByQuestionId(id) {
    const options = {
        url: `/api/answers/question/${id}`
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
