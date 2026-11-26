package com.dorohedoro.hole.command;

import com.dorohedoro.hole.Dorohedoro;
import com.dorohedoro.hole.compat.FtbQuestsInstaller;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/** {@code /dorohedoro ...} server commands. Operators only. */
@EventBusSubscriber(modid = Dorohedoro.MOD_ID)
public final class HoleCommands {
    private HoleCommands() {}

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal(Dorohedoro.MOD_ID)
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("quests")
                        .then(Commands.literal("install").executes(ctx -> installQuests(ctx.getSource())))));
    }

    /** Copies the optional quest book into FTB Quests' folder and reloads it. Needs FTB Quests installed. */
    private static int installQuests(CommandSourceStack source) {
        if (!FtbQuestsInstaller.available()) {
            source.sendFailure(Component.literal("FTB Quests is not installed, so there is no quest book to add to."));
            return 0;
        }
        try {
            FtbQuestsInstaller.Result result = FtbQuestsInstaller.install();
            String how = result.merged() ? "Added the Dorohedoro chapters to the existing quest book"
                    : "Installed the Dorohedoro quest book";
            source.sendSuccess(() -> Component.literal(how + " (" + result.filesWritten() + " files written)."), true);
            for (String backup : result.backups()) {
                source.sendSuccess(() -> Component.literal("Backup: " + backup), false);
            }
            source.getServer().getCommands().performPrefixedCommand(source, "ftbquests reload");
            return 1;
        } catch (Exception e) {
            Dorohedoro.LOGGER.error("Quest book install failed", e);
            source.sendFailure(Component.literal("Quest book install failed: " + e.getMessage()));
            return 0;
        }
    }
}
