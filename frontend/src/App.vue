<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { authService } from './services/api';
import { useUserStateStore } from './stores/userState';
import LessonPlayer from './ui/LessonPlayer.vue';
import Sidebar from './ui/Sidebar.vue';

const userState = useUserStateStore();
const showLoginModal = ref(false);
const displayName = ref('');

const isLoginError = ref(false);
const isAuthChecked = ref(false);

// Only allow access when user is authenticated
const isAuthenticated = computed(() => userState.userId !== null);

onMounted(async () => {
  // Try to load stored user
  const storedUser = authService.loadStoredUser();
  if (storedUser) {
    userState.setUser(storedUser);
    isAuthChecked.value = true;
  } else {
    // Show login modal
    showLoginModal.value = true;
    isAuthChecked.value = true;
  }
});

const handleLogin = async () => {
  if (displayName.value.trim()) {
    isLoginError.value = false;
    const user = await authService.login(displayName.value.trim());
    if (user) {
      userState.setUser(user);
      showLoginModal.value = false;
    } else {
      isLoginError.value = true;
    }

  }
};
</script>

<template>
  <div class="flex h-screen overflow-hidden bg-linear-to-br from-background via-secondary to-accent/20">
    <!-- Login Modal -->
    <div v-if="showLoginModal" class="fixed inset-0 bg-black/70 flex items-center justify-center z-50 backdrop-blur-sm">
      <div class="bg-card border border-border rounded-lg shadow-2xl p-8 max-w-md w-full mx-4">
        <h2 class="text-2xl font-bold text-foreground mb-2">Welcome to Clearner</h2>
        <p class="text-muted-foreground mb-6">Enter your name to get started</p>
        <form @submit.prevent="handleLogin">
          <input v-model="displayName" type="text" placeholder="Your name" autofocus required
            class="w-full px-4 py-3 bg-background border border-border rounded-lg text-foreground placeholder-muted-foreground focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent mb-4" />
          <button type="submit" :disabled="isLoginError"
            class="w-full px-4 py-3 bg-gradient-to-r from-primary to-primary/90 text-primary-foreground font-medium rounded-lg hover:from-primary/90 hover:to-primary shadow-sm hover:shadow transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed">
            Start Learning
          </button>
          <div v-if="isLoginError" class="text-red-500 text-xs italic mt-2">
            Uhm, that's not right, something went wrong. Please try again later or contact support.
          </div>
        </form>
      </div>
    </div>

    <!-- Only render content when authenticated -->
    <template v-if="isAuthenticated && isAuthChecked">
      <Sidebar />
      <main class="flex-1 flex flex-col relative overflow-hidden">
        <LessonPlayer />
      </main>
    </template>
  </div>
</template>