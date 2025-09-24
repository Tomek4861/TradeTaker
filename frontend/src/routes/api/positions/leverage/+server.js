import { getAuthHeader } from '$lib/auth.js';
import { json } from '@sveltejs/kit';
import { API_BE_BASE_URL } from '$lib/config.js';


const url = `${API_BE_BASE_URL}/positions/leverage`;



export async function POST({ request, cookies, fetch }) {
	try {
		const body = await request.json();
		const response = await fetch(url, {
			method: 'POST',
			headers: { 'Content-Type': 'application/json', ...getAuthHeader(cookies) },
			body: JSON.stringify(body)
		});

		if (!response.ok) {
			const data = await response.json();
			return json(
				{ success: false, message: data['error'] },
				{ status: response.status }
			);
		}

		const data = await response.json();
		return json(data);
	} catch (error) {
		return json(
			{
				success: false,
								message: 'An internal error occurred while setting leverage'
			},
			{ status: 500 }
		);
	}
}