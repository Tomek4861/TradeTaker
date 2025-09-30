import { proxyGet, proxyPut } from '$lib/apiProxy.js';

const targetPath = `/settings`;

export async function PUT(event) {
	return proxyPut(event, targetPath);
}

export async function GET(event) {
	return proxyGet(event, targetPath);

}
