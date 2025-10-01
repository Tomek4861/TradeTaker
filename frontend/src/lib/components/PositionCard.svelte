<script>
	import TradingViewWidget from '$lib/components/TradingViewWidget.svelte';
	import { showErrorToast, showSuccessToast } from '$lib/toasts.js';
	import { apiFetch } from '$lib/api.js';


	export let position;

	function getStringAfterColon(inputString) {
		const parts = inputString.split(':');
		return parts.length > 1 ? parts.slice(1).join(':') : inputString;
	}


	// Modal state
	let showConfirm = false;

	function openConfirm() {
		showConfirm = true;
	}

	function closeConfirm() {
		showConfirm = false;
	}

	function confirmClose() {
		closeConfirm();
		handleClose();
	}

	function handleBackdropClick(event) {
		if (event.currentTarget === event.target) {
			closeConfirm();
		}
	}

	function handleKeydown(e) {
		if (!showConfirm) return;
		if (e.key === 'Escape') {
			closeConfirm();
		} else if (e.key === 'Enter') {
			confirmClose();
		}
	}

	async function handleClose() {
		try {
			const payload = {
				isLong: position.isLong,
				ticker: position.ticker,
				size: position.quantity
			};
			const responseJson = await apiFetch('/api/positions/close', {
				method: 'POST',
				headers: { 'Content-Type': 'application/json' },
				body: JSON.stringify(payload)
			});

			if (!responseJson.success) {
				showErrorToast(responseJson.error || 'Failed to close position.');
				return;
			}
			showSuccessToast('Position closed successfully!');
		} catch (err) {
			showErrorToast('Position closing error ' + err.message);
		}
	}
</script>

<svelte:window on:keydown={handleKeydown} />

<div class="flex-none w-full md:w-1/2 md:px-2 bg-zinc-800 rounded-2xl p-0.5 md:bg-transparent md:rounded-none md:p-0">
	<div class="bg-zinc-900 rounded-2xl shadow-xl shadow-white/10 h-full flex flex-col">
		<!-- Chart -->
		<div class="bg-zinc-800 rounded-t-2xl flex items-center justify-center p-1">
			<div class="w-full h-64 md:h-90 lg:h-96 xl:h-106">
				<TradingViewWidget class="w-full h-full" symbol={position.tradingViewFormat} />
			</div>
		</div>

		<!-- Data -->
		<div class="p-4 flex-grow flex flex-row">
			<div class="w-1/5 text-start">
				<p
					class="uppercase border-l-4 px-2.5 mb-1"
					class:border-l-green-600={position.isLong}
					class:border-l-red-600={!position.isLong}>
					{position.isLong ? "Long" : "Short"}
				</p>
				<p class="text-sm text-zinc-400">Leverage: {position.leverage.toFixed(1)}x</p>
			</div>

			<div class="w-3/5 flex flex-col justify-between">
				<div class="text-center">
					<p class="font-medium">{getStringAfterColon(position.ticker)}</p>
					<p class="text-sm text-zinc-400">
						Open Date: {position.openDate.toLocaleDateString()}
					</p>
				</div>
				<div class="mt-3 text-center">
					<p class="text-sm flex flex-col space-y-1 md:inline">
						<span>Value: {position.value.toFixed(2)}$</span>
						<span class="mx-1 text-zinc-500 hidden md:inline">|</span>
						<span>Margin: {position.margin.toFixed(2)}$</span>
					</p>
					<p
						class="text-lg font-semibold"
						class:!text-green-600={position.currentPnl >= 0}
						class:!text-red-500={position.currentPnl < 0}>
						P&L: {position.currentPnl.toFixed(3)}$
						<span class="text-sm font-normal">
							({position.currentPnlPercent.toFixed(2)}%)
						</span>
					</p>
					<p
						class="text-sm font-light"
						class:!text-green-600={position.realizedPnl >= 0}
						class:!text-red-500={position.realizedPnl < 0}>
						Realized P&L: {position.realizedPnl.toFixed(3)}$
					</p>
				</div>
			</div>

			<div class="w-1/5 flex flex-col items-end">
				<button
					class="py-1 px-3 cursor-pointer rounded-xl hover:bg-zinc-800 border-2 border-zinc-600"
					on:click={openConfirm}
					type="button">
					Close
				</button>
				<div class="mt-5"></div>
			</div>
		</div>
	</div>
</div>

{#if showConfirm}
	<!-- Modal Overlay -->
	<div
		class="fixed inset-0 z-50 flex items-center justify-center"
		role="presentation"
		on:keydown={(e) => e.key === 'Escape' && closeConfirm()}>
		<div
			class="absolute inset-0 bg-black/60 backdrop-blur-sm"
			role="button"
			tabindex="-1"
			aria-label="Close dialog"
			on:click={handleBackdropClick}
			on:keydown={(e) => (e.key === 'Enter' || e.key === ' ') && handleBackdropClick(e)}></div>
		<div
			role="dialog"
			aria-modal="true"
			aria-labelledby="dialog-title"
			tabindex="0"
			class="relative bg-zinc-900 text-zinc-200 rounded-2xl border border-zinc-700 shadow-2xl w-[92vw] max-w-md p-5 mx-4">
			<h3 id="dialog-title" class="text-lg font-semibold text-white mb-2">Confirm closing position</h3>
			<p class="text-sm text-zinc-300 leading-relaxed">
				Are you sure you want to close the
				<span class="font-medium">{getStringAfterColon(position.ticker)}</span>
				position with {position.currentPnl >= 0 ? 'an estimated profit of' : 'an estimated loss of'}
				<span class={position.currentPnl >= 0 ? 'text-green-500 font-medium' : 'text-red-500 font-medium'}>
                {position.currentPnl.toFixed(3)}
      </span>
				USD?
			</p>
			<div class="mt-5 flex justify-end gap-2">
				<button
					class="py-1.5 px-3 rounded-xl hover:bg-zinc-800 border-2 border-zinc-600"
					on:click={closeConfirm}
					type="button">
					Cancel
				</button>
				<button
					class="py-1.5 px-3 rounded-xl bg-blue-800 hover:bg-blue-700 transition-colors duration-200 text-white"
					on:click={confirmClose}
					type="button">
					Confirm
				</button>
			</div>
		</div>
	</div>
{/if}
