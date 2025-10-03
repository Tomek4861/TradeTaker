import { json } from '@sveltejs/kit';

export async function POST({  cookies  }) {
	try {
		cookies.delete('auth_token', { path: '/' });

				return json({ success: true, message: 'Logged out successfully' });
	} catch (error) {
		console.error('Logout error:', error);
		return json({ success: false, error: 'An internal error occurred' }, { status: 500 });
	}
}
