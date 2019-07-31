import * as User from '../../models/user';

const RETRIES = 2;

function ApiFetch(options = {}, tryCount = 0) {
    const defaultOptions = {
        url: '',
        headers: {},
        method: 'GET',
        body: null
    };

    const headers = {
        'Access-Control-Allow-Origin': '*',
        'Access-Control-Allow-Credentials': 'true'
    };

    options = Object.assign(defaultOptions, options);
    Object.assign(options.headers, headers);
    options.headers['Content-Type'] = 'application/json';

    let fetchOptions = {
        method: options.method,
        headers: options.headers,
        body: JSON.stringify(options.body)
    };

    if (!options.body) {
        fetchOptions.headers['Content-Type'] = 'text/plain';
        delete fetchOptions.body;
    }

    return fetch(options.url, fetchOptions)
        .then((response) => {
            if (!response.ok && response.status === 401) {
                console.log('401');
                if (tryCount < RETRIES) {
                    return AuthWithGoogle().then(() => {
                        return ApiFetch(options, tryCount+1);
                    });
                }
            }
            if (!response.ok) {
                throw response;
            }
            if (response.status === 204) {
                return {}
            }
            return response.json();
        })
        .then((response) => {
            return response;
        }).catch((e) => {
            throw e;
        });
}

function AuthWithGoogle() {
    let token = null;
    const GoogleAuth = gapi.auth2.getAuthInstance();
    if (GoogleAuth.isSignedIn.get()) {
        const GoogleUser = GoogleAuth.currentUser.get();
        token = GoogleUser.getAuthResponse().id_token;
    }
    return User.signIn(token);
}

export default ApiFetch;
