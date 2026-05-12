# Design Doc: Hybrid History & Session Recovery

## 1. Goal Description
The objective is to solve the conflict between a memory-limited Undo Stack and the need for accurate Session Recovery. By introducing a hybrid approach, we ensure that the application remains memory-efficient (even with 4K images) while guaranteeing that auto-saved sessions can be reconstructed perfectly.

## 2. Architecture Overview
The system will operate with two parallel history flows:
1.  **Memory-Aware Undo Stack (RAM)**: Stores `Command` objects containing full `BufferedImage` references for instant undo/redo. Regulated by a byte-based memory budget.
2.  **Persistent History (Metadata)**: Stores `CommandData` objects (lightweight metadata). This list is never truncated during a session and is used for Auto-Save and Recovery.

## 3. Proposed Changes

### 3.1. Command Interface & Implementations
Add a method to track memory consumption.
- `long getMemorySize()`: Returns the estimated size in bytes.
    - **Raster Commands (Crop, Filter, Transform, Resize)**: `(width * height * 4) * number_of_images`.
    - **Vector Commands (Stick, Grid)**: Constant value (~256 bytes).

### 3.2. HistoryManager
- **Budget Tracking**: Maintain `currentMemoryUsage` (long) and `maxMemoryBudget` (long).
- **Persistent List**: Add `List<CommandData> persistentHistory` to store all actions from the start.
- **Adaptive Truncation**: In `push(Command cmd)`, if `currentMemoryUsage + cmd.getMemorySize() > maxMemoryBudget`, remove the oldest commands from the `undoStack` (and update `currentMemoryUsage`) until the budget is met.
- **Save State Handling**: Update `isSavedStateDropped` logic to account for memory-based drops.

### 3.3. Config & Settings
- Add `HistoryMemoryLevel` enum: `LOW (512MB)`, `MEDIUM (1GB)`, `HIGH (2GB)`.
- Update `AppState` and `ConfigManager` to persist this setting.
- `HistoryManager` will update its `maxMemoryBudget` whenever this setting changes.

### 3.4. Auto-Save Integration
- Update `AutoSaveData` to store the `persistentHistory` instead of capturing from the current (and potentially truncated) `undoStack`.
- Update `AutoSaveManager` to use this persistent list for both saving and reconstruction.

## 4. RAM Budget Levels

| Level | RAM Budget | Target Scenario |
| :--- | :--- | :--- |
| **Low** | 512 MB | Standard laptops, 8GB RAM systems, small images. |
| **Medium** | 1,024 MB (1GB) | Default setting, balanced performance. |
| **High** | 2,048 MB (2GB) | Pro workstations, 4K/8K image editing. |

## 5. Error Handling & Edge Cases
- **Oversized Command**: If a single command exceeds the budget, it will still be allowed (as it's the current action), but it will clear the entire previous undo history to save RAM.
- **Memory Recalculation**: When an image is resized or cropped, existing commands in the stack do not need to be recalculated (as they hold their own image references), but the budget for *new* commands remains consistent.

## 6. Verification Plan

### Automated Tests
- `HistoryManagerMemoryTest`: Verify that dropping occurs correctly based on byte size, not count.
- `PersistentHistoryTest`: Ensure `persistentHistory` remains intact even when `undoStack` is truncated.

### Manual Verification
- Perform 50+ point additions followed by a heavy filter on a large image.
- Verify that old points are NOT dropped from the undo stack, but oldest heavy filters ARE.
- Kill app and verify 100% accurate recovery.
