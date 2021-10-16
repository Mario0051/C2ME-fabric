package com.ishland.c2me.mixin.optimization.reduce_allocs;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.nbt.AbstractListTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.ArrayList;
import java.util.List;

@Mixin(ListTag.class)
public abstract class MixinListTag extends AbstractListTag<Tag> {

    @Shadow private byte type;

    @Shadow @Final private List<Tag> value;

    @Shadow protected abstract boolean canAdd(Tag element);

    @ModifyArg(method = "<init>()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/ListTag;<init>(Ljava/util/List;B)V"), index = 0)
    private static List<Tag> modifyList(List<Tag> list) {
        return new ObjectArrayList<>();
    }

    @Redirect(method = "<init>()V", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/Lists;newArrayList()Ljava/util/ArrayList;"))
    private static <E> ArrayList<E> redirectNewArrayList() {
        return null; // avoid double list creation
    }

    @Override
    public boolean add(Tag element) {
        if (this.canAdd(element)) {
            this.value.add(element);
            return true;
        } else {
            return false;
        }
    }
}
