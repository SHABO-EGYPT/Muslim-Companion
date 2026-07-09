# Quran Android — Complete Architecture & Data Reference

This document is a comprehensive reference for the Quran Android app's architecture, data arrays, and business logic. It covers models, interfaces, hardcoded Madani data, business logic algorithms, sura names in all locales, the audio system, and the page image/text system.

---

## 1. Core Data Structures

### 1.1 `SuraAyah`

```kotlin
// common/data/.../model/SuraAyah.kt
data class SuraAyah(
  @JvmField val sura: Int,
  @JvmField val ayah: Int
) : Comparable<SuraAyah>, Serializable, QuranId {
  override fun compareTo(other: SuraAyah): Int { ... }
  fun after(next: SuraAyah): Boolean
  fun next(quranInfo: QuranInfo): SuraAyah?
  fun prev(quranInfo: QuranInfo): SuraAyah?
  fun id(quranInfo: QuranInfo): Int

  companion object {
    fun min(a: SuraAyah, b: SuraAyah): SuraAyah
    fun max(a: SuraAyah, b: SuraAyah): SuraAyah
  }
}
```

### 1.2 `VerseRange`

```kotlin
// common/data/.../model/VerseRange.kt
data class VerseRange(
  @JvmField val startSura: Int,
  @JvmField val startAyah: Int,
  @JvmField val endingSura: Int,
  @JvmField val endingAyah: Int,
  @JvmField val versesInRange: Int
)
```

### 1.3 `QuranText`

```kotlin
// common/data/.../model/QuranText.kt
data class QuranText(
  val sura: Int,
  val ayah: Int,
  val text: String,
  val extraData: String? = null
)
```

### 1.4 `QuranConstants`

```kotlin
// common/data/.../core/QuranConstants.kt
object QuranConstants {
  const val NUMBER_OF_SURAS = 114
  const val PAGES_FIRST = 1
  const val FIRST_SURA = 1
  const val LAST_SURA = 114
  const val MIN_AYAH = 1
  const val MAX_AYAH = 286
  const val JUZ2_COUNT = 30
}
```

---

## 2. Data Source Interface

### 2.1 `QuranDataSource`

```kotlin
// common/data/.../source/QuranDataSource.kt
interface QuranDataSource {
  val numberOfPages: Int
  val pageForSuraArray: IntArray          // [114] — starting page for each sura
  val suraForPageArray: IntArray          // [604] — sura number for each page
  val ayahForPageArray: IntArray          // [604] — starting ayah for each page
  val pageForJuzArray: IntArray            // [30]  — starting page for each juz
  val juzDisplayPageArrayOverride: Map<Int, Int>  // page→override display juz
  val numberOfAyahsForSuraArray: IntArray  // [114] — ayah count per sura
  val isMakkiBySuraArray: BooleanArray      // [114] — true=makki, false=madani
  val quarterStartByPage: IntArray          // [604] — hizb quarter index per page (-1=none)
  val quartersArray: Array<SuraAyah>        // [240] — 240 hizb quarter positions
  val manzilPageArray: Array<Int>           // (empty for Madani)
  val haveSidelines: Boolean               // false
  val pagesToSkip: Int                     // 0
}
```

### 2.2 `PageProvider`

```kotlin
// common/data/.../source/PageProvider.kt
interface PageProvider {
  fun getDataSource(): QuranDataSource
  fun getPageSizeCalculator(displaySize: DisplaySize): PageSizeCalculator
  fun getImageVersion(): Int
  fun getImagesBaseUrl(): String
  fun getImagesZipBaseUrl(): String
  fun getPatchBaseUrl(): String
  fun getAyahInfoBaseUrl(): String
  fun getDatabasesBaseUrl(): String
  fun getAudioDatabasesBaseUrl(): String
  fun getAudioDirectoryName(): String
  fun getDatabaseDirectoryName(): String
  fun getAyahInfoDirectoryName(): String
  fun getImagesDirectoryName(): String
  fun ayahInfoDbHasGlyphData(): Boolean = false
  fun getPreviewTitle(): Int               // @StringRes
  fun getPreviewDescription(): Int         // @StringRes
  fun getPageContentType(): PageContentType = PageContentType.Image
  fun getFallbackPageType(): String? = null
  fun getQaris(): List<Qari>
  fun getDefaultQariId(): Int
  fun pageType(): String = ""
}
```

---

## 3. Hardcoded Madani Data Arrays

All arrays come from `MadaniDataSource` (`pages/data/madani/.../MadaniDataSource.kt`).

```kotlin
open class MadaniDataSource : QuranDataSource {
  override val numberOfPages = 604
```

### 3.1 `pageForSuraArray` (size 114)

Starting page for each sura (index 0 = sura 1):

```kotlin
/*   1 - 10 */ 1, 2, 50, 77, 106, 128, 151, 177, 187, 208,
/*  11 - 20 */ 221, 235, 249, 255, 262, 267, 282, 293, 305, 312,
/*  21 - 30 */ 322, 332, 342, 350, 359, 367, 377, 385, 396, 404,
/*  31 - 40 */ 411, 415, 418, 428, 434, 440, 446, 453, 458, 467,
/*  41 - 50 */ 477, 483, 489, 496, 499, 502, 507, 511, 515, 518,
/*  51 - 60 */ 520, 523, 526, 528, 531, 534, 537, 542, 545, 549,
/*  61 - 70 */ 551, 553, 554, 556, 558, 560, 562, 564, 566, 568,
/*  71 - 80 */ 570, 572, 574, 575, 577, 578, 580, 582, 583, 585,
/*  81 - 90 */ 586, 587, 587, 589, 590, 591, 591, 592, 593, 594,
/*  91-100 */ 595, 595, 596, 596, 597, 597, 598, 598, 599, 599,
/* 101-110 */ 600, 600, 601, 601, 601, 602, 602, 602, 603, 603,
/* 111-114 */ 603, 604, 604, 604
```

### 3.2 `suraForPageArray` (size 604)

Sura number for each of the 604 pages (index 0 = page 1):

