<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { lessonRunner } from '../engine/lessonRunner';

// Define the structure for our lessons
interface LessonItem {
  path: string;
  name: string;
  topic: string;
}

const isOpen = ref(true);
const selectedTopic = ref('html');
const lessons = ref<LessonItem[]>([]);

// Topics available
const topics = [
  { id: 'html', label: 'HTML', icon: 'html' },
  { id: 'css', label: 'CSS', icon: 'css' },
  { id: 'js', label: 'JS', icon: 'js' }
];

// Load lessons using import.meta.glob
const loadLessons = () => {
  // Get all yaml files in content
  const modules = import.meta.glob('/src/content/**/*.yaml');

  const items: LessonItem[] = [];

  for (const path in modules) {
    // path examples: /src/content/html/lesson-1.yaml
    const parts = path.split('/');
    // parts: ['', 'src', 'content', 'html', 'lesson-1.yaml']

    if (parts.length >= 5) {
      const topic = parts[3] || 'unknown'; // html, css, js
      const filename = parts[4] || 'unknown';
      if (topic === 'unknown' || filename === 'unknown') continue;

      const name = filename.replace('.yaml', '').replace(/-/g, ' ').replace(/\b\w/g, l => l.toUpperCase());

      // Store relative path from 'content' as assumed by lessonRunner
      // lessonRunner.load expecting 'html/lesson-1.yaml'
      const relativePath = `${topic}/${filename}`;

      items.push({
        path: relativePath,
        name: name,
        topic: topic
      });
    }
  }

  // Sort by name (or ideally by some index if available)
  items.sort((a, b) => a.name.localeCompare(b.name));

  lessons.value = items;
};

onMounted(() => {
  loadLessons();
});

const filteredLessons = computed(() => {
  return lessons.value.filter(l => l.topic === selectedTopic.value);
});

const selectLesson = (path: string) => {
  lessonRunner.load(path);
};

const toggleSidebar = () => {
  isOpen.value = !isOpen.value;
};
</script>

