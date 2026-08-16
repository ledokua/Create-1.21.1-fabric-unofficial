#!/usr/bin/env python3
"""Static check of Create's mixins against the mojmap Minecraft jar (javap):
  * every `method = "name"` exists on the @Mixin target class (inherited members flagged, since they can't be injected into)
  * every @At(INVOKE/FIELD target = "Lowner;name(desc)") actually occurs in the bytecode of the named target method(s)
Usage: scripts/check-mixin-targets.py [mixin-dir ...]   (default: foundation/mixin)"""
import glob, os, re, subprocess, sys, functools

JAR = glob.glob(os.path.expanduser("~/.gradle/caches/fabric-loom/minecraftMaven/net/minecraft/minecraft-merged/1.21.1-loom.mappings.1_21_1.layered+hash.2198-v2/*.jar"))[0]

@functools.lru_cache(None)
def javap(cls):
    out = subprocess.run(["javap", "-cp", JAR, "-p", "-c", cls], capture_output=True, text=True).stdout
    if not out.strip() or "Error:" in out[:200]:
        return None
    # split into methods: header lines end with ');' or ') throws ...;' at indent 2 and are followed by Code:
    methods = {}
    cur = None
    for line in out.splitlines():
        m = re.match(r"^  [\w.$<>?\[\], ]*?(?:\b|\$)([\w$<>]+)\((.*?)\)(?: throws .*)?;$", line)
        if m and not line.startswith("    "):
            cur = m.group(1); methods.setdefault(cur, []); methods[cur].append("HEADER:" + line.strip() + "\n")
            continue
        m2 = re.match(r"^  (?:[\w.$<>?\[\], ]+ )?([\w$]+);$", line)  # field
        if m2 and not line.startswith("    "):
            cur = None; methods.setdefault("$field:" + m2.group(1), [""]); continue
        if cur is not None:
            methods[cur][-1] += line + "\n"
    return methods

def desc_to_java(desc):
    """'(Lnet/minecraft/world/item/ItemStack;Z)V' -> ['net.minecraft.world.item.ItemStack', 'boolean']"""
    prim = {'Z':'boolean','B':'byte','C':'char','S':'short','I':'int','J':'long','F':'float','D':'double','V':'void'}
    out, i, params = [], 1, desc[1:desc.index(')')]
    i = 0
    while i < len(params):
        dims = 0
        while params[i] == '[': dims += 1; i += 1
        if params[i] == 'L':
            j = params.index(';', i); t = params[i+1:j].replace('/', '.'); i = j + 1
        else:
            t = prim[params[i]]; i += 1
        out.append(t + '[]' * dims)
    return out

def javap_params(header):
    """javap method header -> list of param type names (generics stripped, varargs normalised)."""
    inner = header[header.index('(') + 1:header.rindex(')')]
    depth, buf, parts = 0, '', []
    for ch in inner:
        if ch == '<': depth += 1
        elif ch == '>': depth -= 1
        elif depth == 0:
            if ch == ',': parts.append(buf.strip()); buf = ''; continue
            buf += ch
    if buf.strip(): parts.append(buf.strip())
    return [p_.replace('...', '[]').split(' ')[-1] if ' ' not in p_ else p_.replace('...', '[]').split()[-1] for p_ in parts]

def resolve(simple, src):
    m = re.search(r"import ([\w.]+\.%s);" % re.escape(simple), src)
    return m.group(1) if m else None

problems = 0
def report(msg):
    global problems; problems += 1; print(msg)

dirs = sys.argv[1:] or ["src/main/java/com/simibubi/create/foundation/mixin"]
for d in dirs:
    for p in sorted(glob.glob(d + "/**/*.java", recursive=True)):
        src = open(p).read()
        tm = re.search(r"@Mixin\((?:value\s*=\s*)?\{?\s*([\w.]+)\.class", src)
        if not tm: continue
        target = tm.group(1)
        cls = resolve(target.split(".")[0], src)
        if cls is None or not cls.startswith("net.minecraft"): continue
        if "." in target: cls = cls + "$" + target.split(".", 1)[1].replace(".", "$")
        ms = javap(cls)
        if ms is None: report(f"{p}: target class {cls} NOT FOUND"); continue
        rel = os.path.relpath(p)
        # each injector annotation block: from '@Inject'/'@WrapOperation'/... up to the following method declaration
        for block in re.findall(r"(@(?:Inject|WrapOperation|Redirect|ModifyArg|ModifyArgs|ModifyVariable|ModifyConstant|ModifyExpressionValue|ModifyReturnValue|WrapWithCondition|WrapMethod|ModifyReceiver)\s*\((?:[^()]|\([^()]*(?:\([^()]*\))*[^()]*\))*\))", src, re.S):
            names = re.findall(r'"([^"]+)"', re.search(r"method\s*=\s*(\{[^}]*\}|\"[^\"]*\")", block).group(1)) if re.search(r"method\s*=", block) else []
            names = [(n.split("(")[0], n[n.index("("):] if "(" in n else None) for n in names]
            targets = re.findall(r'target\s*=\s*"L([\w/$]+);([\w$<>]+)(\(?[^"]*)"', block)
            for name, mdesc in names:
                if name in ("<init>", "<clinit>") or name.startswith("method_") or name.endswith("*"): continue
                if name not in ms:
                    report(f"{rel}: {cls} has no method '{name}' (inherited or missing)"); continue
                bodies = ms[name]
                if mdesc:
                    want = desc_to_java(mdesc)
                    def same(have, want):
                        # javap prints type variables (T, A, ...) without a package; treat those as wildcards
                        return len(have) == len(want) and all(h == w or ('.' not in h and not h.endswith(']')) for h, w in zip(have, want))
                    bodies = [b for b in bodies if same(javap_params(b.split("\n", 1)[0][7:]), want)]
                    if not bodies:
                        report(f"{rel}: {cls} has no overload {name}{mdesc}"); continue
                for owner, mname, desc in targets:
                    key = f"{mname}:{desc}" if desc else mname
                    ownerdot = owner.replace("/", ".")
                    hit = any(key in body and (owner in body.split(key)[0].rsplit("\n", 1)[-1] or ownerdot == cls or owner + "." + key in body) for body in bodies)
                    if not hit:
                        hit = any(("Method " + owner + "." + key in body) or ("Field " + owner + "." + key in body) or (ownerdot == cls and ("Method " + key in body or "Field " + key in body)) for body in bodies)
                    if not hit:
                        report(f"{rel}: '{name}' in {cls} does not invoke/access {owner}.{key}")
        for name in re.findall(r"@Shadow[^;]*?\s(\w+)\s*(?:;|\()", src):
            if name not in ms and "$field:" + name not in ms:
                report(f"{rel}: {cls} has no shadow member {name}")
print(f"{problems} problem(s)")
