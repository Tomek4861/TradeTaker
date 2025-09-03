import { redirect } from '@sveltejs/kit';
import { setAuthToken } from '$lib/auth.js';

export async function load({ url, cookies }) {
	const token = url.searchParams.get('token');

	if (token) {
		setAuthToken(cookies, token);
		console.log('SSO login successful, token set., ' + token);

		throw redirect(303, '/positions');
	} else {
		throw redirect(303, '/login?error=sso_failed');
	}
}
