import { ref, computed } from 'vue'
import { defineStore } from 'pinia'

export const useUserStateStore = defineStore('userState', () => {
  // Track which steps user has completed
  const completedSteps = ref<Set<string>>(new Set())
  
  // Track current lesson progress
  const currentLessonId = ref<string>('')
  const lastStepReached = ref<string>('')
  
  // User preferences
  const preferences = ref({
    autoRun: true,
    showHints: true,
    fontSize: 'medium' as 'small' | 'medium' | 'large'
  })
  
  // Session statistics
  const sessionStats = ref({
    totalStepsAttempted: 0,
    totalStepsCompleted: 0,
    timeSpent: 0, // in seconds
    lastVisit: new Date()
  })
  
  // Mark step as completed
  const completeStep = (stepId: string) => {
    completedSteps.value.add(stepId)
    sessionStats.value.totalStepsCompleted++
  }
  
  // Check if step is completed
  const isStepCompleted = (stepId: string) => {
    return completedSteps.value.has(stepId)
  }
  
  // Reset progress for a lesson
  const resetLessonProgress = (lessonId: string) => {
    if (currentLessonId.value === lessonId) {
      completedSteps.value.clear()
      sessionStats.value.totalStepsAttempted = 0
    }
  }
  
  // Calculate overall progress percentage
  const progressPercentage = computed(() => {
    if (sessionStats.value.totalStepsAttempted === 0) return 0
    return (sessionStats.value.totalStepsCompleted / sessionStats.value.totalStepsAttempted) * 100
  })
  
  return {
    completedSteps,
    currentLessonId,
    lastStepReached,
    preferences,
    sessionStats,
    completeStep,
    isStepCompleted,
    resetLessonProgress,
    progressPercentage
  }
})