<template>
  <div class="flex flex-col h-full bg-card border-r border-border transition-all duration-300 ease-in-out"
    :class="isOpen ? 'w-64' : 'w-16'">
    <!-- Toggle Button (Top) -->
    <div class="p-4 flex items-center justify-between border-b border-border/50">
      <h2 v-if="isOpen" class="font-bold text-foreground tracking-tight">Clearner</h2>
      <button @click="toggleSidebar"
        class="p-1.5 hover:bg-accent rounded-md text-muted-foreground hover:text-foreground transition-colors">
        <svg v-if="isOpen" class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 19l-7-7 7-7m8 14l-7-7 7-7" />
        </svg>
        <svg v-else class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16" />
        </svg>
      </button>
    </div>

    <!-- Topic Selector (Icons) -->
    <div class="flex flex-col p-2 gap-2 border-b border-border/50">
      <button v-for="topic in topics" :key="topic.id" @click="selectedTopic = topic.id"
        class="flex items-center gap-3 p-2 rounded-lg transition-all" :class="selectedTopic === topic.id
          ? 'bg-primary/10 text-primary font-medium'
          : 'hover:bg-accent text-muted-foreground hover:text-foreground'" :title="topic.label">
        <div
          class="w-8 h-8 rounded-md flex items-center justify-center bg-background border border-border text-xs font-mono">
          <svg v-if="topic.id === 'html'" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 512 512">
            <path
              d="M108.4 0h23v22.8h21.2V0h23v69h-23V46h-21v23h-23.2M206 23h-20.3V0h63.7v23H229v46h-23m53.5-69h24.1l14.8 24.3L313.2 0h24.1v69h-23V34.8l-16.1 24.8-16.1-24.8V69h-22.6m89.2-69h23v46.2h32.6V69h-55.6" />
            <path fill="#e44d26" d="m107.6 471-33-370.4h362.8l-33 370.2L255.7 512" />
            <path fill="#f16529" d="M256 480.5V131h148.3L376 447" />
            <path fill="#ebebeb"
              d="M142 176.3h114v45.4h-64.2l4.2 46.5h60v45.3H154.4m2 22.8H202l3.2 36.3 50.8 13.6v47.4l-93.2-26" />
            <path fill="#fff"
              d="M369.6 176.3H255.8v45.4h109.6m-4.1 46.5H255.8v45.4h56l-5.3 59-50.7 13.6v47.2l93-25.8" />
          </svg>

          <svg v-if="topic.id === 'css'" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 512 512">
            <path fill="#639" d="M0 0h840a160 160 0 0 1 160 160v680a160 160 0 0 1-160 160H160A160 160 0 0 1 0 840z" />
            <path fill="#fff"
              d="M358.1 920c-64.23-.06-103.86-36.23-103.1-102.79V648.82q0-50.62 29.64-76.96c35.49-34.19 117.83-36.27 152.59.52 21.42 18.89 29.5 57.48 27.58 93.49h-73.72c.56-14.15-.19-35.58-8.51-43.65-10.81-14.63-39.36-12.91-46.91 2.32q-6.96 12.4-6.96 36.67v146.18q0 45.98 31.96 46.49 14.95 0 22.68-10.85c7.19-8.58 8.31-27.58 7.73-41.32h73.72c5.04 70.07-36.32 119.16-106.71 118.29Zm234.04 0c-71.17.98-103.01-49.66-101.04-118.29h69.59c-1.93 29.92 8.35 57.17 32.99 55.27q16.49 0 23.2-10.33c8.5-12.59 10.09-48.95-2.06-63.02-8.49-13.55-39.03-25.51-55.16-33.57q-34.54-16.53-49.75-39.26c-22.87-33.64-20.75-107.48 11.34-137.4 31.18-36.92 112.61-38.62 143.82-.77 19.25 19.51 27.66 57.9 26.03 93.23h-67.02c.57-14.52-.8-37.95-6.44-46.49q-5.93-10.85-22.42-10.85-29.38 0-29.38 35.12c.21 24.86 9.9 35.06 32.48 45.45 29.24 11.36 66.42 30.76 79.9 54.24 40.2 71.54 12.62 180.82-86.09 176.65Zm224.76 0c-71.17.98-103.01-49.66-101.04-118.29h69.59c-1.93 29.92 8.35 57.17 32.99 55.27q16.49 0 23.2-10.33c8.5-12.59 10.09-48.95-2.06-63.02-8.49-13.55-39.03-25.51-55.16-33.57q-34.54-16.53-49.75-39.26c-22.87-33.64-20.75-107.48 11.34-137.4 31.18-36.92 112.61-38.62 143.82-.77 19.25 19.51 27.66 57.9 26.03 93.23h-67.02c.57-14.52-.8-37.95-6.44-46.49q-5.93-10.85-22.42-10.85-29.38 0-29.38 35.12c.21 24.86 9.9 35.06 32.48 45.45 29.24 11.36 66.42 30.76 79.9 54.24 40.2 71.54 12.62 180.82-86.09 176.65Z" />
          </svg>

          <svg v-if="topic.id === 'js'" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 630 630">
            <rect width="630" height="630" fill="#f7df1e" />
            <path
              d="m423.2 492.19c12.69 20.72 29.2 35.95 58.4 35.95 24.53 0 40.2-12.26 40.2-29.2 0-20.3-16.1-27.49-43.1-39.3l-14.8-6.35c-42.72-18.2-71.1-41-71.1-89.2 0-44.4 33.83-78.2 86.7-78.2 37.64 0 64.7 13.1 84.2 47.4l-46.1 29.6c-10.15-18.2-21.1-25.37-38.1-25.37-17.34 0-28.33 11-28.33 25.37 0 17.76 11 24.95 36.4 35.95l14.8 6.34c50.3 21.57 78.7 43.56 78.7 93 0 53.3-41.87 82.5-98.1 82.5-54.98 0-90.5-26.2-107.88-60.54zm-209.13 5.13c9.3 16.5 17.76 30.45 38.1 30.45 19.45 0 31.72-7.61 31.72-37.2v-201.3h59.2v202.1c0 61.3-35.94 89.2-88.4 89.2-47.4 0-74.85-24.53-88.81-54.075z" />
          </svg>
        </div>
        <span v-if="isOpen" class="text-sm">{{ topic.label }}</span>
      </button>
    </div>

    <!-- Lesson List -->
    <div class="flex-1 overflow-y-auto p-2">
      <div v-if="isOpen" class="space-y-1">
        <button v-for="lesson in filteredLessons" :key="lesson.path" @click="selectLesson(lesson.path)"
          class="w-full text-left px-3 py-2 rounded-md text-sm transition-colors truncate" :class="lessonRunner.currentLesson.value?.id === lesson.path /* This comparison might need adjustment depending on how id is stored */
            ? 'bg-accent text-foreground font-medium'
            : 'text-muted-foreground hover:bg-accent/50 hover:text-foreground'">
          {{ lesson.name }}
        </button>
        <div v-if="filteredLessons.length === 0" class="px-3 py-2 text-xs text-muted-foreground italic">
          No lessons found
        </div>
      </div>
    </div>

    <!-- User/Footer (Optional) -->
    <div class="p-4 border-t border-border/50" v-if="isOpen">
      <!-- Footer content if any -->
    </div>
  </div>
</template>