```kotlin
1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
2, 2, 2, 2, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3,
3, 3, 3, 3, 3, 3, 3, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4,
4, 4, 4, 4, 4, 4, 4, 4, 4, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5,
5, 5, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 7, 7, 7,
7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 8, 8, 8, 8,
8, 8, 8, 8, 8, 8, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 10,
10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 11, 11, 11, 11, 11, 11, 11,
11, 11, 11, 11, 11, 11, 11, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12, 12,
13, 13, 13, 13, 13, 13, 13, 14, 14, 14, 14, 14, 14, 15, 15, 15, 15, 15, 15, 16,
16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 17, 17, 17, 17, 17, 17, 17,
17, 17, 17, 17, 17, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 19, 19, 19, 19,
19, 19, 19, 19, 20, 20, 20, 20, 20, 20, 20, 20, 20, 21, 21, 21, 21, 21, 21, 21,
21, 21, 21, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 23, 23, 23, 23, 23, 23, 23,
23, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 25, 25, 25, 25, 25, 25, 25, 26, 26,
26, 26, 26, 26, 26, 26, 26, 26, 27, 27, 27, 27, 27, 27, 27, 27, 27, 28, 28, 28,
28, 28, 28, 28, 28, 28, 28, 28, 29, 29, 29, 29, 29, 29, 29, 29, 30, 30, 30, 30,
30, 30, 31, 31, 31, 31, 32, 32, 32, 33, 33, 33, 33, 33, 33, 33, 33, 33, 33, 34,
34, 34, 34, 34, 34, 34, 35, 35, 35, 35, 35, 35, 36, 36, 36, 36, 36, 37, 37, 37,
37, 37, 37, 37, 38, 38, 38, 38, 38, 38, 39, 39, 39, 39, 39, 39, 39, 39, 39, 40,
40, 40, 40, 40, 40, 40, 40, 40, 41, 41, 41, 41, 41, 41, 42, 42, 42, 42, 42, 42,
42, 43, 43, 43, 43, 43, 43, 44, 44, 44, 45, 45, 45, 45, 46, 46, 46, 46, 47, 47,
47, 47, 48, 48, 48, 48, 48, 49, 49, 50, 50, 50, 51, 51, 51, 52, 52, 53, 53, 53,
54, 54, 54, 55, 55, 55, 56, 56, 56, 57, 57, 57, 57, 58, 58, 58, 58, 59, 59, 59,
60, 60, 60, 61, 62, 62, 63, 64, 64, 65, 65, 66, 66, 67, 67, 67, 68, 68, 69, 69,
70, 70, 71, 72, 72, 73, 73, 74, 74, 75, 76, 76, 77, 78, 78, 79, 80, 81, 82, 83,
83, 85, 86, 87, 89, 89, 91, 92, 95, 97, 98, 100, 103, 106, 109, 112
```

### 3.3 `ayahForPageArray` (size 604)

Starting ayah for each of the 604 pages (index 0 = page 1):

```kotlin
1, 1, 6, 17, 25, 30, 38, 49, 58, 62, 70, 77, 84, 89,
94, 102, 106, 113, 120, 127, 135, 142, 146, 154, 164, 170, 177, 182,
187, 191, 197, 203, 211, 216, 220, 225, 231, 234, 238, 246, 249, 253,
257, 260, 265, 270, 275, 282, 283, 1, 10, 16, 23, 30, 38, 46,
53, 62, 71, 78, 84, 92, 101, 109, 116, 122, 133, 141, 149, 154,
158, 166, 174, 181, 187, 195, 1, 7, 12, 15, 20, 24, 27, 34,
38, 45, 52, 60, 66, 75, 80, 87, 92, 95, 102, 106, 114, 122,
128, 135, 141, 148, 155, 163, 171, 176, 3, 6, 10, 14, 18, 24,
32, 37, 42, 46, 51, 58, 65, 71, 77, 83, 90, 96, 104, 109,
114, 1, 9, 19, 28, 36, 45, 53, 60, 69, 74, 82, 91, 95,
102, 111, 119, 125, 132, 138, 143, 147, 152, 158, 1, 12, 23, 31,
38, 44, 52, 58, 68, 74, 82, 88, 96, 105, 121, 131, 138, 144,
150, 156, 160, 164, 171, 179, 188, 196, 1, 9, 17, 26, 34, 41,
46, 53, 62, 70, 1, 7, 14, 21, 27, 32, 37, 41, 48, 55,
62, 69, 73, 80, 87, 94, 100, 107, 112, 118, 123, 1, 7, 15,
21, 26, 34, 43, 54, 62, 71, 79, 89, 98, 107, 6, 13, 20,
29, 38, 46, 54, 63, 72, 82, 89, 98, 109, 118, 5, 15, 23,
31, 38, 44, 53, 64, 70, 79, 87, 96, 104, 1, 6, 14, 19,
29, 35, 43, 6, 11, 19, 25, 34, 43, 1, 16, 32, 52, 71,
91, 7, 15, 27, 35, 43, 55, 65, 73, 80, 88, 94, 103, 111,
119, 1, 8, 18, 28, 39, 50, 59, 67, 76, 87, 97, 105, 5,
16, 21, 28, 35, 46, 54, 62, 75, 84, 98, 1, 12, 26, 39,
52, 65, 77, 96, 13, 38, 52, 65, 77, 88, 99, 114, 126, 1,
11, 25, 36, 45, 58, 73, 82, 91, 102, 1, 6, 16, 24, 31,
39, 47, 56, 65, 73, 1, 18, 28, 43, 60, 75, 90, 105, 1,
11, 21, 28, 32, 37, 44, 54, 59, 62, 3, 12, 21, 33, 44,
56, 68, 1, 20, 40, 61, 84, 112, 137, 160, 184, 207, 1, 14,
23, 36, 45, 56, 64, 77, 89, 6, 14, 22, 29, 36, 44, 51,
60, 71, 78, 85, 7, 15, 24, 31, 39, 46, 53, 64, 6, 16,
25, 33, 42, 51, 1, 12, 20, 29, 1, 12, 21, 1, 7, 16,
23, 31, 36, 44, 51, 55, 63, 1, 8, 15, 23, 32, 40, 49,
4, 12, 19, 31, 39, 45, 13, 28, 41, 55, 71, 1, 25, 52,
77, 103, 127, 154, 1, 17, 27, 43, 62, 84, 6, 11, 22, 32,
41, 48, 57, 68, 75, 8, 17, 26, 34, 41, 50, 59, 67, 78,
1, 12, 21, 30, 39, 47, 1, 11, 16, 23, 32, 45, 52, 11,
23, 34, 48, 61, 74, 1, 19, 40, 1, 14, 23, 33, 6, 15,
21, 29, 1, 12, 20, 30, 1, 10, 16, 24, 29, 5, 12, 1,
16, 36, 7, 31, 52, 15, 32, 1, 27, 45, 7, 28, 50, 17,
41, 68, 17, 51, 77, 4, 12, 19, 25, 1, 7, 12, 22, 4,
10, 17, 1, 6, 12, 6, 1, 9, 5, 1, 10, 1, 6, 1,
8, 1, 13, 27, 16, 43, 9, 35, 11, 40, 11, 1, 14, 1,
20, 18, 48, 20, 6, 26, 20, 1, 31, 16, 1, 1, 1, 7,
35, 1, 1, 16, 1, 24, 1, 15, 1, 1, 8, 10, 1, 1,
1, 1
```

### 3.4 `pageForJuzArray` (size 30)

Starting page for each of the 30 juz:

```kotlin
/*  1 - 10 */ 1, 22, 42, 62, 82, 102, 121, 142, 162, 182,
/* 11 - 20 */ 201, 222, 242, 262, 282, 302, 322, 342, 362, 382,
/* 21 - 30 */ 402, 422, 442, 462, 482, 502, 522, 542, 562, 582
```

### 3.5 `juzDisplayPageArrayOverride`

Two pages override the displayed juz' number (the "title" juz' at the top of the page differs from the actual juz' that starts on that page):

```kotlin
mapOf(121 to 6, 201 to 10)
```

- Page 121 → displayed as Juz' 6 (actually starts Juz' 7)
- Page 201 → displayed as Juz' 10 (actually starts Juz' 11)

### 3.6 `numberOfAyahsForSuraArray` (size 114)

