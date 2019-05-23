import ApiFetch from '../components/utils/apifetch';

export function getQuestionsFromUser(userId) {
	const options = {
		url: `/api/users/${userId}/questions`,
	};

	return ApiFetch(options).then((response) => {
		if (response.error) {
			return 'err';
		}
		return response;
	});
}

export function getQuestionById(id) {
	const options = {
		url: `/api/questions/${id}`
	};

	return ApiFetch(options).then((response) => {
		if (response.error) {
			return 'err';
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
			return 'err';
		}
		return response;
	});
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

	return ApiFetch(options).then((response) => {
		if (response.error) {
			return 'err';
		}
		return response;
	});
}
