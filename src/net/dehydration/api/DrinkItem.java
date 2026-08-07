package net.dehydration.api;

import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.consume.UseAction;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.world.World;

/**
 * The {@link DrinkItem} represents any drinkable item
 */
public class DrinkItem extends Item {
    public DrinkItem(net.minecraft.item.Item.Settings settings) {
        super(settings);
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.field_8946;
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        PlayerEntity playerEntity = user instanceof PlayerEntity ? (PlayerEntity) user : null;

        // Trigger consumption of an item if it's the server
        if (playerEntity instanceof ServerPlayerEntity) {
            Criteria.CONSUME_ITEM.trigger((ServerPlayerEntity) playerEntity, stack);
        }

        if (playerEntity != null) {
            // Increment the use statistic
            playerEntity.incrementStat(Stats.field_15372.getOrCreateStat(this));

            // Drinks are not available in creative mode
            if (!playerEntity.getAbilities().creativeMode) {
                // The drink also has to have a FoodComponent
                FoodComponent foodComponent = stack.get(DataComponentTypes.field_50075);
                if (foodComponent != null) {
                    DrinkEvent.EVENT.invoker().onDrink(stack, playerEntity);
                    user.eatFood(world, stack, foodComponent);
                }
            }
        }

        // Create a glass bottle after the item is consumed
        if (playerEntity == null || !playerEntity.getAbilities().creativeMode) {
            if (stack.isEmpty()) {
                return new ItemStack(Items.field_8469);
            }

            if (playerEntity != null) {
                // offerOrDrop is generally safer to use than insertStack
                playerEntity.getInventory().offerOrDrop(new ItemStack(Items.field_8469));
            }
        }

        return stack;
    }
}
