import { json } from '@sveltejs/kit';
import { API_BE_BASE_URL } from '$lib/config.js';
import { getAuthHeader } from '$lib/auth.js';

const url = `${API_BE_BASE_URL}/settings`;

export async function PUT({ request, cookies, fetch }) {
	try {
		const body = await request.json();

		const response = await fetch(url, {
			method: 'PUT',
			headers: { 'Content-Type': 'application/json', ...getAuthHeader(cookies) },
			body: JSON.stringify(body)
		});
		console.log('Response ' + response.status);

		if (!response.ok) {
			return json(
				{ success: false, message: 'Failed to update settings' },
				{ status: response.status }
			);
		}

		const data = await response.json();
		if (data['success']) {
			return json({ success: true, message: 'Settings set!' });
		} else {
			return json({ success: false, message: 'Error updating settings' }, { status: 500 });
		}
	} catch (error) {
		return json(
			{
				success: false,
				message: 'An internal error occurred while updating settings'
			},
			{ status: 500 }
		);
	}
}

export async function GET({ cookies, fetch }) {
	try {
		const response = await fetch(url, {
			method: 'GET',
			headers: { 'Content-Type': 'application/json', ...getAuthHeader(cookies) }
		});
		console.log('Response ' + response.status);

		if (!response.ok) {
			return json(
				{ success: false, message: 'Failed to get settings' },
				{ status: response.status }
			);
		}

		const data = await response.json();

		return json(data);
	} catch (error) {
		return json(
			{
				success: false,
				message: 'An internal error occurred while getting settings'
			},
			{ status: 500 }
		);
	}
}
