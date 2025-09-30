import { proxyGet, proxyPost } from '$lib/apiProxy.js';

const targetPath = `/settings/risk-percentage`;

export async function POST(event) {
	return proxyPost(event, targetPath);
}

export async function GET(event) {
	return proxyGet(event, targetPath);
}


//TODO: inny response