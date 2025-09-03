import { redirect } from '@sveltejs/kit';

export const load = async ({ locals, url }) => {
	console.log(`--- LAYOUT.SERVER: ${url.pathname} ---`);
	console.log('locals.user w layoucie:', locals.user);

	const user = locals.user;

	const publicRoutes = ['/login', '/register'];

	if (!user && !publicRoutes.includes(url.pathname)) {
		throw redirect(303, '/login');
	}
	if (user && publicRoutes.includes(url.pathname)) {
		throw redirect(303, '/positions');
	}

	return {
		user: user
	};
};
