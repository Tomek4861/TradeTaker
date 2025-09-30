import { proxyGet } from '$lib/apiProxy.js';

const targetPath = `/positions/orders`;

export async function GET(event) {
	return proxyGet(event, targetPath);
}
