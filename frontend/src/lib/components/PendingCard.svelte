<script>

	export let order;
	export let onCancel;

	async function handleCancel() {
		onCancel();

	}
</script>


<div class="flex-none w-full md:px-2 bg-zinc-800 rounded-2xl p-0.5 md:bg-transparent md:rounded-none md:p-0">
	<div class="bg-zinc-900 rounded-2xl shadow-xl shadow-white/10 h-full flex flex-col">
		<div class="bg-zinc-800 rounded-t-2xl flex items-center justify-center p-3">
			<p class="text-sm text-zinc-400">Type: {order.orderType}</p>
		</div>

		<div class="p-4 flex-grow flex flex-row">
			<div class="w-1/5 text-start flex flex-col justify-between">
				<div class="">
					<p
						class="uppercase border-l-4 px-2.5 mb-1
                 {order.isLong ? 'border-l-green-600' : 'border-l-red-600'}">
						{order.isLong ? 'Long' : 'Short'}
					</p>
					<p class="text-sm text-zinc-200">Qty: {order.quantity}</p>
				</div>
				<p class="text-xs text-zinc-400"><span class="hidden md:inline">Time in Force:</span> {order.timeInForce}</p>
			</div>

			<div class="w-3/5 flex flex-col justify-between">
				<div class="text-center">
					<p class="font-medium mb-1">{order.ticker}</p>
					<p class="text-sm text-zinc-200">Value: {order.value}$</p>
					<p class="text-sm text-zinc-200">Price: {order.price}$</p>
				</div>
				<div class="mt-3 text-center">
					<p class="text-sm text-zinc-600">Status: {order.status}</p>
				</div>
			</div>

			<div class="w-1/5 flex flex-col items-end justify-between">
				<button
					class="py-1 px-3 cursor-pointer rounded-xl hover:bg-zinc-700 border-2 border-zinc-600 w-20 transition-colors"
					on:click={handleCancel}
					type="button">
					Cancel
				</button>
			</div>
		</div>
	</div>
</div>

