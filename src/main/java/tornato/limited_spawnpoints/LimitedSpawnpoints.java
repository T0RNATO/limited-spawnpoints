package tornato.limited_spawnpoints;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;

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
    static final Style WHY_STYLE = Style.EMPTY.withClickEvent(new ClickEvent.RunCommand(WHY_COMMAND)).withColor(Colors.GREEN);
    static final Text SHORT_MESSAGE =
            Text.literal("Respawn point ").append(
            Text.literal("NOT").withColor(Colors.LIGHT_RED)).append(
            Text.literal(" set. ")).append(
            Text.literal("[Why?]").setStyle(WHY_STYLE)
    );
    static final Text LONG_MESSAGE = Text.literal(
            "In the interests of player interaction, spawnpoints may only be set within " +
            config.radius +
            " blocks of the world spawn."
    ).withColor(Colors.LIGHT_GRAY);

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
            dispatcher.register(CommandManager.literal(WHY_COMMAND).executes(context -> {
                context.getSource().sendFeedback(() -> LONG_MESSAGE, false);
                return 1;
            }
        )));
    }

    public static boolean shouldSetSpawn(ServerPlayerEntity player) {
        var radius = config.radius();
        var shouldSetSpawn = player.getWorld().getLevelProperties().getSpawnPos().toCenterPos().subtract(player.getPos()).horizontalLength() < radius;
        if (!shouldSetSpawn) {
            player.sendMessage(SHORT_MESSAGE);
        }
        return shouldSetSpawn;
    }
}
