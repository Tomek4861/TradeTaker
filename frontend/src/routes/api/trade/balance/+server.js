import { proxyGet } from '$lib/apiProxy.js';

const targetPath = `/bybit/balance`;

export async function GET(event) {
	return proxyGet(event, targetPath);
}
