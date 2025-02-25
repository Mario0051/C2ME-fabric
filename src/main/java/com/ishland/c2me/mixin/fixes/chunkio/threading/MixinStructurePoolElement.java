package com.ishland.c2me.mixin.fixes.chunkio.threading;

import com.ishland.c2me.common.fixes.chunkio.threading.SynchronizedCodec;
import com.mojang.serialization.Codec;
import net.minecraft.structure.pool.StructurePoolElement;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(StructurePoolElement.class)
public class MixinStructurePoolElement {

    @Mutable
    @Shadow @Final public static Codec<StructurePoolElement> field_24953;

    @Dynamic
    @Inject(method = "<clinit>", at = @At("RETURN"))
    private static void onCLInit(CallbackInfo info) {
        field_24953 = new SynchronizedCodec<>(field_24953);
    }

}
