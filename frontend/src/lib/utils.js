import { get } from 'svelte/store';
import jwt from '../stores/authStore.js';

export function getAuthHeader() {
	const token = get(jwt);

	if (!token) {
		return {};
	}

	return {
		Authorization: `Bearer ${token}`
	};
}
