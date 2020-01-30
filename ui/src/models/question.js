import ApiFetch from '../components/utils/apifetch';

export function getQuestionsFromUser(userId, limit = 10) {
    const options = {
        url: `/api/users/${userId}/questions?limit=${limit}`
    };

    return ApiFetch(options);
}

export function getLatestQuestions(limit = 10) {
    const options = {
        url: `/api/questions/latest?limit=${limit}`
    };

    return ApiFetch(options);
}

export function getPopularQuestions(limit = 10) {
    const options = {
        url: `/api/questions/popular?limit=${limit}`
    };

    return ApiFetch(options);
}

export function getPopularUnansweredQuestions(limit = 10) {
    const options = {
        url: `/api/questions/popularunanswered?limit=${limit}`
    };

    return ApiFetch(options);
}

export function getRecentlyAcceptedQuestions(limit = 10) {
    const options = {
        url: `/api/questions/recentlyaccepted?limit=${limit}`
    };

    return ApiFetch(options);
}

export function getQuestionById(id) {
    const options = {
        url: `/api/questions/${id}`
    };

    return ApiFetch(options);
}

export function searchQuestions(param, limit = 50) {
    const encodedParam = encodeURIComponent(param);
    const options = {
        url: `/api/questions?search=${encodedParam}&limit=${limit}`
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

