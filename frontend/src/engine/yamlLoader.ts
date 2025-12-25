import yaml from 'js-yaml';
import type { Lesson } from './types';

export async function loadLesson(path: string): Promise<Lesson> {
    // In a real local-web setup, we might fetch this via HTTP from the backend
    // or import it if using Vite's explicit import features (e.g. import.meta.glob).
    // For now, let's assume we can fetch it via fetch() if it's served in public or via an API.
    // The 'content' directory is in 'src', so Vite might need configuration to serve it as raw,
    // OR we can use dynamic imports if we know the path structure.

    // Strategy: We will use Vite's ?raw import for known paths if possible, 
    // or basic fetch if served from public. 
    // Given the structure, let's assume we are fetching from a served endpoint or properly configured Vite asset.

    // HOWEVER, simplified approach for this stage: 
    // We'll use a fetch to a hypothetical endpoint that serves these, 
    // OR we can rely on Vite's `import.meta.glob` to bundle them.

    // Let's go with `import.meta.glob` as it's the most "Vite-native" way to load src assets dynamically 
    // without a backend logic change right now.

    const modules = import.meta.glob('/src/content/**/*.yaml', { query: '?raw', import: 'default' });

    // Construct the key. path is expected to be relative like 'html/lesson-1.yaml'
    const key = `/src/content/${path}`;

    if (!modules[key]) {
        throw new Error(`Lesson not found: ${path}`);
    }

    const rawYaml = await modules[key]() as string;
    return yaml.load(rawYaml) as Lesson;
}
