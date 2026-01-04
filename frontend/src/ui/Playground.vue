<script setup lang="ts">
import Prism from 'prismjs';
import 'prismjs/components/prism-css';
import 'prismjs/components/prism-javascript';
import 'prismjs/components/prism-markup';
import 'prismjs/themes/prism-tomorrow.css';
import { computed, onMounted, ref, watch } from 'vue';

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
const textareaRef = ref<HTMLTextAreaElement | null>(null);
const preRef = ref<HTMLPreElement | null>(null);
const lineNumbersRef = ref<HTMLDivElement | null>(null);
const isRunning = ref(false);
const showPasteAlert = ref(false);
const showNoVisibleContentWarning = ref(false);

const lineCount = computed(() => code.value.split('\n').length);

const highlightedCode = computed(() => {
  const lang = props.language || 'html';
  const prismLang = lang === 'html' ? 'markup' : lang === 'js' ? 'javascript' : lang;

  if (Prism.languages[prismLang]) {
    // We need to escape HTML entities for the pre block manually if we were just dumping text,
    // but Prism.highlight returns HTML.
    // However, we must ensure that the trailing newline is preserved visually.
    const highlighted = Prism.highlight(code.value, Prism.languages[prismLang], prismLang);
    // Add a zero-width space if the code ends with a newline, to ensure the last line is rendered
    return code.value.endsWith('\n') ? highlighted + '<br>' : highlighted;
  }
  return code.value // Fallback
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;") + (code.value.endsWith('\n') ? '<br>' : '');
});

watch(() => props.initialCode, (newCode) => {
  if (newCode !== undefined && newCode !== code.value) {
    code.value = newCode;
    runCode();
  }
});

function handlePaste(e: ClipboardEvent) {
  // Log paste attempt to backend
  const content = e.clipboardData?.getData('text') || '';
  fetch('http://localhost:8080/api/log/paste', {
    method: 'POST',
    body: content
  }).catch(err => console.error("Failed to log paste", err));

  // Prevent paste
  e.preventDefault();

  // Show alert
  showPasteAlert.value = true;

  // Hide alert after 3 seconds
  setTimeout(() => {
    showPasteAlert.value = false;
  }, 4000);
}

function handleScroll() {
  if (textareaRef.value) {
    const scrollTop = textareaRef.value.scrollTop;
    if (lineNumbersRef.value) {
      lineNumbersRef.value.scrollTop = scrollTop;
    }
    if (preRef.value) {
      preRef.value.scrollTop = scrollTop;
    }
  }
}

