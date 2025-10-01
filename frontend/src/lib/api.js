export async function apiFetch(url, options) {
	try {
		const response = await fetch(url, options);

		if (!response.ok) {
			const errorData = await response.json();
						throw new Error(errorData.error || `API Error: ${response.status}`);
		}

		return response.json();

	} catch (err) {
		throw err;
	}
}