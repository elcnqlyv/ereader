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

## Controls

- Library: `1` opens the file browser, `2` opens settings.
- Reader: arrows move pages, `0` returns to the library.
- Reader: `6` goes to a page, `7` toggles a bookmark, `8` jumps to it,
  and `9` searches forward.
- `*` switches theme and `#` cycles font size.

The file browser uses JSR-75 `FileConnection` and starts at
`file:///E:/`. EPUB files are read through the OPF spine and XHTML text is
rendered alongside plain text and Markdown files.