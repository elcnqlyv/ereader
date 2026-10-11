# Simple ereader for J2ME devices
## Made as a hobby project for reading books in my Nokia Asha 302

## Build

The project uses Apache Ant and a 32-bit JDK 8:

```bash
ant clean package
```

The generated MIDlet files are written to `dist/EReader.jar` and
`dist/EReader.jad`. The optional desktop emulator command is:

```bash
ant run
```

For phone installation, transfer **both** files to the same folder and open
`EReader.jad`; do not launch the JAR directly. The release build uses the full
ProGuard distribution with J2ME preverification and a legacy Java 1.1 class
format required by older Nokia JVMs.

Signing is intentionally opt-in and takes credentials on the command line;
no keystore or password is stored in the repository:

```bash
ant sign -Dsign.keystore=/path/to/keystore \
  -Dsign.storepass=changeit -Dsign.alias=ereader
```

## Controls

- Library: `1` opens the file browser, `2` opens settings, `*` toggles theme,
  and `#` cycles font size.
- Reader: arrows move pages, `0` returns to the library.
- Reader: `2` opens the table of contents, `6` goes to a page, `7` toggles a
  bookmark, `8` jumps to it, and `9` searches (with wrap-around).
- Reader/settings: `3`, `4`, and `5` select small, medium, and large fonts.
The reader always shows `0 back` in its footer. EPUB chapter/TOC navigation is
available with `2`; chapters remain combined into one reading stream after a
TOC selection.

The file browser uses JSR-75 `FileConnection` and starts at
`file:///E:/`. EPUB files are read through the OPF spine and XHTML text is
rendered alongside plain text and Markdown files. Opening an EPUB may request
file access once; accept that request to allow the book to be read.

The JAR contains the complete MIDlet manifest and can be installed directly on
phones that do not expose JAD files. The JAD is optional metadata for phones
that require descriptor-based installation. The logo remains packaged as a
resource, but is not declared as a MIDlet icon because the Asha launcher can
crash while decoding custom icons.

The packaged logo is retained as an optional resource, but is not drawn in the
reader screens and is not declared as the application icon. Release builds
force-clean their output directory so obsolete smoke-test classes cannot be
carried into the JAR.