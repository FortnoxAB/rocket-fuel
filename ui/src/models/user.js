import ApiFetch from '../components/utils/apifetch';

export function getUser(id) {
    const options = {
        url: `/api/user/id/${id}`
    };

    return ApiFetch(options);
}

export function signIn(token) {
    const options = {
        url: '/api/user/authenticate/',
        headers: {
            authorizationToken: token
        }
    };

    return ApiFetch(options);
}
