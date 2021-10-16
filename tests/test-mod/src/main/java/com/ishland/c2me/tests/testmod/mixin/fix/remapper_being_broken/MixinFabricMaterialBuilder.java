package com.ishland.c2me.tests.testmod.mixin.fix.remapper_being_broken;

import net.minecraft.block.Material;
import net.minecraft.block.MaterialColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Pseudo
@Mixin(targets = "net.fabricmc.fabric.api.block.FabricMaterialBuilder")
public abstract class MixinFabricMaterialBuilder extends Material.Builder {

    public MixinFabricMaterialBuilder(MaterialColor color) {
        super(color);
    }

    @Shadow
    public abstract Material build();

    @Unique
    public Material method_15813() {
        return this.build();
    }

}
