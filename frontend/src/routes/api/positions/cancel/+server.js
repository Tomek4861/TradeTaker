import { json } from '@sveltejs/kit';
import { API_BE_BASE_URL } from '$lib/config.js';
import { getAuthHeader } from '$lib/auth.js';

const url = `${API_BE_BASE_URL}/positions/cancel-order`;

export async function POST({ request, cookies, fetch }) {
	try {
		const body = await request.json();

		const response = await fetch(url, {
			method: 'POST',
			headers: { 'Content-Type': 'application/json', ...getAuthHeader(cookies) },
			body: JSON.stringify(body)
		});
		console.log('Response ' + response.status);


		if (!response.ok) {
			return json(
				{ success: false, message: 'Failed to cancel order' },
				{
					status: response.status
				}
			);
		}
		const data = await response.json();

		return json(data, { status: 200 });
	} catch (error) {
		console.error('Position close error:', error);
		return json({ message: 'Internal server error while canceling order' }, { status: 500 });
	}
}
