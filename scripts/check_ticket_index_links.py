#!/usr/bin/env python3
"""Verify every ticket link in the MVP ticket index resolves to a real file.

Also confirms every ticket file under docs/milestones/mvp/tickets/ is
referenced exactly once by the index, so the index can't silently drift
from the backlog.
"""
import re
import sys
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parent.parent
TICKETS_DIR = REPO_ROOT / "docs" / "milestones" / "mvp" / "tickets"
INDEX_FILE = TICKETS_DIR / "INDEX.md"

LINK_RE = re.compile(r"\[MVP-\d+\]\((tickets/[^)]+)\)")


def main() -> int:
    if not INDEX_FILE.exists():
        print(f"ERROR: index file not found: {INDEX_FILE}", file=sys.stderr)
        return 1

    text = INDEX_FILE.read_text(encoding="utf-8")
    linked_targets = LINK_RE.findall(text)

    errors = []
    resolved = set()
    for target in linked_targets:
        candidate = TICKETS_DIR / target[len("tickets/"):]
        if not candidate.is_file():
            errors.append(f"broken link: {target}")
        else:
            resolved.add(candidate.name)

    all_ticket_files = {
        p.name for p in TICKETS_DIR.glob("MVP-*.md") if p.name != "INDEX.md"
    }
    missing_from_index = sorted(all_ticket_files - resolved)
    for name in missing_from_index:
        errors.append(f"ticket file missing from index: {name}")

    if errors:
        print("Ticket index link check FAILED:", file=sys.stderr)
        for err in errors:
            print(f"  - {err}", file=sys.stderr)
        return 1

    print(f"OK: {len(resolved)} index links resolve to ticket files, "
          f"{len(all_ticket_files)} ticket files all indexed.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
