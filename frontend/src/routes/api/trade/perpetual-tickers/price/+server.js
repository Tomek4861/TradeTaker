import { json } from '@sveltejs/kit';
import { API_BE_BASE_URL } from '$lib/config.js';
import { getAuthHeader } from '$lib/auth.js';


export async function GET({ url, cookies, fetch }) {

	const params = url.searchParams.toString();
	const targetUrl = `${API_BE_BASE_URL}/bybit/ticker-price${params ? `?${params}` : ''}`;

	try {
		const response = await fetch(targetUrl, {
			method: 'GET',
			headers: { 'Content-Type': 'application/json', ...getAuthHeader(cookies) }
		});


		if (!response.ok) {
			return json(
				{ success: false, message: 'Failed to get price' },
				{ status: response.status }
			);
		}

		const data = await response.text();

		return json({ success: true, price: data });
	} catch (error) {
		return json(
			{
				success: false,
				message: 'An internal error occurred while getting price'
			},
			{ status: 500 }
		);
	}
}
