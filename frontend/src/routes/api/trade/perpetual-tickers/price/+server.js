import { proxyGet } from '$lib/apiProxy.js';

const targetPath = `/bybit/ticker-price`;

export async function GET(event) {
	return proxyGet(event, targetPath);
}
