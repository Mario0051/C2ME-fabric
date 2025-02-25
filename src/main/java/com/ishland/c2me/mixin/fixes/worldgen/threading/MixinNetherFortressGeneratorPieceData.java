package com.ishland.c2me.mixin.fixes.worldgen.threading;

import com.ishland.c2me.common.fixes.worldgen.threading.INetherFortressGeneratorPieceData;
import net.minecraft.structure.NetherFortressGenerator;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.concurrent.atomic.AtomicInteger;

@Mixin(NetherFortressGenerator.PieceData.class)
public class MixinNetherFortressGeneratorPieceData implements INetherFortressGeneratorPieceData {

    private final AtomicInteger generatedCountAtomic = new AtomicInteger();

    @Dynamic
    @Redirect(method = "*", at = @At(value = "FIELD", target = "Lnet/minecraft/structure/NetherFortressGenerator$PieceData;field_14502:I", opcode = Opcodes.GETFIELD))
    private int redirectGetGeneratedCount(NetherFortressGenerator.PieceData pieceData) {
        return this.generatedCountAtomic.get();
    }

    @Dynamic
    @Redirect(method = "*", at = @At(value = "FIELD", target = "Lnet/minecraft/structure/NetherFortressGenerator$PieceData;field_14502:I", opcode = Opcodes.PUTFIELD), require = 0)
    private void redirectSetGeneratedCount(NetherFortressGenerator.PieceData pieceData, int value) {
        this.generatedCountAtomic.set(value);
    }

    @Override
    public AtomicInteger getGeneratedCountAtomic() {
        return this.generatedCountAtomic;
    }
}
