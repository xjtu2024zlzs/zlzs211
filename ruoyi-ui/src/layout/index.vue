<template>
  <div
    :class="classObj"
    class="app-wrapper"
    :style="wrapperStyle"
  >
    <div
      v-if="device === 'mobile' && sidebar.opened"
      class="drawer-bg"
      @click="handleClickOutside"
    />

    <sidebar
      v-if="!sidebar.hide"
      class="sidebar-container"
    />

    <!-- 左侧菜单拖拽条 -->
    <div
      v-if="!sidebar.hide && sidebar.opened && device !== 'mobile'"
      class="sidebar-resizer"
      @mousedown="startResize"
    />

    <div
      :class="{ hasTagsView: needTagsView, sidebarHide: sidebar.hide }"
      class="main-container"
    >
      <div :class="{ 'fixed-header': fixedHeader }">
        <navbar @setLayout="setLayout" />
        <tags-view v-if="needTagsView" />
      </div>

      <app-main />
      <settings ref="settingRef" />
    </div>
  </div>
</template>

<script setup>
import { computed, ref, watch, watchEffect, onMounted, onBeforeUnmount } from 'vue'
import { useWindowSize } from '@vueuse/core'
import Sidebar from './components/Sidebar/index.vue'
import { AppMain, Navbar, Settings, TagsView } from './components'
import useAppStore from '@/store/modules/app'
import useSettingsStore from '@/store/modules/settings'

const appStore = useAppStore()
const settingsStore = useSettingsStore()

const theme = computed(() => settingsStore.theme)
const sidebar = computed(() => appStore.sidebar)
const device = computed(() => appStore.device)
const needTagsView = computed(() => settingsStore.tagsView)
const fixedHeader = computed(() => settingsStore.fixedHeader)

const classObj = computed(() => ({
  hideSidebar: !sidebar.value.opened,
  openSidebar: sidebar.value.opened,
  withoutAnimation: sidebar.value.withoutAnimation,
  mobile: device.value === 'mobile'
}))

/**
 * 左侧菜单宽度
 * 默认 300px
 * 拖动后保存到 localStorage
 */
const sidebarWidth = ref(Number(localStorage.getItem('sidebarWidth')) || 300)

const wrapperStyle = computed(() => {
  return {
    '--sidebar-width': sidebarWidth.value + 'px',
    '--current-color': theme.value,
    '--current-color-light': theme.value + '1a',
    '--current-color-dark-bg': theme.value + '33'
  }
})

let resizing = false

function startResize(e) {
  resizing = true
  document.body.style.cursor = 'col-resize'
  document.body.style.userSelect = 'none'
  e.preventDefault()
}

function onMouseMove(e) {
  if (!resizing) return

  const minWidth = 220
  const maxWidth = 420

  let width = e.clientX
  width = Math.max(minWidth, Math.min(maxWidth, width))

  sidebarWidth.value = width
}

function onMouseUp() {
  if (!resizing) return

  resizing = false
  localStorage.setItem('sidebarWidth', sidebarWidth.value)
  document.body.style.cursor = ''
  document.body.style.userSelect = ''
}

onMounted(() => {
  window.addEventListener('mousemove', onMouseMove)
  window.addEventListener('mouseup', onMouseUp)
})

onBeforeUnmount(() => {
  window.removeEventListener('mousemove', onMouseMove)
  window.removeEventListener('mouseup', onMouseUp)
})

const { width } = useWindowSize()
const WIDTH = 992 // refer to Bootstrap's responsive design

watch(() => device.value, () => {
  if (device.value === 'mobile' && sidebar.value.opened) {
    appStore.closeSideBar({ withoutAnimation: false })
  }
})

watchEffect(() => {
  if (width.value - 1 < WIDTH) {
    appStore.toggleDevice('mobile')
    appStore.closeSideBar({ withoutAnimation: true })
  } else {
    appStore.toggleDevice('desktop')
  }
})

function handleClickOutside() {
  appStore.closeSideBar({ withoutAnimation: false })
}

const settingRef = ref(null)

function setLayout() {
  settingRef.value.openSetting()
}
</script>

<style lang="scss" scoped>
@use "@/assets/styles/mixin.scss" as mix;
@use "@/assets/styles/variables.module.scss" as vars;

.app-wrapper {
  @include mix.clearfix;
  position: relative;
  height: 100%;
  width: 100%;

  &.mobile.openSidebar {
    position: fixed;
    top: 0;
  }
}

.main-container:has(.fixed-header) {
  height: 100vh;
  overflow: hidden;
}

.drawer-bg {
  background: #000;
  opacity: 0.3;
  width: 100%;
  top: 0;
  height: 100%;
  position: absolute;
  z-index: 999;
}

/* 左侧菜单拖拽条 */
.sidebar-resizer {
  position: fixed;
  left: calc(var(--sidebar-width, 300px) - 3px);
  top: 0;
  width: 6px;
  height: 100vh;
  cursor: col-resize;
  z-index: 1002;
  background: transparent;
}

.sidebar-resizer:hover {
  background: rgba(64, 158, 255, 0.18);
}

.fixed-header {
  position: fixed;
  top: 0;
  right: 0;
  z-index: 9;
  width: calc(100% - var(--sidebar-width, 300px));
  transition: width 0.28s;
}

.hideSidebar .fixed-header {
  width: calc(100% - 54px);
}

.sidebarHide .fixed-header {
  width: 100%;
}

.mobile .fixed-header {
  width: 100%;
}

.mobile .sidebar-resizer {
  display: none;
}
</style>