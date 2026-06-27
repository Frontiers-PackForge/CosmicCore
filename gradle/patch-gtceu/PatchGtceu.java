import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public final class PatchGtceu {

    private static final String TARGET_CLASS = "com/gregtechceu/gtceu/core/config/GTEarlyConfig.class";
    private static final String OWNER = "net/neoforged/neoforge/data/loading/DatagenModLoader";
    private static final String METHOD = "isRunningDataGen";

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("usage: PatchGtceu <in.jar> <out.jar>");
            System.exit(64);
        }
        Path in = Path.of(args[0]);
        Path out = Path.of(args[1]);
        int[] patched = { 0 };

        try (ZipFile zf = new ZipFile(in.toFile());
                ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(out.toFile())))) {
            Enumeration<? extends ZipEntry> entries = zf.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                byte[] data;
                try (InputStream is = zf.getInputStream(entry)) {
                    data = is.readAllBytes();
                }
                if (entry.getName().equals(TARGET_CLASS)) {
                    data = patchClass(data, patched);
                }
                ZipEntry copy = new ZipEntry(entry.getName());
                copy.setTime(entry.getTime());
                zos.putNextEntry(copy);
                zos.write(data);
                zos.closeEntry();
            }
        }

        System.out.println("PatchGtceu: replaced " + patched[0] + " " + METHOD + " call(s) in GTEarlyConfig");
        if (patched[0] == 0) {
            System.err.println("PatchGtceu: ERROR - no " + OWNER + "." + METHOD + " call found; gtceu changed, update this patcher");
            System.exit(2);
        }
        System.out.println("PatchGtceu: wrote " + out + " (" + Files.size(out) + " bytes)");
    }

    private static byte[] patchClass(byte[] data, int[] patched) {
        ClassReader reader = new ClassReader(data);
        ClassNode node = new ClassNode();
        reader.accept(node, 0);
        for (MethodNode method : node.methods) {
            for (AbstractInsnNode insn : method.instructions.toArray()) {
                if (insn instanceof MethodInsnNode call
                        && call.getOpcode() == Opcodes.INVOKESTATIC
                        && OWNER.equals(call.owner)
                        && METHOD.equals(call.name)) {
                    method.instructions.set(insn, new InsnNode(Opcodes.ICONST_0));
                    patched[0]++;
                }
            }
        }
        ClassWriter writer = new ClassWriter(0);
        node.accept(writer);
        return writer.toByteArray();
    }
}
