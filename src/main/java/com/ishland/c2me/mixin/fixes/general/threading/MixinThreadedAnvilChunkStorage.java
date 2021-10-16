package com.ishland.c2me.mixin.fixes.general.threading;

import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.thread.ThreadExecutor;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ThreadedAnvilChunkStorage.class)
public class MixinThreadedAnvilChunkStorage {

    @Shadow @Final private ThreadExecutor<Runnable> mainThreadExecutor;

    @Shadow @Final private ServerWorld world;

    @Dynamic
    @Redirect(method = "method_20616", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ThreadedAnvilChunkStorage$TicketManager;addTicketWithLevel(Lnet/minecraft/server/world/ChunkTicketType;Lnet/minecraft/util/math/ChunkPos;ILjava/lang/Object;)V"))
    private <T> void redirectAddLightTicket(ThreadedAnvilChunkStorage.TicketManager ticketManager, ChunkTicketType<T> type, ChunkPos pos, int level, T argument) {
        if (this.world.getServer().getThread() != Thread.currentThread()) {
            this.mainThreadExecutor.execute(() -> ticketManager.addTicketWithLevel(type, pos, level, argument));
        } else {
            ticketManager.addTicketWithLevel(type, pos, level, argument);
        }
    }

}
