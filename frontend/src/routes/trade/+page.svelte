<script>
	import { onDestroy, onMount } from 'svelte';
	import Select from 'svelte-select';
	import TradingViewWidget from '$lib/components/TradingViewWidget.svelte';
	import { goto } from '$app/navigation';
	import { showErrorToast, showSuccessToast } from '$lib/toasts.js';


	let items = [];

	let selectedTicker = { value: null, label: null, exchangeFormat: null };
	let screenWidth = 0;
	$: isMobile = screenWidth < 768;

	let accountBalance = 0;
	let riskPercent = 0;

	let leverage = 1;
	let entryPrice = null;
	let stopLoss = null;
	let leverageLimits = new Map(); // not used

	let takeProfits = [
		{ price: null, percentage: null }
	];

	let moveSLToBEIndex = 0;

	let positionSize = null;
	let requiredMargin = null;
	let potentialLoss = null;
	let potentialProfit = null;
	let riskRewardRatio = null;
	let positionValue = null;

	let debounceTimeout;
	let isSubmitting = false;

	let isMarket = true;
	let tickerPrice = null;

	$: if (selectedTicker.exchangeFormat && isMarket) {
		getMarketPrice(selectedTicker.exchangeFormat);

	}

	const intervalsIds = new Set();

	$: effectiveEntryPrice = isMarket ? tickerPrice : entryPrice;

	$: isLong = effectiveEntryPrice && stopLoss ? effectiveEntryPrice > stopLoss : false;


	$: previewPositionParams = JSON.stringify({
		ticker: selectedTicker?.exchangeFormat ?? null,
		isMarket,
		effectiveEntryPrice,
		stopLoss,
		leverage,
		positionSize,
		takeProfits
	});

	$: if (previewPositionParams) {
		clearTimeout(debounceTimeout);
		if (selectedTicker && effectiveEntryPrice && stopLoss) {
			debounceTimeout = setTimeout(previewPositionInfo, 500);
		}
	}



	const marketPriceFetchDelay = 3000;

	onMount(async () => {
		items = await loadTradableTickers();
		selectedTicker = items.at(0);
		await loadRiskPercentage();
		await loadAccountBalance();
		screenWidth = window.innerWidth;
		window.addEventListener('resize', () => {
			screenWidth = window.innerWidth;
		});
		let intervalId = setInterval(() => {
			if (selectedTicker.exchangeFormat && isMarket) {
				getMarketPrice(selectedTicker.exchangeFormat);
			}
		}, marketPriceFetchDelay);
		intervalsIds.add(intervalId);

	});

	onDestroy(() => {
		intervalsIds.forEach(intervalId => clearInterval(intervalId));
	});


	async function getMarketPrice(ticker) {
		try {
			const response = await fetch(`/api/trade/perpetual-tickers/price?ticker=${ticker}`, {
				headers: { 'Content-Type': 'application/json' }
			});
			const data = await response.json();
			if (data.success) {
				tickerPrice = parseFloat(data.price);
			}

		} catch (error) {
			console.error('Failed to get ticker price', error);
		}
	}


	async function loadRiskPercentage() {
		try {
			const response = await fetch(`/api/settings/risk-percentage/`, {
				headers: { 'Content-Type': 'application/json' }
			});
			const data = await response.json();

			riskPercent = parseFloat(data.riskPercentage);

		} catch (error) {
			showErrorToast(error.message);
			console.error('Failed to load risk percentage', error);
		}
	}

	async function loadAccountBalance() {
		try {
			const response = await fetch('/api/trade/balance', {
				headers: { 'Content-Type': 'application/json' }
			});
			const result = await response.json();

			accountBalance = parseFloat(result.balance);
		} catch (error) {
			console.error('Error fetching balance:', error);
		}
	}


	async function loadTradableTickers() {
		try {
			const response = await fetch(`/api/trade/perpetual-tickers`, {
				headers: { 'Content-Type': 'application/json' }
			});


			const responseJson = await response.json();

			const responseData = JSON.parse(responseJson['data']);

			responseData.forEach(elem => {
				leverageLimits.set(elem['instrumentEntry']['symbol'], elem['instrumentEntry']['leverageFilter']['maxLeverage']);
			});

			return responseData.map(elem => ({
				label: elem['instrumentEntry']['symbol'],
				value: elem['tradingViewFormat'],
				exchangeFormat: elem['instrumentEntry']['symbol']

			}));

		} catch (error) {
			showErrorToast(error.message);
			console.error(error);
			return [];
		}
	}
 // TODO: Fix UI FOR TP's on mobile

	function addTakeProfit() {
		takeProfits = [...takeProfits, { price: null, percentage: null }];
	}

	function removeTakeProfit(indexToRemove) {
		takeProfits = takeProfits.filter((_, index) => index !== indexToRemove);

		if (moveSLToBEIndex >= indexToRemove && moveSLToBEIndex > 0) {
			moveSLToBEIndex = moveSLToBEIndex - 1;
		}
	}

	async function previewPositionInfo() {
		if (!selectedTicker || !effectiveEntryPrice || !stopLoss) {
			return;
		}


		const payload = {
			ticker: selectedTicker.exchangeFormat,
			isLong: isLong,
			entryPrice: effectiveEntryPrice,
			stopLoss: stopLoss,
			takeProfitLevels: isMarket ? takeProfits.filter(p => p.price > 0 && p.percentage != null) : null
		};

		try {
			const response = await fetch(`/api/positions/preview`, {
				method: 'POST',
				headers: {
					'Content-Type': 'application/json'

				},
				body: JSON.stringify(payload)
			});

			const responseData = await response.json();
			console.log(responseData);

			if (responseData.success) {

				leverage = parseFloat(responseData.leverage);
				requiredMargin = parseFloat(responseData['requiredMargin']);
				potentialLoss = parseFloat(responseData['potentialLoss']);
				potentialProfit = parseFloat(responseData['potentialProfit']);
				riskRewardRatio = parseFloat(responseData['riskToRewardRatio']);
				positionSize = parseFloat(responseData['size']);
				positionValue = parseFloat(responseData['value']);

			}

		} catch (error) {
			console.error('Failed to process position data:', error);
		}
	}

	async function setLeverageOnExchange() {
		const payload = {
			leverage: leverage,
			ticker: selectedTicker.exchangeFormat
		};
		try {
			const response = await fetch(`/api/positions/leverage`, {
				method: 'POST',
				headers: {
					'Content-Type': 'application/json'

				},
				body: JSON.stringify(payload)
			});

			const responseData = await response.json();

			if (responseData.success) {
				showSuccessToast('Leverage successfully set!');
				return true;
			} else {
				showErrorToast(responseData.error);

			}
		} catch (error) {
			showErrorToast('Failed to set leverage');
			return false;
		}
	}


	async function openNewTrade() {
		const payload = {
			ticker: selectedTicker.exchangeFormat,
			isLong: isLong,
			entryPrice: isMarket ? null : entryPrice,
			size: positionSize,
			stopLoss: stopLoss,
			takeProfitLevels: isMarket ? takeProfits.filter(p => p.price > 0) : null
		};

		try {
			const response = await fetch(`/api/positions/open`, {
				method: 'POST',
				headers: {
					'Content-Type': 'application/json'

				},
				body: JSON.stringify(payload)
			});

			const responseData = await response.json();

			if (responseData.success) {
				showSuccessToast('Position placed successfully!');
				return true;
			} else {
								showErrorToast(responseData.error);
				return false;
			}


		} catch (error) {
			showErrorToast('Failed to open trade');
			return false;

		}
	}

	async function handleOpenTradeButton() {
		isSubmitting = true;

		if (!selectedTicker || !stopLoss || !positionSize || !effectiveEntryPrice) {
			showErrorToast('Please fill all required fields.');
			isSubmitting = false;
			return;
		}
		try {
			const leverageStatus = await setLeverageOnExchange();
			if (!leverageStatus) return;
			const positionStatus = await openNewTrade();
			if (positionStatus) {
				setTimeout(async () => {
					await goto('/positions');
				}, 3200);
			}
		} finally {
			isSubmitting = false;
		}
	}

	function normalizePercent(v, index) {
		if (v == null || v === '') return null;

		let x = Number(v);
		if (!Number.isFinite(x)) return null;

		x = Math.max(0, x);

		const othersSum = takeProfits.reduce((sum, tp, i) => {
			if (i === index) return sum;
			const p = Number(tp.percentage);
			const clamped = Number.isFinite(p) ? Math.max(0, Math.min(100, p)) : 0;
			return sum + clamped;
		}, 0);

		// max % for curr TP
		const available = Math.max(0, 100 - othersSum);

		x = Math.min(x, available);
		return Math.round(x);
	}


