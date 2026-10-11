# EReader — J2ME EPUB Reader for Nokia Asha 302

**Progress Log & Roadmap**

---

## Project Goal

A J2ME MIDlet EPUB reader for the Nokia Asha 302 (Series 40, MIDP 2.0, CLDC 1.1, 320×240 landscape QWERTY).

**Requirements:**
- Reads DRM-free EPUB 2 and EPUB 3 from SD card (`file:///E:/`)
- Also reads plain `.txt` files (Phase 1 proof of concept)
- File browser + library screen with cover thumbnails
- Paginated reader, Apple Books-style (right arrow = next page)
- Table of contents, chapter navigation, bookmarks, in-book search, go-to-page
- Themes: **Sepia** and **Gruvbox Dark**, switchable
- Adjustable font size, line spacing, margins
- Saves last position per book (RMS)
- Azerbaijani, Cyrillic, and English text support
- File browser rooted at SD card
- Signed JAR preferred, but unsigned OK if signing is blocked

**License:** CC BY-NC-SA 4.0 (noncommercial, share-alike).

---

## Development Environment (Final Choice)

We abandoned NetBeans 8.2 + Sun WTK because:
- Oracle locked the WTK behind a login wall
- NetBeans 8.2 mirrors are 404 / rate-limited
- The WTK emulator is broken on modern Arch Linux

Instead we use a **lean, CLI-driven toolchain**:

| Component | Path | Version | Purpose |
|---|---|---|---|
| 32-bit JDK 8 (Zulu) | `/usr/lib/jvm/java-8-openjdk-32` | 1.8.0_504 | Compile + run tools |
| Apache Ant | system package | 1.10.17 | Build automation |
| ProGuard | `~/tools/proguard6.2.2/lib/proguard.jar` | 6.2.2 | J2ME preverification / obfuscation |
| MicroEmulator | `~/netbeans/microemulator-2.0.4/` | 2.0.4 | Test emulator |
| J2ME API jars | `~/ereader/lib/` | CLDC 1.1 / MIDP 2.0 | Compile-time classpath |
| VS Code | system package | latest | Editor |

**Environment variables** (add to `~/.bashrc`):
```bash
export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-32
export PROGUARD_HOME=$HOME/tools/proguard6.2.2
export PATH=$JAVA_HOME/bin:$PATH
```

---

## What's Been Done

### ✅ 2026-10-11 — Startup crash isolation build

- Removed `MIDlet-Icon` from the manifest. The Asha launcher may decode the icon
  before starting the MIDlet, and this was never reproducible in MicroEmulator.
  `res/logo.png` remains packaged as an optional resource.
- Deferred construction of `FileBrowserCanvas` until the user presses `1`.
  Startup no longer opens `file:///E:/` or requests file access before the
  library is visible.
- Bumped the package to version `0.2` so the diagnostic build is installed as
  a new application version.
- Made `package` depend on `clean`, preventing stale classes such as the old
  `EReaderMidlet$HelloCanvas` smoke-test class from entering a release.
- Rebuilt from a clean tree: `dist/EReader.jar` is 143310 bytes, contains no
  `HelloCanvas`, uses class-file major version 45, and has no `MIDlet-Icon`.
- The bounded MicroEmulator startup test completed without an exception. This
  is only a desktop compatibility check; real Asha confirmation is still
  required.

### 🔬 2026-10-11 — Added a minimal launch probe

- The reported black/white display cycle does not provide an application-level
  error log, so a second EReader build would not isolate the failing layer.
- Added `ant diagnostic`, which produces `dist/launch-probe.jar` containing
  only one preverified MIDlet and one blank `Canvas`.
- The probe has no icon, RMS access, JSR-75 access, KXML, JZlib, EPUB code, or
  application screens. It uses class-file major version 45.
- If the probe also causes the black/white cycle, the failure is outside the
  EReader code and should be investigated as a Series 40 Java installation,
  firmware, memory, or device-runtime issue. If it displays `Launch probe OK`,
  the EReader startup graph remains the relevant layer.

### 🔬 2026-10-11 — Added an EReader-class startup probe