function runCode() {
  if (!iframeRef.value) return;

  isRunning.value = true;
  showNoVisibleContentWarning.value = false;

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
    content = /*html*/`
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

    content = /*html*/ `
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
      </html>`;
  }


  const tempDiv = document.createElement('div');
  tempDiv.innerHTML = content;

  // Clear document and write new content
  doc.write(content);
  doc.close();

  // Check for visible content after a short delay to allow rendering
  setTimeout(() => {
    if (props.language === 'html' || !props.language) {
      const bodyText = doc.body?.innerText?.trim() || '';
      // Also check if there are any visual elements that might not have text (like images or colored divs)
      // For now, a simple text check + child check is a reasonable heuristic for "invisible"
      const hasChildren = doc.body?.children?.length > 0;

      // Heuristic: If body is empty of text AND has no significant children, it might be "invisible"
      // But user specifically asked about checks for things "not in the body" like title
      // If the code contains <title> but body is empty, that's a good signal.

      if (!bodyText && !hasChildren && code.value.trim().length > 0) {
        showNoVisibleContentWarning.value = true;
      }
    }
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
    <div
      class="bg-linear-to-r from-slate-600 to-zinc-600 px-4 py-3 flex items-center justify-between border-b border-slate-700">
      <div class="flex items-center space-x-3">
        <div class="flex items-center justify-center w-8 h-8 bg-white/20 rounded-lg backdrop-blur-sm">
          <svg class="w-5 h-5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
              d="M10 20l4-16m4 4l4 4-4 4M6 16l-4-4 4-4" />
          </svg>
        </div>
        <div>
          <span class="text-sm font-bold text-white">Code Playground</span>
          <span class="ml-2 px-2 py-0.5 bg-white/20 backdrop-blur-sm rounded text-xs font-medium text-white">
            {{ language?.toUpperCase() || 'HTML' }}
          </span>
        </div>
      </div>

      <button @click="runCode" :disabled="isRunning"
        class="flex items-center space-x-2 px-4 py-2 bg-white hover:bg-gray-100 text-slate-600 font-semibold text-sm rounded-lg shadow-md transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed">
        <svg v-if="!isRunning" class="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
          <path fill-rule="evenodd"
            d="M10 18a8 8 0 100-16 8 8 0 000 16zM9.555 7.168A1 1 0 008 8v4a1 1 0 001.555.832l3-2a1 1 0 000-1.664l-3-2z"
            clip-rule="evenodd" />
        </svg>
        <svg v-else class="w-4 h-4 animate-spin" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
            d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
        </svg>
        <span>{{ isRunning ? 'Running...' : 'Run Code' }}</span>
      </button>
    </div>

    <!-- Editor & Preview Split -->
    <div class="flex-1 flex flex-col lg:flex-row min-h-0 relative">
      <!-- Paste Alert Overlay -->
      <div v-if="showPasteAlert" class="absolute inset-x-0 top-4 z-50 flex justify-center pointer-events-none">
        <div
          class="bg-amber-500 text-white px-6 py-3 rounded-lg shadow-lg flex items-center space-x-3 pointer-events-auto animate-bounce">
          <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
              d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
          </svg>
          <div>
            <p class="font-bold">No Pasting Allowed!</p>
            <p class="text-xs opacity-90">Type it out yourself - you'll grasp the concept faster.</p>
          </div>
        </div>
      </div>

      <!-- Editor Section -->
      <div
        class="flex-1 flex flex-col border-b lg:border-b-0 lg:border-r border-gray-200 bg-gray-50 min-h-0 h-1/2 lg:h-full">
        <div class="bg-gray-100 px-4 py-2 border-b border-gray-200 flex items-center justify-between shrink-0">
          <span class="text-xs font-semibold text-gray-600 uppercase tracking-wider flex items-center space-x-2">
            <svg class="w-4 h-4 text-gray-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
            </svg>
            <span>Your Code</span>
          </span>
          <span class="text-xs text-gray-500">{{ code.split('\n').length }} lines</span>
        </div>

        <div class="flex-1 flex overflow-hidden relative">
          <!-- Line Numbers -->
          <div ref="lineNumbersRef"
            class="w-10 bg-gray-200 border-r border-gray-300 pt-4 pb-4 px-2 text-right select-none overflow-hidden text-gray-400 font-mono text-sm leading-6">
            <div v-for="n in lineCount" :key="n">{{ n }}</div>
          </div>

          <!-- Editor Container -->
          <!-- Editor Container -->
          <div class="flex-1 relative font-mono text-sm leading-6 bg-[#2d2d2d]">
            <!-- Highlight Overlay (Back Layer) -->
            <pre ref="preRef"
              class="absolute inset-0 p-4 m-0 overflow-hidden pointer-events-none whitespace-pre break-normal text-gray-100 z-0"
              aria-hidden="true"
              style="font-family: monospace;"><code class="block font-mono text-sm leading-6" v-html="highlightedCode"></code></pre>

            <!-- Textarea (Front Layer) -->
            <textarea ref="textareaRef" v-model="code" @scroll="handleScroll" @paste="handlePaste"
              class="absolute inset-0 w-full h-full p-4 bg-transparent text-transparent caret-white resize-none focus:outline-none focus:ring-0 ring-0 leading-6 whitespace-pre z-10 placeholder:text-gray-500"
              style="color: transparent; background: transparent; -webkit-text-fill-color: transparent;"
              placeholder="Type your code here..." spellcheck="false"></textarea>
          </div>
        </div>
      </div>

      <!-- Preview Section -->
      <div class="flex-1 flex flex-col bg-white min-h-0 h-1/2 lg:h-full relative">
        <div class="bg-gray-100 px-4 py-2 border-b border-gray-200 flex items-center justify-between shrink-0">
          <span class="text-xs font-semibold text-gray-600 uppercase tracking-wider flex items-center space-x-2">
            <svg class="w-4 h-4 text-gray-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
            </svg>
            <span>Live Preview</span>
          </span>
          <div class="flex items-center space-x-2">
            <div class="w-2 h-2 rounded-full bg-green-500 animate-pulse"></div>
            <span class="text-xs text-green-600 font-medium">Live</span>
          </div>
        </div>

        <div class="flex-1 relative bg-white overflow-hidden">
          <iframe ref="iframeRef" class="w-full h-full border-none" title="Preview"
            sandbox="allow-scripts allow-same-origin"></iframe>

          <!-- No Visible Content Warning -->
          <div v-if="showNoVisibleContentWarning"
            class="absolute bottom-4 right-4 max-w-xs bg-blue-50 border-blue-400 p-4 shadow-lg rounded animate-fade-in-up transition-all duration-300">
            <div class="flex">
              <div class="shrink-0">
                <svg class="h-5 w-5 text-blue-400" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20"
                  fill="currentColor">
                  <path fill-rule="evenodd"
                    d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z"
                    clip-rule="evenodd" />
                </svg>
              </div>
              <div class="ml-3">
                <p class="text-sm text-blue-700">
                  <span class="font-bold">Hey:</span> everything is working, you are not seeing anything cause tags like
                  &lt;title&gt; don't appear in the
                  page body.
                </p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Footer with helpful tips -->
    <div class="bg-gray-50 px-4 py-2 border-t border-gray-200 shrink-0">
      <div class="flex items-center justify-between text-xs text-gray-600">
        <div class="flex items-center space-x-4">
          <span class="flex items-center space-x-1">
            <svg class="w-3 h-3" fill="currentColor" viewBox="0 0 20 20">
              <path fill-rule="evenodd"
                d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z"
                clip-rule="evenodd" />
            </svg>
            <span>Edit the code and click "Run Code" to see changes</span>
          </span>
        </div>
        <span class="text-gray-400">Press Ctrl+Enter to run</span>
      </div>
    </div>
  </div>
</template>