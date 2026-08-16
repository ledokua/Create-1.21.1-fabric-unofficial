#!/usr/bin/env python3
"""
Vendors upstream Create's resources (src/main/resources + src/generated/resources) into this repo and
rewrites the NeoForge-specific JSON to its Fabric equivalent. Re-runnable: it wipes and re-copies
assets/ and data/ each time (our own fabric.mod.json / mixins / accesswidener / META-INF are untouched).

    scripts/convert-resources.py [/path/to/upstream/Create]

Conversions:
  * "neoforge:conditions" -> "fabric:load_conditions"
        neoforge:mod_loaded {modid}   -> fabric:all_mods_loaded {values:[modid]}
        neoforge:not {value}          -> fabric:not {value}
        neoforge:tag_empty {tag}      -> fabric:not { fabric:tags_populated {registry: minecraft:item, values:[tag]} }
  * item ingredients
        {"type":"neoforge:compound","ingredients":[..]}   -> {"fabric:type":"fabric:any","ingredients":[..]}
        {"type":"neoforge:block_tag","tag":..}            -> {"fabric:type":"create:block_tag","tag":..}
        {"type":"neoforge:components","items":..,"components":..}
                                                          -> {"fabric:type":"fabric:components","base":..,"components":..}
    (fluid ingredients keep their "type": Create's FluidIngredient registry accepts the neoforge:* aliases)
  * data/create/neoforge/biome_modifier/** dropped (AllBiomeModifiers uses Fabric BiomeModifications in code)
  * data/neoforge/data_maps/** kept as-is (foundation.datamap.DataMapLoader reads NeoForge's layout)
  * scripts/extra-lang/<locale>.json merged into assets/create/lang/<locale>.json (Fabric-only keys, e.g. EMI tag names)
  * scripts/extra-resources/** copied over afterwards: things NeoForge itself provides (c:milk fluid tag for Create's
    own milk fluid) and Fabric-port-only files (bridge gametest templates)
"""
import json
import os
import shutil
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
REPO = os.path.dirname(HERE)
UPSTREAM = sys.argv[1] if len(sys.argv) > 1 else "/home/ledok/Projects/Mods/Create"
OUT = os.path.join(REPO, "src/main/resources")

stats = {}

# Files NeoForge (not upstream Create) provides and that Fabric lacks, plus Fabric-port-only resources; copied after vendoring.
EXTRA_RESOURCES = os.path.join(HERE, "extra-resources")
# Lang keys merged into Create's own lang files (Fabric-only: EMI tag translations for c:/trinkets:/curios: tags).
EXTRA_LANG = os.path.join(HERE, "extra-lang")


def merge_extra_lang():
    if not os.path.isdir(EXTRA_LANG):
        return
    for f in os.listdir(EXTRA_LANG):
        if not f.endswith(".json"):
            continue
        with open(os.path.join(EXTRA_LANG, f)) as fh:
            extra = json.load(fh)
        target = os.path.join(OUT, "assets/create/lang", f)
        lang = {}
        if os.path.isfile(target):
            with open(target) as fh:
                lang = json.load(fh)
        lang.update(extra)
        with open(target, "w") as fh:
            json.dump(dict(sorted(lang.items())), fh, indent=2, ensure_ascii=False)
            fh.write("\n")
        stats["lang keys merged"] = stats.get("lang keys merged", 0) + len(extra)


def bump(k):
    stats[k] = stats.get(k, 0) + 1


def convert_condition(c):
    t = c.get("type")
    if t == "neoforge:mod_loaded":
        bump("cond:mod_loaded")
        return {"condition": "fabric:all_mods_loaded", "values": [c["modid"]]}
    if t == "neoforge:not":
        bump("cond:not")
        return {"condition": "fabric:not", "value": convert_condition(c["value"])}
    if t == "neoforge:tag_empty":
        bump("cond:tag_empty")
        return {"condition": "fabric:not", "value": {"condition": "fabric:tags_populated", "registry": "minecraft:item", "values": [c["tag"]]}}
    if t == "neoforge:and":
        bump("cond:and")
        return {"condition": "fabric:and", "values": [convert_condition(v) for v in c["values"]]}
    if t == "neoforge:or":
        bump("cond:or")
        return {"condition": "fabric:any", "values": [convert_condition(v) for v in c["values"]]}
    if t == "neoforge:true":
        return {"condition": "fabric:true"}
    if t == "neoforge:false":
        return {"condition": "fabric:not", "value": {"condition": "fabric:true"}}
    raise ValueError("unknown condition " + str(c))


def convert_ingredient(o):
    """Item ingredient objects only. Returns converted dict or None if untouched."""
    t = o.get("type")
    if t == "neoforge:compound" and "ingredients" in o:
        bump("ing:compound")
        return {"fabric:type": "fabric:any", "ingredients": o["ingredients"]}
    if t == "neoforge:block_tag":
        bump("ing:block_tag")
        return {"fabric:type": "create:block_tag", "tag": o["tag"]}
    if t == "neoforge:components" and "items" in o and "fluids" not in o:
        bump("ing:components")
        base = o["items"]
        if isinstance(base, str):
            base = {"item": base}
        return {"fabric:type": "fabric:components", "base": base, "components": o["components"]}
    return None


def walk(o):
    if isinstance(o, dict):
        if "neoforge:conditions" in o:
            o["fabric:load_conditions"] = [convert_condition(c) for c in o.pop("neoforge:conditions")]
        conv = convert_ingredient(o)
        if conv is not None:
            o.clear()
            o.update(conv)
        for v in list(o.values()):
            walk(v)
    elif isinstance(o, list):
        for v in o:
            walk(v)


def copy_tree(src, dst):
    for root, dirs, files in os.walk(src):
        rel = os.path.relpath(root, src)
        target = os.path.join(dst, rel)
        os.makedirs(target, exist_ok=True)
        for f in files:
            shutil.copy2(os.path.join(root, f), os.path.join(target, f))


def main():
    for d in ("assets", "data"):
        shutil.rmtree(os.path.join(OUT, d), ignore_errors=True)
    for sub in ("src/main/resources", "src/generated/resources"):
        for d in ("assets", "data"):
            s = os.path.join(UPSTREAM, sub, d)
            if os.path.isdir(s):
                copy_tree(s, os.path.join(OUT, d))
    shutil.copy2(os.path.join(UPSTREAM, "src/main/resources/icon.png"), os.path.join(OUT, "icon.png"))
    shutil.rmtree(os.path.join(OUT, "data/create/neoforge"), ignore_errors=True)

    for root, _, files in os.walk(os.path.join(OUT, "data")):
        for f in files:
            if not f.endswith(".json"):
                continue
            p = os.path.join(root, f)
            with open(p) as fh:
                try:
                    j = json.load(fh)
                except json.JSONDecodeError as e:
                    print("skip (bad json)", p, e)
                    continue
            before = json.dumps(j, sort_keys=True)
            walk(j)
            if json.dumps(j, sort_keys=True) != before:
                with open(p, "w") as fh:
                    json.dump(j, fh, indent=2)
                    fh.write("\n")
                bump("files changed")
    copy_tree(EXTRA_RESOURCES, OUT)
    merge_extra_lang()
    for root, _, files in os.walk(EXTRA_RESOURCES):
        for _ in files:
            bump("extra files")
    for k in sorted(stats):
        print(f"{stats[k]:6d}  {k}")


if __name__ == "__main__":
    main()
