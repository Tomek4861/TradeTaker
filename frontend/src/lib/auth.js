export function setAuthToken(cookies, token) {
  if (!token) return;
  cookies.set('auth_token', token, {
    path: '/',
    httpOnly: true,
    secure: false, // true in prod
    sameSite: 'strict',
    maxAge: 60 * 60 * 24 * 30 // 30 days
  });
}

