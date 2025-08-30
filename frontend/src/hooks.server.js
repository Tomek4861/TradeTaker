import { NON_PROXY_API_BASE_URL } from '$lib/config.js';

export const handle = async ({ event, resolve }) => {
	event.locals.user = null;

	const authHeader = event.request.headers.get('authorization');
	console.log('Auth Header:', authHeader);


	if (authHeader) {
		try {
			const response = await event.fetch(`${NON_PROXY_API_BASE_URL}/auth/status`, {
				headers: {
					'Content-Type': 'application/json',
					"authorization": authHeader


				}
			});

			console.log('API response status:', response.status);

			if (response.ok) {
				// event.locals.user = await response.json();
				// console.log('User set in locals:', event.locals.user);
				console.log("Response OK but not setting user for now.");
			} else {
				console.log('API returned non-ok status, clearing user.');
				event.locals.user = null;
			}
		} catch (error) {
			console.error('API error in hooks.server.js:', error);
			event.locals.user = null;
		}
	}

	return resolve(event);
};
