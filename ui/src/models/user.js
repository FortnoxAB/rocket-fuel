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
        method: 'POST',
        headers: {
            authorizationToken: token
        }
    };

    return ApiFetch(options);
}

export function signOut() {
    const options = {
        url: '/api/user/authenticate/',
        method: 'DELETE'
    };

    return ApiFetch(options);
}
