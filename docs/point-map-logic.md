# Point Map Export Logic & Calculations

This document explains the mathematical logic behind the **Save Point Map** feature in Portrai-Tool, focusing on how physical dimensions, pixels, and DPI interact to produce precise, print-ready results.

## 1. Core Concepts

### 1.1 Original Scale ($S_{orig}$)
The application maintains a `scale` value (found in `AppState`), which represents the physical distance covered by a single pixel of the original image in centimeters (**cm/px**).
*   **Formula**: $PhysicalSize_{orig} = Pixels_{orig} \times S_{orig}$

### 1.2 Dots Per Inch (DPI)
DPI is the spatial printing resolution. The industry standard for high-quality printing is **300 DPI**.
*   **Constant**: $1 \text{ inch} = 2.54 \text{ cm}$
*   **Pixels per cm**: $P_{cm} = \frac{DPI}{2.54}$

## 2. Export Calculation Logic

When a user exports a Point Map via the `ExportPointMapDialog`, the system performs the following steps:

### Step 1: Determine Target Physical Size
The user inputs a target width ($W_{cm}$) and height ($H_{cm}$) for the paper. 
*   **Constraint**: The aspect ratio is locked to match the original image.
*   **Effective Scale** ($S_{eff}$): If the user changes the paper size, the "effective" scale for that specific export becomes:
    $$S_{eff} = \frac{W_{cm}}{W_{px, orig}}$$

### Step 2: Calculate Output Pixel Dimensions
Based on the selected **Target DPI** (e.g., 300), the output image dimensions are calculated:
*   **Output Width**: $W_{px, out} = \text{round}\left(\frac{W_{cm}}{2.54} \times DPI_{target}\right)$
*   **Output Height**: $H_{px, out} = \text{round}\left(\frac{H_{cm}}{2.54} \times DPI_{target}\right)$

### Step 3: Coordinate Transformation
To place the points correctly on the higher-resolution export canvas, original coordinates $(X_{orig}, Y_{orig})$ are translated:
*   **Factor**: $F = S_{eff} \times \frac{DPI_{target}}{2.54}$
*   **Output Coordinates**: 
    $$X_{out} = \text{round}(X_{orig} \times F)$$
    $$Y_{out} = \text{round}(Y_{orig} \times F)$$

## 3. Marker Dimension Consistency

The "Point Markers" are designed to have fixed physical sizes regardless of the image resolution or DPI.

| Marker Part | Target Size (mm) | Target Size (cm) |
| :--- | :--- | :--- |
| **Circle Radius** | 1.0 mm | 0.1 cm |
| **Crosshair Width** | 1.5 mm | 0.15 cm |
| **Crosshair Length** | 5.25 mm | 0.525 cm |

At render time, these are converted to pixels using the target DPI:
$$\text{Pixels} = \frac{\text{Size in cm}}{2.54} \times DPI_{target}$$

This ensures that when you print the resulting PNG at its intended size, the markers will measure exactly 1mm and 1.5mm on paper.

## 4. Export Formats

The system supports two main formats for exporting the Point Map:

### 4.1 PNG Image (Transparent)
*   **Best for**: Digital overlays, web use, or manual placement in other design software.
*   **Characteristics**: High resolution (DPI-aware), transparent background.
*   **Printing**: Requires the user to manually set the print size to match the target $W_{cm} \times H_{cm}$.

### 4.2 PDF Document (Recommended for Printing)
*   **Best for**: Direct printing and ensuring absolute physical precision.
*   **Logic**:
    1.  The system creates a PDF page with the **exact physical dimensions** ($W_{cm} \times H_{cm}$) specified in the dialog.
    2.  The high-resolution marker image is embedded into this page, filling it completely.
*   **Advantage**: When you open the PDF in any viewer (like Chrome or Acrobat) and hit Print (at 100% scale), the resulting paper output is guaranteed to match the chosen physical size. This eliminates scaling errors common in image viewers.

## 5. Printing Workflow

### How to Print:
1.  **Using PDF**: Export as `.pdf`, open the file, and print at **"Actual Size"** or **"100% Scale"**. This is the most reliable method.
2.  **Using PNG**: Export as `.png`. If you insert this into a document (like Word or Canva), ensure you set the image's dimensions to the exact $W_{cm} \times H_{cm}$ defined during export.
3.  **Transparency**: Both formats preserve transparency (or a white background in PDF depending on the viewer), allowing for clean overlays.

---
*Note: If `scale` in AppState is not set (zero), the system uses a fallback of 0.01 cm/px to prevent division-by-zero errors.*
