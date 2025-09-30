import { json } from '@sveltejs/kit';
import { API_BE_BASE_URL } from '$lib/config.js';
import { getAuthHeader } from '$lib/auth.js';

async function proxyRequest(event, targetPath) {
	const { request, cookies, fetch, url } = event;

	try {
		const headers = {
			'Content-Type': 'application/json',
			...getAuthHeader(cookies)
		};

		const options = {
			method: request.method,
			headers: headers
		};

		if (request.method !== 'GET' && request.method !== 'HEAD') {
			try {
				const body = await request.json();
				options.body = JSON.stringify(body);
			} catch (e) {
				console.error('Failed to parse request body', e);
				return json({ message: `Invalid request body: ${e.message}` }, { status: 400 });
			}
		}

		const targetUrl = `${API_BE_BASE_URL}${targetPath}${url.search}`;
		console.log(`Proxying request to: ${targetUrl}`);

		const response = await fetch(targetUrl, options);

		const responseBody = await response.text();
		let data = null;
		if (responseBody) {
			try {
				data = JSON.parse(responseBody);
			} catch (e) {
				console.error('Failed to parse backend response as JSON', e);
				return json({ message: 'Invalid JSON response from backend.' }, { status: 502 });
			}
		}

		return json(data, { status: response.status });
	} catch (error) {
		console.error(`API Proxy Error for ${targetPath}:`, error);
		return json({ message: `Internal Server Error for ${targetPath}.` }, { status: 500 });
	}
}

export async function proxyPost(event, targetPath) {
	return proxyRequest(event, targetPath);
}

export async function proxyGet(event, targetPath) {
	return proxyRequest(event, targetPath);
}
export async function proxyPut(event, targetPath) {
	return proxyRequest(event, targetPath);
}
