import ApiFetch from '../components/utils/apifetch';

export function getQuestions(id) {
	const options = {
		url: `/api/users/${id}/questions`,
	};

	return ApiFetch(options).then((response) => {
		if (response.error) {
			return 'test';
		}
		return response;
	});
}

export function getQuestion(id) {
	const options = {
		url: `/api/users/${id}/questions`,
	};

	return ApiFetch(options).then((response) => {
		if (response.error) {
			return 'test';
		}
		return response;
	});
}

export function searchQuestions(param) {
	const options = {
		url: `/api/questions?search=${param}`,
	};

	return ApiFetch(options).then((response) => {
		if (response.error) {
			return 'test';
		}
		return response;
	});
}
