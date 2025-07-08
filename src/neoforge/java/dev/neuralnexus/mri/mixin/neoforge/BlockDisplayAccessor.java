package dev.neuralnexus.mri.mixin.neoforge;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Display;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Display.BlockDisplay.class)
public interface BlockDisplayAccessor {
    @Invoker("readAdditionalSaveData")
    void mri$readAdditionalSaveData(CompoundTag tag);
}