```kotlin
/*   1 - 14 */ 7, 286, 200, 176, 120, 165, 206, 75, 129, 109, 123, 111, 43, 52,
/*  15 - 28 */ 99, 128, 111, 110, 98, 135, 112, 78, 118, 64, 77, 227, 93, 88,
/*  29 - 42 */ 69, 60, 34, 30, 73, 54, 45, 83, 182, 88, 75, 85, 54, 53,
/*  43 - 56 */ 89, 59, 37, 35, 38, 29, 18, 45, 60, 49, 62, 55, 78, 96,
/*  57 - 70 */ 29, 22, 24, 13, 14, 11, 11, 18, 12, 12, 30, 52, 52, 44,
/*  71 - 84 */ 28, 28, 20, 56, 40, 31, 50, 40, 46, 42, 29, 19, 36, 25,
/*  85 - 98 */ 22, 17, 19, 26, 30, 20, 15, 21, 11, 8, 8, 19, 5, 8,
/* 99 - 114 */ 8, 11, 11, 8, 3, 9, 5, 4, 7, 3, 6, 3, 5, 4, 5, 6
```

### 3.7 `isMakkiBySuraArray` (size 114, true=makki, false=madani)

```kotlin
/*   1 - 10 */  true, false, false, false, false, true, true, false, false, true,
/*  11 - 20 */  true, true, false, true, true, true, true, true, true, true,
/*  21 - 30 */  true, false, true, false, true, true, true, true, true, true,
/*  31 - 40 */  true, true, false, true, true, true, true, true, true, true,
/*  41 - 50 */  true, true, true, true, true, true, false, false, false, true,
/*  51 - 60 */  true, true, true, true, false, true, false, false, false, false,
/*  61 - 70 */  false, false, false, false, false, false, true, true, true, true,
/*  71 - 80 */  true, true, true, true, true, false, true, true, true, true,
/*  81 - 90 */  true, true, true, true, true, true, true, true, true, true,
/*  91-100 */  true, true, true, true, true, true, true, false, false, true,
/* 101-110 */  true, true, true, true, true, true, true, true, true, false,
/* 111-114 */  true, true, true, true
```

### 3.8 `quarterStartByPage` (size 604)

Hizb quarter index (+1) for each page. `-1` means no quarter starts on that page. Indices are 1-based (1..240):

```kotlin
-1, -1, -1, -1, 1, -1, 2, -1, 3,
-1, 4, -1, -1, 5, -1, -1, 6, -1, 7, -1, -1, 8, -1, 9, -1, -1, 10, -1, 11, -1,
-1, 12, -1, 13, -1, -1, 14, -1, 15, -1, -1, 16, -1, 17, -1, 18, -1, -1, 19,
-1, 20, -1, -1, 21, -1, 22, -1, -1, 23, -1, -1, 24, -1, 25, -1, -1, 26, -1,
27, -1, -1, 28, -1, 29, -1, -1, 30, -1, 31, -1, -1, 32, -1, 33, -1, -1, 34,
-1, 35, -1, -1, 36, -1, 37, -1, -1, 38, -1, -1, 39, -1, 40, -1, 41, -1, 42,
-1, -1, 43, -1, -1, 44, -1, 45, -1, -1, 46, -1, 47, -1, 48, -1, -1, 49, -1,
50, -1, -1, 51, -1, -1, 52, -1, 53, -1, -1, 54, -1, -1, 55, -1, 56, -1, 57,
-1, 58, -1, 59, -1, -1, 60, -1, -1, 61, -1, 62, -1, 63, -1, -1, -1, 64, -1,
65, -1, -1, 66, -1, -1, 67, -1, -1, 68, -1, 69, -1, 70, -1, 71, -1, -1, 72,
-1, 73, -1, -1, 74, -1, 75, -1, -1, 76, -1, 77, -1, 78, -1, -1, 79, -1, 80,
-1, -1, 81, -1, 82, -1, -1, 83, -1, -1, 84, -1, 85, -1, -1, 86, -1, 87, -1,
-1, 88, -1, 89, -1, 90, -1, 91, -1, -1, 92, -1, 93, -1, -1, 94, -1, 95, -1,
-1, -1, 96, -1, 97, -1, -1, 98, -1, 99, -1, -1, 100, -1, 101, -1, 102, -1,
-1, 103, -1, -1, 104, -1, 105, -1, -1, 106, -1, -1, 107, -1, 108, -1, -1,
109, -1, 110, -1, -1, 111, -1, 112, -1, 113, -1, -1, 114, -1, 115, -1, -1,
116, -1, -1, 117, -1, 118, -1, 119, -1, -1, 120, -1, 121, -1, 122, -1, -1,
123, -1, -1, 124, -1, -1, 125, -1, 126, -1, 127, -1, -1, 128, -1, 129, -1,
130, -1, -1, 131, -1, -1, 132, -1, 133, -1, 134, -1, -1, 135, -1, -1, 136,
-1, 137, -1, -1, 138, -1, -1, 139, -1, 140, -1, 141, -1, 142, -1, -1, 143,
-1, -1, 144, -1, 145, -1, -1, 146, -1, 147, -1, 148, -1, -1, 149, -1, -1,
150, -1, 151, -1, -1, 152, -1, 153, -1, 154, -1, -1, 155, -1, -1, 156, -1,
157, -1, 158, -1, -1, 159, -1, -1, 160, -1, 161, -1, -1, 162, -1, -1, 163,
-1, -1, 164, -1, 165, -1, -1, 166, -1, 167, -1, 168, -1, -1, 169, 170, -1,
-1, 171, -1, 172, -1, 173, -1, -1, 174, -1, -1, 175, -1, -1, 176, -1, 177,
-1, 178, -1, -1, 179, -1, 180, -1, -1, 181, -1, 182, -1, -1, 183, -1, -1,
184, -1, 185, -1, -1, 186, -1, 187, -1, -1, 188, -1, 189, -1, -1, 190, -1,
191, -1, -1, 192, -1, 193, -1, 194, -1, 195, -1, -1, 196, -1, 197, -1, -1,
198, -1, -1, 199, -1, -1, 200, -1, -1, 201, -1, 202, -1, -1, 203, -1, -1,
204, -1, 205, -1, 206, -1, 207, -1, -1, 208, -1, 209, -1, 210, -1, -1, 211,
-1, 212, -1, -1, 213, -1, 214, -1, -1, 215, -1, -1, 216, -1, 217, -1, -1, 218,
-1, -1, 219, -1, -1, 220, 221, -1, -1, -1, 222, -1, 223, -1, 224, -1, 225, -1,
226, -1, -1, 227, -1, -1, 228, -1, -1, 229, -1, 230, -1, 231, -1, -1, 232, -1,
-1, 233, -1, 234, -1, 235, -1, 236, -1, -1, 237, -1, 238, -1, -1, 239, -1, -1,
-1, -1, -1
```

### 3.9 `quartersArray` (size 240)

Each hizb quarter is a `SuraAyah(sura, ayah)`. There are 60 hizbs × 4 quarters = 240 entries:

