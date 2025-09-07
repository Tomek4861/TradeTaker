import { API_BE_BASE_URL } from '$lib/config.js';

export const handle = async ({ event, resolve }) => {
	console.log(`\n--- HOOK: ${event.request.method} ${event.url.pathname} ---`);

	const token = event.cookies.get('auth_token');
	console.log('Cookie token:', token ? `Yes ${token.length}` : 'No');
	event.locals.user = null;

	if (token) {
		try {
			const response = await event.fetch(`${API_BE_BASE_URL}/auth/status`, {
				headers: {
					Authorization: `Bearer ${token}`
				}
			});
			console.log('BE respone (/auth/status):', response.status, response.statusText);
			if (response.ok) {
				event.locals.user = await response.text();
			}
		} catch (error) {
			console.error('API error in hooks.server.js:', error);
		}
	}

	return resolve(event);
};
