export interface Visualization {
    type: 'highlight' | 'appear' | 'type' | 'draw' | 'none';
    target?: string; // CSS selector or element ID
    content?: string; // For 'appear' or 'type'
    params?: Record<string, any>;
}

export interface SupplementalContent {
    title: string;
    content: string;
    type: 'tip' | 'example' | 'warning'; // 'tip' | 'example' | 'warning'
}

export interface Task {
    instruction: string;
    expected?: string; // Regex or exact string matches could be complex, keeping simple for now
    starterCode?: string; // Code to preload in playground
    language?: 'html' | 'css' | 'js';
}

export interface Step {
    id: string;
    explanation: string;
    code?: string;
    visual?: Visualization;
    task?: Task; // New field for interactive tasks
    supplemental?: SupplementalContent;
    // Transitions could be handled here if we want non-linear, but sticking to linear for now
}

export interface Lesson {
    id: string;
    topic: string;
    title: string;
    steps: Step[];
}

export const EngineState = {
    INIT: 'INIT',
    EXPLAIN: 'EXPLAIN',
    ANIMATE: 'ANIMATE',
    SHOW_CODE: 'SHOW_CODE',
    WAIT_FOR_USER: 'WAIT_FOR_USER',
    COMPLETE: 'COMPLETE'
} as const;

export type EngineState = typeof EngineState[keyof typeof EngineState];