- Added `ant minimal`, producing `dist/ereader-core-probe.jar`.
- This probe uses the exact application class name
  `com.ereader.EReaderMidlet` and the same MIDP/CLDC manifest, but its class
  contains only `Display`, one blank `Canvas`, and no EReader dependencies.
- `EReader core OK` means the class name and manifest are accepted. A crash
  here would point to a class-name/install conflict; a successful launch means
  the original EReader startup dependency graph is the failing layer.

### 🔬 2026-10-11 — Added an RMS settings probe

- Added `ant settings`, producing `dist/ereader-settings-probe.jar`.
- This uses the EReader class name and tests only `SettingsStore.load()`, the
  `Settings` model, and a blank Canvas.
- It excludes the library, file browser, reader, EPUB parser, KXML, and JZlib.

### 🔬 2026-10-11 — Added a library startup probe

- Added `ant library`, producing `dist/ereader-library-probe.jar`.
- This tests `LibraryStore.load()`, `Book`, `Theme`, and `LibraryCanvas`
  rendering with the exact EReader MIDlet class name.
- It excludes JSR-75, the reader, EPUB parsing, KXML, and JZlib.

### 🔬 2026-10-11 — Added a JSR-75 file browser probe

- Added `ant browser`, producing `dist/ereader-browser-probe.jar`.
- This wires key `1` to the real `FileBrowserCanvas` and tests directory
  enumeration from the default `file:///E:/` root.
- It excludes the reader, EPUB parsing, KXML, and JZlib.

The browser probe can enumerate files and return to the library. Its original
`openPath()` callback was deliberately empty, so selecting a file had no
visible result; the probe now displays the selected path to test that handoff.

### 🔬 2026-10-11 — Added a reader startup probe

- Added `ant reader`, producing `dist/ereader-reader-probe.jar`.
- This adds the real `ReaderCanvas` and opens selected plain-text files.
- It excludes EPUB parsing, KXML, JZlib, search/page/TOC screens, and settings
  UI.

The reader probe initially treated every selected file as plain text, so an
EPUB appeared as ZIP gibberish. It now shows an `Opening EPUB...` screen and
loads only the first spine chapter asynchronously. This avoids the redundant
whole-book and TOC scans while isolating ZIP/XML parsing from full-book memory
usage.

### ✅ 2026-10-11 — EPUB first-chapter path confirmed

- The Asha 302 opened the first EPUB spine chapter and displayed three pages
  of readable text using the diagnostic build.
- The earlier gibberish was only from the plain-text-only reader probe.
- The real MIDlet now lazily constructs the reader, settings, search, page,
  and TOC screens instead of resolving all of them during `startApp()`. This
  keeps EPUB and reader classes out of the initial library startup path.

### 🔬 2026-10-11 — Isolated startup listener interfaces

- The full MIDlet still crashed even though every feature probe passed.
- The startup class previously implemented seven listener interfaces, forcing
  the Asha verifier to resolve the complete UI graph from the MIDlet class
  itself.
- Replaced those declarations with small lazy listener adapters. The MIDlet
  now implements only `MIDlet`; adapters are attached as each screen is
  created.
- Clean package and MicroEmulator startup passed with class-file major
  version 45.

### 🔬 2026-10-11 — Added a library/settings screen probe

- Added `ant screens`, producing `dist/ereader-screens-probe.jar`.
- The library probe intentionally had empty callbacks, so settings and file
  selection were not expected to work there.
- This probe wires `LibraryCanvas` to the real `SettingsCanvas`; it still
  excludes JSR-75, the reader, EPUB parsing, KXML, and JZlib.

### ✅ 1. Toolchain installed and verified

- [x] `lib32-libxtst`, `lib32-libxrender`, `lib32-libxi` installed
- [x] 32-bit JDK 8 (Zulu) downloaded from Azul CDN, extracted to `/usr/lib/jvm/java-8-openjdk-32`, registered with `archlinux-java`
- [x] Verified: `java -version` → `1.8.0_504` (Zulu 32-bit)
- [x] Apache Ant installed via `pacman`
- [x] ProGuard 6.2.2 jar downloaded from Maven Central
- [x] MicroEmulator 2.0.4 downloaded from SourceForge, verified working (ran the bundled demo)

