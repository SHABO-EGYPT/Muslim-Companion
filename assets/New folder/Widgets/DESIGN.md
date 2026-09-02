---
name: Noor Divine
colors:
  surface: '#121414'
  surface-dim: '#121414'
  surface-bright: '#38393a'
  surface-container-lowest: '#0c0f0f'
  surface-container-low: '#1a1c1c'
  surface-container: '#1e2020'
  surface-container-high: '#282a2b'
  surface-container-highest: '#333535'
  on-surface: '#e2e2e2'
  on-surface-variant: '#d0c5af'
  inverse-surface: '#e2e2e2'
  inverse-on-surface: '#2f3131'
  outline: '#99907c'
  outline-variant: '#4d4635'
  surface-tint: '#e9c349'
  primary: '#f2ca50'
  on-primary: '#3c2f00'
  primary-container: '#d4af37'
  on-primary-container: '#554300'
  inverse-primary: '#735c00'
  secondary: '#bbc7da'
  on-secondary: '#253140'
  secondary-container: '#3e4a59'
  on-secondary-container: '#adb9cc'
  tertiary: '#c7cedd'
  on-tertiary: '#29313c'
  tertiary-container: '#abb3c1'
  on-tertiary-container: '#3d4551'
  error: '#ffb4ab'
  on-error: '#690005'
  error-container: '#93000a'
  on-error-container: '#ffdad6'
  primary-fixed: '#ffe088'
  primary-fixed-dim: '#e9c349'
  on-primary-fixed: '#241a00'
  on-primary-fixed-variant: '#574500'
  secondary-fixed: '#d7e3f7'
  secondary-fixed-dim: '#bbc7da'
  on-secondary-fixed: '#101c2a'
  on-secondary-fixed-variant: '#3c4857'
  tertiary-fixed: '#dbe3f1'
  tertiary-fixed-dim: '#bfc7d5'
  on-tertiary-fixed: '#141c26'
  on-tertiary-fixed-variant: '#3f4753'
  background: '#121414'
  on-background: '#e2e2e2'
  surface-variant: '#333535'
typography:
  display-lg:
    fontFamily: Noto Serif
    fontSize: 48px
    fontWeight: '600'
    lineHeight: 56px
    letterSpacing: -0.02em
  headline-lg:
    fontFamily: Noto Serif
    fontSize: 32px
    fontWeight: '600'
    lineHeight: 40px
  headline-lg-mobile:
    fontFamily: Noto Serif
    fontSize: 24px
    fontWeight: '600'
    lineHeight: 32px
  headline-md:
    fontFamily: Noto Serif
    fontSize: 20px
    fontWeight: '500'
    lineHeight: 28px
  body-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  body-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
  label-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 12px
    fontWeight: '600'
    lineHeight: 16px
    letterSpacing: 0.05em
  label-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 10px
    fontWeight: '500'
    lineHeight: 14px
    letterSpacing: 0.02em
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  unit: 4px
  margin-page: 20px
  gutter: 12px
  card-padding: 16px
  stack-sm: 8px
  stack-md: 16px
  stack-lg: 24px
---

## Brand & Style

The design system is rooted in spiritual tranquility and sophisticated elegance. It targets a modern audience seeking a refined, premium experience for their daily spiritual practices. The emotional response is one of calm, reverence, and clarity.

The visual style is a blend of **Modern Corporate** reliability and **Tactile/Premium** luxury. It uses a "Midnight & Metallic" aesthetic, characterized by:
- Deep, immersive backgrounds that evoke the night sky.
- Golden accents that represent light and divinity.
- Subtle Islamic geometric patterns used as textural overlays rather than primary motifs.
- High-contrast typography that balances traditional serif authority with clean sans-serif readability.

## Colors

The palette is strictly limited to maintain a premium, focused atmosphere.

