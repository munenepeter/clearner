<script setup lang="ts">
import { computed, onMounted, watch } from 'vue';
import { lessonRunner } from '../engine/lessonRunner';
import { EngineState } from '../engine/types';
import { useUserStateStore } from '../stores/userState';
import CodePanel from './CodePanel.vue';
import InstructionPanel from './InstructionPanel.vue';
import Visualizer from './Visualizer.vue';

const userState = useUserStateStore();

onMounted(() => {
  lessonRunner.load('html/lesson-1-structure.yaml');
  userState.currentLessonId = 'html/lesson-1-structure.yaml';
  userState.lastStepReached = 'step-1';
});

const runner = lessonRunner;

// Watch for step changes to update user state
watch(() => runner.currentStep.value, (newStep) => {
  if (newStep?.id) {
    userState.lastStepReached = newStep.id;
    userState.sessionStats.totalStepsAttempted++;
  }
});

// Watch for task completion
watch(() => runner.taskCompleted.value, (completed) => {
  if (completed && runner.currentStep.value?.id) {
    userState.completeStep(runner.currentStep.value.id);
  }
});

const canGoNext = computed(() => {
  if (runner.currentStep.value?.task) {
    return runner.taskCompleted.value;
  }
  return true;
});

const isTaskMode = computed(() => {
  return !!runner.currentStep.value?.task;
});

const canGoPrevious = computed(() => {
  return runner.currentStepIndex.value > 0;
});

const progressPercentage = computed(() => {
  const total = runner.currentLesson.value?.steps.length || 1;
  const current = runner.currentStepIndex.value + 1;
  return (current / total) * 100;
});

// Calculate completion percentage based on user state
const userProgressPercentage = computed(() => {
  const total = runner.currentLesson.value?.steps.length || 0;
  if (total === 0) return 0;

  const completed = runner.currentLesson.value?.steps.filter(step =>
    userState.isStepCompleted(step.id)
  ).length || 0;

  return (completed / total) * 100;
});
</script>

