# Portrai-Tool Modernized - Project Documentation

This document provides a comprehensive overview of the architecture, implementation details, and a source code navigation guide for the **Portrai-Tool** project.

## 1. Project Overview

**Portrai-Tool** is a modern, Java Swing-based desktop application designed for image processing, measurement, and annotation. It allows users to open images, apply live-preview color filters, and use various tools to place markers (sticky points), measure distances (Point-to-Point), draw grids, and export the point data to Excel.

The application has been recently modernized from a legacy AWT/Swing implementation. The new architecture prioritizes responsiveness, maintainability, decoupling of state and UI, and uses a modern Look and Feel (FlatLaf).

### Key Technologies
*   **Java 17**: Core language.
*   **Swing & AWT**: Core UI framework for desktop rendering and windowing.
*   **FlatLaf**: Modern cross-platform Look and Feel for Swing components.
*   **MigLayout**: Robust layout manager for complex Swing dialogs.
*   **FastExcel (Dhatim)**: High-performance library for exporting point data to `.xlsx` files.
*   **Maven**: Build and dependency management.

---

## 2. Architectural Overview

The application follows a structured, loosely-coupled architecture, separating the User Interface (UI), core state management, tools logic, and background processing.

### 2.1 State Management (`core.state`)
State is centrally managed to ensure consistency across the UI, canvas, and tools.
*   **`AppState`**: The global source of truth. It stores application-wide settings (zoom, scale, active file path, window size, custom label configurations, brush color). It acts as a central hub where listeners can observe property changes.
*   **`CanvasState`**: A decoupled subset of `AppState` specifically handling the visual elements on the canvas (image offsets, lists of `SPoint` markers, grid markers). This separation ensures that history operations don't unnecessarily alter global application settings.

### 2.2 The Command Pattern for History (`core.history`)
Undo/Redo functionality is implemented using the **Command Pattern**.
*   **`HistoryManager`**: Maintains two stacks (`undoStack` and `redoStack`). It manages the capacity of history and fires events when the history state changes (notifying the UI to enable/disable Undo/Redo buttons or update the "Modified" status).
*   **`Command` Interface**: Every user action that modifies the canvas state (e.g., placing a point, applying a filter, drawing a grid) is encapsulated in a class implementing `Command` (e.g., `StickCommand`, `FilterCommand`, `GridCommand`).
*   Each `Command` knows how to `execute()` itself and `undo()` itself, ensuring robust state recovery.

### 2.3 Tools Architecture (`tools`)
The application uses a **State Pattern** for user interactions on the canvas. 
*   **`Tool` Interface**: Defines lifecycle methods corresponding to mouse events (`onMousePressed`, `onMouseDragged`, `onMouseReleased`, `onPaint`).
*   **`ToolManager`**: A singleton-like registry that initializes and holds instances of the available tools.
*   **Concrete Tools**:
    *   `HandTool`: For panning the canvas.
    *   `StickTool`: For placing permanent sticky points (`SPoint`).
    *   `P2PTool`: (Point-to-Point) For measuring distances and drawing lines between two points.
    *   `GridTool`: For drawing scalable grids over the image.
    *   `ZoomCanvasTool`: For click-to-zoom functionality.
*   The `ImageCanvas` maintains a reference to the `activeTool` and delegates all mouse and paint events to it.

### 2.4 Rendering Engine (`ui.canvas`)
*   **`ImageCanvas`**: A custom `JPanel` responsible for drawing the image, grid, markers, and preview elements.
    *   It uses standard Java 2D Graphics (`Graphics2D`).
    *   Supports hardware-accelerated panning and zooming by applying AffineTransforms (`g2d.translate`, `g2d.scale`).
    *   Delegates tool-specific rendering to the `activeTool.onPaint()`.
*   **`RenderUtils`**: Extracts static drawing logic (drawing points, grids, labels) out of the canvas to keep the canvas class clean.

### 2.5 Background Processing (`workers`)
To keep the UI Thread (Event Dispatch Thread - EDT) responsive, heavy operations are offloaded to background threads using `SwingWorker`.
*   **`ImageLoadWorker`**: Loads image files asynchronously.
*   **`SaveWorker`**: Handles writing the canvas output (including markers and grids) to a `.png` file.
*   **`FilterWorker`**: Applies complex pixel-by-pixel color filters in the background, essential for the smooth "Live Preview" feature without freezing the UI.

---

## 3. Implementation Details Explanations

### 3.1 Live Preview Filtering
When a user opens the `FilterDialog`, the application needs to show the filter effect in real-time on the main canvas.
1.  **Optimization (LOD - Level of Detail)**: `ImageCanvas.getVisiblePreviewData()` calculates exactly which portion of the image is currently visible in the viewport. If the user is zoomed out, it scales the cropped region down to screen resolution *before* processing.
2.  **Asynchronous Processing**: `applyLivePreviewToMainCanvas` creates a new `FilterWorker`. If an old worker is still running (because the user is dragging sliders quickly), it cancels the old worker.
3.  **Overlay Rendering**: The processed image segment (`tempPreviewImage`) is temporarily drawn over the main background image during the `paintComponent` phase. Once the user applies the filter, it is baked into the actual `backgroundImage` via a `FilterCommand`.

