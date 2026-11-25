#!/usr/bin/env python3
"""Ubah skuad asli jadi nama plesetan untuk OTAFM.
Pakai:  python tools/parody.py skuad_asli.csv > app/src/main/assets/squads.json
CSV (tanpa header): club,name,pos,ovr,age
 - club harus sama persis dengan nama klub plesetan di game (lihat db.json)
 - pos: GK / DEF / MID / FWD
Aturan: nama depan dan belakang diubah 1 huruf vokal (a>o, e>i, i>e, o>u, u>a), aksen dibuang.
Hasil selalu sama untuk nama yang sama (deterministik)."""
import csv, json, sys, hashlib, unicodedata

SW = {"a": "o", "e": "i", "i": "e", "o": "u", "u": "a"}

def strip(s):
    return "".join(c for c in unicodedata.normalize("NFD", s) if unicodedata.category(c) != "Mn")

def tweak(w, seed):
    w = strip(w)
    idx = [i for i, c in enumerate(w) if c.lower() in "aeiou"]
    if len(w) < 3 or not idx:
        return w + "o"
    k = idx[seed % len(idx)]
    c = w[k]; n = SW[c.lower()]
    return w[:k] + (n.upper() if c.isupper() else n) + w[k + 1:]

def parody(name):
    seed = int(hashlib.md5(name.encode()).hexdigest(), 16)
    parts = strip(name).split()
    if len(parts) == 1:
        return tweak(parts[0], seed)
    parts[0] = tweak(parts[0], seed)
    parts[-1] = tweak(parts[-1], seed >> 8)
    return " ".join(parts)

def main():
  out = {}
  for row in csv.reader(open(sys.argv[1], encoding="utf-8")):
    if len(row) < 5: continue
    club, name, pos, ovr, age = [x.strip() for x in row[:5]]
    out.setdefault(club, []).append(f"{parody(name)}|{pos}|{ovr}|{age}")
  print(json.dumps(out, ensure_ascii=False, separators=(",", ":")))

if __name__ == "__main__":
  main()
