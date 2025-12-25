
# Local Learning System (HTML, CSS, JavaScript)

## Overview

This project is a **local web-based learning system** designed to teach HTML, CSS, and JavaScript through animated, step-driven lessons. The system runs entirely on the user's machine and is accessed through a browser via a local domain or `localhost`.

The system is intentionally split into **frontend** and **backend** source trees for development and version control. At runtime, however, the system behaves as a **single local web application** served by a Java backend.

The frontend is responsible for:
- Lesson rendering
- Animations
- User interaction
- Visual explanations

The backend is responsible for:
- Serving the frontend
- Persisting learner progress
- Synchronizing data when online
- Interacting with external APIs (notifications, integrations)

---

## High-Level Architecture

```

Browser (Vue.js)
|
| HTTP / WebSocket
v
Java Local Web Server
|
+-- SQLite (local persistence)
+-- Sync engine
+-- External API integrations

```

The browser is a client only.  
The Java process is the single server process.

---

## Repository Structure

```

/
├── frontend/
│   ├── content/
│   │   ├── html/
│   │   ├── css/
│   │   └── js/
│   │
│   ├── engine/
│   │   ├── lessonRunner.ts
│   │   ├── stateMachine.ts
│   │   └── animationPrimitives.ts
│   │
│   ├── renderers/
│   │   ├── web/
│   │   └── vue/
│   │
│   ├── ui/
│   │   ├── LessonPlayer.vue
│   │   ├── Timeline.vue
│   │   └── CodePanel.vue
│   │
│   └── package.json
│
├── backend/
│   ├── controllers/
│   ├── services/
│   ├── storage/
│   ├── integrations/
│   └── Main.java
│
├── README.md
├── IMPLEMENTATION.md
└── DESIGNSPEC.md

```

---

## Core Design Goals

1. **Local-first operation**
   - The system must function without internet access.
   - All learner progress is stored locally.

2. **Deterministic learning flow**
   - Lessons advance in defined steps.
   - Animations and explanations are synchronized.

3. **Separation of concerns**
   - Content is data, not logic.
   - Rendering is separate from lesson sequencing.
   - Backend logic is isolated from presentation.

4. **Long-term maintainability**
   - Lessons are atomic and reusable.
   - New renderers can be added without rewriting content.
   - Backend integrations do not affect frontend logic.

---

## Lesson Model

Lessons are defined as structured data files describing:
- A single concept
- A sequence of steps
- Associated visuals and code examples

Lessons do not contain executable logic. They are interpreted by the lesson engine.

---

## State Machine-Based Learning

The learning flow is controlled by a **finite state machine**.

Each lesson step transitions the learner through a predictable set of states such as:

- Initialization
- Explanation
- Visual emphasis
- Code display
- Interaction
- Completion

This approach ensures:
- Repeatable behavior
- Pausing and replay support
- Precise animation timing
- Clear separation between “what” and “how”

The state machine is documented in detail in `IMPLEMENTATION.md`.

---

## Runtime Behavior

At runtime:
- The frontend is built into static assets.
- The backend serves these assets over HTTP.
- API endpoints are exposed under `/api`.
- The browser loads the application from a local address.

The system can optionally be accessed via a custom local domain defined in the host system’s hosts file.

---

## Deployment Summary

- Frontend is built once.
- Frontend output is embedded into the backend.
- Backend is packaged as a single executable JAR.
- User runs the JAR and accesses the application through a browser.

No public hosting is required.


