<script setup lang="ts">
import { computed } from 'vue';
import type { SupplementalContent } from '../engine/types';

const props = defineProps<{
  text?: string;
  topic?: string;
  supplemental?: SupplementalContent;
}>();

const formattedText = computed(() => {
  if (!props.text) return '';
  let content = props.text
    // Bold
    .replace(/\*\*(.*?)\*\*/g, '<strong class="font-semibold text-primary">$1</strong>')
    // Inline code
    .replace(/`(.*?)`/g, '<code class="bg-muted px-1.5 py-0.5 rounded text-sm font-mono text-red-500">$1</code>');
  return content;
});
</script>

<template>
  <div class="bg-card rounded-xl shadow-sm border border-border overflow-hidden h-full flex flex-col">
    <!-- Content -->
    <div class="p-5 flex-1 overflow-y-auto">
      <div class="text-base leading-relaxed text-foreground prose prose-sm max-w-none">
        <div v-if="text" class="whitespace-pre-wrap" v-html="formattedText"></div>
        <div v-else class="space-y-4">
          <div class="h-4 bg-muted rounded animate-pulse w-3/4"></div>
          <div class="h-4 bg-muted rounded animate-pulse"></div>
          <div class="h-4 bg-muted rounded animate-pulse w-5/6"></div>
        </div>
      </div>
    </div>

    <!-- Supplemental Content (Tip/Example) -->
    <div v-if="supplemental" class="px-3 pb-3 flex-shrink-0">
      <div class="flex items-start space-x-3 bg-accent/5 border border-accent/20 rounded-lg p-4">
        <div class="flex-shrink-0 mt-0.5">
          <!-- Tip Icon -->
          <svg v-if="supplemental.type === 'tip'" class="w-5 h-5 text-accent-foreground" fill="currentColor" viewBox="0 0 20 20">
            <path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z" clip-rule="evenodd" />
          </svg>
          <!-- Example Icon -->
          <svg v-else-if="supplemental.type === 'example'" class="w-5 h-5 text-accent-foreground" fill="none" stroke="currentColor" viewBox="0 0 24 24">
             <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19.428 15.428a2 2 0 00-1.022-.547l-2.384-.477a6 6 0 00-3.86.517l-.318.158a6 6 0 01-3.86.517L6.05 15.21a2 2 0 00-1.806.547M8 4h8l-1 1v5.172a2 2 0 00.586 1.414l5 5c1.26 1.26.367 3.414-1.415 3.414H4.828c-1.782 0-2.674-2.154-1.414-3.414l5-5A2 2 0 009 10.172V5L8 4z" />
          </svg>
          <!-- Warning Icon -->
          <svg v-else class="w-5 h-5 text-yellow-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
          </svg>
        </div>
        <div class="flex-1 min-w-0">
          <p class="text-sm font-medium text-accent-foreground truncate">{{ supplemental.title }}</p>
          <p class="text-xs text-accent-foreground/80 mt-1 whitespace-pre-wrap">{{ supplemental.content }}</p>
        </div>
      </div>
    </div>
  </div>
</template>