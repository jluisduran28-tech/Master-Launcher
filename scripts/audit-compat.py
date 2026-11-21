#!/usr/bin/env python3
"""Revisa web/index.html y lista funciones de CSS/JS con su versión mínima de Chromium.
Uso: python3 scripts/audit-compat.py   (útil después de cada cambio grande)"""
import re, sys, pathlib
s = pathlib.Path(__file__).resolve().parent.parent.joinpath('web/index.html').read_text(encoding='utf-8')
css = s[s.find('<style>'):s.find('</style>')]; js = s[s.rfind('<script>'):]
CSS = [('inset (usar top/left/right/bottom)', r'[;{ ]inset:', 87), ('gap en flex (hay polyfill)', r'[;{]gap:', 84),
       ('translate:/scale: (hay respaldo)', r'[;{ ]translate:|[;{ ]scale:', 104), ('overflow:clip (hay respaldo)', 'overflow:clip', 90),
       ('aspect-ratio', r'[^-]aspect-ratio:', 88), ('clamp()/min()/max()', r'clamp\(|[^a-z-]min\(|[^a-z-]max\(', 79),
       (':is/:where/:has', r':is\(|:where\(|:has\(', 88), ('color-mix', 'color-mix', 111)]
JS = [('flechas =>', r'=>', 45), ('plantillas `', r'`', 41), ('?.', r'\?\.[a-zA-Z_\[(]', 80), ('??', r'\?\?', 80),
      ('let/const', r'\b(let|const)\s', 49), ('async/await', r'\basync\b|\bawait\b', 55), ('replaceAll', 'replaceAll', 85),
      ('.at(', r'\.at\(', 92), ('.flat(', r'\.flat\(|\.flatMap\(', 69)]
bad = 0
for title, rules, txt in (('CSS', CSS, css), ('JS', JS, js)):
    print('==', title)
    for name, rx, v in sorted(rules, key=lambda r: -r[2]):
        n = len(re.findall(rx, txt))
        flag = '' if n == 0 else ('  <- revisar' if 'respaldo' not in name and 'polyfill' not in name else '  (cubierto)')
        if n and not flag.strip().startswith('(cubierto)'): bad += 1
        print(f'  Chromium {v:>3}+  {name:<38} x{n}{flag}')
sys.exit(1 if bad else 0)
