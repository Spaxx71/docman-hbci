# docman-hbci

eXist-db FinTS/HBCI bridge for Docman (`hbci4java.existdb`), based on [hbci4j/hbci4java](https://github.com/hbci4j/hbci4java).

## Version 0.6.0

- `hbci4j-core` **4.1.17**
- Persistent passport path (default `{exist.home}/docman-passports/`)
- Modes: `interactive` / `auto` (`SCA_REQUIRED` in auto)
- Challenge media as structured XML with **SVG** preferred (flicker/QR/photo)
- `hbci4java:resetPassport($accountId, $path?)`

## Build

```bat
create_banking.cmd
```

Output: `exist-xar-package/build/hbci4java-0.6.0.xar`

## Deploy in Docman

```
/exist/rest/db/apps/docman/update_module_hbci.xqy
```

Passports are kept unless `?resetPassports=true`.
