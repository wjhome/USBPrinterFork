<template>
	<div class="usb-printer">
		<button @click="getUSBDevices">获取USB设备列表</button>
		<button @click="printTest" :disabled="!isTargetDeviceConnected">测试打印（目标设备）</button>

		<!-- 目标设备连接状态 -->
		<div class="status">
			目标设备 (VID:1317, PID:42151) 状态:
			<span :class="isTargetDeviceConnected ? 'connected' : 'disconnected'">
				{{ isTargetDeviceConnected ? '已连接' : '未连接' }}
			</span>
		</div>

		<!-- 设备列表 -->
		<div v-if="devices.length">
			<h3>已连接设备：</h3>
			<ul>
				<li v-for="device in devices" :key="`${device.vid}-${device.pid}`">
					VID: {{ device.vid }}, PID: {{ device.pid }}, 名称: {{ device.name }}
					<span v-if="device.vid === 1317 && device.pid === 42151" class="target-tag">（目标设备）</span>
				</li>
			</ul>
		</div>

		<!-- 打印状态 -->
		<div class="print-status" v-if="printStatus">{{ printStatus }}</div>
	</div>
</template>

<script setup>
import { registerPlugin } from '@capacitor/core'
import { ref, onMounted } from 'vue'

const USBPrintPlugin = registerPlugin('USBPrintPlugin')

const devices = ref([])
const printStatus = ref('')
const isTargetDeviceConnected = ref(false) // 目标设备状态

// 获取设备列表并检测目标设备
const getUSBDevices = async () => {
	try {
		const result = await USBPrintPlugin.getDevices()
		devices.value = result.devices || []
		console.log('设备列表:', devices.value)

		// 检测目标设备是否存在
		isTargetDeviceConnected.value = devices.value.some((device) => device.vid === 1317 && device.pid === 42151)
		printStatus.value = isTargetDeviceConnected.value ? '发现目标设备' : '未发现目标设备'
	} catch (e) {
		console.error('获取设备失败:', e)
		printStatus.value = `获取设备失败: ${e.message}`
		isTargetDeviceConnected.value = false
	}
}

// 测试打印（仅使用目标设备）
const printTest = async () => {
	if (!isTargetDeviceConnected.value) {
		printStatus.value = '目标设备未连接'
		return
	}

	try {
		const targetDevice = devices.value.find((d) => d.vid === 1317 && d.pid === 42151)
		await USBPrintPlugin.printText({
			vid: targetDevice.vid,
			pid: targetDevice.pid,
			text: `扫描内容: ${data.value || '无'}\n测试打印时间: ${new Date().toLocaleString()}`,
		})
		printStatus.value = '打印成功!'
	} catch (e) {
		console.error('打印失败:', e)
		printStatus.value = `打印失败: ${e.message}`
	}
}

// 监听设备变化（新增：定期刷新设备列表）
onMounted(() => {
	getUSBDevices()
	// 每5秒刷新一次设备状态
	setInterval(getUSBDevices, 5000)
})
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
