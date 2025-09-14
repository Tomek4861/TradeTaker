import { json } from '@sveltejs/kit';
import { API_BE_BASE_URL } from '$lib/config.js';
import { getAuthHeader } from '$lib/auth.js';

const url = `${API_BE_BASE_URL}/bybit/balance`;

export async function GET({ cookies, fetch }) {
	try {
		const response = await fetch(url, {
			method: 'GET',
			headers: { 'Content-Type': 'application/json', ...getAuthHeader(cookies) }
		});
		console.log('Response ' + response.status);

		if (!response.ok) {
			return json(
				{ success: false, message: 'Failed to get balance' },
				{ status: response.status }
			);
		}

		const data = await response.text();

		return json({ success: true, balance: data });
	} catch (error) {
		return json(
			{
				success: false,
				message: 'An internal error occurred while getting balance'
			},
			{ status: 500 }
		);
	}
}
