package net.dehydration.block.entity;

import net.dehydration.init.ItemInit;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeveledCauldronBlock;
import net.minecraft.block.cauldron.CauldronBehavior;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

import java.util.Map;
import java.util.function.Predicate;

public class CauldronBehaviorAccess {

    public static void registerBehavior() {
        Map<Item, CauldronBehavior> EMPTY_CAULDRON_BEHAVIOR_MAP = CauldronBehavior.EMPTY_CAULDRON_BEHAVIOR.comp_1982();
        Map<Item, CauldronBehavior> WATER_CAULDRON_BEHAVIOR_MAP = CauldronBehavior.WATER_CAULDRON_BEHAVIOR.comp_1982();

        CauldronBehavior EMPTY_CAULDRON_DRAIN_BOWL = (state, world, pos, player, hand, stack) -> {
            if (!world.isClient()) {
                Item item = stack.getItem();
                player.setStackInHand(hand, ItemUsage.exchangeStack(stack, player, new ItemStack(Items.field_8428)));
                player.incrementStat(Stats.field_15373);
                player.incrementStat(Stats.field_15372.getOrCreateStat(item));
                world.setBlockState(pos, Blocks.field_27097.getDefaultState().with(LeveledCauldronBlock.LEVEL, 2));
                world.playSound(null, pos, SoundEvents.field_14834, SoundCategory.field_15245, 1.0F, 1.0F);
                world.emitGameEvent(null, GameEvent.field_28166, pos);
            }

            return ItemActionResult.success(world.isClient());
        };
        EMPTY_CAULDRON_BEHAVIOR_MAP.put(ItemInit.WATER_BOWL, EMPTY_CAULDRON_DRAIN_BOWL);
        EMPTY_CAULDRON_BEHAVIOR_MAP.put(ItemInit.PURIFIED_WATER_BOWL, EMPTY_CAULDRON_DRAIN_BOWL);

        CauldronBehavior WATER_CAULDRON_DRAIN_BOWL = (state, world, pos, player, hand, stack) -> {
            int currentLevel = state.get(LeveledCauldronBlock.LEVEL);
            if (LeveledCauldronBlock.MAX_LEVEL - currentLevel >= 2) {
                if (!world.isClient()) {
                    Item item = stack.getItem();
                    player.setStackInHand(hand, ItemUsage.exchangeStack(stack, player, new ItemStack(Items.field_8428)));
                    player.incrementStat(Stats.field_15373);
                    player.incrementStat(Stats.field_15372.getOrCreateStat(item));
                    world.setBlockState(pos, state.with(LeveledCauldronBlock.LEVEL, currentLevel + 2));
                    world.playSound(null, pos, SoundEvents.field_14834, SoundCategory.field_15245, 1.0F, 1.0F);
                    world.emitGameEvent(null, GameEvent.field_28166, pos);
                }

                return ItemActionResult.success(world.isClient());
            }
            return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        };
        WATER_CAULDRON_BEHAVIOR_MAP.put(ItemInit.WATER_BOWL, WATER_CAULDRON_DRAIN_BOWL);
        WATER_CAULDRON_BEHAVIOR_MAP.put(ItemInit.PURIFIED_WATER_BOWL, WATER_CAULDRON_DRAIN_BOWL);

        CauldronBehavior WATER_CAULDRON_FILL_BOWL = (state, world, pos, player, hand, stack) -> {
            int currentLevel = state.get(LeveledCauldronBlock.LEVEL);
            if (currentLevel >= 2) {
                if (!world.isClient()) {
                    Item item = stack.getItem();
                    player.setStackInHand(hand, ItemUsage.exchangeStack(stack, player, new ItemStack(ItemInit.WATER_BOWL)));
                    player.incrementStat(Stats.field_15373);
                    player.incrementStat(Stats.field_15372.getOrCreateStat(item));
                    if (currentLevel > 2) {
                        world.setBlockState(pos, state.with(LeveledCauldronBlock.LEVEL, currentLevel - 2));
                    } else {
                        world.setBlockState(pos, Blocks.field_10593.getDefaultState());
                    }
                    world.playSound(null, pos, SoundEvents.field_15126, SoundCategory.field_15245, 1.0f, 1.0f);
                    world.emitGameEvent(null, GameEvent.field_28167, pos);
                }

                return ItemActionResult.success(world.isClient());
            }
            return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        };
        WATER_CAULDRON_BEHAVIOR_MAP.put(Items.field_8428, WATER_CAULDRON_FILL_BOWL);
    }

    public static ItemActionResult fillFromCauldron(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack, ItemStack output, Predicate<BlockState> predicate, SoundEvent soundEvent) {
        if (!predicate.test(state)) {
            return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (!world.isClient()) {
            Item item = stack.getItem();
            player.setStackInHand(hand, ItemUsage.exchangeStack(stack, player, output));
            player.incrementStat(Stats.field_15373);
            player.incrementStat(Stats.field_15372.getOrCreateStat(item));
            int currentLevel = state.get(LeveledCauldronBlock.LEVEL);
            if (currentLevel <= 1) {
                world.setBlockState(pos, Blocks.field_10593.getDefaultState());
            } else {
                world.setBlockState(pos, state.with(LeveledCauldronBlock.LEVEL, currentLevel - 1));
            }
            world.playSound(null, pos, soundEvent, SoundCategory.field_15245, 1.0F, 1.0F);
            world.emitGameEvent(null, GameEvent.field_28167, pos);
        }
        return ItemActionResult.success(world.isClient());
    }
}
