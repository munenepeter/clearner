<script setup lang="ts">
import { lessonRunner } from '../engine/lessonRunner';
import type { Task, Visualization } from '../engine/types';
import Playground from './Playground.vue';

defineProps<{
    visual?: Visualization;
    task?: Task;
}>();

function onRun(code: string) {
    lessonRunner.checkTask(code);
}
</script>

<template>
    <div class="h-full">
        <!-- Task Mode: Show Playground with Task Banner -->
        <div v-if="task" class="h-full flex flex-col space-y-3">
            <!-- Task Banner -->
            <div class="bg-chart-4 border-2 rounded-xl p-4 transition-all duration-300"
                :class="lessonRunner.taskCompleted.value ? 'border-primary/50' : 'border-accent/50'">
                <div class="flex items-start justify-between gap-3">
                    <div class="flex items-start space-x-3 flex-1 min-w-0">
                        <div class="flex-shrink-0 w-8 h-8 rounded-lg flex items-center justify-center transition-all duration-300 shadow-sm"
                            :class="lessonRunner.taskCompleted.value ? 'bg-primary text-primary-foreground' : 'bg-accent text-accent-foreground'">
                            <svg v-if="!lessonRunner.taskCompleted.value" class="w-4 h-4" fill="none"
                                stroke="currentColor" viewBox="0 0 24 24">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                    d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                            </svg>
                            <svg v-else class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                    d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                            </svg>
                        </div>
                        <div class="flex-1 min-w-0">
                            <div class="flex items-center space-x-2 mb-1">
                                <span class="text-xs font-bold uppercase tracking-wider"
                                    :class="lessonRunner.taskCompleted.value ? 'text-primary' : 'text-accent'">
                                    {{ lessonRunner.taskCompleted.value ? '✓ Task Completed' : 'Your Turn!' }}
                                </span>
                            </div>
                            <p class="text-sm leading-relaxed break-words"
                                :class="lessonRunner.taskCompleted.value ? 'text-accent' : 'text-accent'">
                                {{ task.instruction }}
                            </p>
                        </div>
                    </div>

                    <!-- Success Badge -->
                    <div v-if="lessonRunner.taskCompleted.value"
                        class="flex items-center space-x-2 px-3 py-1.5 bg-primary text-primary-foreground rounded-lg shadow-sm animate-pulse shrink-0">
                        <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
                            <path fill-rule="evenodd"
                                d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z"
                                clip-rule="evenodd" />
                        </svg>
                        <span class="text-xs font-bold">Success!</span>
                    </div>
                </div>
            </div>

            <!-- Playground -->
            <div class="flex-1 min-h-0">
                <Playground :initialCode="task.starterCode" @run="onRun" :language="task.language || 'html'" />
            </div>
        </div>

        <!-- Normal Visualizer Mode: Browser Preview -->
        <div v-else class="bg-card rounded-xl shadow-sm border border-border h-full flex flex-col overflow-hidden">
            <!-- Browser Chrome -->
            <div class="bg-muted border-b border-border px-4 py-2.5 flex items-center space-x-3">
                <div class="flex items-center space-x-4 shrink-0 text-muted-foreground/40">
                    <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5"
                        stroke="currentColor" class="size-3">
                        <path stroke-linecap="round" stroke-linejoin="round" d="M10.5 19.5 3 12m0 0 7.5-7.5M3 12h18" />
                    </svg>

                    <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5"
                        stroke="currentColor" class="size-3">
                        <path stroke-linecap="round" stroke-linejoin="round" d="M13.5 4.5 21 12m0 0-7.5 7.5M21 12H3" />
                    </svg>

                    <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none"
                        stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"
                        class="size-3" data-v-c80f2bde="">
                        <path d="M21 12a9 9 0 1 1-9-9c2.52 0 4.93 1 6.74 2.74L21 8"></path>
                        <path d="M21 3v5h-5"></path>
                    </svg>
                </div>

                <div
                    class="flex-1 flex items-center bg-card border border-border rounded-2xl px-3 py-1.5 text-xs text-muted-foreground overflow-hidden">
                    <svg class="w-3 h-3 text-muted-foreground mr-2 shrink-0" fill="none" stroke="currentColor"
                        viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                            d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
                    </svg>
                    <span class="font-mono truncate">learn-to-code.dev/preview</span>
                </div>
            </div>

            <!-- Content Area -->
            <div class="flex-1 bg-card overflow-auto relative">
                <!-- Visualization Types -->
                <div v-if="visual?.type === 'highlight'" class="absolute inset-0 flex items-center justify-center p-4">
                    <div class="text-center animate-pulse">
                        <div
                            class="inline-flex flex-col sm:flex-row items-center space-y-3 sm:space-y-0 sm:space-x-3 px-6 py-4 bg-primary/10 border-2 border-primary/30 rounded-lg max-w-full">
                            <svg class="w-8 h-8 text-primary shrink-0" fill="currentColor" viewBox="0 0 20 20">
                                <path
                                    d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z" />
                            </svg>
                            <div class="text-center sm:text-left">
                                <div class="text-sm font-semibold text-primary mb-1">Focus On</div>
                                <span class="text-lg font-bold text-foreground break-words">{{ visual.target }}</span>
                            </div>
                        </div>
                    </div>
                </div>

                <div v-else-if="visual?.type === 'appear'"
                    class="absolute inset-0 flex items-center justify-center p-4">
                    <div class="text-center animate-bounce">
                        <div
                            class="inline-flex flex-col sm:flex-row items-center space-y-3 sm:space-y-0 sm:space-x-3 px-6 py-4 bg-primary/10 border-2 border-accent/30 rounded-lg max-w-full">
                            <svg class="w-8 h-8 text-primary shrink-0" fill="currentColor" viewBox="0 0 20 20">
                                <path fill-rule="evenodd"
                                    d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z"
                                    clip-rule="evenodd" />
                            </svg>
                            <div class="text-center">
                                <div class="text-sm font-semibold text-accent-foreground mb-1">New Code</div>
                                <span class="text-lg font-bold text-accent-foreground font-mono break-all">{{ visual.content
                                }}</span>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Default Empty Browser View -->
                <div v-else class="h-full flex items-center justify-center p-8">
                    <div class="text-center max-w-md">
                        <div
                            class="w-20 h-20 mx-auto mb-6 bg-primary/10 rounded-2xl flex items-center justify-center">
                            <svg class="w-10 h-10 text-primary" fill="none" stroke="currentColor"
                                viewBox="0 0 24 24">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                                    d="M9.75 17L9 20l-1 1h8l-1-1-.75-3M3 13h18M5 17h14a2 2 0 002-2V5a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
                            </svg>
                        </div>
                        <h3 class="text-lg font-semibold text-foreground mb-2">Visual Preview</h3>
                        <p class="text-sm text-muted-foreground">
                            Interactive examples and visual outputs will appear here as you learn. This helps you see
                            how your code creates real web pages!
                        </p>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>