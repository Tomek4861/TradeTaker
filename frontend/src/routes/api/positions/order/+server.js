import { json } from '@sveltejs/kit';
import { API_BE_BASE_URL } from '$lib/config.js';
import { getAuthHeader } from '$lib/auth.js';

const url = `${API_BE_BASE_URL}/positions/orders`;



export async function GET({ cookies, fetch }) {
	try {
		const response = await fetch(url, {
			method: 'GET',
			headers: { 'Content-Type': 'application/json', ...getAuthHeader(cookies) }
		});
		console.log('Response ' + response.status);

		if (!response.ok) {
			const data = await response.json();

			return json(
				{ success: false, message: data['errorMessage'] },
				{ status: response.status }
			);
		}

		const data = await response.json();

		return json(data);
	} catch (error) {
		return json(
			{
				success: false,
				message: 'An internal error occurred while fetching positions'
			},
			{ status: 500 }
		);
	}
}
