import { proxyPost } from '$lib/apiProxy.js';

const targetPath = `/positions/preview`;

export async function POST(event) {
	return proxyPost(event, targetPath);
}

//TODO: inny response

