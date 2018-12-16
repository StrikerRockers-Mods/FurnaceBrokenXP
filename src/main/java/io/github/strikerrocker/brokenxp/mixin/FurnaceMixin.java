package io.github.strikerrocker.brokenxp.mixin;

import net.minecraft.block.AbstractFurnaceBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.recipe.smelting.AbstractSmeltingRecipe;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Iterator;
import java.util.Map;

@Mixin(AbstractFurnaceBlock.class)
public class FurnaceMixin {

    @Inject(at = @At("HEAD"), method = "onBlockRemoved(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Z)V")
    public void onBroken(BlockState var1, World var2, BlockPos var3, BlockState var4, boolean var5, CallbackInfo info) {
        if (var1.getBlock() != var4.getBlock()) {
            BlockEntity var6 = var2.getBlockEntity(var3);
            if (var6 instanceof AbstractFurnaceBlockEntity) {
                if (!var2.isClient) {
                    Iterator iterator = ((AbstractFurnaceBlockEntity) var6).method_11198().entrySet().iterator();

                    while (iterator.hasNext()) {
                        Map.Entry<Identifier, Integer> entry = (Map.Entry) iterator.next();
                        AbstractSmeltingRecipe recipe = (AbstractSmeltingRecipe) var2.getRecipeManager().get(entry.getKey());
                        float xp;
                        if (recipe != null) {
                            xp = recipe.getExperience();
                        } else {
                            xp = 0.0F;
                        }

                        int value =  entry.getValue();
                        int size;
                        if (xp == 0.0F) {
                            value = 0;
                        } else if (xp < 1.0F) {
                            size = MathHelper.floor((float) value * xp);
                            if (size < MathHelper.ceil((float) value * xp) && Math.random() < (double) ((float) value * xp - (float) size)) {
                                ++size;
                            }

                            value = size;
                        }

                        while (value > 0) {
                            size = ExperienceOrbEntity.roundToOrbSize(value);
                            value -= size;
                            var2.spawnEntity(new ExperienceOrbEntity(var2, var3.getX(), var3.getY() + 0.5D, var3.getZ() + 0.5D, size));
                        }
                    }
                }
            }
        }
    }
}