```kotlin
arrayOf(
  /* hizb 1  */ SuraAyah(1, 1), SuraAyah(2, 26), SuraAyah(2, 44), SuraAyah(2, 60),
  /* hizb 2  */ SuraAyah(2, 75), SuraAyah(2, 92), SuraAyah(2, 106), SuraAyah(2, 124),
  /* hizb 3  */ SuraAyah(2, 142), SuraAyah(2, 158), SuraAyah(2, 177), SuraAyah(2, 189),
  /* hizb 4  */ SuraAyah(2, 203), SuraAyah(2, 219), SuraAyah(2, 233), SuraAyah(2, 243),
  /* hizb 5  */ SuraAyah(2, 253), SuraAyah(2, 263), SuraAyah(2, 272), SuraAyah(2, 283),
  /* hizb 6  */ SuraAyah(3, 15), SuraAyah(3, 33), SuraAyah(3, 52), SuraAyah(3, 75),
  /* hizb 7  */ SuraAyah(3, 93), SuraAyah(3, 113), SuraAyah(3, 133), SuraAyah(3, 153),
  /* hizb 8  */ SuraAyah(3, 171), SuraAyah(3, 186), SuraAyah(4, 1), SuraAyah(4, 12),
  /* hizb 9  */ SuraAyah(4, 24), SuraAyah(4, 36), SuraAyah(4, 58), SuraAyah(4, 74),
  /* hizb 10 */ SuraAyah(4, 88), SuraAyah(4, 100), SuraAyah(4, 114), SuraAyah(4, 135),
  /* hizb 11 */ SuraAyah(4, 148), SuraAyah(4, 163), SuraAyah(5, 1), SuraAyah(5, 12),
  /* hizb 12 */ SuraAyah(5, 27), SuraAyah(5, 41), SuraAyah(5, 51), SuraAyah(5, 67),
  /* hizb 13 */ SuraAyah(5, 82), SuraAyah(5, 97), SuraAyah(5, 109), SuraAyah(6, 13),
  /* hizb 14 */ SuraAyah(6, 36), SuraAyah(6, 59), SuraAyah(6, 74), SuraAyah(6, 95),
  /* hizb 15 */ SuraAyah(6, 111), SuraAyah(6, 127), SuraAyah(6, 141), SuraAyah(6, 151),
  /* hizb 16 */ SuraAyah(7, 1), SuraAyah(7, 31), SuraAyah(7, 47), SuraAyah(7, 65),
  /* hizb 17 */ SuraAyah(7, 88), SuraAyah(7, 117), SuraAyah(7, 142), SuraAyah(7, 156),
  /* hizb 18 */ SuraAyah(7, 171), SuraAyah(7, 189), SuraAyah(8, 1), SuraAyah(8, 22),
  /* hizb 19 */ SuraAyah(8, 41), SuraAyah(8, 61), SuraAyah(9, 1), SuraAyah(9, 19),
  /* hizb 20 */ SuraAyah(9, 34), SuraAyah(9, 46), SuraAyah(9, 60), SuraAyah(9, 75),
  /* hizb 21 */ SuraAyah(9, 93), SuraAyah(9, 111), SuraAyah(9, 122), SuraAyah(10, 11),
  /* hizb 22 */ SuraAyah(10, 26), SuraAyah(10, 53), SuraAyah(10, 71), SuraAyah(10, 90),
  /* hizb 23 */ SuraAyah(11, 6), SuraAyah(11, 24), SuraAyah(11, 41), SuraAyah(11, 61),
  /* hizb 24 */ SuraAyah(11, 84), SuraAyah(11, 108), SuraAyah(12, 7), SuraAyah(12, 30),
  /* hizb 25 */ SuraAyah(12, 53), SuraAyah(12, 77), SuraAyah(12, 101), SuraAyah(13, 5),
  /* hizb 26 */ SuraAyah(13, 19), SuraAyah(13, 35), SuraAyah(14, 10), SuraAyah(14, 28),
  /* hizb 27 */ SuraAyah(15, 1), SuraAyah(15, 49), SuraAyah(16, 1), SuraAyah(16, 30),
  /* hizb 28 */ SuraAyah(16, 51), SuraAyah(16, 75), SuraAyah(16, 90), SuraAyah(16, 111),
  /* hizb 29 */ SuraAyah(17, 1), SuraAyah(17, 23), SuraAyah(17, 50), SuraAyah(17, 70),
  /* hizb 30 */ SuraAyah(17, 99), SuraAyah(18, 17), SuraAyah(18, 32), SuraAyah(18, 51),
  /* hizb 31 */ SuraAyah(18, 75), SuraAyah(18, 99), SuraAyah(19, 22), SuraAyah(19, 59),
  /* hizb 32 */ SuraAyah(20, 1), SuraAyah(20, 55), SuraAyah(20, 83), SuraAyah(20, 111),
  /* hizb 33 */ SuraAyah(21, 1), SuraAyah(21, 29), SuraAyah(21, 51), SuraAyah(21, 83),
  /* hizb 34 */ SuraAyah(22, 1), SuraAyah(22, 19), SuraAyah(22, 38), SuraAyah(22, 60),
  /* hizb 35 */ SuraAyah(23, 1), SuraAyah(23, 36), SuraAyah(23, 75), SuraAyah(24, 1),
  /* hizb 36 */ SuraAyah(24, 21), SuraAyah(24, 35), SuraAyah(24, 53), SuraAyah(25, 1),
  /* hizb 37 */ SuraAyah(25, 21), SuraAyah(25, 53), SuraAyah(26, 1), SuraAyah(26, 52),
  /* hizb 38 */ SuraAyah(26, 111), SuraAyah(26, 181), SuraAyah(27, 1), SuraAyah(27, 27),
  /* hizb 39 */ SuraAyah(27, 56), SuraAyah(27, 82), SuraAyah(28, 12), SuraAyah(28, 29),
  /* hizb 40 */ SuraAyah(28, 51), SuraAyah(28, 76), SuraAyah(29, 1), SuraAyah(29, 26),
  /* hizb 41 */ SuraAyah(29, 46), SuraAyah(30, 1), SuraAyah(30, 31), SuraAyah(30, 54),
  /* hizb 42 */ SuraAyah(31, 22), SuraAyah(32, 11), SuraAyah(33, 1), SuraAyah(33, 18),
  /* hizb 43 */ SuraAyah(33, 31), SuraAyah(33, 51), SuraAyah(33, 60), SuraAyah(34, 10),
  /* hizb 44 */ SuraAyah(34, 24), SuraAyah(34, 46), SuraAyah(35, 15), SuraAyah(35, 41),
  /* hizb 45 */ SuraAyah(36, 28), SuraAyah(36, 60), SuraAyah(37, 22), SuraAyah(37, 83),
  /* hizb 46 */ SuraAyah(37, 145), SuraAyah(38, 21), SuraAyah(38, 52), SuraAyah(39, 8),
  /* hizb 47 */ SuraAyah(39, 32), SuraAyah(39, 53), SuraAyah(40, 1), SuraAyah(40, 21),
  /* hizb 48 */ SuraAyah(40, 41), SuraAyah(40, 66), SuraAyah(41, 9), SuraAyah(41, 25),
  /* hizb 49 */ SuraAyah(41, 47), SuraAyah(42, 13), SuraAyah(42, 27), SuraAyah(42, 51),
  /* hizb 50 */ SuraAyah(43, 24), SuraAyah(43, 57), SuraAyah(44, 17), SuraAyah(45, 12),
  /* hizb 51 */ SuraAyah(46, 1), SuraAyah(46, 21), SuraAyah(47, 10), SuraAyah(47, 33),
  /* hizb 52 */ SuraAyah(48, 18), SuraAyah(49, 1), SuraAyah(49, 14), SuraAyah(50, 27),
  /* hizb 53 */ SuraAyah(51, 31), SuraAyah(52, 24), SuraAyah(53, 26), SuraAyah(54, 9),
  /* hizb 54 */ SuraAyah(55, 1), SuraAyah(56, 1), SuraAyah(56, 75), SuraAyah(57, 16),
  /* hizb 55 */ SuraAyah(58, 1), SuraAyah(58, 14), SuraAyah(59, 11), SuraAyah(60, 7),
  /* hizb 56 */ SuraAyah(62, 1), SuraAyah(63, 4), SuraAyah(65, 1), SuraAyah(66, 1),
  /* hizb 57 */ SuraAyah(67, 1), SuraAyah(68, 1), SuraAyah(69, 1), SuraAyah(70, 19),
  /* hizb 58 */ SuraAyah(72, 1), SuraAyah(73, 20), SuraAyah(75, 1), SuraAyah(76, 19),
  /* hizb 59 */ SuraAyah(78, 1), SuraAyah(80, 1), SuraAyah(82, 1), SuraAyah(84, 1),
  /* hizb 60 */ SuraAyah(87, 1), SuraAyah(90, 1), SuraAyah(94, 1), SuraAyah(100, 9))
```

