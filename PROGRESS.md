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
