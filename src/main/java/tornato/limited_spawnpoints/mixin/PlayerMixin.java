package tornato.limited_spawnpoints.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import tornato.limited_spawnpoints.LimitedSpawnpoints;

@Mixin(ServerPlayerEntity.class)
class PlayerMixin {
    @WrapWithCondition(method = "trySleep", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;setSpawnPoint(Lnet/minecraft/server/network/ServerPlayerEntity$Respawn;Z)V"))
    private boolean foo(ServerPlayerEntity player, ServerPlayerEntity.Respawn respawn, boolean sendMessage) {
        return LimitedSpawnpoints.shouldSetSpawn(player);
    }
}