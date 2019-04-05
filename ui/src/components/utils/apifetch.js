function ApiFetch(options = {}) {
	const defaultOptions = {
		url: '',
		headers: {},
		method:'GET',
		body: null,
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

	return fetch(`${options.url}`, fetchOptions)
		.then((response) => {
			if (!response.ok) {
				throw response;
			}
			return response.json();
		})
		.then((response) => {
			return response;
		}).catch((e) => {
			throw e;
		});
}

export default ApiFetch;
