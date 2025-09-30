import { proxyPost } from '$lib/apiProxy.js';

export async function POST(event) {
	return proxyPost(event, '/positions/leverage');
}

//TODO: Tutaj inny response