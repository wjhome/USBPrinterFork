<script setup>
import HelloWorld from './components/HelloWorld.vue'
import BarcodeScanner from 'simple-barcode-scanner'
import { onMounted, ref } from 'vue'

const data = ref(null)
const result = ref(null)
const scanner = BarcodeScanner({ validKey: /^[a-zA-Z0-9\-]$/ }) // eslint-disable-line
function scannerOn() {
	scanner.on(
		(code, event) => {
			event.preventDefault()
			data.value = code
			result.value = event
			setInterval(() => {
				scannerDestroy()
			}, 10000)
		},
		(error) => {
			console.error(error)
		}
	)
}
function scannerDestroy() {
	scanner.off()
}

onMounted(() => {
	scannerOn()
})
</script>

<template>
	<div>demo</div>
	<div>data: {{ data }}</div>
	<div>result: {{ result }}</div>
	<button @click="scannerOn">scan now</button>
	123
	<HelloWorld />
</template>

<style scoped>
.logo {
	height: 6em;
	padding: 1.5em;
	will-change: filter;
	transition: filter 300ms;
}
.logo:hover {
	filter: drop-shadow(0 0 2em #646cffaa);
}
.logo.vue:hover {
	filter: drop-shadow(0 0 2em #42b883aa);
}
</style>
