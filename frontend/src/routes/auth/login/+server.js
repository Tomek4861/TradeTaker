import { json } from '@sveltejs/kit';
import { setAuthToken } from '$lib/auth.js';
import { proxyPost } from '$lib/apiProxy.js';

const targetPath = '/auth/login';

export async function POST(event) {
	try {
		const resp = await proxyPost(event, targetPath, false);

		const status = resp.status;
		const contentType = resp.headers.get('content-type') ?? 'text/plain';

		const raw = await resp.text();

		let payload = null;
		try {
			payload = JSON.parse(raw);
		} catch {
			// payload null
		}

		const token = payload?.data;
		if (token) {
			setAuthToken(event.cookies, token);
			console.log('Set auth token');
		}

		if (payload) {
			return json(payload, { status });
		}

		return new Response(raw, {
			status,
			headers: { 'content-type': contentType }
		});
	} catch (error) {
		console.error('Login error:', error);
		return json(
			{ success: false, message: 'Failed to obtain token from response' },
			{ status: 500 }
		);
	}
}
