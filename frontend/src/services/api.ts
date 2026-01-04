import { ref } from 'vue'

const API_BASE = 'http://localhost:8080/api'

export interface User {
    id: string
    displayName: string
    createdAt: number
    lastActiveAt: number
    syncStatus: string
}

export interface Progress {
    lessonId: string
    currentStep: number
    completed: boolean
}

class AuthService {
    private currentUser = ref<User | null>(null)

    async login(displayName: string): Promise<User | null> {
        try {
            const response = await fetch(`${API_BASE}/auth/login`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ displayName })
            })

            if (response.ok) {
                const user = await response.json()
                this.currentUser.value = user
                localStorage.setItem('clearner_user', JSON.stringify(user))
                return user
            }
        } catch (error) {
            console.error('Login failed:', error)
        }
        return null
    }

    loadStoredUser(): User | null {
        const stored = localStorage.getItem('clearner_user')
        if (stored) {
            try {
                const user = JSON.parse(stored)
                this.currentUser.value = user
                return user
            } catch (e) {
                console.error('Failed to parse stored user:', e)
            }
        }
        return null
    }

    getUser() {
        return this.currentUser
    }

    logout() {
        this.currentUser.value = null
        localStorage.removeItem('clearner_user')
    }
}

class ProgressService {
    async saveProgress(userId: string, lessonId: string, currentStep: number, completed: boolean) {
        if (!userId) {
            console.error('Cannot save progress: userId is missing or null')
            return
        }

        try {
            const response = await fetch(`${API_BASE}/progress`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ userId, lessonId, currentStep, completed })
            })

            if (!response.ok) {
                const errorText = await response.text()
                console.error('Failed to save progress:', response.status, errorText)
            }
        } catch (error) {
            console.error('Failed to save progress:', error)
        }
    }

    async getProgress(userId: string): Promise<Progress[]> {
        try {
            const response = await fetch(`${API_BASE}/progress/${userId}`)
            if (response.ok) {
                return await response.json()
            }
        } catch (error) {
            console.error('Failed to fetch progress:', error)
        }
        return []
    }

    async logPaste(userId: string, lessonId: string, stepIndex: number) {
        try {
            await fetch(`${API_BASE}/log/paste`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ userId, lessonId, stepIndex })
            })
        } catch (error) {
            console.error('Failed to log paste:', error)
        }
    }
}

export const authService = new AuthService()
export const progressService = new ProgressService()
