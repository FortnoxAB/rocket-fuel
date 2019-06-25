import ApiFetch from '../components/utils/apifetch';

export function getUser(id) {
    const options = {
        url: `/api/user/id/${id}`
    };

    return ApiFetch(options).then((response) => {
        if (response.error) {
            return;
        }
        return response;
    });
}

export function signIn(token) {
    const options = {
        url: '/api/user/authenticate/',
        headers: {
            authorizationToken: token
        }
    };

    return ApiFetch(options).then((response) => {
        if (response.error) {
            return;
        }
        return response;
    });
}

export function deleteQuestion(questionId) {
    const options = {
        url: `/api/me/questions/${questionId}`,
        method: 'DELETE'
    };

    return ApiFetch(options).then((response) => {
        if (response.error) {
            return;
        }
        return response;
    });
}

export function updateQuestion(questionId, body) {
    const options = {
        url: `/api/me/questions/${questionId}`,
        method: 'PUT',
        body: body
    };

    return ApiFetch(options).then((response) => {
        if (response.error) {
            return;
        }
        return response;
    });
}

export function updateAnswer(answerId, body) {
    const options = {
        url: `/api/me/answers/${answerId}`,
        method: 'PUT',
        body: body
    };

    return ApiFetch(options).then((response) => {
        if (response.error) {
            return;
        }
        return response;
    });
}

export function deleteAnswer(answerId) {
    const options = {
        url: `/api/me/answers/${answerId}`,
        method: 'DELETE'
    };

    return ApiFetch(options).then((response) => {
        if (response.error) {
            return;
        }
        return response;
    });
}