**Dead ends we hit (documented for posterity):**
- Oracle JDK direct downloads (BundleId guessing, login redirects)
- Adoptium API for JDK 8 x86 — no such build exists
- NetBeans 8.2 installer links (all 404 or 500)
- Sun WTK 2.5.2 download (returns an HTML login page, not the installer)
- `yay -S netbeans8` — AUR package deleted

### ✅ 2. Project skeleton created

Directory: `~/ereader/`

```
ereader/
├── .vscode/              (settings.json — TODO, see below)
├── lib/
│   ├── cldcapi11.jar
│   ├── midpapi20.jar
│   └── microemu-nokiaui.jar
├── res/                  (empty for now)
├── src/
│   └── com/ereader/
│       ├── EReaderMidlet.java     (smoke test only)
│       ├── model/                 (empty)
│       ├── store/                 (empty)
│       ├── ui/                    (empty)
│       └── util/                  (empty)
├── MANIFEST.MF
├── proguard.pro
└── build.xml
```

### ✅ 3. Build files authored

- `MANIFEST.MF` — MIDlet descriptor
- `build.xml` — Ant script (clean / compile / preverify / package / run)
- `proguard.pro` — ProGuard rules with J2ME `-microedition` preverification
- `src/com/ereader/EReaderMidlet.java` — smoke test MIDlet (draws sepia canvas, "Smoke test OK")

### ✅ 4. Smoke test — **BUILD FIXED / LAUNCHED**

We fixed the original failure by replacing the `env.PROGUARD_HOME` path with a direct `${user.home}` path, adding `<property environment="env" />` for compatibility, and correcting the MicroEmulator launch jar path.

The release build uses the full ProGuard distribution because the Maven-Central
ProGuard jar does not include the J2ME preverifier.

Validated steps:
- `ant clean package` — succeeded, generated `dist/EReader.jar` and `dist/EReader.jad`
- `ant run` — launched MicroEmulator with the JAD; the process moved to the background in the editor, which indicates the MIDlet startup path is valid

Current status: smoke-test packaging is working and the app is launching into the emulator; no further build blocker remains before Phase 1.

---

## What Needs To Be Done

### 🔴 Immediate (Phase 1 smoke test — finish this first)

- [x] Fix `build.xml`: add `<property environment="env" />` and use the full ProGuard path `${user.home}/tools/proguard6.2.2/lib/proguard.jar`
- [x] Fix `build.xml` `run` target: use `${user.home}/netbeans/microemulator-2.0.4/microemulator.jar`
- [x] Use the full ProGuard distribution with `-microedition` preverification
- [x] Run `ant clean package` → produced `dist/EReader.jar` and `dist/EReader.jad`
- [x] Run `ant run` → MicroEmulator launched with the generated JAD (backgrounded by the UI)

**Status:** The smoke-test build path is fixed and validated. We can proceed to the Phase 1 source implementation work.
### 🟡 Phase 1 — Text reader (MVP on the phone)

