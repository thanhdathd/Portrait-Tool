# Design Spec: Auto Save with Session Recovery

## 1. Overview
The **Auto Save** feature provides an automatic backup mechanism for the user's editing progress. It periodically saves the current state (points, grids, image path, and settings) to a specialized file format (`.pdt`) in a background process. This ensures data persistence even in the event of unexpected application termination.

## 2. Requirements

### 2.1 Functional Requirements
- **FR1: Periodic Backup**: Automatically save the current state every $X$ minutes (default 5).
- **FR2: Background Execution**: The saving process must run on a background thread to avoid UI freezing.
- **FR3: Custom Format**: Save data in JSON format with a `.pdt` (Portrait Data Template) extension.
- **FR4: Persistence Location**: Store files in `%USERHOME%/.myapp/autosave/`.
- **FR5: Configurable Interval**: Users can enable/disable auto-save and change the interval via the Settings dialog.
- **FR6: Session Recovery**: On startup, detect existing `.pdt` files and offer to restore the session.
- **FR7: File Integrity**: Use an "atomic save" strategy (write to `.tmp` then rename) to prevent corruption.

### 2.2 Technical Requirements
- **Language**: Java 17.
- **Serialization**: Use **Gson** library for JSON mapping.
- **Concurrency**: Use `ScheduledExecutorService` for the timer.

## 3. Architecture & Data Flow

### 3.1 Data Model (PDT Structure)
The `.pdt` file will represent the state of an editing session:
```json
{
  "version": "1.0",
  "timestamp": "2026-05-10T01:25:00Z",
  "imagePath": "C:/Absolute/Path/To/Image.jpg",
  "scale": 1.0,
  "gridSize": 40.0,
  "gridInCm": false,
  "stickyPoints": [
    { 
      "id": 1, 
      "X": 100, 
      "Y": 200, 
      "color": "#00FFFF",
      "direction": "EAST",
      "isCustomPlacement": true,
      "customGap": 20,
      "customAngle": 45
    }
  ],
  "grids": []
}
```

> [!NOTE]
> Serialization of `java.awt.Color` will be handled by converting to/from Hex strings (e.g., `#RRGGBB`). The `Direction` enum will be serialized by its name.

### 3.2 Components
- **`AutoSaveManager`**:
    - Manage the `ScheduledExecutorService`.
    - Handles the serialization logic using Gson.
    - Manages file cleanup (deleting old autosave files when a project is closed normally).
- **`AppState` Extensions**:
    - Add `isAutoSaveEnabled` (boolean) and `autoSaveInterval` (int).
- **`SettingsDialog` Update**:
    - UI fields for the new settings.
- **`MainFrame` Hooks**:
    - Initialize `AutoSaveManager` on startup.
    - Execute the recovery check before opening the main window.

## 4. Implementation Details

### 4.1 Dependency Addition
Add Google Gson to `pom.xml`:
```xml
<dependency>
    <groupId>com.google.code.gson</groupId>
    <artifactId>gson</artifactId>
    <version>2.10.1</version>
</dependency>
```

### 4.2 Recovery Logic Flow
1. App Start.
2. `AutoSaveManager.checkPendingRecovery()`.
3. If file found -> `JOptionPane.showConfirmDialog`.
4. If "Yes":
    - Load image from `imagePath`.
    - Deserialize points/grids into `CanvasState`.
    - Apply settings to `AppState`.
5. If "No" or "Success": Continue.

## 5. Success Criteria
- A `.pdt` file is generated in the autosave folder every 5 minutes.
- The app remains responsive during the save process.
- Modifying the interval in Settings updates the frequency immediately.
- If the app is killed, the next launch correctly offers to restore the points on the correct image.
