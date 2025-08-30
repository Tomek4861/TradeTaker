import { writable } from 'svelte/store';
import { browser } from '$app/environment';

const initialValue = browser ? window.localStorage.getItem('jwt_token') : null;

const jwt = writable(initialValue);

jwt.subscribe((value) => {
	console.log("JWT store updated:", value);
	if (browser) {
		console.log("Updating localStorage with JWT:", value);
		if (value) {
			window.localStorage.setItem('jwt_token', value);
		} else {
			window.localStorage.removeItem('jwt_token');
		}
	}
});

export default jwt;
