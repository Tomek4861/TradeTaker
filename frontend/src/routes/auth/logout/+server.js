import { json } from '@sveltejs/kit';

export async function POST({ request, cookies, fetch }) {
	try {
		cookies.delete('auth_token', { path: '/' });

		return json({ success: true, message: 'Logged out successfully' });
	} catch (error) {
		console.error('Logout error:', error);
		return json({ success: false, message: 'An internal error occurred' }, { status: 500 });
	}
}