### 3.2 Zoom and Pan Coordination
Zooming and panning are intricately linked to ensure the image stays centered or follows the mouse correctly.
*   **Offsets (`imageOffsetX`, `imageOffsetY`)**: Stored in `CanvasState`. When panning (via `HandTool`), these offsets are modified.
*   **Scrollpane Integration**: `ImageCanvas` is wrapped in a `JScrollPane`. The canvas dynamically calculates its `PreferredSize` based on the zoom level. If the zoomed image is larger than the viewport, native scrollbars appear.
*   **Zoom Window**: A floating dialog (`ZoomWindow`) provides a magnifying glass effect. It listens to mouse movements on the `ImageCanvas`, translates the scaled screen coordinates back to unscaled image coordinates, and renders a zoomed-in crop of the original `backgroundImage`.

### 3.3 Keyboard Shortcuts & Input Maps
Global shortcuts (like Ctrl+Z, Spacebar to temporarily pan) are implemented using Swing's `InputMap` and `ActionMap` on the `JRootPane` and `ImageCanvas`. This is preferred over `KeyListener` because it doesn't require the component to have focus, ensuring shortcuts work regardless of which panel is clicked.

---

## 4. Source Code Navigation Guide

Below is a breakdown of the package structure to help you find specific functionalities quickly.

> **Note on Legacy Code:** The packages `images`, `user.Enum`, `userpackage`, and `transform` contain legacy code from previous versions. They are kept primarily as a technical reference source. Some classes in `userpackage` (e.g., `SPoint`) and `user.Enum` are still actively used in the new codebase, but they are planned to be migrated out later. For more details, please refer to [LEGACY_CODE.md](LEGACY_CODE.md).

```text
src/main/java/
├── config/
│   └── ConfigManager.java        # Saves and loads AppState (e.g., last used colors) to config.ini.
├── core/
│   ├── actions/                  # Shared UI actions (KeyActions).
│   ├── fileio/                   # File loading utilities (Thumbnails, FileChoosers).
│   ├── history/                  # Undo/Redo logic (Command, HistoryManager, Concrete Commands).
│   ├── image/                    # Core image processing utilities.
│   ├── launcher/                 # AppLauncher.java (The main() entry point, FlatLaf setup).
│   └── state/                    # AppState.java, CanvasState.java (Global source of truth).
├── filter/                       # Image filter implementations.
│   ├── FilterProperties.java     # Data structure for filter sliders (RGBA, mode).
│   └── RGBFilter, RedGrayFilter..# Specific pixel manipulation logic.
├── images/                       # [LEGACY REFERENCE]
│   └── Img.java                  # Legacy/wrapper image object.
├── tools/                        # Interactive Canvas Tools.
│   ├── ToolManager.java          # Tool registry.
│   ├── Tool.java                 # Interface for all tools.
│   └── HandTool, StickTool, etc. # Concrete tool implementations.
├── transform/                    # [LEGACY REFERENCE]
│   └── ResizeBox.java            # Legacy UI helper for resizing operations.
├── ui/                           # User Interface components.
│   ├── MainFrame.java            # The primary application window, menus, toolbars.
│   ├── CustomCursors.java        # Custom mouse cursors for different tools.
│   ├── canvas/
│   │   ├── ImageCanvas.java      # The core rendering surface (JPanel).
│   │   └── RenderUtils.java      # Helpers for drawing shapes on the canvas.
│   └── dialogs/                  # Popups and configuration windows.
│       ├── FilterDialog.java     # UI for adjusting RGB filters.
│       ├── SettingsDialog.java   # App settings.
│       └── ZoomWindow.java       # The floating magnifying glass window.
├── user/
│   └── Enum/                     # [LEGACY REFERENCE] Enums (some still used, planned to move).
├── userpackage/                  # [LEGACY REFERENCE] UI components, domain objects (some still used).
│   ├── SPoint.java               # The primary object representing a marker/sticky point.
│   └── ExcelExporter.java        # (Legacy) Excel logic.
├── utils/                        
│   ├── ExcelExportUtils.java     # Modern implementation of .xlsx export using FastExcel.
│   └── Utils.java                # General helpers.
└── workers/                      # Background Tasks (SwingWorker).
    ├── ImageLoadWorker.java      # Async loading.
    ├── SaveWorker.java           # Async saving.
    └── FilterWorker.java         # Async live-preview filtering.
```

### Quick Pointers for Common Tasks:
*   **Want to add a new Tool?**
    1. Create a class implementing `Tool` in the `tools` package.
    2. Register it in `ToolManager.java`.
    3. Add a toolbar button and shortcut in `ui.MainFrame.java`.
*   **Need to change how points are drawn?**
    1. Look at `ui.canvas.RenderUtils.drawStickyPoints()`.
*   **Want to add a new Setting/Preference?**
    1. Add the variable and Getter/Setter to `core.state.AppState.java`.
    2. Add the UI control to `ui.dialogs.SettingsDialog.java`.
    3. Ensure it is saved/loaded in `config.ConfigManager.java`.
*   **Debugging Undo/Redo?**
    1. Look at `core.history.HistoryManager.java` and the specific `Command` class related to the action.

---
*Generated by Antigravity AI*
