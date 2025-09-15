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

		<div v-if="printStatus" class="status">{{ printStatus }}</div>
	</div>
</template>

<script setup>
import { registerPlugin } from '@capacitor/core'
import { ref } from 'vue'

// 注册插件
const USBPrintPlugin = registerPlugin('USBPrintPlugin')

const devices = ref([])
const printStatus = ref('')

// 获取设备列表
const getUSBDevices = async () => {
	try {
		const result = await USBPrintPlugin.getDevices()
		devices.value = result.devices || []
		printStatus.value = `找到 ${devices.value.length} 台设备`
	} catch (e) {
		printStatus.value = '获取设备失败：' + e.message
	}
}

// 测试打印
const printTest = async () => {
	if (devices.value.length === 0) {
		printStatus.value = '请先获取设备列表'
		return
	}

	try {
		// 使用第一个设备打印
		const target = devices.value[0]
		await USBPrintPlugin.printText({
			vid: target.vid,
			pid: target.pid,
			text: '测试打印：Hello USB Printer!\n这是第二行内容\n中文测试：打印成功',
		})
		printStatus.value = '打印成功！'
	} catch (e) {
		printStatus.value = '打印失败：' + e.message
	}
}
</script>

<style scoped>
.usb-printer {
	padding: 20px;
}

button {
	margin: 0 10px 10px 0;
	padding: 8px 16px;
	cursor: pointer;
}

button:disabled {
	opacity: 0.6;
	cursor: not-allowed;
}

.status {
	margin: 10px 0;
	padding: 8px;
	background-color: #f0f0f0;
	border-radius: 4px;
}

.connected {
	color: green;
	font-weight: bold;
}

.disconnected {
	color: red;
}

.target-tag {
	margin-left: 10px;
	color: blue;
	font-size: 0.8em;
}

.print-status {
	margin-top: 10px;
	padding: 8px;
	color: #333;
}

ul {
	margin: 10px 0;
	padding-left: 20px;
}

li {
	margin: 5px 0;
}
</style>
