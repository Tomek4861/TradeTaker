import { json } from '@sveltejs/kit';
import { API_BE_BASE_URL } from '$lib/config.js';
import { getAuthHeader } from '$lib/auth.js';


export async function GET({ url, cookies, fetch }) {

	const params = url.searchParams.toString();
	const targetUrl = `${API_BE_BASE_URL}/stats/pnl${params ? `?${params}` : ''}`;

	try {
		const response = await fetch(targetUrl, {
			method: 'GET',
			headers: { 'Content-Type': 'application/json', ...getAuthHeader(cookies) }
		});


		if (!response.ok) {
			return json(
				{ success: false, message: 'Failed to get stats' },
				{ status: response.status }
			);
		}

		const data = await response.json();

						return json({ success: true, data });
	} catch (error) {
		return json(
			{
				success: false,
				message: 'An internal error occurred while getting stats'
			},
			{ status: 500 }
		);
	}
}
