import { createApp } from 'vue'
import AdminApp from './AdminApp.vue'
import App from './App.vue'
import './style.css'
import './admin.css'

const rootComponent = window.location.pathname.startsWith('/admin') ? AdminApp : App

createApp(rootComponent).mount('#app')
