import ApiFetch from '../components/utils/apifetch';

export function getQuestionsFromUser(userId) {
    const options = {
        url: `/api/users/${userId}/questions`
    };

    return ApiFetch(options);
}

export function getLatestQuestion(limit = 10) {
    const options = {
        url: `/api/questions/latest?limit=${limit}`
    };

    return ApiFetch(options);
}

export function getQuestionById(id) {
    const options = {
        url: `/api/questions/${id}`
    };

    return ApiFetch(options);
}

export function searchQuestions(param) {
    const options = {
        url: `/api/questions?search=${param}`
    };

    return ApiFetch(options);
}

export function createQuestion(question, token) {
    const options = {
        url: `/api/questions`,
        method: 'POST',
        body: question,
        headers: {
            authorizationToken: token
        }
    };

    return ApiFetch(options);
}

export function deleteQuestion(questionId) {
    const options = {
        url: `/api/questions/${questionId}`,
        method: 'DELETE'
    };

    return ApiFetch(options);
}

export function updateQuestion(questionId, body) {
    const options = {
        url: `/api/questions/${questionId}`,
        method: 'PUT',
        body: body
    };

    return ApiFetch(options);
}

export function upVoteQuestion(questionId) {
    const options = {
        url: `/api/questions/${questionId}/upvote`,
        method: 'POST'
    };

    return ApiFetch(options);
}

export function downVoteQuestion(questionId) {
    const options = {
        url: `/api/questions/${questionId}/downvote`,
        method: 'POST'
    };

    return ApiFetch(options);
}

