#!/usr/bin/env python3
"""Router de modelos por costo.
Uso: python harness/run.py <tarea> "<prompt>" [archivo ...] [--milestone]
- Elige modelo segun la tarea (models.json).
- Envia SOLO los archivos indicados, recortados.
- Imprime la respuesta recortada y registra tokens en harness/usage.csv.
Requiere: ANTHROPIC_API_KEY
"""
import json, os, sys, pathlib, datetime, urllib.request

ROOT = pathlib.Path(__file__).parent
cfg = json.load(open(ROOT / "models.json"))
SYSTEM = ("Eres un ingeniero de un launcher Android TV (Kotlin). "
          "Responde solo con el resultado: diff o codigo, sin explicaciones largas.")

def main():
    args = [a for a in sys.argv[1:] if a != "--milestone"]
    milestone = "--milestone" in sys.argv
    if len(args) < 2:
        sys.exit(__doc__)
    task, prompt, files = args[0], args[1], args[2:]
    tier = cfg["routes"].get(task, cfg["default_tier"])
    t = cfg["tiers"][tier]
    ctx = ""
    for f in files:
        txt = pathlib.Path(f).read_text(errors="ignore")[: cfg["max_file_chars"]]
        ctx += f"\n--- {f} ---\n{txt}"
    body = json.dumps({
        "model": t["model"], "max_tokens": t["max_tokens"], "system": SYSTEM,
        "messages": [{"role": "user", "content": prompt + ctx}],
    }).encode()
    req = urllib.request.Request(
        "https://api.anthropic.com/v1/messages", body,
        {"content-type": "application/json",
         "x-api-key": os.environ["ANTHROPIC_API_KEY"],
         "anthropic-version": "2023-06-01"})
    r = json.load(urllib.request.urlopen(req))
    out = "".join(b.get("text", "") for b in r["content"])
    u = r["usage"]
    with open(ROOT / "usage.csv", "a") as fh:
        fh.write(f"{datetime.datetime.now():%F %T},{task},{tier},{u['input_tokens']},{u['output_tokens']}\n")
    if milestone:
        with open(ROOT.parent / "MILESTONES.md", "a") as fh:
            fh.write(f"- {datetime.date.today()} [{task}] {prompt[:100]}\n")
    print(out[:3000])

main()
