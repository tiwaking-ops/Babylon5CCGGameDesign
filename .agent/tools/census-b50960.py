#!/usr/bin/env python3
# Standing Java 6 construct census (B5-0960 instrument), tracked per the DONE B5-1667 disposition.
# Copied byte-identically from tmp-scans/b50960/census.py by B5-1877 (Buffy (glm-5.3-flash) 16, 2026-10-01).
# Provenance: authored B5-0960; re-run byte-identical at B5-1012; the AGENTS.md section 2a standing
# command pointed at the git-ignored tmp-scans path, which a fresh clone cannot resolve (B5-1667).
# This tracked copy is the canonical instrument; the tmp-scans original stays in place, untouched.
# Run: PYTHONIOENCODING=utf-8 py .agent/tools/census-b50960.py
# Gate: exit 0 = clean (code-lines 0 on every family under TRACKED b5ccg/src); the census is the
# record and the API-level second look, javac stays decisive on syntax (authority split per B5-1479).

import os, re, subprocess, json

OUT = 'tmp-scans/b50960'

def tracked(glob):
    out = subprocess.run(['git', 'ls-files', glob], capture_output=True, text=True)
    return sorted(x for x in out.stdout.strip().split('\n') if x)

CONSTRUCTS = [
    ('arrow',       re.compile(r'->')),
    ('methodref',   re.compile(r'::')),
    ('stream',      re.compile(r'\.stream\s*\(')),
    ('computeIfAbsent', re.compile(r'\bcomputeIfAbsent\s*\(')),
    ('computeIfPresent', re.compile(r'\bcomputeIfPresent\s*\(')),
    ('compute',     re.compile(r'\.compute\s*\(')),
    ('merge',       re.compile(r'\.merge\s*\(')),
    ('functionalInterface', re.compile(r'@FunctionalInterface')),
    ('tryWithResources', re.compile(r'\btry\s*\(')),
    ('diamond',     re.compile(r'<\s*>')),
    ('forEach',     re.compile(r'\bforEach\s*\(')),
    ('removeIf',    re.compile(r'\bremoveIf\s*\(')),
    ('getOrDefault', re.compile(r'\bgetOrDefault\s*\(')),
]

def mask(src):
    """Blank line content inside /* */ and // comments and string literals; keep lines."""
    out = []
    i, n = 0, len(src)
    in_block = in_line = in_str = in_chr = False
    cur = []
    while i < n:
        c = src[i]
        nxt = src[i+1] if i+1 < n else ''
        if in_block:
            cur.append(' ' if c != '\n' else '\n')
            if c == '*' and nxt == '/':
                cur.append('  '); i += 2; in_block = False; continue
            i += 1; continue
        if in_line:
            if c == '\n':
                in_line = False; out.append(''.join(cur)); cur = []; continue
            cur.append(' '); i += 1; continue
        if in_str:
            if c == '\\':
                cur.append('  '); i += 2; continue
            if c == '"':
                in_str = False; cur.append(' '); i += 1; continue
            cur.append(' ' if c != '\n' else '\n'); i += 1; continue
        if in_chr:
            if c == '\\':
                cur.append('  '); i += 2; continue
            if c == "'":
                in_chr = False; cur.append(' '); i += 1; continue
            cur.append(' '); i += 1; continue
        if c == '/' and nxt == '*':
            in_block = True; cur.append('  '); i += 2; continue
        if c == '/' and nxt == '/':
            in_line = True; cur.append('  '); i += 2; continue
        if c == '"':
            in_str = True; cur.append(' '); i += 1; continue
        if c == "'":
            in_chr = True; cur.append(' '); i += 1; continue
        cur.append(c); i += 1
    out.append(''.join(cur))
    return ''.join(out)

def census(files, label):
    print('===', label, len(files), 'files ===')
    for name, rx in CONSTRUCTS:
        hits = []
        for f in files:
            if not os.path.exists(f):
                print('MISSING-FILE', f); continue
            src = open(f, encoding='utf-8', errors='replace').read()
            m = mask(src)
            sl = src.split('\n'); ml = m.split('\n')
            for ln, (a, b) in enumerate(zip(sl, ml), 1):
                occ = len(rx.findall(a))
                if occ:
                    code = bool(rx.search(b))
                    hits.append((f, ln, a.strip()[:110], code, occ))
        print('--', name, 'lines-with-hits', len(hits), 'occurrences', sum(h[4] for h in hits), 'code-lines', sum(1 for h in hits if h[3]))
        for h in hits:
            print('   %s | %s%d x%d | %s' % (h[0], '' if h[3] else 'PROSE ', h[1], h[4], h[2]))

src_files = tracked('b5ccg/src/*.java') + tracked('b5ccg/src/**/*.java')
src_files = sorted(set(src_files))
arch_files = tracked('b5ccg/src-java8-archive/**/*.java') + tracked('b5ccg/src-java8-archive/*.java')
arch_files = sorted(set(arch_files))
by_pkg = {}
for f in src_files:
    parts = f.split('/')
    key = parts[3] if len(parts) > 4 else '(root)'
    by_pkg[key] = by_pkg.get(key, 0) + 1
print('src file census:', len(src_files), by_pkg)
print('archive file census:', len(arch_files))
census(src_files, 'TRACKED JAVA b5ccg/src')
census(arch_files, 'FROZEN JAVA src-java8-archive (read-only)')
