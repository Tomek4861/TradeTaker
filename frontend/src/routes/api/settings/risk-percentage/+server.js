import { json } from '@sveltejs/kit';
import { API_BE_BASE_URL } from '$lib/config.js';

const url = `${API_BE_BASE_URL}/settings/risk-percentage`;

export async function POST({ request, cookies, fetch }) {
	try {
		const body = await request.json();

		const response = await fetch(url, {
			method: 'POST',
			headers: { 'Content-Type': 'application/json' },
			body: JSON.stringify(body)
		});
		console.log('Response ' + response.status);

		if (!response.ok) {
			return json(
				{ success: false, message: 'Invalid risk percentage' },
				{ status: response.status }
			);
		}

		const data = await response.json();
		if (data['success']) {
			return json({ success: true, message: 'Risk percentage set!' });
		} else {
			return json({ success: false, message: 'Error setting risk percentage' }, { status: 500 });
		}
	} catch (error) {
		return json(
			{
				success: false,
				message: 'An internal error occurred while setting risk percentage'
			},
			{ status: 500 }
		);
	}
}

export async function GET({ fetch }) {
	try {
		const response = await fetch(url, {
			method: 'GET'
		});
		console.log('Response ' + response.status);

		if (!response.ok) {
			return json(
				{ success: false, message: 'Failed to get risk percentage' },
				{ status: response.status }
			);
		}

		const data = await response.text();

		return json({ success: true, riskPercentage: data });
	} catch (error) {
		return json(
			{
				success: false,
				message: 'An internal error occurred while getting risk percentage'
			},
			{ status: 500 }
		);
	}
}
