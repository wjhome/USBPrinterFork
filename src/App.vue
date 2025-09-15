<script setup>
import HelloWorld from './components/HelloWorld.vue'
import BarcodeScanner from 'simple-barcode-scanner'
import { onMounted, ref } from 'vue'

const data = ref(null)
const result = ref(null)
const scanner = BarcodeScanner({ validKey: /^[a-zA-Z0-9\-]$/ }) // eslint-disable-line

// 启动扫描（修复setInterval为setTimeout，避免重复关闭）
function scannerOn() {
	scanner.on(
		(code, event) => {
			event.preventDefault()
			data.value = code
			result.value = event
			// 扫描成功后10秒关闭（仅执行一次）
			setTimeout(() => {
				scannerDestroy()
			}, 10000)
		},
		(error) => {
			console.error('扫描错误:', error)
		}
	)
}

// 关闭扫描
function scannerDestroy() {
	if (scanner.isListening()) {
		// 检查是否在监听状态
		scanner.off()
		console.log('扫描已关闭')
	}
}

onMounted(() => {
	scannerOn()
})
</script>

<template>
	<div>
		<div>扫描结果: {{ data || '未扫描到内容' }}</div>
		<button @click="scannerOn">重新扫描</button>
		<HelloWorld />
	</div>
</template>

<style scoped>
button {
	margin-top: 10px;
	padding: 8px 16px;
	cursor: pointer;
}
</style>
