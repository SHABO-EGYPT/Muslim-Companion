---
name: Sacred Serenity
colors:
  surface: '#101415'
  surface-dim: '#101415'
  surface-bright: '#363a3b'
  surface-container-lowest: '#0b0f10'
  surface-container-low: '#191c1e'
  surface-container: '#1d2022'
  surface-container-high: '#272a2c'
  surface-container-highest: '#323537'
  on-surface: '#e0e3e5'
  on-surface-variant: '#c6c6cd'
  inverse-surface: '#e0e3e5'
  inverse-on-surface: '#2d3133'
  outline: '#909097'
  outline-variant: '#45464d'
  surface-tint: '#bec6e0'
  primary: '#bec6e0'
  on-primary: '#283044'
  primary-container: '#0f172a'
  on-primary-container: '#798098'
  inverse-primary: '#565e74'
  secondary: '#e9c349'
  on-secondary: '#3c2f00'
  secondary-container: '#af8d11'
  on-secondary-container: '#342800'
  tertiary: '#bcc7de'
  on-tertiary: '#263143'
  tertiary-container: '#0c1829'
  on-tertiary-container: '#768197'
  error: '#ffb4ab'
  on-error: '#690005'
  error-container: '#93000a'
  on-error-container: '#ffdad6'
  primary-fixed: '#dae2fd'
  primary-fixed-dim: '#bec6e0'
  on-primary-fixed: '#131b2e'
  on-primary-fixed-variant: '#3f465c'
  secondary-fixed: '#ffe088'
  secondary-fixed-dim: '#e9c349'
  on-secondary-fixed: '#241a00'
  on-secondary-fixed-variant: '#574500'
  tertiary-fixed: '#d8e3fb'
  tertiary-fixed-dim: '#bcc7de'
  on-tertiary-fixed: '#111c2d'
  on-tertiary-fixed-variant: '#3c475a'
  background: '#101415'
  on-background: '#e0e3e5'
  surface-variant: '#323537'
typography:
  display-lg:
    fontFamily: Libre Caslon Text
    fontSize: 48px
    fontWeight: '700'
    lineHeight: 56px
    letterSpacing: -0.02em
  headline-lg:
    fontFamily: Libre Caslon Text
    fontSize: 32px
    fontWeight: '600'
    lineHeight: 40px
  headline-lg-mobile:
    fontFamily: Libre Caslon Text
    fontSize: 28px
    fontWeight: '600'
    lineHeight: 36px
  body-lg:
    fontFamily: Inter
    fontSize: 18px
    fontWeight: '400'
    lineHeight: 28px
  body-md:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  label-md:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '600'
    lineHeight: 20px
    letterSpacing: 0.05em
rounded:
  sm: 0.125rem
  DEFAULT: 0.25rem
  md: 0.375rem
  lg: 0.5rem
  xl: 0.75rem
  full: 9999px
spacing:
  base: 8px
  container-padding-mobile: 20px
  container-padding-desktop: 40px
  gutter: 16px
  section-gap: 64px
---

## Brand & Style

The design system is rooted in the concepts of **Khashu** (solemnity) and **Ihsan** (excellence). It serves as a digital sanctuary for the modern Muslim, balancing ancient spiritual traditions with contemporary premium aesthetics. 

The visual style is **Minimalist High-Contrast**. It utilizes expansive dark surfaces to represent the infinite night sky, punctuated by precise, elegant gold accents that symbolize divine light. Subtle Islamic geometric patterns are integrated as low-opacity textures rather than focal points, creating a sense of heritage without cluttering the interface. The emotional response is one of calm, focus, and reverence.

## Colors

The palette is dominated by **Midnight Navy** (#0F172A), a deep, authoritative base that provides a focused environment for prayer times and Quranic reading. 

**Celestial Gold** (#D4AF37) is used exclusively for high-priority elements, active states, and sacred iconography. **Slate Blue** (#1E293B) acts as a secondary container color to create subtle depth against the primary background. Text primarily uses **Off-White** (#F8FAFC) to ensure high legibility against the dark canvas while avoiding the harshness of pure white.

## Typography

The typographic hierarchy relies on the tension between a traditional serif and a modern sans-serif. **Libre Caslon Text** is reserved for titles, Quranic verses (translation), and headings, providing a scholarly and timeless feel. 

**Inter** handles all functional UI text and long-form body content, ensuring maximum readability and a clean, systematic appearance. For mobile devices, headings scale down slightly to maintain a single-column focus without excessive wrapping. Label styles use increased letter spacing and uppercase styling to denote metadata and small UI controls.

## Layout & Spacing

This design system uses a **Fluid Grid** with generous inner margins to create a "breathing" layout. On mobile, a 4-column structure is used with 20px side margins; on desktop, this expands to a 12-column layout centered within a 1200px max-width container.

The spacing rhythm is intentional and slow. Large vertical gaps between sections (64px+) prevent the interface from feeling "busy," encouraging a meditative pace of interaction. Vertical rhythm is built on an 8px base unit.

## Elevation & Depth

Hierarchy is established through **Tonal Layers** and **Low-Contrast Outlines**. Because the primary background is extremely dark, traditional black shadows are ineffective.

Instead, elevation is achieved by:
1. **Surface Lifting:** Floating cards use a slightly lighter shade of navy (#1E293B).
2. **Gold Accents:** Primary actions are defined by their color brilliance rather than their height.
3. **Subtle Outlines:** A 1px border using a 10% opacity gold or 20% opacity slate defines the boundaries of interactive elements without adding visual weight.
4. **Background Textures:** Islamic geometric patterns are applied as a fixed background overlay at 3% opacity, giving the "floor" of the app a sense of physical texture.

## Shapes

The shape language is **Soft** and architectural. We avoid the overly playful nature of pill shapes in favor of precise, refined corners. 

Buttons and input fields use a 0.25rem (4px) radius, echoing the geometry found in traditional tilework and mosque architecture. Larger containers like prayer time cards or Quranic study modules use a 0.5rem (8px) radius to feel more approachable while remaining sophisticated.

## Components

### Buttons
Primary buttons are solid Gold (#D4AF37) with Navy text, using the `label-md` typographic style. Secondary buttons are outlined in 1px Gold with transparent backgrounds.

### Prayer Cards
The most prominent UI element. They should feature the `headline-lg` serif font for the time, and a subtle geometric pattern crop in the background to differentiate the "Current Prayer" from upcoming ones.

### Lists & Navigation
Lists use thin dividers at 10% opacity. Navigation icons are custom-drawn with thin strokes (1.5px) and no fill, except for the active state which transitions to a solid Gold fill.

### Input Fields
Fields are dark (#0F172A) with a 1px Slate (#1E293B) border. On focus, the border transitions to a glowing 1px Gold.

### Chips/Tags
Used for Categorizing Hadith or Surahs. They are small, dark-pill shapes with a thin gold border and `label-sm` text.