<template>
  <div ref="wrapRef" class="cad-viewer" :style="viewerStyle">
    <div v-if="!modelData" class="cad-viewer__empty">
      <span>{{ emptyText }}</span>
    </div>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import * as THREE from 'three'
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls.js'
import { STLLoader } from 'three/examples/jsm/loaders/STLLoader.js'

const props = defineProps({
  modelData: {
    type: [ArrayBuffer, null],
    default: null
  },
  emptyText: {
    type: String,
    default: '请先生成 CAD 模型'
  },
  height: {
    type: String,
    default: '500px'
  }
})

const wrapRef = ref(null)
const viewerStyle = computed(() => ({
  height: props.height,
  minHeight: props.height
}))
const hasLoadedModel = ref(false)
let renderer
let scene
let camera
let controls
let mesh
let frameId
let resizeObserver

function initScene() {
  if (!wrapRef.value || renderer) return

  scene = new THREE.Scene()
  scene.background = new THREE.Color(0xc3cad4)

  camera = new THREE.PerspectiveCamera(45, 1, 0.1, 5000)
  camera.position.set(520, -520, 260)

  renderer = new THREE.WebGLRenderer({ antialias: true, preserveDrawingBuffer: true })
  renderer.setPixelRatio(window.devicePixelRatio || 1)
  renderer.outputColorSpace = THREE.SRGBColorSpace
  wrapRef.value.appendChild(renderer.domElement)

  controls = new OrbitControls(camera, renderer.domElement)
  controls.enableDamping = true
  controls.dampingFactor = 0.08

  scene.add(new THREE.HemisphereLight(0xffffff, 0x8fa3b8, 2.2))
  const keyLight = new THREE.DirectionalLight(0xffffff, 2.2)
  keyLight.position.set(240, -320, 420)
  scene.add(keyLight)

  const fillLight = new THREE.DirectionalLight(0xdde8ff, 1.1)
  fillLight.position.set(-420, 260, 220)
  scene.add(fillLight)

  resizeObserver = new ResizeObserver(resize)
  resizeObserver.observe(wrapRef.value)
  resize()
  animate()
}

function resize() {
  if (!wrapRef.value || !renderer || !camera) return
  const width = wrapRef.value.clientWidth || 1
  const height = wrapRef.value.clientHeight || 1
  renderer.setSize(width, height, false)
  camera.aspect = width / height
  camera.updateProjectionMatrix()
}

function animate() {
  frameId = requestAnimationFrame(animate)
  controls?.update()
  renderer?.render(scene, camera)
}

function captureImage(type = 'image/png') {
  if (!renderer || !scene || !camera || !mesh || !hasLoadedModel.value) return ''
  controls?.update()
  renderer.render(scene, camera)
  return renderer.domElement.toDataURL(type)
}

function clearModel() {
  hasLoadedModel.value = false
  if (!mesh) return
  scene.remove(mesh)
  mesh.geometry?.dispose()
  mesh.material?.dispose()
  mesh = null
}

function loadModel(buffer) {
  initScene()
  clearModel()
  if (!buffer || !scene) return

  const loader = new STLLoader()
  const geometry = loader.parse(buffer)
  geometry.computeVertexNormals()
  geometry.center()

  const box = new THREE.Box3().setFromBufferAttribute(geometry.attributes.position)
  const size = new THREE.Vector3()
  box.getSize(size)
  const maxSize = Math.max(size.x, size.y, size.z, 1)

  const material = new THREE.MeshStandardMaterial({
    color: 0x565f70,
    metalness: 0.22,
    roughness: 0.36
  })
  mesh = new THREE.Mesh(geometry, material)
  scene.add(mesh)
  hasLoadedModel.value = true

  camera.position.set(maxSize * 0.72, -maxSize * 0.68, maxSize * 0.34)
  camera.zoom = 1.28
  camera.near = Math.max(maxSize / 1000, 0.1)
  camera.far = maxSize * 10
  camera.updateProjectionMatrix()
  controls.target.set(0, 0, 0)
  controls.update()
}

onMounted(() => {
  initScene()
  loadModel(props.modelData)
})

watch(() => props.modelData, value => {
  loadModel(value)
})

onBeforeUnmount(() => {
  if (frameId) cancelAnimationFrame(frameId)
  resizeObserver?.disconnect()
  controls?.dispose()
  clearModel()
  renderer?.dispose()
  renderer?.domElement?.remove()
})

defineExpose({
  captureImage,
  hasModel: () => hasLoadedModel.value
})
</script>

<style scoped>
.cad-viewer {
  position: relative;
  min-height: 0;
  overflow: hidden;
  border: 1px solid #e1e7ef;
  border-radius: 6px;
  background: #c3cad4;
}

.cad-viewer :deep(canvas) {
  display: block;
  width: 100%;
  height: 100%;
}

.cad-viewer__empty {
  position: absolute;
  inset: 0;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #7a8da3;
  font-size: 14px;
  pointer-events: none;
}
</style>
