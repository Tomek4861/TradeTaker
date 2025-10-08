import { proxyGet, proxyPost } from '$lib/apiProxy.js';

const targetPath = `/positions/open`;

export async function POST(event) {
	return proxyPost(event, targetPath);
}
export async function GET(event) {
	return proxyGet(event, targetPath);
}
