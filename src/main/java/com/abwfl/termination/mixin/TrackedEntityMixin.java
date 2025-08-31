package com.abwfl.termination.mixin;

import com.abwfl.termination.Termination;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.server.level.ChunkMap$TrackedEntity")
public abstract class TrackedEntityMixin {
    @Final
    @Shadow
    Entity entity;

    @Shadow
    public abstract void removePlayer(ServerPlayer pPlayer);

    @Inject(method = "updatePlayer",
            at = @At("HEAD"), cancellable = true)
    private void onUpdatePlayer(ServerPlayer player, CallbackInfo ci) {
        if (Termination.terminationList.shouldHideEntity(player.getStringUUID(), entity.getType())) {
            this.removePlayer(player);
            ci.cancel();
        }
    }
}