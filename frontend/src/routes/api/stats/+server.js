import { proxyGet } from '$lib/apiProxy.js';

const targetPath = `/stats/pnl`;

export async function GET(event) {
	return proxyGet(event, targetPath);
}