The initial Phase 1 source skeleton is now present. The current implementation has:
- model objects and RMS-backed settings, library, and bookmark stores
- sepia and Gruvbox themes
- text pagination with arrow-key navigation
- library, file-browser, settings, and reader canvases
- keyboard navigation from the library to files (`1`) and settings (`2`), plus back (`0`)
- last reader page persisted per book in RMS and restored when reopening
- reader input streams and connections closed after loading
- JSR-75 `FileConnection` support added for real directory enumeration
- reader wrapping now uses font metrics and display width, with CRLF normalization
- file-browser listings keep the selected row visible in long directories
- library listings keep the selected row visible and ignore the empty-library placeholder
- changing reader font size recalculates wrapping and keeps the page in range
- file browser filters to supported text books and library entries use readable filenames
- settings screen shows the active theme and font choice
- reader supports bookmark toggle (`7`) and jump to the saved bookmark (`8`)
- reader supports forward in-book search from the current page (`9`)
- in-book search wraps to the beginning after reaching the end
- reader supports go-to-page from numeric input (`6`)
- EPUB foundation dependencies and streaming ZIP/XML helpers are now present
- first EPUB spine chapter can be extracted, XHTML-stripped, and opened in the reader
- all readable OPF spine chapters are concatenated with chapter breaks
- EPUB runtime dependencies use KXML 2.3.0 and a reduced CLDC-compatible JZlib runtime
- Ant now merges those runtime classes into the MIDlet before ProGuard packaging
- README now documents build commands, controls, and supported book formats
- optional `ant sign` target added without storing signing credentials; it refreshes JAD size after signing
- bounded MicroEmulator smoke check completed successfully after packaging; the launcher was terminated by the test timeout to avoid leaving a persistent GUI process
- `v0.1.0` test release published with unsigned `EReader.jar` and `EReader.jad` assets for real-device validation
- `v0.1.1` preverified test release prepared after the first device reported `java/lang/VerifyError`
- fixed EPUB library selection, made EPUB failures visible, adapted menu rows/footer layout to font metrics, and added explicit `3`/`4`/`5` font-size shortcuts
- corrected ZIP local-header parsing alignment, which caused EPUB `EOFException` failures
- forced every packaged class to legacy Java 1.1 class format after the Asha reported `ClassFormatError: Bad version information`
- EPUB loading now reuses one JSR-75 file connection for the complete book, avoiding repeated permission prompts for each ZIP entry
- EPUB spine chapters are now collected with one sequential ZIP scan instead of rescanning the archive for every chapter
- reader titles are width-limited, the footer reserves a dedicated back row, and common bullet glyphs are normalized to ASCII markers
- restored the last device-accepted ZIP loading path after `v0.1.6` produced an invalid-application report, and packaged the supplied logo as a cached 72x39 resource on every main screen
- changed the packaged logo to an indexed-color PNG, declared it as `MIDlet-Icon`, and prepared a JAR-first installation path for phones that cannot open JAD files
- removed the unstable v0.1.6 ZIP batch implementation and replaced it with a clean two-pass sequential reader that limits EPUB rescans while preserving device-safe JAR packaging
- removed the logo's white background with transparency while retaining an indexed, low-resolution PNG
- fixed the remaining EPUB truncation bug by preserving compressed bytes that the inflater reads ahead of ZIP data descriptors
- prepared v0.1.10 as a direct standalone JAR release; the convenience ZIP is no longer included
- changed EPUB passes to use a fresh JSR-75 file connection each time, avoiding the Asha 302 "InputStream is already open" limitation
- replaced the packaged icon with the supplied All Aboard artwork and removed all in-app upper-right logo drawing
- replaced the JZlib input loop with a finished-state loop and final empty-input flush, matching the working J2ME inflater pattern used by MPGram-derived code
- changed compressed-input delivery to one-byte bounded reads so the Asha cannot lose ZIP descriptor or next-entry bytes through stream read-ahead
- regenerated the icon from the newer supplied artwork as a transparent RGBA PNG; the previous indexed conversion retained the light background
- reserved a real header band so long file names cannot overlap reader text
- added reliable visible bookmark state with numeric-key fallbacks for mark/jump
- added an EPUB table-of-contents screen built from spine chapter headings and chapter positions
- removed stale smoke-test output from the release packaging path by forcing a clean output directory before each release build
- replaced the icon with the latest attached `logo-png.png`, preserving its portrait artwork and transparent background in a centered 48x48 icon
- reverted the latest icon resource to an opaque 64-color indexed PNG after the Asha launcher crashed while parsing the truecolor RGBA icon; this retains the new artwork using the device-safe format from v0.1.8
- restored the exact v0.1.8 icon bytes after the phone continued crashing with converted replacements; this is the last confirmed launcher-safe resource

The file browser now enumerates directories through JSR-75 `FileConnection`. The MicroEmulator JSR-75 module is included for desktop testing; on the Asha 302, the device's built-in JSR-75 implementation is used.

The Phase 1 source implementation is complete. The remaining exit criterion is validation on an actual Asha 302.
- [x] `src/com/ereader/ui/FileBrowserCanvas.java` — real SD-card file browser with directory navigation
- [x] `src/com/ereader/ui/SettingsCanvas.java` — settings screen
- [x] `src/com/ereader/EReaderMidlet.java` — real MIDlet (initial implementation)

