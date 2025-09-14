import { json } from '@sveltejs/kit';
import { API_BE_BASE_URL } from '$lib/config.js';
import { getAuthHeader } from '$lib/auth.js';

const url = `${API_BE_BASE_URL}/positions/preview`;

export async function POST({ request, cookies, fetch }) {
	try {
		const body = await request.json();

		const response = await fetch(url, {
			method: 'POST',
			headers: { 'Content-Type': 'application/json', ...getAuthHeader(cookies) },
			body: JSON.stringify(body)
		});
		console.log('Response ' + response.status);

		const raw = await response.text();
		let payload;
		try {
			payload = raw ? JSON.parse(raw) : null;
		} catch {
			payload = raw ? { message: raw } : null;
		}

		if (!response.ok) {
			return json(payload || { message: 'Failed to preview position' }, {
				status: response.status
			});
		}

		return json(payload ?? {}, { status: 200 });
	} catch (error) {
		console.error('Preview proxy error:', error);
		return json({ message: 'Internal server error while previewing position' }, { status: 500 });
	}
}
