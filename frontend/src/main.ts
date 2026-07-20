import { createApp } from 'vue'
import AdminApp from './AdminApp.vue'
import App from './App.vue'
import OrganizerApp from './OrganizerApp.vue'
import './style.css'
import './admin.css'
import './organizer.css'

const rootComponent = window.location.pathname.startsWith('/admin')
  ? AdminApp
  : window.location.pathname.startsWith('/organizer')
    ? OrganizerApp
    : App

createApp(rootComponent).mount('#app')
