package com.abwfl.termination.mixin;

import com.abwfl.termination.Termination;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(Mob.class)
public abstract class MobMixin {
    @Shadow
    @Nullable
    private LivingEntity target;

    @Inject(method = "setTarget",
            at = @At("HEAD"),
            cancellable = true)
    private void onSetTarget(LivingEntity target, CallbackInfo ci) {
        if (target instanceof ServerPlayer player) {
            Entity entity = (Entity)(Object)this;
            if (Termination.terminationList.shouldHideEntity(player.getStringUUID(), entity.getType())) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "doHurtTarget",
            at = @At("HEAD"),
            cancellable = true)
    private void onDoHurtTarget(Entity pEntity, CallbackInfoReturnable<Boolean> cir) {
        if (target instanceof ServerPlayer player) {
            Entity entity = (Entity)(Object)this;
            if (Termination.terminationList.shouldHideEntity(player.getStringUUID(), entity.getType())) {
                this.target = null;
                cir.setReturnValue(false);
            }
        }
    }
}