package net.dehydration.compat;

import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiWorldInteractionRecipe;
import dev.emi.emi.api.stack.EmiStack;
import net.dehydration.DehydrationMain;
import net.dehydration.init.FluidInit;
import net.dehydration.init.ItemInit;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class DehydrationEmiPlugin implements EmiPlugin {

    @Override
    public void register(EmiRegistry registry) {
        ItemStack purifiedWaterBottle = ItemVariant.of(Items.field_8574).toStack();
        purifiedWaterBottle.set(DataComponentTypes.field_49651, new PotionContentsComponent(ItemInit.PURIFIED_WATER));

        registry.addRecipe(EmiWorldInteractionRecipe.builder().id(DehydrationMain.identifierOf("/emi/recipe/water_bowl")).leftInput(EmiStack.of(Items.field_8428)).rightInput(EmiStack.of(Fluids.WATER), false).output(EmiStack.of(ItemInit.WATER_BOWL)).build());
        registry.addRecipe(EmiWorldInteractionRecipe.builder().id(DehydrationMain.identifierOf("/emi/recipe/purified_water_bowl")).leftInput(EmiStack.of(Items.field_8428)).rightInput(EmiStack.of(FluidInit.PURIFIED_WATER), false).output(EmiStack.of(ItemInit.PURIFIED_WATER_BOWL)).build());
        registry.addRecipe(EmiWorldInteractionRecipe.builder().id(DehydrationMain.identifierOf("/emi/recipe/purified_water_bottle")).leftInput(EmiStack.of(Items.field_8469)).rightInput(EmiStack.of(FluidInit.PURIFIED_WATER), false).output(EmiStack.of(purifiedWaterBottle)).build());
    }
}
