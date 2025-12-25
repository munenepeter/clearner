import { computed, ref } from 'vue';
import type { Lesson } from './types';
import { EngineState } from './types';
import { loadLesson } from './yamlLoader';

export class LessonRunner {
    // Reactive state management
    state = ref<EngineState>(EngineState.INIT);
    currentLesson = ref<Lesson | null>(null);
    currentStepIndex = ref(0);

    // Task state
    userCode = ref('');
    taskCompleted = ref(false);
    taskFailed = ref(false);
    failMessage = ref('');

    // Computed properties helpers
    currentStep = computed(() => {
        if (!this.currentLesson.value) return null;
        return this.currentLesson.value.steps[this.currentStepIndex.value] || null;
    });

    isLastStep = computed(() => {
        if (!this.currentLesson.value) return false;
        return this.currentStepIndex.value >= this.currentLesson.value.steps.length - 1;
    });

    async load(lessonPath: string) {
        this.state.value = EngineState.INIT;
        try {
            this.currentLesson.value = await loadLesson(lessonPath);
            this.currentStepIndex.value = 0;
            this.startStep();
        } catch (e) {
            console.error("Failed to load lesson", e);
            // Handle error state if needed
        }
    }

    private startStep() {
        if (!this.currentStep.value) return;

        // Reset task state
        this.taskCompleted.value = false;
        this.taskFailed.value = false;
        this.failMessage.value = '';
        this.userCode.value = this.currentStep.value.task?.starterCode || '';

        // Transition mechanism
        this.state.value = EngineState.EXPLAIN;

        // Auto-advance logic could go here, for now we will be manual or triggered by UI
        // For the "Explain -> Animate -> Code" flow:

        setTimeout(() => {
            if (this.state.value === EngineState.EXPLAIN) {
                this.state.value = EngineState.ANIMATE;

                // Let animation run for a bit, then show code
                // In a real app, the Visualizer component might emit an event "animationComplete"
                // For now, we simulate a delay or let the user click "Next"
            }
        }, 2000); // minimal auto-advance for demo
    }

    next() {
        // State transitions based on current state
        switch (this.state.value) {
            case EngineState.EXPLAIN:
                this.state.value = EngineState.ANIMATE;
                break;
            case EngineState.ANIMATE:
                this.state.value = EngineState.SHOW_CODE;
                break;
            case EngineState.SHOW_CODE:
                this.state.value = EngineState.WAIT_FOR_USER;
                break;
            case EngineState.WAIT_FOR_USER:
            case EngineState.COMPLETE:
                this.nextStep();
                break;
        }
    }

    // Force advance to next step (skipping internal states)
    nextStep() {
        if (this.isLastStep.value) {
            this.state.value = EngineState.COMPLETE;
        } else {
            this.currentStepIndex.value++;
            this.startStep();
        }
    }

    checkTask(code: string) {
        this.userCode.value = code;

        // Reset both states first
        this.taskCompleted.value = false;
        this.taskFailed.value = false;
        this.failMessage.value = '';

        const task = this.currentStep.value?.task;

        if (!task) return;

        if (task.expected) {
            if (code.includes(task.expected)) {
                this.taskCompleted.value = true;
            } else {
                this.taskFailed.value = true;
                this.failMessage.value = "Hmm, that's odd";
            }
        } else {
            // If no expected string, just mark as completed
            this.taskCompleted.value = true;
        }
    }

    previousStep() {
        if (this.currentStepIndex.value > 0) {
            this.currentStepIndex.value--;
            this.startStep();
        }
    }
}

// Singleton instance if needed, or can be instantiated per player
export const lessonRunner = new LessonRunner();