### 3.10 Other Madani Data Source Values

```kotlin
override val manzilPageArray: Array<Int> = emptyArray()
override val haveSidelines: Boolean = false
override val pagesToSkip: Int = 0
```

---

## 4. Business Logic (`QuranInfo`)

```kotlin
// common/data/.../core/QuranInfo.kt
class QuranInfo @Inject constructor(quranDataSource: QuranDataSource) {
```

### 4.1 Properties

```kotlin
val suraPageStart = quranDataSource.pageForSuraArray          // [114]
private val pageSuraStart = quranDataSource.suraForPageArray    // [604]
private val pageAyahStart = quranDataSource.ayahForPageArray    // [604]
private val juzPageStart = quranDataSource.pageForJuzArray      // [30]
private val juzPageOverride: Map<Int, Int> = quranDataSource.juzDisplayPageArrayOverride
private val pageRub3Start = quranDataSource.quarterStartByPage  // [604]
private val suraNumAyahs = quranDataSource.numberOfAyahsForSuraArray  // [114]
private val suraIsMakki = quranDataSource.isMakkiBySuraArray    // [114]
private val manazil = quranDataSource.manzilPageArray
val quarters = quranDataSource.quartersArray                    // [240]

val skip = quranDataSource.pagesToSkip  // 0 for Madani
private val firstPage = PAGES_FIRST + skip  // 1
val numberOfPages = quranDataSource.numberOfPages  // 604
val numberOfPagesConsideringSkipped = numberOfPages - skip  // 604
private val numberOfPagesDual = numberOfPagesConsideringSkipped / 2 +
    numberOfPagesConsideringSkipped % 2  // 302
```

### 4.2 Key Methods

#### Page ↔ Sura/Ayah

```kotlin
fun getPageNumberForSura(sura: Int): Int
  // → suraPageStart[sura - 1]  (O(1), direct lookup)

fun getSuraNumberFromPage(page: Int): Int
  // → Linear scan of suraPageStart[0..NUMBER_OF_SURAS),
  //   returns first sura whose start page > page (O(114))

fun getSuraOnPage(page: Int) = pageSuraStart[page - 1]  // O(1)

fun getFirstAyahOnPage(page: Int): Int = pageAyahStart[page - 1]  // O(1)

fun getPageFromSuraAyah(sura: Int, ayah: Int): Int
  // → Binary-search-style linear walk from suraPageStart[sura-1]-1
  //   through pages until pageSuraStart[index] > sura or
  //   (pageSuraStart[index] == sura && pageAyahStart[index] > ayah)

fun getPageBounds(inputPage: Int): IntArray
  // → Returns [startSura, startAyah, endSura, endAyah]
  //   Clamps input between firstPage and numberOfPages.
  //   Special-cases last page (Sura 114, ayah 6).
  //   If next page is same sura, endAyah = nextPageAyah - 1.
  //   If next page is next sura with ayah>1, endAyah = nextPageAyah - 1.
  //   Otherwise endAyah = suraNumAyahs of previous sura.

fun getVerseRangeForPage(page: Int): VerseRange
  // → Uses getPageBounds() and computes versesInRange via getAyahId diff.
```

#### Juz'

```kotlin
fun getStartingPageForJuz(juz: Int): Int = juzPageStart[juz - 1]  // O(1)

fun getJuzFromPage(page: Int): Int
  // → Linear scan of juzPageStart, returns first juz where start > page,
  //   or juz where start == page (returns i+1). Defaults to 30. (O(30))

fun getJuzForDisplayFromPage(page: Int): Int
  // → Returns actual juz unless page is in juzPageOverride (pages 121→6, 201→10)

fun getJuzFromSuraAyah(sura: Int, ayah: Int, juz: Int): Int
  // → Uses quarters[juz * 8] to get the starting point of next juz.
  //   If sura/ayah >= that start point, returns juz+1, else returns juz.
```

#### Global Ayah ID

```kotlin
fun getAyahId(sura: Int, ayah: Int): Int
  // → Sum of suraNumAyahs[0..sura-2] + ayah (O(114))

fun getSuraAyahFromAyahId(ayahId: Int): SuraAyah
  // → Subtracts suraNumAyahs[sura] until ayahIdentifier <= 0 (O(114))

fun diff(start: SuraAyah, end: SuraAyah): Int
  // → getAyahId(end) - getAyahId(start)
```

#### Quran Info

```kotlin
fun getNumberOfAyahs(sura: Int): Int = suraNumAyahs[sura - 1]  // O(1), -1 if out of bounds
fun getNumberOfAyahsInQuran(): Int = suraNumAyahs.sum()         // 6236
fun isValidPage(page: Int): Boolean = page in firstPage..numberOfPages
fun isMakki(sura: Int) = suraIsMakki[sura - 1]
fun manzilForPage(page: Int): Int = manazil.indexOfFirst { it > page }
  // → Returns -1 always for Madani (emptyArray)
```

#### ViewPager Position Mapping

The app uses a right-to-left ViewPager (Quran reads right-to-left). Dual-page tablet layout maps pages as `[right][left]`.

```kotlin
fun getPageFromPosition(position: Int, isDualPagesVisible: Boolean): Int {
  // Dual:   ((numberOfPagesDual - position) * 2 + skip) - 1
  // Single: (numberOfPagesConsideringSkipped - position) + skip
}

fun getPositionFromPage(page: Int, isDualPagesVisible: Boolean): Int {
  // Dual:   numberOfPagesDual - (odd? page/2 + 1 : page/2) + (odd? skip : 0)
  // Single: (numberOfPagesConsideringSkipped - page) + skip
}

fun mapDualPageToSinglePage(page: Int): Int
fun mapSinglePageToDualPage(page: Int): Int
```

