# Design Specification

This document defines the visual system, color palette, typography, and layout constraints for the learning interface.

The design prioritizes:
- Readability
- Visual stability
- Low cognitive load
- Long session comfort

Tailwind CSS is used as the primary styling system.

---

## Color System

The system uses OKLCH color space for perceptual consistency.

### Semantic Tokens

- Background
- Foreground
- Primary
- Secondary
- Accent
- Muted
- Destructive
- Border
- Ring

Both light and dark themes are supported via CSS variables.

Theme switching must not alter layout or spacing.

---

## Typography

### Font Families

- Sans: `DM Sans`
- Serif: `Lora`
- Monospace: `IBM Plex Mono`

### Usage Rules

- Sans: UI text, labels, navigation
- Serif: Long-form explanations
- Monospace: Code blocks and inline code

Line length for explanations should not exceed 75 characters.

---

## Spacing and Layout

- Base spacing unit: `0.24rem`
- Layouts must follow a vertical rhythm
- Avoid dense stacking of controls

---

## Radius and Shape

- Global radius: `0.15rem`
- Corners are subtle and consistent
- No exaggerated rounding

---

## Shadows

Shadows are used sparingly to indicate hierarchy.

Rules:
- No shadows on body background
- Cards may use `shadow-sm` or `shadow`
- Avoid stacking multiple shadows

---

## UI Constraints

- No animated layout shifts
- Animations must be purposeful
- Color is not the sole indicator of state
- Focus states must be visible

---

## Code Presentation

- Monospace font only
- Clear contrast against background
- Syntax highlighting must not overpower text

---

## Accessibility

- Minimum contrast ratios must be respected
- Focus outlines must remain visible
- Font scaling must not break layout

---

## Tailwind Integration

Design tokens are mapped via CSS variables and exposed to Tailwind through theme configuration.

Direct hex values are not permitted in components.
All colors must reference semantic tokens.

---

## Visual Consistency Rules

- Same action = same color everywhere
- Same spacing rules across all screens
- No component-specific palettes

---

## Final Note

This design system is intentionally restrained.  
Consistency and clarity take precedence over visual novelty.
