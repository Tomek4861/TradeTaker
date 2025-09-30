import { proxyGet } from '$lib/apiProxy.js';

const targetPath = `/bybit/perpetual-tickers`;

export async function GET(event) {
	return proxyGet(event, targetPath);
}