#### Quarter/Rub' El-Hizb

```kotlin
fun getRub3FromPage(page: Int): Int = pageRub3Start[page - 1]  // O(1), -1 if none
fun getQuarterByIndex(quarter: Int) = quarters[quarter]
```

#### List of Suras Starting on Page

```kotlin
fun getListOfSurahWithStartingOnPage(page: Int): List<Int>
  // → Iterates suraPageStart from startIndex until > page
```

---

## 5. Madani Page Provider (`MadaniPageProvider`)

```kotlin
// pages/madani/.../provider/madani/MadaniPageProvider.kt
class MadaniPageProvider : PageProvider {
  override fun getDataSource() = dataSource  // lazy MadaniDataSource
  override fun getPageSizeCalculator(displaySize: DisplaySize) = DefaultPageSizeCalculator(displaySize)
  override fun getImageVersion() = 8

  override fun getImagesBaseUrl() = "https://android.quran.com/data/"
  override fun getImagesZipBaseUrl() = "https://android.quran.com/data/zips/"
  override fun getPatchBaseUrl() = "https://android.quran.com/data/patches/v"
  override fun getAyahInfoBaseUrl() = "https://android.quran.com/data/databases/ayahinfo/"
  override fun getDatabasesBaseUrl() = "https://android.quran.com/data/databases/"
  override fun getAudioDatabasesBaseUrl() = "https://android.quran.com/data/databases/audio/"
  override fun getImagesDirectoryName() = ""
  override fun getAudioDirectoryName() = "audio"
  override fun getDatabaseDirectoryName() = "databases"
  override fun getAyahInfoDirectoryName() = getDatabaseDirectoryName()

  override fun getPreviewTitle() = R.string.madani_title
  override fun getPreviewDescription() = R.string.madani_description

  override fun getDefaultQariId(): Int = 0
```

### 5.1 Built-in Qari Definitions

```kotlin
override fun getQaris(): List<Qari> {
  return listOf(
    Qari(
      0,  // id
      R.string.qari_minshawi_murattal_gapless,
      url = "https://download.quranicaudio.com/quran/muhammad_siddeeq_al-minshaawee/",
      path = "minshawi_murattal",
      hasGaplessAlternative = false,
      db = "minshawi_murattal"       // → isGapless = true
    ),
    Qari(
      1,
      R.string.qari_husary_gapless,
      url = "https://download.quranicaudio.com/quran/mahmood_khaleel_al-husaree/",
      path = "husary",
      hasGaplessAlternative = false,
      db = "husary"                   // → isGapless = true
    ),
    Qari(
      2,
      R.string.qari_basfar,
      url = "https://mirrors.quranicaudio.com/everyayah/Abdullah_Basfar_192kbps/",
      path = "2",
      hasGaplessAlternative = false,
      db = null                       // → isGapless = false
    )
  )
}
```

---

## 6. Audio/Recitation System

### 6.1 `Qari` Model (canonical)

```kotlin
// common/data/.../model/audio/Qari.kt
data class Qari(
  val id: Int,
  @StringRes val nameResource: Int,  // reference to string resource for display name
  val url: String,                    // base URL for mp3 files
  val opusUrl: String? = null,       // base URL for opus files
  val path: String,                   // local directory name under audio/
  val hasGaplessAlternative: Boolean,
  val db: String? = null,            // non-null = gapless recitation, names the timing DB
) {
  val databaseName: String?  // db if non-null/non-empty
  val isGapless: Boolean     // databaseName != null
  val hasOpus: Boolean       // opusUrl != null && non-empty

  fun url(extension: String): String  // returns opusUrl for "opus", url for "mp3"
}
```

### 6.2 `QariItem` Model (Parcelable, for audio module)

```kotlin
// common/audio/.../model/QariItem.kt
@Parcelize
data class QariItem(
  val id: Int,
  val name: String,          // resolved from context.getString(nameResource)
  val url: String,
  val opusUrl: String? = null,
  val path: String,
  val hasGaplessAlternative: Boolean,
  val db: String? = null
) : Parcelable {
  val databaseName: String?
  val isGapless: Boolean
  fun hasOpus(): Boolean
  fun url(extension: String): String

  companion object {
    fun fromQari(context: Context, qari: Qari): QariItem
  }
}
```

### 6.3 `AudioRequest` Model

```kotlin
// common/audio/.../model/playback/AudioRequest.kt
@Parcelize
data class AudioRequest(
  val start: SuraAyah,
  val end: SuraAyah,
  val qari: QariItem,
  val repeatInfo: Int = 0,            // 0 = no repeat, < 0 = repeat count, > 0 = repeat mode
  val rangeRepeatInfo: Int = 0,
  val enforceBounds: Boolean,
  val playbackSpeed: Float = 1f,
  val shouldStream: Boolean,
  val audioPathInfo: AudioPathInfo,
  val wordHighlighting: Boolean = false
) : Parcelable
```

### 6.4 `AudioPathInfo` Model

```kotlin
// common/audio/.../model/playback/AudioPathInfo.kt
@Parcelize
data class AudioPathInfo(
  val urlFormat: String,              // URL pattern template
  val localDirectory: String,         // local storage directory
  val gaplessDatabase: String?,        // gapless timing DB filename (null if not gapless)
  val allowedExtensions: List<String>  // e.g. ["mp3"] or ["opus"]
) : Parcelable
```

### 6.5 Audio URL Construction Strategy

#### Gapless Recitations (db != null)

- **URL Format**: `"{baseUrl}%03d{ext}"` — single sura number, zero-padded to 3 digits
- **Extension**: `.mp3`
- **Example**: `https://download.quranicaudio.com/quran/muhammad_siddeeq_al-minshaawee/001.mp3`
- **Local file**: `{audioDirectory}/{path}/{suraNumber:03d}.mp3`
- **Gapless Database**: SQLite database named `{db}.db` stored in the audio databases directory, providing timing offsets for each ayah within each sura

#### Gapped Recitations (db == null)

- **URL Format**: `"{baseUrl}{suraNumber:03d}{ayahNumber:03d}.mp3"`
- **Each ayah is a separate file**
- **Example**: `https://mirrors.quranicaudio.com/everyayah/Abdullah_Basfar_192kbps/001001.mp3`
- **Local file**: `{audioDirectory}/{path}/{suraNumber:03d}{ayahNumber:03d}.mp3`

#### Opus Support

Qaris with `opusUrl` can use the `.opus` extension instead of `.mp3`. The `url(extension)` method selects the correct base URL based on format.

### 6.6 Qari Download Info

Built-in Qaris summary:

| ID | Name | Gapless? | DB | Base URL |
|----|------|----------|-----|---------|
| 0 | Minshawi (Murattal) | Yes | `minshawi_murattal` | `https://download.quranicaudio.com/quran/muhammad_siddeeq_al-minshaawee/` |
| 1 | Husary | Yes | `husary` | `https://download.quranicaudio.com/quran/mahmood_khaleel_al-husaree/` |
| 2 | Basfar | No | null | `https://mirrors.quranicaudio.com/everyayah/Abdullah_Basfar_192kbps/` |

