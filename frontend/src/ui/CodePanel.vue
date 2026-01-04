<script setup lang="ts">
import Prism from 'prismjs';
import 'prismjs/themes/prism-tomorrow.css'; // Use a dark theme by default, or match needed style
import { computed, ref } from 'vue';

// Import languages you need
import 'prismjs/components/prism-css';
import 'prismjs/components/prism-javascript';
import 'prismjs/components/prism-markup'; // HTML

const props = defineProps<{
  code?: string;
  visible: boolean;
  language?: string; // Add language prop if needed, default to html/markup
}>();

const highlightedCode = computed(() => {
  if (!props.code) return '';
  const lang = props.language || 'html';
  // Map 'html' to 'markup' for Prism
  const prismLang = lang === 'html' ? 'markup' : lang === 'js' ? 'javascript' : lang;

  if (Prism.languages[prismLang]) {
    return Prism.highlight(props.code, Prism.languages[prismLang], prismLang);
  }
  return props.code; // Fallback to plain text
});

const copied = ref(false);

const copyCode = async () => {
  if (props.code) {
    await navigator.clipboard.writeText(props.code);
    copied.value = true;
    setTimeout(() => {
      copied.value = false;
    }, 2000);
  }
};

const lineCount = computed(() => {
  return (props.code || '').split('\n').length;
});
</script>

<template>
  <div class="h-full flex flex-col bg-card rounded-xl shadow-sm border border-border overflow-hidden">
    <!-- Header -->
    <div
      class="flex items-center justify-between bg-gradient-to-r from-muted to-muted/80 px-4 py-3 border-b border-border">
      <div class="flex items-center space-x-3 min-w-0">
        <div class="flex items-center space-x-2 ml-3 min-w-0">
          <svg class="w-4 h-4 text-primary shrink-0" fill="currentColor" viewBox="0 0 20 20">
            <path fill-rule="evenodd"
              d="M12.316 3.051a1 1 0 01.633 1.265l-4 12a1 1 0 11-1.898-.632l4-12a1 1 0 011.265-.633zM5.707 6.293a1 1 0 010 1.414L3.414 10l2.293 2.293a1 1 0 11-1.414 1.414l-3-3a1 1 0 010-1.414l3-3a1 1 0 011.414 0zm8.586 0a1 1 0 011.414 0l3 3a1 1 0 010 1.414l-3 3a1 1 0 11-1.414-1.414L16.586 10l-2.293-2.293a1 1 0 010-1.414z"
              clip-rule="evenodd" />
          </svg>
          <span class="text-sm font-medium text-foreground truncate">Code Example</span>
          <span class="text-xs text-muted-foreground px-1.5 py-0.5 bg-card rounded" v-if="visible && code">
            {{ lineCount }} line{{ lineCount !== 1 ? 's' : '' }}
          </span>
        </div>
      </div>

      <button @click="copyCode"
        class="flex items-center space-x-2 px-3 py-1.5 text-xs font-medium text-foreground hover:text-primary bg-card hover:bg-accent/10 border border-border hover:border-accent/30 rounded-md transition-all duration-200 shrink-0"
        :disabled="!code">
        <svg v-if="!copied" class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
            d="M8 16H6a2 2 0 01-2-2V6a2 2 0 012-2h8a2 2 0 012 2v2m-6 12h8a2 2 0 002-2v-8a2 2 0 00-2-2h-8a2 2 0 00-2 2v8a2 2 0 002 2z" />
        </svg>
        <svg v-else class="w-4 h-4 text-primary" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
        </svg>
        <span class="hidden sm:inline">{{ copied ? 'Copied!' : 'Copy Code' }}</span>
        <span class="sm:hidden">{{ copied ? '✓' : 'Copy' }}</span>
      </button>
    </div>

    <!-- Code Content -->
    <div class="flex-1 overflow-auto bg-muted/30 transition-all duration-300"
      :class="{ 'opacity-100': visible, 'opacity-0': !visible }">
      <div v-if="visible && code" class="flex h-full">
        <!-- Line numbers -->
        <div
          class="flex-shrink-0 py-4 px-3 text-right text-muted-foreground text-sm font-mono select-none bg-card/50 border-r border-border">
          <div v-for="n in lineCount" :key="n" class="leading-6">
            {{ n }}
          </div>
        </div>

        <!-- Code content -->
        <div class="flex-1 overflow-auto">
          <pre
            class="py-4 px-4 text-sm font-mono leading-6 text-foreground whitespace-pre"><code class="block" v-html="highlightedCode"></code></pre>
        </div>
      </div>

      <!-- Empty state -->
      <div v-else class="h-full flex items-center justify-center p-8">
        <div class="text-center px-6 py-8 max-w-sm">
          <svg class="w-14 h-14 text-muted-foreground/50 mx-auto mb-4" fill="none" stroke="currentColor"
            viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
              d="M10 20l4-16m4 4l4 4-4 4M6 16l-4-4 4-4" />
          </svg>
          <p class="text-muted-foreground text-sm">
            Code examples will appear here. Study them carefully to understand how each piece works!
          </p>
        </div>
      </div>
    </div>

    <!-- Footer with helpful info -->
    <div class="bg-card/50 px-4 py-2 border-t border-border">
      <div class="flex items-center justify-between text-xs text-muted-foreground">
        <span class="flex items-center space-x-2 truncate">
          <svg class="w-4 h-4 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z" />
          </svg>
          <span class="truncate">Try to understand the code before copying</span>
        </span>
        <span class="shrink-0 hidden sm:block" v-if="visible && code">{{ lineCount }} lines</span>
      </div>
    </div>
  </div>
</template>