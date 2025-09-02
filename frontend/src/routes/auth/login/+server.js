import { json } from '@sveltejs/kit';
import { API_BE_BASE_URL } from '$lib/config.js';
import { setAuthToken } from '$lib/auth.js';

export async function POST({ request, cookies, fetch }) {
	try {
		const body = await request.json();
		console.log(body);

		const response = await fetch(`${API_BE_BASE_URL}/auth/login`, {
			method: 'POST',
			headers: { 'Content-Type': 'application/json' },
			body: JSON.stringify(body)
		});
		console.log('Response ' + response.status);

		if (!response.ok) {
			return json({ message: 'Invalid login data' }, { status: response.status });
		}

		const data = await response.json();
		const token = data['accessToken'];
		if (token) {
			setAuthToken(cookies, token);

			return json({ success: true });
		} else {
			return json({ message: 'Token not found in response' }, { status: 500 });
		}
	} catch (error) {
		console.error('Login error:', error);
		return json({ message: 'An internal error occurred' }, { status: 500 });
	}
}