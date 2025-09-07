<script>
	import { onMount } from 'svelte';
	import { showErrorToast, showSuccessToast } from '$lib/toasts.js';

	const secretPlaceholder = '******';

	let apiKey = '';
	let secretKey = '';
	let riskPercentage = 0;
	let isLoading = true;
	let settingsSaveStatus = 'idle';

	async function loadData() {
		isLoading = true;
		try {
			const response = await fetch('/api/settings', {
				method: 'GET',
				headers: {
					'Content-Type': 'application/json'
				}
			});
			// if (!response.ok) {
			// 	throw new Error('Failed to fetch settings');
			// }
			const data = await response.json();

			if (!data['success']) {
				showErrorToast(data['message']);
				return;
			}
			apiKey = data['apiKey'];
			secretKey = secretPlaceholder;
			riskPercentage = data['riskPercentage'];

		} catch (error) {
			showErrorToast(error.message);
		} finally {
			isLoading = false;
		}
	}


	async function handleSaveAllSettings(event) {
		try {
			event.preventDefault();
			const payload = {
				riskPercentage: riskPercentage
			};
			if (secretKey !== secretPlaceholder) {
				payload.apiKey = apiKey;
				payload.secretKey = secretKey;
			}
			const response = await fetch('/api/settings', {
				method: 'PUT',
				headers: {
					'Content-Type': 'application/json'
				},
				body: JSON.stringify(payload)
			});
			// if (!response.ok) {
			// 	throw new Error('Failed to fetch settings');
			// }
			const data = await response.json();

			if (!data['success']) {
				showErrorToast(data['message']);
				return;
			}
			showSuccessToast('Settings saved!');
			settingsSaveStatus = 'success';
			setTimeout(() => settingsSaveStatus = 'idle', 3000);


		} catch (error) {
			showErrorToast(error.message);
		}
	}

	onMount(() => {
		loadData();
	});


</script>


<div class="page-wrapper flex items-center flex-col">
	<div
		class="form-wrapper bg-zinc-900 px-4 md:px-12 pt-8 pb-12 rounded-2xl flex flex-col min-h-[520px] w-full max-w-2xl shadow-xl shadow-white/10">

		<h2 class="text-3xl font-bold mb-6 text-center">Settings</h2>

		<form class="flex flex-col h-full" on:submit={handleSaveAllSettings}>
			<div class="flex-grow">
				<div class="flex justify-between items-center mb-4">
					<h3 class="text-2xl font-semibold">Accounts</h3>
				</div>

				<div class="space-y-3 mb-8 min-h-[10rem] relative">
					{#if isLoading}
						<div class="inset-0 flex items-center justify-center min-h-[300px]">
							<div class="w-8 h-8 border-4 border-zinc-600 border-t-blue-500 rounded-full animate-spin"></div>
						</div>
					{:else }
						<div class="space-y-5">
							<div>
								<label class="block mb-2 text-left" for="account-risk">Risk per trade (%)</label>
								<input id="account-risk" bind:value={riskPercentage}
											 class="bg-zinc-800 rounded-xl px-4 py-3 w-full" type="number" min="0.1" max="20" step="0.1"
											 required />
							</div>
							<div>
								<label class="block mb-2 text-left" for="api-key">API Key</label>
								<input id="api-key" bind:value={apiKey}
											 class="bg-zinc-800 rounded-xl px-4 py-3 w-full" placeholder="API key" required />
							</div>
							<div>
								<label class="block mb-2 text-left" for="secret-key">Secret Key</label>
								<input id="secret-key" bind:value={secretKey}
											 class="bg-zinc-800 rounded-xl px-4 py-3 w-full" placeholder="Secret Key" required
											 type="password" />
							</div>
						</div>
					{/if}


					<button type="submit"
									class="mt-8 py-3 rounded-xl w-full transition-colors duration-1000 {settingsSaveStatus === 'success' ? 'bg-green-600' : 'bg-blue-800 hover:bg-blue-700'}">
						Save Settings
					</button>
				</div>
			</div>
		</form>
	</div>
</div>


<style>

    .deposit-input::-webkit-inner-spin-button,
    .deposit-input::-webkit-outer-spin-button {
        -webkit-appearance: none;
        margin: 0;
    }

    .deposit-input[type='number'] {
    ] -moz-appearance: textfield;
    }
</style>