<template>
  <div class="flex flex-col h-screen bg-linear-to-br from-background via-secondary to-accent/10 font-sans">
    <!-- Top Navigation Bar -->
    <div class="bg-card border-b border-border shadow-sm sticky top-0 z-10">
      <div class="max-w-full mx-auto px-4 sm:px-6 py-3">
        <div class="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3">
          <!-- User & Lesson Info -->
          <div class="flex items-center space-x-4 min-w-0 flex-1">
            <!-- User Profile (Simple) -->
            <div
              class="hidden md:flex items-center space-x-2 px-3 py-1.5 bg-accent/20 rounded-full border border-accent/20">
              <div
                class="w-6 h-6 rounded-full bg-linear-to-br from-primary to-primary/80 flex items-center justify-center text-xs text-primary-foreground font-bold">
                {{ userState.username.charAt(0).toUpperCase() }}
              </div>
              <span class="text-sm font-medium text-foreground truncate max-w-[100px]">
                {{ userState.username }}
              </span>
            </div>

            <div class="h-8 w-px bg-border hidden md:block"></div>

            <!-- Context Info -->
            <div class="min-w-0 flex-1 flex flex-col justify-center">
              <div class="flex items-center space-x-2 text-xs text-muted-foreground">
                <span class="font-semibold text-primary uppercase tracking-wider text-[10px]">
                  {{ runner.currentLesson.value?.topic || 'Course' }}
                </span>
                <span>/</span>
                <span class="truncate">
                  Concept: {{ runner.currentStep.value?.title || runner.currentStep.value?.id || 'Loading...' }}
                </span>
              </div>
              <h1 class="text-sm sm:text-base font-bold text-foreground truncate leading-tight">
                {{ runner.currentLesson.value?.title || 'Interactive Lesson' }}
              </h1>
            </div>
          </div>

          <!-- Progress & Controls -->
          <div class="flex flex-col sm:flex-row items-start sm:items-center gap-3 w-full sm:w-auto">
            <!-- Progress Info -->
            <div class="flex items-center space-x-3 w-full sm:w-auto">
              <div class="text-right min-w-[80px]">
                <div class="text-sm font-semibold text-foreground">
                  Step {{ runner.currentStepIndex.value + 1 }} of {{ runner.currentLesson.value?.steps.length || 0 }}
                </div>
                <div class="text-xs text-muted-foreground">
                  {{ Math.round(userProgressPercentage) }}% Mastered
                </div>
              </div>
              <div class="flex-1 sm:w-32">
                <div class="w-full h-2 bg-muted rounded-full overflow-hidden">
                  <div
                    class="h-full bg-gradient-to-r from-primary to-primary/80 rounded-full transition-all duration-500 ease-out"
                    :style="{ width: progressPercentage + '%' }"></div>
                </div>
                <div class="w-full h-1 mt-1 bg-muted/30 rounded-full overflow-hidden">
                  <div
                    class="h-full bg-gradient-to-r from-accent to-accent/70 rounded-full transition-all duration-700 ease-out"
                    :style="{ width: userProgressPercentage + '%' }"></div>
                </div>
              </div>
            </div>

            <!-- Navigation Buttons -->
            <div class="flex items-center space-x-2 w-full sm:w-auto">
              <button @click="runner.previousStep()" :disabled="!canGoPrevious"
                class="flex-1 sm:flex-none px-4 py-2 text-sm font-medium text-foreground bg-card border border-border rounded-lg hover:bg-accent hover:border-accent disabled:opacity-40 disabled:cursor-not-allowed disabled:hover:bg-card disabled:hover:border-border transition-all duration-200 flex items-center justify-center space-x-1">
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
                </svg>
                <span class="hidden sm:inline">Previous</span>
              </button>

              <button @click="runner.next()" :disabled="!canGoNext"
                class="flex-1 sm:flex-none px-4 py-2 text-sm font-medium text-primary-foreground bg-gradient-to-r from-primary to-primary/90 rounded-lg hover:from-primary/90 hover:to-primary shadow-sm hover:shadow disabled:opacity-40 disabled:cursor-not-allowed disabled:hover:from-primary disabled:hover:to-primary/90 transition-all duration-200 flex items-center justify-center space-x-1">
                <span>{{ runner.currentStep.value?.task && !runner.taskCompleted.value ? 'Complete Task' : 'Next'
                }}</span>
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
                </svg>
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Main Content Area -->
    <div class="flex-1 overflow-hidden p-3 sm:p-4 lg:p-6">
      <div class="h-full grid grid-cols-1 lg:grid-cols-2 gap-4 lg:gap-6">

        <!-- Standard Layout: Instructions + Visualizer (Left), Code (Right) -->
        <template v-if="!isTaskMode">
          <!-- Left Panel -->
          <div class="flex flex-col gap-4 lg:gap-6 h-full min-h-0">
            <!-- Instruction Panel -->
            <div class="flex-shrink-0 min-h-0"
              :class="runner.currentStep.value?.visual ? 'h-auto max-h-[40%]' : 'h-full'">
              <InstructionPanel :text="runner.currentStep.value?.explanation" :topic="runner.currentLesson.value?.topic"
                :supplemental="runner.currentStep.value?.supplemental" />
            </div>

            <!-- Visualizer -->
            <div v-if="runner.currentStep.value?.visual" class="flex-1 min-h-0">
              <Visualizer :visual="runner.currentStep.value?.visual" :task="runner.currentStep.value?.task" />
            </div>
          </div>

          <!-- Right Panel: Code Display -->
          <div class="h-full min-h-0">
            <CodePanel :code="runner.currentStep.value?.code" :visible="runner.state.value === EngineState.SHOW_CODE ||
              runner.state.value === EngineState.WAIT_FOR_USER ||
              runner.state.value === EngineState.COMPLETE" />
          </div>
        </template>

        <!-- Task Layout: Instructions + Code (Left), Playground (Right) -->
        <template v-else>
          <!-- Left Panel -->
          <div class="flex flex-col gap-4 lg:gap-6 h-full min-h-0">
            <!-- Instruction Panel -->
            <div class="flex-shrink-0 max-h-[40%] min-h-0">
              <InstructionPanel :text="runner.currentStep.value?.explanation" :topic="runner.currentLesson.value?.topic"
                :supplemental="runner.currentStep.value?.supplemental" />
            </div>

            <!-- Code Panel (Reference) -->
            <div class="flex-1 min-h-0">
              <CodePanel :code="runner.currentStep.value?.code" :visible="true" />
            </div>
          </div>

          <!-- Right Panel: Playground -->
          <div class="h-full min-h-0">
            <Visualizer :visual="runner.currentStep.value?.visual" :task="runner.currentStep.value?.task" />
          </div>
        </template>

      </div>
    </div>

    <!-- Bottom Status Bar -->
    <div class="bg-card border-t border-border px-4 py-2">
      <div class="flex items-center justify-between text-xs">
        <div class="flex items-center space-x-4 text-muted-foreground">
          <div class="flex items-center space-x-1">
            <div :class="[
              'w-2 h-2 rounded-full',
              runner.currentStep.value?.task
                ? runner.taskCompleted.value
                  ? 'bg-green-500 animate-pulse'
                  : runner.taskFailed.value
                    ? 'bg-red-500 animate-bounce'
                    : 'bg-amber-500'
                : 'bg-primary'
            ]"></div>
            <span>
              {{ runner.currentStep.value?.task
                ? runner.taskCompleted.value
                  ? 'Task completed!'
                  : runner.taskFailed.value
                    ? runner.failMessage.value || 'Not quite right yet'
                    : 'Task pending'
                : 'Learning mode' }}
            </span>
          </div>
          <span class="hidden sm:inline">
            {{ userState.sessionStats.totalStepsCompleted }} steps mastered
          </span>
        </div>
        <div class="text-muted-foreground">
          <span class="hidden sm:inline">Press </span>Ctrl+Enter<span class="hidden sm:inline"> to run code</span>
        </div>
      </div>
    </div>
  </div>
</template>