import { computed, ref } from 'vue';
import type { Lesson } from './types';
import { EngineState } from './types';

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

    async load(lessonId: string) {
        this.state.value = EngineState.INIT;
        try {
            // Fetch from backend
            const response = await fetch(`http://localhost:8080/api/lessons/${lessonId}`);
            if (!response.ok) throw new Error("Failed to fetch lesson");
            const lessonData = await response.json();

            // TODO: Validate/Parse lessonData if needed, for now assuming it matches Lesson type
            this.currentLesson.value = lessonData as Lesson;

            this.currentStepIndex.value = 0;
            this.loadProgress(); // Restore progress if any
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
            case EngineState.ANIMATE:
                // Fast forward to show code + wait for user
                this.state.value = EngineState.SHOW_CODE;
                // In this optimized flow, we might want to just go to complete/next step
                // depending on if there is a task.
                // If there is no task, we can just go to next step?
                // Let's stick to the plan: consolidated state.
                this.state.value = EngineState.WAIT_FOR_USER;
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
            this.saveProgress();
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

            //check with regex
            let regex = new RegExp(task.expected);
            let match = regex.exec(code);
            if (match) {
                this.taskCompleted.value = true;
                this.saveProgress(); // Save on successful task completion too?
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

    // --- Persistence ---

    saveProgress() {
        const progress = {
            lessonId: this.currentLesson.value?.id, // Assuming lesson has ID, or use loaded ID
            stepIndex: this.currentStepIndex.value,
            timestamp: new Date().toISOString()
        };

        // 1. Save to Cookie
        document.cookie = `user_progress=${JSON.stringify(progress)}; path=/; max-age=31536000`; // 1 year

        // 2. Sync to Backend
        fetch('http://localhost:8080/api/progress', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(progress)
        }).catch(err => console.error("Failed to sync progress", err));
    }

    loadProgress() {
        // Load from Cookie
        const match = document.cookie.match(new RegExp('(^| )user_progress=([^;]+)'));
        if (match) {
            try {
                // Parse but don't use yet if not needed, or just log
                // const progress = JSON.parse(decodeURIComponent(match[2]));
                console.log("Found progress cookie, logic to restore state can be added here.");
            } catch (e) {
                console.error("Failed to parse progress cookie", e);
            }
        }
        // Ideally we fetch from backend too, but cookie is faster for locally returning user.
    }
}

// Singleton instance if needed, or can be instantiated per player
export const lessonRunner = new LessonRunner();
