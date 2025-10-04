import {handleAuthProxyPost} from '$lib/authProxyHandler.js';

const targetPath = '/auth/register';

export async function POST(event) {
	return handleAuthProxyPost(event, targetPath, {
		setCookie: true,
		sendAuth: false,
		errorLabel: 'Registration'
	});
}
