import {handleAuthProxyPost} from '$lib/authProxyHandler.js';

const targetPath = '/auth/login';

export async function POST(event) {
	return handleAuthProxyPost(event, targetPath, {
		setCookie: true,
		sendAuth: false,
		errorLabel: 'Login'
	});
}