### 6.7 Download Strategy

- **Streaming**: `shouldStream=true` — play directly from URL, no local storage
- **Download**: Files saved to `{audioDirectory}/{qari.path}/{filename}` on external storage
- **Gapless databases**: Downloaded from `{audioDatabasesBaseUrl}{db}.db` to `{databasesDirectory}/audio/{db}.db`

---

## 7. Sura Names — All Locales

Sura names are defined in `common/ui/core/src/main/res/values-*/sura_names.xml`. Each locale has two arrays:

- `sura_names` (string-array, 114 items) — the name as displayed
- `sura_names_translation` (string-array, 114 items) — translated meaning (optional per locale)

### 7.1 Locales Available

| Locale | Code | File |
|--------|------|------|
| Default (English with diacritics) | `values` | `sura_names.xml` |
| Arabic | `values-ar` | `sura_names.xml` |
| Bosnian | `values-bs` | `sura_names.xml` |
| German | `values-de` | `sura_names.xml` |
| Spanish | `values-es` | `sura_names.xml` |
| Persian | `values-fa` | `sura_names.xml` |
| French | `values-fr` | `sura_names.xml` |
| Croatian | `values-hr` | `sura_names.xml` |
| Hungarian | `values-hu` | `sura_names.xml` |
| Indonesian | `values-in` | `sura_names.xml` |
| Kazakh | `values-kk` | `sura_names.xml` |
| Dutch | `values-nl` | `sura_names.xml` |
| Polish | `values-pl` | `sura_names.xml` |
| Russian | `values-ru` | `sura_names.xml` |
| Serbian | `values-sr` | `sura_names.xml` |
| Turkish | `values-tr` | `sura_names.xml` |
| Uyghur | `values-ug` | `sura_names.xml` |
| Uzbek | `values-uz` | `sura_names.xml` |
| Vietnamese | `values-vi` | `sura_names.xml` |

### 7.2 English Transliteration (Default) — `sura_names`

```
1.  Al-Fātihah          2.  Al-Baqarah          3.  Āli-ʿImrān
4.  An-Nisāʾ            5.  Al-Māʾidah          6.  Al-Anʿām
7.  Al-Aʿrāf            8.  Al-Anfāl            9.  At-Tawbah
10. Yūnus               11. Hūd                 12. Yūsuf
13. Ar-Raʿd             14. Ibrāhīm             15. Al-Ḥijr
16. An-Naḥl             17. Al-Isrāʾ            18. Al-Kahf
19. Maryam              20. Ṭā-Hā               21. Al-Anbiyāʾ
22. Al-Ḥajj             23. Al-Muʾminūn         24. An-Nūr
25. Al-Furqān           26. Ash-Shuʿarāʾ        27. An-Naml
28. Al-Qaṣaṣ            29. Al-ʿAnkabūt         30. Ar-Rūm
31. Luqmān              32. As-Sajdah           33. Al-Aḥzāb
34. Sabaʾ               35. Fāṭir               36. Yā-Sīn
37. Aṣ-Ṣāffāt           38. Ṣād                 39. Az-Zumar
40. Ghāfir              41. Fuṣṣilat            42. Ash-Shūrā
43. Az-Zukhruf          44. Ad-Dukhān           45. Al-Jāthiyah
46. Al-Aḥqāf            47. Muḥammad            48. Al-Fatḥ
49. Al-Ḥujurāt          50. Qāf                 51. Adh-Dhāriyāt
52. Aṭ-Ṭūr              53. An-Najm             54. Al-Qamar
55. Ar-Raḥmān           56. Al-Wāqiʿah          57. Al-Ḥadīd
58. Al-Mujādilah        59. Al-Ḥashr            60. Al-Mumtaḥanah
61. Aṣ-Ṣaff             62. Al-Jumuʿah          63. Al-Munāfiqūn
64. At-Taghābun         65. Aṭ-Ṭalāq            66. At-Taḥrīm
67. Al-Mulk             68. Al-Qalam            69. Al-Ḥāqqah
70. Al-Maʿārij          71. Nūḥ                 72. Al-Jinn
73. Al-Muzzammil        74. Al-Muddaththir      75. Al-Qiyāmah
76. Al-Insān            77. Al-Mursalāt         78. An-Nabaʾ
79. An-Nāziʿāt          80. ʿAbasa              81. At-Takwīr
82. Al-Infiṭār          83. Al-Muṭaffifīn       84. Al-Inshiqāq
85. Al-Burūj            86. Aṭ-Ṭāriq            87. Al-Aʿlā
88. Al-Ghāshiyah        89. Al-Fajr             90. Al-Balad
91. Ash-Shams           92. Al-Layl             93. Aḍ-Ḍuḥā
94. Ash-Sharḥ           95. At-Tīn              96. Al-ʿAlaq
97. Al-Qadr             98. Al-Bayyinah         99. Az-Zalzalah
100. Al-ʿĀdiyāt         101. Al-Qāriʿah         102. At-Takāthur
103. Al-ʿAṣr            104. Al-Humazah         105. Al-Fīl
106. Quraysh            107. Al-Māʿūn           108. Al-Kawthar
109. Al-Kāfirūn         110. An-Naṣr            111. Al-Masad
112. Al-Ikhlāṣ          113. Al-Falaq           114. An-Nās
```

### 7.3 English Translation — `sura_names_translation`

```
1.  The Opening                2.  The Cow                   3.  The Family of Imran
4.  The Women                  5.  The Table Spread With Food 6.  The Cattle
7.  The Heights                8.  The Spoils of War         9.  The Repentance
10. Jonah                      11. (Prophet) Hūd            12. (Prophet) Joseph
13. The Thunder                14. Abraham                  15. The Rocky Tract
16. The Bees                   17. The Journey by Night     18. The Cave
19. Mary                       20. Ta-Ha                    21. The Prophets
22. The Pilgrimage             23. The Believers            24. The Light
25. The Criterion              26. The Poets                27. The Ants
28. The Narration              29. The Spider               30. The Romans
31. Luqmān                     32. The Prostration          33. The Confederates
34. Sheba                      35. The Originator of Creation 36. Ya Sin
37. Those Ranged in Ranks      38. The Letter "Saad"        39. The Groups
40. The Forgiver               41. They are explained in detail 42. The Consultation
43. The Gold Adornments        44. The Smoke                45. The Kneeling
46. The Curved Sand-hills      47. Muḥammad                 48. The Victory
49. The Dwellings              50. The Letter "Qaf"         51. The Winds that Scatter
52. The Mount                  53. The Star                 54. The Moon
55. The Most Gracious          56. The Event                57. Iron
58. The Woman Who Disputes     59. The Gathering            60. The Woman to be examined
61. The Row or the Rank        62. Friday                   63. The Hypocrites
64. Mutual Loss and Gain       65. The Divorce              66. The Prohibition
67. Dominion                   68. The Pen                  69. The Inevitable
70. The Ways of Ascent         71. Noah                     72. The Jinn
73. The One wrapped in Garments 74. The One Enveloped       75. The Resurrection
76. Man                        77. Those sent forth         78. The News
79. Those Who Pull Out         80. He Frowned               81. Winding Round
82. The Cleaving               83. Those Who Deal in Fraud  84. The Splitting Asunder
85. The Big Stars "Burūj"      86. The Night-Comer          87. The Most High
88. The Overwhelming           89. The Break of Day         90. The City
91. The Sun                    92. The Night                93. The Forenoon
94. The Opening Forth          95. The Fig                  96. The Clot
97. The Night of Decree        98. The Clear Evidence       99. The Earthquake
100. Those That Run            101. The Striking Hour       102. The piling Up
103. The Time                  104. The Slanderer           105. The Elephant
106. Quraish                   107. The Small Kindnesses    108. A River in Paradise
109. The Disbelievers          110. The Help                111. The Palm Fibre
112. The Purity                113. The Daybreak            114. Mankind
```

