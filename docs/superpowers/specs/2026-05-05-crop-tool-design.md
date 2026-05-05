# Crop Tool Design Specification

## 1. Overview
The Crop Tool is a highly interactive, artist-focused mouse tool designed for `Portrai-Tool`. It provides predefined aspect ratios (Square, Golden Ratio, A4, etc.) overlaid with classic composition guides (Rule of Thirds, Golden Spiral, Crosshair) to assist in framing artwork.

## 2. Interaction Model
The interaction centers around two primary states: **Fly Mode** and **Review Mode**.

### State 1: Fly Mode
- **Activation**: Selecting the Crop Tool places it in Fly Mode.
- **Mouse Move**: The crop frame follows the mouse cursor seamlessly around the canvas.
- **Mouse Wheel**: Zooms the underlying canvas, keeping the crop frame size fixed relative to the screen.
- **Shift + Mouse Wheel**: Resizes the crop frame.
- **Alt Modifier**: When held alongside Shift+Wheel, it reduces the resizing speed for fine, sophisticated adjustments.
- **Left/Right Arrow Keys**: Cycles horizontally through a predefined list of `CropProfile`s (combining aspect ratios and composition guides).
- **Esc**: Exits the Crop Tool entirely and switches the active tool back to the Hand Tool.
- **Left Click**: Drops the frame at the current location and transitions the tool into **Review Mode**.

### State 2: Review Mode
- **State Change**: The frame is now stationary on the canvas, allowing the artist to review the composition.
- **Mouse Move**: Disabled (the frame is locked).
- **Enter Key**: Confirms the crop. Executes the underlying `CropCommand`.
- **Esc Key**: Cancels the temporary placement, returning the tool to **Fly Mode** where the frame once again follows the mouse.

## 3. UI/Visuals
- **Overlay**: The area outside the crop frame will be tinted with a dark, semi-transparent overlay to emphasize the cropped region.
- **Composition Guides**: High-contrast, precise geometric lines (e.g., Rule of Thirds grid, Golden Spiral) rendered inside the active crop frame.
- **Filmstrip Navigator**: A horizontal list of miniature crop frames is drawn at the bottom of the current crop frame. It highlights the currently selected profile and updates as the user cycles through profiles with the arrow keys.

## 4. Architecture & Data Flow

### The Data Model
- `CropRatio`: Enum/Class defining the aspect ratio (e.g., `SQUARE (1:1)`, `GOLDEN (1.618:1)`, `A4 (1:1.414)`).
- `CropGuide`: Enum/Class defining the composition rendering logic (e.g., `RULE_OF_THIRDS`, `GOLDEN_SPIRAL`, `DIAGONAL`).
- `CropProfile`: A combination object containing one `CropRatio` and one `CropGuide`.

### The Tool (`tools.CropTool`)
- Implements the `Tool` interface.
- Maintains state variables for the current mode (`isReviewMode`), the current mouse position, the current frame size, and the selected `CropProfile` index.
- Handles all `onMouse*` and Keybinding events specific to the interaction model outlined above.
- The `onPaint` method handles drawing the dark overlay, the crop guide lines, and the miniature filmstrip.

### The Command (`core.history.CropCommand`)
- Triggers upon pressing `Enter` in Review Mode.
- Performs a **Deep Copy** of the cropped area from the `BufferedImage`. This is the safest approach, ensuring complete separation of memory and preserving the history stack's integrity.
- **Marker Translation**: Iterates through all `SPoint`s and grid markers in `CanvasState`, mathematically shifting their Cartesian coordinates to align with the new cropped origin.
- **Marker Cleanup**: Any `SPoint` or grid element falling completely outside the newly cropped boundaries will be automatically deleted from the state to maintain a clean workspace.

## 5. Scope constraints
- The tool focuses purely on bounding-box cropping with fixed/scalable ratios. Arbitrary free-transform resizing of individual sides (like standard photo editors) is excluded to prioritize the predefined compositional workflow.
