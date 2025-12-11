package tornato.limited_spawnpoints.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import tornato.limited_spawnpoints.LimitedSpawnpoints;

@Mixin(ServerPlayer.class)
class PlayerMixin {
    @WrapWithCondition(method = "startSleepInBed", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;setRespawnPosition(Lnet/minecraft/server/level/ServerPlayer$RespawnConfig;Z)V"))
    private boolean foo(ServerPlayer player, ServerPlayer.RespawnConfig respawn, boolean sendMessage) {
        return LimitedSpawnpoints.shouldSetSpawn(player);
    }
}