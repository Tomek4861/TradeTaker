<script>
	import '../app.css';
	import { SvelteToast } from '@zerodevx/svelte-toast';
	import NavMenu from '$lib/components/NavMenu.svelte';
	import userStore from '../stores/authStore.js';
	import { page } from '$app/stores';


	export let data;


	$: $userStore = $page.data.user;


</script>


<main class="min-h-screen bg-zinc-950 flex justify-center text-gray-100 p-3">
	<div class="w-full md:w-4/5">
		<NavMenu user={data.user} />
		<slot />
	</div>
</main>

<SvelteToast />