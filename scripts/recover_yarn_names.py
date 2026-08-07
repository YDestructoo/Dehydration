#!/usr/bin/env python3
"""Recover named Yarn member identifiers left as old intermediary names.

Loom's cross-version migrateMappings can leave members like field_1234/method_1234
when intermediary identifiers are not stable across the Minecraft versions.  The
source version's Yarn mapping still knows the original named identifier, so use
that mapping to restore the human-readable source name before doing the real API
port.
"""
from __future__ import annotations

import io
import re
import urllib.request
import zipfile
from pathlib import Path

OLD_YARN = "1.21.1+build.3"
MAPPINGS_URL = (
    "https://maven.fabricmc.net/net/fabricmc/yarn/"
    f"{OLD_YARN}/yarn-{OLD_YARN}-v2.jar"
)
SOURCE_ROOT = Path("src/net")
INTERMEDIARY_RE = re.compile(r"\b(?:field|method|comp)_\d+\b")


def load_member_map() -> dict[str, str]:
    print(f"Downloading source mappings: {MAPPINGS_URL}")
    with urllib.request.urlopen(MAPPINGS_URL, timeout=60) as response:
        data = response.read()

    with zipfile.ZipFile(io.BytesIO(data)) as zf:
        with zf.open("mappings/mappings.tiny") as fh:
            lines = io.TextIOWrapper(fh, encoding="utf-8")
            header = next(lines).rstrip("\n").split("\t")
            if header[:3] != ["tiny", "2", "0"]:
                raise RuntimeError(f"Unexpected Tiny header: {header!r}")
            namespaces = header[3:]
            try:
                intermediary_index = namespaces.index("intermediary")
                named_index = namespaces.index("named")
            except ValueError as exc:
                raise RuntimeError(f"Expected intermediary/named namespaces: {namespaces}") from exc

            # Member rows contain: kind, descriptor, <one name per namespace>.
            mapping: dict[str, str] = {}
            for raw in lines:
                row = raw.strip().split("\t")
                if not row or row[0] not in {"f", "m"}:
                    continue
                names = row[2:]
                if len(names) <= max(intermediary_index, named_index):
                    continue
                intermediary = names[intermediary_index]
                named = names[named_index]
                if INTERMEDIARY_RE.fullmatch(intermediary) and named and named != intermediary:
                    mapping[intermediary] = named

    print(f"Loaded {len(mapping)} old intermediary -> named member mappings")
    return mapping


def main() -> None:
    if not SOURCE_ROOT.is_dir():
        raise SystemExit(f"Missing migrated source tree: {SOURCE_ROOT}")

    mapping = load_member_map()
    changed_files = 0
    replacements = 0
    unresolved: set[str] = set()

    for path in sorted(SOURCE_ROOT.rglob("*.java")):
        original = path.read_text(encoding="utf-8")
        local_replacements = 0

        def replace(match: re.Match[str]) -> str:
            nonlocal local_replacements, replacements
            token = match.group(0)
            replacement = mapping.get(token)
            if replacement is None:
                unresolved.add(token)
                return token
            local_replacements += 1
            replacements += 1
            return replacement

        updated = INTERMEDIARY_RE.sub(replace, original)
        if updated != original:
            path.write_text(updated, encoding="utf-8")
            changed_files += 1
            print(f"{path}: {local_replacements} replacements")

    print(f"Recovered {replacements} identifiers across {changed_files} Java files")
    if unresolved:
        print("Unresolved intermediary-looking identifiers:")
        for token in sorted(unresolved):
            print(f"  {token}")


if __name__ == "__main__":
    main()