**Phase 1 exit criteria:**
- App installs and runs on the Asha 302
- [x] File browser navigates the SD card, opens `.txt` files
- Reader paginates, arrow keys page forward/back
- Theme toggle (`*`), font size toggle (`#`)
- [x] Position saved while reading, restored on reopen
- Library lists recently opened books

### 🟢 Phase 2 — EPUB support

- [x] Add KXML 2.3.0 and a reduced CLDC-compatible JZlib runtime to `lib/`
- [x] `src/com/ereader/epub/ZipReader.java` — sequential ZIP reader + inflater using JZlib
- [x] `src/com/ereader/epub/ContainerParser.java` — parse `META-INF/container.xml`
- [x] `src/com/ereader/epub/OpfParser.java` — parse the OPF manifest and spine
- [ ] `src/com/ereader/epub/NcxParser.java` — parse NCX / EPUB 3 nav doc for TOC
- [x] `src/com/ereader/epub/XhtmlStripper.java` — extract text from XHTML, minimal inline markup
- [ ] `src/com/ereader/epub/ImageDecoder.java` — decode PNG/JPEG from ZIP for inline charts
- [ ] `src/com/ereader/epub/CssSubset.java` — parse a subset of CSS (italic, bold, colors, alignment)
- [x] `src/com/ereader/epub/EpubReader.java` — facade tying it together
- [x] Wire `FileBrowserCanvas` to route `.epub` files to `EpubReader`
- [x] Basic multi-chapter reading through the OPF spine
- [ ] TOC UI

**Phase 2 constraints:**
- Heap is tight (few MB). Stream chapter-by-chapter; do not load the full book into memory.
- Ignore unsupported CSS rather than failing.
- Images must be decoded incrementally if large.

### 🔵 Phase 3 — Polish & signing

- [x] In-book search (forward text match)
- [x] Go-to-page dialog (page number)
- [x] Bookmarks UI (inline toggle and jump)
- [ ] Go-to-percent dialog
- [ ] Cover thumbnails on library screen (decode cover image, downscale)
- [ ] Font size finer control (5 levels instead of 3)
- [x] Optional signing target for the phone:
  - Option A: `keytool` self-signed cert + `jarsigner`
  - Option B: grab `jadtool` from an archived WTK and sign properly
- [ ] Copy signed JAR+JAD to SD card, install on the real phone
- [ ] Add a Nokia Asha 302 **device profile to MicroEmulator** so testing matches 320×240

---

## Known Gotchas & Workarounds

### Ant environment variables
Ant does not read `os.environ` by default. `<property environment="env" />` must appear before any `${env.*}` reference. **Preferred:** avoid `env.*` altogether and use `${user.home}`.

### ProGuard `-microedition`
Requires the **full ProGuard distribution** (not the `proguard-base` jar from Maven Central). The full distribution includes the J2ME preverifier. When we're ready for real-phone packaging, download:

```
https://sourceforge.net/projects/proguard/files/proguard/6.2/proguard6.2.2.zip/download
```

If preverification cannot be obtained, MIDP devices will often still run non-preverified JARs, but with warnings. Worth testing.

### JDK 8 `source`/`target`
JDK 8 javac rejects `source="1.4"`. If the build fails on that, change to `source="1.5" target="1.5"`. CLDC 1.1 targets Java 1.3 language level, but 1.4 source is the closest javac accepts; 1.5 is a common practical compromise and works in practice for MIDlets.

### MicroEmulator device sizing
Default device is a small portrait phone. To match the Asha 302, use the "large" device or configure a 320×240 profile. We'll do this in Phase 3.

### File paths on the real phone
The Asha 302 exposes the memory card as `file:///E:/` via JSR-75 `FileConnection`. Root of the browser should be `E:/` and default books folder `E:/Books/`. Empty `E:/` will fall through to the list of drives if the folder doesn't exist.

---

## Next Action

The original build blocker and smoke-test checklist are complete. Use:

```bash
cd ~/ereader
ant clean package
ant run
```

For the current build, `ant clean package` is the primary repeatable validation command. `ant run` can be used for interactive testing; bounded smoke validation was last run successfully.