### 7.4 Arabic Names — `values-ar/sura_names.xml`

```
1.  الفَاتِحَةِ         2.  البَقَرَةِ          3.  آلِ عِمۡرَانَ
4.  النِّسَاءِ          5.  المَائـِدَةِ        6.  الأَنۡعَامِ
7.  الأَعۡرَافِ         8.  الأَنفَالِ          9.  التَّوۡبَةِ
10. يُونُسَ             11. هُودٍ              12. يُوسُفَ
13. الرَّعۡدِ           14. إِبۡرَاهِيمَ        15. الحِجۡرِ
16. النَّحۡلِ           17. الإِسۡرَاءِ        18. الكَهۡفِ
19. مَرۡيَمَ            20. طه                 21. الأَنبِيَاءِ
22. الحَجِّ             23. المُؤۡمِنُونَ      24. النُّورِ
25. الفُرۡقَانِ         26. الشُّعَرَاءِ        27. النَّمۡلِ
28. القَصَصِ            29. العَنكَبُوتِ        30. الرُّومِ
31. لُقۡمَانَ           32. السَّجۡدَةِ        33. الأَحۡزَابِ
34. سَبَإٍ              35. فَاطِرٍ            36. يسٓ
37. الصَّافَّاتِ         38. صٓ                 39. الزُّمَرِ
40. غَافِرٍ             41. فُصِّلَتۡ          42. الشُّورَىٰ
43. الزُّخۡرُفِ         44. الدُّخَانِ          45. الجَاثِيَةِ
46. الأَحۡقَافِ         47. مُحَمَّدٍ          48. الفَتۡحِ
49. الحُجُرَاتِ         50. قٓ                 51. الذَّارِيَاتِ
52. الطُّورِ             53. النَّجۡمِ          54. القَمَرِ
55. الرَّحۡمَٰن         56. الوَاقِعَةِ         57. الحَدِيدِ
58. المُجَادلَةِ         59. الحَشۡرِ           60. المُمۡتَحنَةِ
61. الصَّفِّ             62. الجُمُعَةِ         63. المُنَافِقُونَ
64. التَّغَابُنِ         65. الطَّلَاقِ          66. التَّحۡرِيمِ
67. المُلۡكِ             68. القَلَمِ           69. الحَاقَّةِ
70. المَعَارِجِ         71. نُوحٍ              72. الجِنِّ
73. المُزَّمِّلِ         74. المُدَّثِّرِ        75. القِيَامَةِ
76. الإِنسَانِ           77. المُرۡسَلَاتِ      78. النَّبَإِ
79. النَّازِعَاتِ       80. عَبَسَ             81. التَّكۡوِيرِ
82. الانفِطَارِ          83. المُطَفِّفِينَ     84. الانشِقَاقِ
85. البُرُوجِ            86. الطَّارِقِ         87. الأَعۡلَىٰ
88. الغَاشِيَةِ         89. الفَجۡرِ           90. البَلَدِ
91. الشَّمۡسِ           92. اللَّيۡلِ          93. الضُّحَىٰ
94. الشَّرۡحِ           95. التِّينِ           96. العَلَقِ
97. القَدۡرِ            98. البَيِّنَةِ         99. الزَّلۡزَلَةِ
100. العَادِيَاتِ       101. القَارِعَةِ        102. التَّكَاثُرِ
103. العَصۡرِ           104. الهُمَزَةِ         105. الفِيلِ
106. قُرَيۡشٍ           107. المَاعُونِ         108. الكَوۡثَرِ
109. الكَافِرُونَ       110. النَّصۡرِ         111. المَسَدِ
112. الإِخۡلَاصِ        113. الفَلَقِ           114. النَّاسِ
```

The remaining 17 locale files follow the same structure with names translated into their respective languages.

---

## 8. Page Image System

### 8.1 Image Versioning

```kotlin
override fun getImageVersion() = 8
```

Images follow the version number. When images are updated, the version is incremented to force re-download.

### 8.2 Image URL Pattern

- **Base**: `https://android.quran.com/data/`
- **Individual pages**: `{imagesBaseUrl}page{page}.png` (via `getImagesBaseUrl()`)
- **Zip downloads**: `{imagesZipBaseUrl}{type}_pages{version}.zip`
  - Example: `https://android.quran.com/data/zips/madani_pages8.zip`
- **Patches**: `{patchBaseUrl}{version}/` (differential updates between image versions)

### 8.3 Local Storage

- **Images directory**: `{app files}/{imagesDirectoryName}/` (empty string = root data dir)
- Files named `page{number}.png`
- Zips extracted to the images directory

### 8.4 `PageSizeCalculator`

```kotlin
class DefaultPageSizeCalculator(displaySize: DisplaySize) : PageSizeCalculator
```

Handles dimension calculations for rendering pages at correct aspect ratios based on device display size.

### 8.5 Database URLs

- **Ayah info DB**: `{ayahInfoBaseUrl}ayahinfo_{type}.db` → `{databasesDirectory}/ayahinfo_{type}.db`
- **Audio Gapless DB**: `{audioDatabasesBaseUrl}{db}.db` → `{databasesDirectory}/audio/{db}.db`
- **Base databases**: `{databasesBaseUrl}`

---

## 9. Implementation Guide Summary

### 9.1 To Implement in a New App

1. **Create `MadaniDataSource`** implementing `QuranDataSource` — paste all arrays above
2. **Create `MadaniPageProvider`** implementing `PageProvider` — set image version, base URLs, and qaris
3. **Create `QuranInfo`** — inject `QuranDataSource`, all business logic is already implemented
4. **Audio Module** — implement `AudioRequest`/`AudioPathInfo` construction based on qari type (gapless vs gapped)
5. **Page Rendering** — use `PageProvider.getImageVersion()` + `getImagesBaseUrl()` for image URLs, or implement text rendering via `QuranText` model
6. **DI** — wire everything with your dependency injection framework (the app uses Metro/Anvil)

### 9.2 Key Architecture Decisions

- **Pagination**: ViewPager-based, right-to-left, with dual-page support for tablets
- **Audio**: Per-ayah files for gapped qaris, per-sura files with timing database for gapless qaris
- **Content**: Primarily image-based (`PageContentType.Image`) with optional text overlay
- **Data distribution**: All structural data is hardcoded (no network needed for page/sura/juz mapping); actual page images, audio files, and text content are downloaded on demand