- **Primary (Gold):** `#D4AF37`. Used for interactive elements, primary status indicators, and headers. In high-priority components, use a linear gradient from `#C5A028` to `#E6C65D` to simulate a metallic sheen.
- **Secondary (Deep Navy):** `#1A2634`. The primary surface color for cards, containers, and widgets.
- **Tertiary (Midnight):** `#0F1721`. The base background color for the entire application.
- **Neutral (Parchment):** `#F5F5F5`. Used sparingly for secondary body text and icons that require high legibility without the dominance of gold.

Secondary accents for functionality:
- **Success/Progress:** Use the primary gold.
- **Subtle borders:** A low-opacity gold (20-30% alpha) over navy surfaces.

## Typography

This design system uses a dual-font strategy to balance elegance with modern utility. 

**Noto Serif** is used for all "Divine" elements: prayer names, Quranic text, and main headings. It conveys authority and timelessness. 

**Plus Jakarta Sans** is used for "Functional" elements: times, dates, labels, and UI controls. Its soft, rounded terminals provide a friendly and approachable counter-balance to the formal serif.

Titles and numbers (like clock times or tasbih counters) should often use the primary gold color to create a clear visual hierarchy. Arabic script should be rendered with generous line-height to ensure diacritics are clearly legible.

## Layout & Spacing

The layout follows a **fluid grid** model optimized for mobile. 

- **Widget Strategy:** For the mobile widget, use a "contained" layout. Elements are grouped into cards with a standard padding of 16px.
- **Rhythm:** Utilize a 4px baseline grid. Most vertical spacing should be multiples of 8px (8, 16, 24).
- **Safe Margins:** Maintain a minimum 20px horizontal margin on the screen edges.
- **Reflow:** On smaller widget sizes, typography should scale down using the `-mobile` tokens, and secondary information (like the Hijri date) should be hidden to prioritize the core prayer time or counter.

## Elevation & Depth

Visual hierarchy is achieved through **Tonal Layers** and **Subtle Inner Glows**.

- **Level 0 (Base):** Midnight color (`#0F1721`) with a faint Islamic geometric pattern at 5% opacity.
- **Level 1 (Surface):** Deep Navy (`#1A2634`). This is the standard for cards and widgets. 
- **Depth Effects:** Instead of heavy drop shadows, use a subtle **inner border** (1px, 15% gold opacity) to define edges. 
- **Highlight:** Active elements or "Current Prayer" cards feature a subtle golden outer glow (`0px 4px 20px rgba(212, 175, 55, 0.15)`).
- **Geometric Overlay:** Use vector patterns as masks within the Navy surfaces to create a sense of depth without cluttering the foreground.

## Shapes

The shape language is "Substantial Rounded."

- **Standard Containers:** Use `16px` (rounded-lg) for main widget backgrounds and large cards.
- **Interactive Elements:** Buttons and small selection chips use `12px` (rounded-md).
- **Icons & Avatars:** Circular containers are reserved for Quranic chapters, Qibla compasses, and user profiles.
- **Borders:** When borders are used (e.g., on secondary buttons), they should be consistent 1px strokes.

## Components

### Buttons
- **Primary:** Solid gold gradient background with dark navy text (Noto Serif Bold).
- **Secondary:** Transparent background with a 1px gold border and gold text (Plus Jakarta Sans Semibold).

### Cards
- Background: `#1A2634`.
- Padding: `16px`.
- Optional: Top-right corner can feature a small, low-opacity geometric corner motif.

### Progress Indicators (Tasbih/Prayer)
- Use thick, gold strokes for circular progress.
- Background tracks should be Navy with a 10% white overlay to differentiate from the base.

### Widgets (Specific Guidance)
- **Small (2x2):** Focus on a single data point (Next Prayer Time) with a large gold display font.
- **Medium (4x2):** Include a progress bar for the current prayer window and a quick-action button for 'Azkar'.
- **Icons:** Use thin-line gold icons (2px stroke) for clarity against the dark navy backgrounds.

### Inputs/Fields
- Subtle navy backgrounds with gold labels. Active state indicated by a full-opacity gold bottom border.