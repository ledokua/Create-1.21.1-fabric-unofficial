/**
 * Methods NeoForge patches onto vanilla classes that Create's code calls directly ({@code
 * Entity#getPersistentData()}, {@code BlockEntity#invalidateCapabilities()}, ...). Each interface here is
 * attached to its vanilla class through Loom <b>interface injection</b> ({@code loom:injected_interfaces} in
 * fabric.mod.json), so the compiler sees the vanilla type as implementing it and ported call sites compile
 * unchanged; the matching mixin under {@code ..mixin} implements the interface at runtime. See
 * PORTING_PLAN.md, "NeoForge → Fabric translation strategy".
 */
package com.simibubi.create.foundation.extension;
