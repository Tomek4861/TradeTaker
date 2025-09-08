export function setAuthToken(cookies, token) {
	if (!token) return;
	cookies.set('auth_token', token, {
		path: '/',
		httpOnly: true,
		secure: false, // true in prod
		sameSite: 'lax',
		maxAge: 60 * 60 * 24 * 30 // 30 days
	});
}

export function getAuthHeader(cookies) {
	const token = cookies.get('auth_token');
	if (token) {
		return { Authorization: `Bearer ${token}` };
	}
	return {};
}
