package com.dorohedoro.hole.compat;

import com.dorohedoro.hole.Dorohedoro;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLPaths;

/**
 * Copies the optional Dorohedoro quest book out of the mod jar into {@code config/ftbquests/quests}. FTB Quests only
 * reads quests from that folder, never from a mod jar, so a player or server owner who wants the book runs
 * {@code /dorohedoro quests install} once. Modpacks can ship the same files directly instead.
 *
 * <ul>
 *   <li>No quest book yet: the whole book is copied, including its {@code data.snbt} settings.</li>
 *   <li>A book already exists (a modpack's own quests): only the Dorohedoro chapter group, its four chapter files and
 *       its translations are added. The pack's settings and other chapters are left alone, and the two shared files
 *       that are edited ({@code chapter_groups.snbt} and {@code lang/en_us.snbt}) are backed up first.</li>
 *   <li>Running it again updates the Dorohedoro chapters in place. Quest IDs never change, so progress is kept.</li>
 * </ul>
 *
 * Nothing here touches FTB classes, and it does nothing at all unless FTB Quests is loaded.
 */
public final class FtbQuestsInstaller {
    public static final String MOD_ID = "ftbquests";
    /** Every Dorohedoro quest object ID starts with this, so our entries are easy to find in shared files. */
    static final String ID_PREFIX = "0D0E0B0E";
    static final String GROUP_ID = ID_PREFIX + "00000001";
    private static final String RESOURCE_ROOT = "/dorohedoro_compat/ftbquests/quests/";

    private FtbQuestsInstaller() {}

    public static boolean available() {
        return ModList.get().isLoaded(MOD_ID);
    }

    public record Result(boolean merged, int filesWritten, List<String> backups) {}

    public static Result install() throws IOException {
        return install(FMLPaths.CONFIGDIR.get().resolve("ftbquests").resolve("quests"));
    }

    static Result install(Path book) throws IOException {
        List<String> manifest = readResourceLines("manifest.txt");
        boolean existing = hasChapters(book);
        List<String> backups = new ArrayList<>();
        int written = 0;
        for (String file : manifest) {
            if (file.isBlank()) continue;
            String ours = readResource(file);
            Path target = book.resolve(file);
            Files.createDirectories(target.getParent());
            if (!existing || !Files.exists(target)) {
                Files.writeString(target, ours, StandardCharsets.UTF_8);
                written++;
                continue;
            }
            switch (file) {
                case "data.snbt" -> { /* the existing book keeps its own settings */ }
                case "chapter_groups.snbt" -> {
                    String current = Files.readString(target, StandardCharsets.UTF_8);
                    if (!current.contains("\"" + GROUP_ID + "\"")) {
                        backups.add(backup(target));
                        Files.writeString(target, addChapterGroup(current), StandardCharsets.UTF_8);
                        written++;
                    }
                }
                case "lang/en_us.snbt" -> {
                    String current = Files.readString(target, StandardCharsets.UTF_8);
                    String merged = mergeLang(current, ours);
                    if (!merged.equals(current)) {
                        backups.add(backup(target));
                        Files.writeString(target, merged, StandardCharsets.UTF_8);
                        written++;
                    }
                }
                default -> {
                    // Our own chapter files: always replaced with the current version.
                    Files.writeString(target, ours, StandardCharsets.UTF_8);
                    written++;
                }
            }
        }
        return new Result(existing, written, backups);
    }

    private static boolean hasChapters(Path book) throws IOException {
        Path chapters = book.resolve("chapters");
        if (!Files.isDirectory(chapters)) return false;
        try (var files = Files.list(chapters)) {
            return files.anyMatch(f -> f.toString().endsWith(".snbt"));
        }
    }

    private static String backup(Path file) throws IOException {
        Path copy = file.resolveSibling(file.getFileName() + ".before-dorohedoro");
        Files.copy(file, copy, StandardCopyOption.REPLACE_EXISTING);
        return copy.toString();
    }

    /** Adds our group as the last entry of the {@code chapter_groups} list. */
    static String addChapterGroup(String current) {
        int list = current.indexOf("chapter_groups:");
        if (list < 0) {
            return "{\n\tchapter_groups: [\n\t\t{ id: \"" + GROUP_ID + "\" }\n\t]\n}\n";
        }
        int open = current.indexOf('[', list);
        int close = matchingBracket(current, open);
        String inside = current.substring(open + 1, close).strip();
        String entry = "{ id: \"" + GROUP_ID + "\" }";
        String body = inside.isEmpty() ? "\n\t\t" + entry + "\n\t" : "\n\t\t" + inside + "\n\t\t" + entry + "\n\t";
        return current.substring(0, open + 1) + body + current.substring(close);
    }

    private static int matchingBracket(String text, int open) {
        int depth = 0;
        boolean quoted = false;
        for (int i = open; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '"' && text.charAt(i - 1) != '\\') quoted = !quoted;
            if (quoted) continue;
            if (c == '[') depth++;
            if (c == ']' && --depth == 0) return i;
        }
        throw new IllegalArgumentException("unbalanced chapter_groups list");
    }

    /**
     * Drops any older Dorohedoro entries from a book's translation file and appends the current ones before its closing
     * brace. Entries belonging to other chapters are copied through untouched.
     */
    static String mergeLang(String current, String ours) {
        List<String> kept = new ArrayList<>();
        String[] lines = current.replace("\r\n", "\n").split("\n");
        boolean skippingList = false;
        for (String line : lines) {
            if (skippingList) {
                if (line.strip().equals("]")) skippingList = false;
                continue;
            }
            String key = line.strip();
            int colon = key.indexOf(':');
            if (colon > 0 && key.substring(0, colon).contains("." + ID_PREFIX)) {
                skippingList = key.endsWith("[");
                continue;
            }
            kept.add(line);
        }
        int last = kept.size() - 1;
        while (last >= 0 && kept.get(last).isBlank()) last--;
        if (last < 0 || !kept.get(last).strip().equals("}")) {
            throw new IllegalArgumentException("translation file does not end with }");
        }
        List<String> ourLines = new ArrayList<>(List.of(ours.replace("\r\n", "\n").split("\n")));
        ourLines.removeIf(String::isBlank);
        // Strip our file's own braces and keep its entries.
        List<String> entries = ourLines.subList(1, ourLines.size() - 1);
        List<String> out = new ArrayList<>(kept.subList(0, last));
        out.addAll(entries);
        out.add("}");
        return String.join("\n", out) + "\n";
    }

    private static List<String> readResourceLines(String name) throws IOException {
        return List.of(readResource(name).replace("\r\n", "\n").split("\n"));
    }

    private static String readResource(String name) throws IOException {
        try (InputStream in = Dorohedoro.class.getResourceAsStream(RESOURCE_ROOT + name)) {
            if (in == null) throw new IOException("missing bundled quest file " + name);
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
