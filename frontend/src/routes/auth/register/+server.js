import { API_BE_BASE_URL } from '$lib/config.js';
import { json } from '@sveltejs/kit';
import { setAuthToken } from '$lib/auth.js';

export async function POST({ request, cookies, fetch }) {
	try {
		const requestBody = await request.json();
		const response = await fetch(`${API_BE_BASE_URL}/auth/register`, {
			method: 'POST',
			headers: { 'Content-Type': 'application/json' },
			body: JSON.stringify(requestBody)
		});
		if (!response.ok) {
			return json(
				{ success: false, message: 'Invalid registration data' },
				{ status: response.status }
			);
		}
		const respJson = await response.json();
		const token = respJson['accessToken'];
		if (token) {
			setAuthToken(cookies, token);
			return json({ success: true, message: 'Registration successful' });
		} else {
			return json({ success: false, message: 'Token not found in response' }, { status: 500 });
		}
	} catch (error) {
		console.error('Registration error:', error);
		return json({ success: false, message: 'An internal error occurred' }, { status: 500 });
	}
}