</script>


<div class="flex items-center flex-col">
	<div
		class="w-auto md:min-w-2/3 bg-zinc-900 md:px-10 pt-4 pb-12 rounded-2xl text-center flex flex-col justify-between min-h-[60vh] max-w-full md:max-w-lg shadow-xl shadow-white/10">
		<Select --item-hover-bg="#52525b" --list-background="#27272a" bind:value={selectedTicker} containerStyles="
        background-color: #27272a;
        border: 1px solid #374151;
        border-radius: 0.5rem;
        color: #ffffff;
        width: 100%;
        max-width: 20rem;
        margin: 0 auto;
      "
						{items}
						placeholder="Select Ticker"
		/>


		<div class="bg-zinc-800 rounded-t-2xl flex items-center justify-center p-1 mt-3">
			<div class="w-full h-64 md:h-90 lg:h-96 xl:h-106 ">
				{#key selectedTicker}
					<TradingViewWidget class="w-full h-full" symbol={selectedTicker.value} />
				{/key}
			</div>
		</div>

		<div class="w-full max-w-md mx-auto mt-6 px-4">

			<div class="flex justify-center space-x-6 bg-zinc-800 p-2 rounded-xl mb-6 text-sm">
				<div>
					<span class="text-zinc-400">Balance: </span>
					<span class="font-mono text-white">${accountBalance.toLocaleString('en-US')}</span>
				</div>
				<div>
					<span class="text-zinc-400">Risk per Trade: </span>
					<span class="font-mono text-white">{riskPercent}%</span>
				</div>
			</div>


			<div class="flex items-center space-x-2 mb-4">
				<span class="text-zinc-400 w-24 text-start">Leverage:</span>
				<input
					bind:value={leverage}
					class="bg-zinc-800   text-center w-24 rounded-xl px-4 py-2  focus:outline-none focus:ring-2 focus:ring-blue-600"
					max="100"
					min="1"
					step="1"
					disabled
					type="number"
				/>
								<span class="">x</span>

				<div class="flex-1 flex justify-end">
					<p class="uppercase border-r-4 px-2.5 mb-1 text-lg"
						 class:border-r-green-600={isLong}
						 class:border-r-red-600={!isLong}>
						{isLong ? 'Long' : 'Short'}
					</p>
				</div>
			</div>

			<div class="flex items-center space-x-2 mb-2">
				<span class="text-zinc-400 w-24 text-start">Order type:</span>
				<div class="inline-flex items-center bg-zinc-800 border border-zinc-600 rounded-xl p-1 select-none" role="group"
						 aria-label="Order type">
					<button
						type="button"
						class="px-3 py-1 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-600 transition-colors"
						class:bg-blue-700={isMarket}
						class:text-white={isMarket}
						class:text-zinc-400={!isMarket}
						aria-pressed={isMarket}
						on:click={() => { isMarket = true; entryPrice = null; }}
					>
						Market
					</button>
					<button
						type="button"
						class="px-3 py-1 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-600 transition-colors"
						class:bg-blue-700={!isMarket}
						class:text-white={!isMarket}
						class:text-zinc-400={isMarket}
						aria-pressed={!isMarket}
						on:click={() => { isMarket = false; }}
					>
						Limit
					</button>
				</div>
			</div>

			<div class="flex items-center space-x-2 mb-4">
				<span class="text-zinc-400 w-24 text-start">Entry:</span>
				{#if isMarket}
					<input
						value={tickerPrice}
						class="bg-zinc-800 w-full rounded-xl px-4 py-2  focus:outline-none cursor-default"
						readonly
					/>
				{:else}
					<input
						bind:value={entryPrice}
						class="bg-zinc-800 w-full rounded-xl px-4 py-2 focus:outline-none focus:ring-2 focus:ring-blue-600"
						placeholder="Limit Entry Price"
						type="number"
					/>
				{/if}
			</div>


			<div class="flex items-center space-x-2 mb-4">
				<span class="text-zinc-400 w-24 text-start">Stop Loss:</span>
				<input
					bind:value={stopLoss}
					class="bg-zinc-800  w-full rounded-xl px-4 py-2 focus:outline-none focus:ring-2 focus:ring-blue-600"
					placeholder="Stop Loss"
					type="number"
				/>
			</div>
			{#if isMarket}


				<p class="text-zinc-400 w-full text-left mb-2">Take Profits:</p>

				{#each takeProfits as tp, index (index)}
					<div class="flex items-center gap-2 mb-4 flex-wrap">

						<input
							type="radio"
							id="tp-be-{index}"
							name="sl_be_after_tp"
							class="form-radio h-4 w-4 bg-zinc-700 border-zinc-600 text-blue-600 focus:ring-blue-500"
							bind:group={moveSLToBEIndex}
							value={index}
						/>
						<label for="tp-be-{index}" class="text-zinc-400 w-16 text-start text-nowrap">TP {index + 1}:</label>

						<div class="flex-1 flex gap-2 min-w-[200px]">
							<input
								type="number"
								bind:value={tp.price}
								placeholder="Price"
								class="bg-zinc-800 flex-1 min-w-0 rounded-xl px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-600 text-sm"
							/>

							<div class="relative">
								<input
									type="number"
									bind:value={tp.percentage}
									placeholder=""
									min="0"
									max="100"
									step="1"
									inputmode="decimal"
									aria-label="TP precent"
									class="bg-zinc-800 max-w-20 rounded-xl px-2 pr-8 py-2 text-right focus:outline-none focus:ring-2 focus:ring-blue-600 text-sm"
									on:blur={() => (tp.percentage = normalizePercent(tp.percentage, index))}
								/>
								<span
									class="pointer-events-none absolute right-2 top-1/2 -translate-y-1/2 text-zinc-400 text-sm">%</span>
							</div>


						</div>

						<button
							class="py-1 px-3  cursor-pointer rounded-xl hover:bg-zinc-800 border-2 border-zinc-600 disabled:opacity-50 disabled:cursor-not-allowed "
							on:click={() => removeTakeProfit(index)}
							disabled={takeProfits.length <= 1}
							tabindex="0"
							type="button">
							Remove
						</button>
					</div>
				{/each}
				<button
					class="py-1 px-3  cursor-pointer rounded-xl hover:bg-zinc-800 border-2 border-zinc-600 mb-4 disabled:opacity-50 disabled:cursor-not-allowed"
					disabled={takeProfits.length >= 5}
					on:click={addTakeProfit}
					tabindex="0"
					type="button">
					Add Take Profit
				</button>
			{/if}


			<div class="space-y-2 bg-zinc-800 rounded-xl p-4 mb-4">
				<div class="flex justify-between">
					<span class="text-zinc-400">Required margin:</span>
					<span class="text-white">{requiredMargin ? `$${requiredMargin.toFixed(2)}` : '-'}</span>
				</div>
				<div class="flex justify-between">
					<span class="text-zinc-400">Value:</span>
					<span class="text-white">{ positionValue ? `$${positionValue.toFixed(2)}` : '-'}</span>
				</div>
				<div class="flex justify-between">
					<span class="text-zinc-400">Size:</span>
					<span class="text-white">{positionSize ? `${positionSize.toFixed(2)}` : '-'}</span>
				</div>
				<div class="flex justify-between">
					<span class="text-zinc-400">Potential loss:</span>
					<span class="text-red-500">{potentialLoss ? `$${potentialLoss.toFixed(2)}` : '-'}</span>
				</div>
				<div class="flex justify-between">
					<span class="text-zinc-400">Potential profit:</span>
					<span class="text-green-500">{potentialProfit ? `$${potentialProfit.toFixed(2)}` : '-'}</span>
				</div>
				<div class="flex justify-between">
					<span class="text-zinc-400">Risk to Reward ratio:</span>

					<span
						class:text-green-500={riskRewardRatio >2}
						class:text-yellow-400={riskRewardRatio >= 1 && riskRewardRatio <= 2}
						class:text-red-500={riskRewardRatio <1}
						class:text-zinc-400={riskRewardRatio == null}
					>
						{riskRewardRatio ? `1 : ${riskRewardRatio.toFixed(1)}` : '-'}
					</span>
				</div>
			</div>

			<button
				class="mt-5 bg-blue-800 hover:bg-blue-700 py-3 rounded-xl w-full text-lg
            transition-colors duration-200 max-w-xs mx-auto"
				disabled={isSubmitting}
				on:click={handleOpenTradeButton}
			>
				Open {isLong ? 'Long' : "Short"}
			</button>
		</div>
	</div>
</div>

<style>
    input[type="number"]::-webkit-inner-spin-button,
    input[type="number"]::-webkit-outer-spin-button {
        -webkit-appearance: none;
        margin: 0;
    }

    input[type="number"] {
        appearance: textfield;
    }
</style>