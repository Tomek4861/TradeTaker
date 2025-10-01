<script>
	import { Motion } from 'svelte-motion';
	import { showErrorToast, showSuccessToast } from '$lib/toasts';
	import { page } from '$app/stores';
	import { apiFetch } from '$lib/api.js';

	export let user = null;

	let left = 0;
	let width = 0;
	let opacity = 0;
	let navItemNodes = [];

	const baseNavs = [
		{ name: 'Trade', link: '/trade' },
		{ name: 'Positions', link: '/positions' },
		{ name: 'Stats', link: '/stats' },
		{ name: 'Settings', link: '/settings' }
	];

	$: navs = user
		? [...baseNavs, { name: 'Log out', link: '/logout' }]
		: [...baseNavs, { name: 'Log in', link: '/login' }];

	async function handleLogout(event) {
		event.preventDefault();

		try {


						const responseJson = await apiFetch('/auth/logout', {
				method: 'POST',
			})

			showSuccessToast('Successfully logged out!');
			setTimeout(500, () => {
				window.reload();
			});
		} catch (error) {
			showErrorToast(error.message);
		}
	}

	const setActiveIndicator = () => {
		const activeIndex = navs.findIndex((nav) => nav.link === $page.url.pathname);

		if (activeIndex !== -1 && navItemNodes[activeIndex]) {
			const node = navItemNodes[activeIndex];
			const nodeRect = node.getBoundingClientRect();
			left = node.offsetLeft;
			width = nodeRect.width;
			opacity = 1;
		} else {
			opacity = 0;
		}
	};

	$: if ($page && navItemNodes.length > 0) {
		setActiveIndicator();
	}

	let positionMotion = (node) => {
		let refNode = () => {
			let mint = node.getBoundingClientRect();
			left = node.offsetLeft;
			width = mint.width;
			opacity = 1;
		};
		node.addEventListener('mouseenter', refNode);
		return {};
	};
</script>

<nav>
	<div class="md:hidden">
		<div class="flex justify-between mb-8 border-b border-b-zinc-800 pb-4">
			<h1 class="text-3xl font-bold">Crypto Manager</h1>
			<input type="checkbox" id="menu-toggle" class="peer hidden" />
			<label for="menu-toggle" class="z-50 cursor-pointer border rounded px-2 py-1 text-2xl flex items-center">
				&#9776;
			</label>
			<label for="menu-toggle" class="fixed inset-0 bg-black/50 z-40 hidden peer-checked:block"
						 aria-hidden="true"></label>
			<div
				class="fixed top-0 right-0 h-full w-3/4 max-w-sm bg-zinc-900 transform transition-transform duration-300 z-50 translate-x-full peer-checked:translate-x-0">
				<div class="flex justify-end p-4">
					<label for="menu-toggle" class="cursor-pointer text-2xl">&times;</label>
				</div>
				<ul class="flex flex-col space-y-2 px-4">
					{#each navs as item}
						<li
							class="text-center px-4 py-3 border border-zinc-700 rounded-2xl bg-zinc-900 transition-colors hover:bg-blue-700"
							class:bg-blue-700={$page.url.pathname === item.link}
						>
							{#if item.name === 'Log out'}
								<button on:click|preventDefault={handleLogout} class="block w-full">{item.name}</button>
							{:else}
								<a href={item.link} class="block w-full">{item.name}</a>
							{/if}
						</li>
					{/each}
				</ul>
			</div>
		</div>
	</div>

	<div class="hidden md:block">
		<h1 class="text-4xl font-bold mb-3 text-center">Crypto Manager</h1>
		<div class="py-10 w-full">
			<ul
				on:mouseleave={setActiveIndicator}
				class="relative mx-auto flex w-fit rounded-full border-2 border-zinc-700 bg-zinc-900 p-1"
			>
				{#each navs as item, i}
					<li
						use:positionMotion
						bind:this={navItemNodes[i]}
						class="relative z-10 block cursor-pointer px-3 py-2 text-white md:px-5 md:py-3"
					>
						{#if item.name === 'Log out'}
							<button class="text-lg" on:click|preventDefault={handleLogout}>{item.name}</button>
						{:else}
							<a
								class="text-lg transition-colors"
								class:font-bold={$page.url.pathname === item.link}
								href={item.link}>{item.name}</a>
						{/if}
					</li>
				{/each}
				<Motion
					animate={{ left, width, opacity }}
					transition={{ type: 'spring', stiffness: 400, damping: 30 }}
					let:motion
				>
					<li use:motion class="absolute z-0 h-7 rounded-full bg-blue-700 md:h-12 top-1/2 -translate-y-1/2"></li>
				</Motion>
			</ul>
		</div>
	</div>
</nav>