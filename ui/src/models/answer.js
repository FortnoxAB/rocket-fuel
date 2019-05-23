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

	return ApiFetch(options).then((response) => {
		if (response.error) {
			return 'err';
		}
		return response;
	});

}

export function getAnswersByQuestionId(id) {
	const options = {
		url: `/api/answers/question/${id}`
	};

	return ApiFetch(options).then((response) => {
		if (response.error) {
			return 'err';
		}
		return response;
	});
}

export function acceptAnswer(id) {
	const options = {
		url: `/api/answers/accept/${id}`,
		method: 'PATCH'
	};

	return ApiFetch(options).then((response) => {
		if (response.error) {
			return 'err';
		}
		return response;
	});
}
