#!/usr/bin/env bash
# Regenerates the CLASSES list in CreateEventSubscribers.java from every @EventBusSubscriber-annotated class
# (top-level and nested; nested classes are emitted as Outer$Inner binary names).
set -euo pipefail
cd "$(dirname "$0")/.."
target=src/main/java/com/simibubi/create/foundation/event/bus/CreateEventSubscribers.java
classes=$(python3 - <<'PY'
import re,os
out=[]
for root,_,files in os.walk('src/main/java'):
    if 'foundation/event/bus' in root: continue
    for f in files:
        if not f.endswith('.java'): continue
        p=os.path.join(root,f); s=open(p).read()
        if '@EventBusSubscriber' not in s: continue
        pkg=re.search(r'^package ([\w.]+);',s,re.M).group(1)
        # walk annotations followed by class decls; track nesting by brace depth is overkill: assume nested
        # subscriber classes are direct static inner classes of the top-level class (true for Create).
        top=f[:-5]
        for m in re.finditer(r'@EventBusSubscriber(?:\([^)]*\))?\s*(?:@\w+(?:\([^)]*\))?\s*)*(?:public\s+|private\s+|protected\s+)?(?:static\s+)?(?:final\s+)?(?:abstract\s+)?(?:class|interface|enum)\s+(\w+)',s):
            name=m.group(1)
            out.append(pkg+'.'+top if name==top else pkg+'.'+top+'$'+name)
print('\n'.join(sorted(set(out))))
PY
)
{
  awk '/GENERATED-BEGIN/{print; exit} {print}' "$target"
  n=$(echo "$classes" | wc -l); i=0
  while read -r c; do i=$((i+1)); if [ "$i" -lt "$n" ]; then echo "		\"$c\","; else echo "		\"$c\""; fi; done <<< "$classes"
  awk 'f{print} /GENERATED-END/{f=1; print}' "$target"
} > "$target.tmp"
mv "$target.tmp" "$target"
echo "listed $(echo "$classes" | wc -l) subscriber classes"
