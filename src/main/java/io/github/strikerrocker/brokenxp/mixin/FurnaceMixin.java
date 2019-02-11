package io.github.strikerrocker.brokenxp.mixin;

import com.google.common.collect.Maps;
import net.minecraft.block.AbstractFurnaceBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.recipe.cooking.CookingRecipe;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(AbstractFurnaceBlock.class)
public class FurnaceMixin {

    private static void spawnXp(World world, int size, float xp, BlockPos pos) {
        int value;
        if (xp == 0.0F) {
            size = 0;
        } else if (xp < 1.0F) {
            value = MathHelper.floor((float) size * xp);
            if (value < MathHelper.ceil((float) size * xp) && Math.random() < (double) ((float) size * xp - (float) value)) {
                ++value;
            }
            size = value;
        }

        while (size > 0) {
            value = ExperienceOrbEntity.roundToOrbSize(size);
            size -= value;
            world.spawnEntity(new ExperienceOrbEntity(world, pos.getX(), pos.getY() + 0.5D, pos.getZ() + 0.5D, value));
        }

    }

    @Inject(at = @At("HEAD"), method = "onBlockRemoved(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Z)V")
    public void onBroken(BlockState var1, World var2, BlockPos var3, BlockState var4, boolean var5, CallbackInfo info) {
        if (var1.getBlock() != var4.getBlock()) {
            BlockEntity blockEntity = var2.getBlockEntity(var3);
            if (blockEntity instanceof AbstractFurnaceBlockEntity && !var2.isClient) {
                Map<Identifier, Integer> recipesUsed = Maps.newHashMap();
                CompoundTag tag = new CompoundTag();
                blockEntity.toTag(tag);

                for (int i = 0; i < tag.getShort("RecipesUsedSize"); ++i) {
                    Identifier identifier_1 = new Identifier(tag.getString("RecipeLocation" + i));
                    int recipeAmt = tag.getInt("RecipeAmount" + i);
                    recipesUsed.put(identifier_1, recipeAmt);
                }

                for (Map.Entry<Identifier, Integer> map : recipesUsed.entrySet()) {
                    var2.getRecipeManager().get(map.getKey()).ifPresent((recipe_1) -> spawnXp(var2, map.getValue(), ((CookingRecipe) recipe_1).getExperience(), var3));
                }
            }
        }
    }
}
