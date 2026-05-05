# Legacy Code Reference

This document outlines the legacy packages and classes within the **Portrai-Tool** project. These components originate from previous development phases and are primarily retained as a technical reference source.

## Overview of Legacy Packages

The following packages contain legacy code:

1.  **`images`**
2.  **`transform`**
3.  **`user.Enum`**
4.  **`userpackage`**

### 1. `images`
*   **Purpose**: Contains legacy wrapper classes and utilities for image handling.
*   **Status**: Kept for reference. Modern image handling is now managed through the new `core.image` and `ui.canvas` components.

### 2. `transform`
*   **Purpose**: Previously handled UI operations related to resizing and transforming elements on the canvas (e.g., `ResizeBox.java`).
*   **Status**: Kept for reference. The modern architecture utilizes `Graphics2D` affine transformations (`g2d.translate`, `g2d.scale`) directly within the rendering engine.

### 3. `user.Enum`
*   **Purpose**: Contains various enumerations used throughout the legacy application (e.g., `MouseMode`, `Direction`, `Transform`).
*   **Status**: Mixed. While primarily legacy, **some enums in this package are still actively used in the modernized codebase**. 
*   **Future Plan**: These actively used enums are planned to be moved to more appropriate packages (such as `core.state` or `tools`) in future refactoring efforts to fully deprecate this package.

### 4. `userpackage`
*   **Purpose**: A catch-all package from the legacy codebase that included UI components, dialogs, domain objects, and export logic (e.g., `ExcelExporter.java`).
*   **Status**: Mixed. Most UI components and dialogs have been replaced by modern Swing/FlatLaf implementations in the `ui` package. However, **some core domain objects, notably `SPoint.java` (which represents a sticky point/marker), are still actively used in the new codebase**.
*   **Future Plan**: Classes like `SPoint` that are essential to the current application logic are scheduled to be migrated to the `core` or `core.state` packages. Once all active dependencies are migrated, the remainder of this package will serve strictly as a reference.

---
*Note: When working on new features, avoid adding new dependencies to these packages unless interacting with the currently active classes (`SPoint`, specific Enums). Always prefer the modernized architecture (`core`, `ui`, `tools`).*
