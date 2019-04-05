import ApiFetch from '../components/utils/apifetch';

export function getUser(id) {
	const options = {
		url: `/api/user/id/${id}`,
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
