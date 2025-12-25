/** @type {import('tailwindcss').Config} */
export default {
    content: [
        "./index.html",
        "./src/**/*.{vue,js,ts,jsx,tsx}",
    ],
    theme: {
        extend: {
            fontFamily: {
                max: ['"DM Sans"', 'sans-serif'],
                serif: ['Lora', 'serif'],
                mono: ['"IBM Plex Mono"', 'monospace'],
            },
            colors: {
                // Semantic tokens based on Design Spec
                background: 'oklch(var(--background) / <alpha-value>)',
                foreground: 'oklch(var(--foreground) / <alpha-value>)',
                primary: 'oklch(var(--primary) / <alpha-value>)',
                secondary: 'oklch(var(--secondary) / <alpha-value>)',
                muted: 'oklch(var(--muted) / <alpha-value>)',
                accent: 'oklch(var(--accent) / <alpha-value>)',
            }
        },
    },
    plugins: [],
}
