import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Handle
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.AnnotationNode
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.InvokeDynamicInsnNode
import org.objectweb.asm.tree.MethodNode

/**
 * Post-compile pass: propagates {@code @Environment(SIDE)} from annotated methods to the synthetic lambda
 * methods that only they reference (transitively). Fabric Loader strips annotated methods at class load but
 * leaves their lambda bodies behind, and on a dedicated server the verifier then trips over client-only
 * types used inside those bodies (upstream Create's common classes carry lots of {@code @OnlyIn(CLIENT)}
 * handlers). NeoForge tolerates that; Fabric's dev launcher refuses to load client classes on the server.
 */
class EnvironmentLambdaStripper {
    static final String ENV_DESC = 'Lnet/fabricmc/api/Environment;'

    static void process(File classesDir) {
        int patched = 0
        classesDir.eachFileRecurse { File f ->
            if (!f.name.endsWith('.class')) return
            byte[] bytes = f.bytes
            ClassNode cn = new ClassNode()
            new ClassReader(bytes).accept(cn, 0)
            if (patchClass(cn)) {
                ClassWriter cw = new ClassWriter(0)
                cn.accept(cw)
                f.bytes = cw.toByteArray()
                patched++
            }
        }
        if (patched > 0)
            println("EnvironmentLambdaStripper: annotated lambdas in ${patched} classes")
    }

    private static String envOf(MethodNode m) {
        for (AnnotationNode a : (m.visibleAnnotations ?: []) + (m.invisibleAnnotations ?: [])) {
            if (a.desc == ENV_DESC && a.values != null) {
                for (int i = 0; i < a.values.size(); i += 2) {
                    if (a.values[i] == 'value') {
                        def v = a.values[i + 1]
                        return (v instanceof String[]) ? v[1] : v.toString()
                    }
                }
            }
        }
        return null
    }

    /** Lambda methods (same class) referenced by invokedynamic in the given method. */
    private static Set<String> lambdaRefs(ClassNode cn, MethodNode m) {
        Set<String> refs = new LinkedHashSet<>()
        for (AbstractInsnNode insn = m.instructions.first; insn != null; insn = insn.next) {
            if (insn instanceof InvokeDynamicInsnNode) {
                for (Object arg : ((InvokeDynamicInsnNode) insn).bsmArgs) {
                    if (arg instanceof Handle && ((Handle) arg).owner == cn.name && ((Handle) arg).name.startsWith('lambda$'))
                        refs.add(((Handle) arg).name + ((Handle) arg).desc)
                }
            }
        }
        return refs
    }

    static boolean patchClass(ClassNode cn) {
        Map<String, MethodNode> byKey = [:]
        cn.methods.each { byKey[it.name + it.desc] = it }
        // who references each lambda
        Map<String, Set<String>> referrers = [:].withDefault { new LinkedHashSet<String>() }
        cn.methods.each { m -> lambdaRefs(cn, m).each { referrers[it].add(m.name + m.desc) } }

        boolean changed = false
        boolean progress = true
        while (progress) {
            progress = false
            for (Map.Entry<String, Set<String>> e : referrers.entrySet()) {
                MethodNode lambda = byKey[e.key]
                if (lambda == null || envOf(lambda) != null) continue
                // every referrer must be annotated with the same side
                String side = null
                boolean allStripped = true
                for (String r : e.value) {
                    String s = envOf(byKey[r])
                    if (s == null || (side != null && s != side)) { allStripped = false; break }
                    side = s
                }
                if (!allStripped || side == null) continue
                AnnotationNode an = new AnnotationNode(ENV_DESC)
                an.values = ['value', ['Lnet/fabricmc/api/EnvType;', side] as String[]]
                if (lambda.visibleAnnotations == null) lambda.visibleAnnotations = []
                lambda.visibleAnnotations.add(an)
                changed = true
                progress = true
            }
        }
        return changed
    }
}
