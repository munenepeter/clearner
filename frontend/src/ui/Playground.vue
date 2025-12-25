<script setup lang="ts">
import { onMounted, ref, watch } from 'vue';

const props = defineProps<{
  initialCode?: string;
  language?: 'html' | 'css' | 'js';
}>();

const emit = defineEmits<{
  (e: 'update:code', code: string): void;
  (e: 'run', code: string): void;
}>();

const code = ref(props.initialCode || '');
const iframeRef = ref<HTMLIFrameElement | null>(null);
const isRunning = ref(false);

watch(() => props.initialCode, (newCode) => {
  if (newCode !== undefined && newCode !== code.value) {
    code.value = newCode;
    runCode();
  }
});

function runCode() {
  if (!iframeRef.value) return;

  isRunning.value = true;
  
  const doc = iframeRef.value.contentWindow?.document;
  if (!doc) {
    isRunning.value = false;
    return;
  }

  doc.open();
  
  let content = '';
  
  if (props.language === 'html' || !props.language) {
    content = code.value;
  } else if (props.language === 'css') {
    content = `
      <!DOCTYPE html>
      <html>
      <head>
        <style>${code.value}</style>
      </head>
      <body>
        <h1>CSS Playground</h1>
        <p>Style me!</p>
        <div class="box">Box</div>
      </body>
      </html>
    `;
  } else if (props.language === 'js') {
    content = `
      <!DOCTYPE html>
      <html>
      <body>
        <h1>JS Playground</h1>
        <div id="output"></div>
        <script>
          const output = document.getElementById('output');
          console.log = function(msg) {
            const div = document.createElement('div');
            div.textContent = '> ' + msg;
            output.appendChild(div);
          };
          ${code.value}
        <\/script>
      </body>
      </html>
    `;
  }

  doc.write(content);
  doc.close();
  
  setTimeout(() => {
    isRunning.value = false;
  }, 300);
  
  emit('update:code', code.value);
  emit('run', code.value);
}

onMounted(() => {
  if (code.value) {
    runCode();
  }
});
</script>

<template>
  <div class="h-full flex flex-col bg-white rounded-xl shadow-md border border-gray-200 overflow-hidden">
    <!-- Toolbar -->
    <div class="bg-gradient-to-r from-slate-600 to-zinc-600 px-4 py-3 flex items-center justify-between border-b border-slate-700">
      <div class="flex items-center space-x-3">
        <div class="flex items-center justify-center w-8 h-8 bg-white/20 rounded-lg backdrop-blur-sm">
          <svg class="w-5 h-5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 20l4-16m4 4l4 4-4 4M6 16l-4-4 4-4" />
          </svg>
        </div>
        <div>
          <span class="text-sm font-bold text-white">Code Playground</span>
          <span class="ml-2 px-2 py-0.5 bg-white/20 backdrop-blur-sm rounded text-xs font-medium text-white">
            {{ language?.toUpperCase() || 'HTML' }}
          </span>
        </div>
      </div>
      
      <button 
        @click="runCode" 
        :disabled="isRunning"
        class="flex items-center space-x-2 px-4 py-2 bg-white hover:bg-gray-100 text-slate-600 font-semibold text-sm rounded-lg shadow-md transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed"
      >
        <svg v-if="!isRunning" class="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
          <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM9.555 7.168A1 1 0 008 8v4a1 1 0 001.555.832l3-2a1 1 0 000-1.664l-3-2z" clip-rule="evenodd" />
        </svg>
        <svg v-else class="w-4 h-4 animate-spin" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
        </svg>
        <span>{{ isRunning ? 'Running...' : 'Run Code' }}</span>
      </button>
    </div>

    <!-- Editor & Preview Split -->
    <div class="flex-1 flex flex-col lg:flex-row min-h-0">
      <!-- Editor Section -->
      <div class="flex-1 flex flex-col border-b lg:border-b-0 lg:border-r border-gray-200 bg-gray-50">
        <div class="bg-gray-100 px-4 py-2 border-b border-gray-200 flex items-center justify-between">
          <span class="text-xs font-semibold text-gray-600 uppercase tracking-wider flex items-center space-x-2">
            <svg class="w-4 h-4 text-gray-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
            </svg>
            <span>Your Code</span>
          </span>
          <span class="text-xs text-gray-500">{{ code.split('\n').length }} lines</span>
        </div>
        
        <textarea 
          v-model="code" 
          class="flex-1 w-full p-4 font-mono text-sm bg-gray-900 text-gray-100 resize-none focus:outline-none focus:ring-2 focus:ring-inset focus:ring-slate-500"
          placeholder="Type your code here..."
          spellcheck="false"
        ></textarea>
      </div>
      
      <!-- Preview Section -->
      <div class="flex-1 flex flex-col bg-white">
        <div class="bg-gray-100 px-4 py-2 border-b border-gray-200 flex items-center justify-between">
          <span class="text-xs font-semibold text-gray-600 uppercase tracking-wider flex items-center space-x-2">
            <svg class="w-4 h-4 text-gray-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
            </svg>
            <span>Live Preview</span>
          </span>
          <div class="flex items-center space-x-2">
            <div class="w-2 h-2 rounded-full bg-green-500 animate-pulse"></div>
            <span class="text-xs text-green-600 font-medium">Live</span>
          </div>
        </div>
        
        <div class="flex-1 relative bg-white overflow-auto">
          <iframe 
            ref="iframeRef" 
            class="w-full h-full border-none"
            title="Preview"
            sandbox="allow-scripts allow-same-origin"
          ></iframe>
        </div>
      </div>
    </div>

    <!-- Footer with helpful tips -->
    <div class="bg-gray-50 px-4 py-2 border-t border-gray-200">
      <div class="flex items-center justify-between text-xs text-gray-600">
        <div class="flex items-center space-x-4">
          <span class="flex items-center space-x-1">
            <svg class="w-3 h-3" fill="currentColor" viewBox="0 0 20 20">
              <path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z" clip-rule="evenodd" />
            </svg>
            <span>Edit the code and click "Run Code" to see changes</span>
          </span>
        </div>
        <span class="text-gray-400">Press Ctrl+Enter to run</span>
      </div>
    </div>
  </div>
</template>