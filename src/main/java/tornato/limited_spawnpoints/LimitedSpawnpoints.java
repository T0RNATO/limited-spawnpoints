package tornato.limited_spawnpoints;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.CommonColors;
import java.io.FileReader;
import java.io.FileWriter;

public class LimitedSpawnpoints implements ModInitializer {
    public record Config(int radius) {}

    interface ThrowingSupplier<T> {
        T get() throws Exception;
    }

    /// Exceptions are a myth made up by the <strike>government</strike> compiler
    public static <T> T unchecked(@SuppressWarnings("ClassEscapesDefinedScope") ThrowingSupplier<T> s) {
        try {
            return s.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static final Config config;

    // Config loading
    static {
        var configFile = FabricLoader.getInstance().getConfigDir().resolve("limited_spawnpoints.json").toFile();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        if (unchecked(configFile::createNewFile)) {
            config = new Config(1000);
            unchecked(() -> {
                var writer = new FileWriter(configFile);
                writer.write(gson.toJson(config));
                writer.close();
                return null;
            });
        } else {
            config = gson.fromJson(unchecked(() -> new FileReader(configFile)), Config.class);
        }
    }

    // Text constants
    static final String WHY_COMMAND = "why-spawnpoint-limit";
    static final Style WHY_STYLE = Style.EMPTY.withClickEvent(new ClickEvent.RunCommand(WHY_COMMAND)).withColor(CommonColors.GREEN);
    static final Component SHORT_MESSAGE =
            Component.literal("Respawn point ").append(
            Component.literal("NOT").withColor(CommonColors.SOFT_RED)).append(
            Component.literal(" set. ")).append(
            Component.literal("[Why?]").setStyle(WHY_STYLE)
    );
    static final Component LONG_MESSAGE = Component.literal(
            "In the interests of player interaction, spawnpoints may only be set within " +
            config.radius +
            " blocks of the world spawn."
    ).withColor(CommonColors.LIGHT_GRAY);

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
            dispatcher.register(Commands.literal(WHY_COMMAND).executes(context -> {
                context.getSource().sendSuccess(() -> LONG_MESSAGE, false);
                return 1;
            }
        )));
    }

    public static boolean shouldSetSpawn(ServerPlayer player) {
        var radius = config.radius();
        var shouldSetSpawn = player.level().getLevelData().getRespawnData().pos().getCenter().subtract(player.position()).horizontalDistance() < radius;
        if (!shouldSetSpawn) {
            player.sendSystemMessage(SHORT_MESSAGE);
        }
        return shouldSetSpawn;
    }
}
