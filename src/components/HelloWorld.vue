<template>
	<div class="usb-printer">
		<button @click="getUSBDevices">获取USB设备列表</button>
		<button @click="printTest">测试打印</button>

		<div v-if="devices.length">
			<h3>已连接设备：</h3>
			<ul>
				<li v-for="device in devices" :key="device.name">VID: {{ device.vid }}, PID: {{ device.pid }}, 名称: {{ device.name }}</li>
			</ul>
		</div>

		<div v-if="printStatus">{{ printStatus }}</div>
	</div>
</template>

<script setup>
// 改为使用 registerPlugin 注册插件
import { registerPlugin } from '@capacitor/core'
import { ref, onMounted } from 'vue'

// 注册插件（名称必须与原生 @CapacitorPlugin(name = "USBPrintPlugin") 完全一致）
const USBPrintPlugin = registerPlugin('USBPrintPlugin')

// 响应式变量管理
const devices = ref([])
const printStatus = ref('')

// 获取USB设备列表
const getUSBDevices = async () => {
	try {
		const result = await USBPrintPlugin.getDevices()
		devices.value = result.devices || []
		console.log('设备列表:', devices.value)
	} catch (e) {
		console.error('获取设备失败:', e)
		printStatus.value = '获取设备失败，请检查USB连接'
	}
}

// 打印测试
const printTest = async () => {
	if (devices.value.length === 0) {
		printStatus.value = '请先获取设备列表'
		return
	}

	try {
		// 使用第一个设备进行打印（实际场景可改为用户选择）
		const targetDevice = devices.value[0]
		await USBPrintPlugin.printText({
			vid: targetDevice.vid,
			pid: targetDevice.pid,
			text: 'Hello, Vue3 + Capacitor USB Print!',
		})
		printStatus.value = '打印成功!'
	} catch (e) {
		console.error('打印失败:', e)
		printStatus.value = `打印失败: ${e.message}`
	}
}

// 组件挂载时自动获取设备列表
onMounted(() => {
	getUSBDevices()
})
</script>

<style scoped>
/* 样式保持不变 */
.usb-printer {
	padding: 20px;
}

button {
	margin: 0 10px 10px 0;
	padding: 8px 16px;
	cursor: pointer;
}

ul {
	margin: 10px 0;
	padding-left: 20px;
}

li {
	margin: 5px 0;
}
</style>
