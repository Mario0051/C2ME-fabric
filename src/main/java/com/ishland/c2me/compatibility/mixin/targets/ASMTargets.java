package com.ishland.c2me.compatibility.mixin.targets;

import net.minecraft.world.gen.feature.LakeFeature;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = {
        LakeFeature.class
})
public class ASMTargets {
}
