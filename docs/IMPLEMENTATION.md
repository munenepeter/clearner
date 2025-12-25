# Implementation Notes

This document describes how the system is implemented conceptually, using pseudocode and structural explanations rather than language-specific details.

---

## Lesson Data Structure

Each lesson is defined as a structured document.

```

Lesson {
id
topic
concept
steps[]
}

```

Each step represents a single instructional action.

```

Step {
explanationText
visualAction
codeSample
mappings
}

```

---

## Lesson Engine

The lesson engine is responsible for:
- Loading lessons
- Advancing steps
- Coordinating animations
- Managing learner interaction

---

## State Machine

The engine operates as a finite state machine.

### States

```

INIT
EXPLAIN
ANIMATE
SHOW_CODE
WAIT_FOR_USER
COMPLETE

```

### State Transition Logic (Pseudocode)

```

state = INIT

while state != COMPLETE:
switch state:
case INIT:
loadLesson()
state = EXPLAIN

```
case EXPLAIN:
  displayExplanation()
  state = ANIMATE

case ANIMATE:
  runVisualAnimation()
  state = SHOW_CODE

case SHOW_CODE:
  renderCodeSample()
  state = WAIT_FOR_USER

case WAIT_FOR_USER:
  waitForNext()
  if lastStep:
    state = COMPLETE
  else:
    state = EXPLAIN
```

```

---

## Animation Primitives

Animations are not custom per lesson.  
Lessons declare intent, and the engine executes predefined primitives.

Examples:

```

highlight(element)
typeText(code, speed)
fadeIn(component)
compare(left, right)

```

Lessons reference these primitives symbolically.

---

## Frontend–Backend Communication

### REST API

```

GET  /api/lessons
POST /api/progress
GET  /api/progress

```

### WebSocket (optional)

Used for:
- Sync status updates
- Notification delivery
- Progress confirmation

---

## Persistence

Local persistence uses SQLite.

```

Table: lesson_progress

* lesson_id
* step_index
* updated_at
* sync_status

```

---

## Sync Algorithm (Simplified)

```

if online:
for each record where sync_status = DIRTY:
pushToRemote(record)
markAsSYNCED()

```

Conflict resolution is timestamp-based.

---

## Backend Startup Flow

```

main():
startHttpServer()
initializeDatabase()
loadStaticAssets()
exposeApiEndpoints()

```

---

## Frontend Startup Flow

```

onLoad():
fetchLessons()
fetchProgress()
initializeStateMachine()

```

---

## Failure Handling

- Network failures do not block UI
- Sync retries are queued
- Backend remains authoritative for state
