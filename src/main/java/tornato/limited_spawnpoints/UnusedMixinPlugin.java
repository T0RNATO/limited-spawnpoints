package tornato.limited_spawnpoints;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.Version;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

import static tornato.limited_spawnpoints.LimitedSpawnpoints.unchecked;

public class UnusedMixinPlugin implements IMixinConfigPlugin {
    @Override public void onLoad(String s) {}
    @Override public String getRefMapperConfig() {return null;}
    @Override public void acceptTargets(Set<String> set, Set<String> set1) {}
    @Override public List<String> getMixins() {return null;}
    @Override public void preApply(String s, ClassNode classNode, String s1, IMixinInfo iMixinInfo) {}
    @Override public void postApply(String s, ClassNode classNode, String s1, IMixinInfo iMixinInfo) {}

    static boolean is216 = FabricLoader.getInstance().getModContainer("minecraft").get().getMetadata().getVersion().compareTo(unchecked(() -> Version.parse("1.21.6"))) >= 0;

    @Override
    public boolean shouldApplyMixin(String targetClass, String mixinClass) {
        return switch (mixinClass) {
            case "tornato.limited_spawnpoints.mixin.PlayerMixin216" -> is216;
            case "tornato.limited_spawnpoints.mixin.PlayerMixin211" -> !is216;
            default -> throw new IllegalStateException("Unexpected value: " + mixinClass);
        };
    }
}
