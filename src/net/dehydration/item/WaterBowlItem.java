package net.dehydration.item;

import java.util.Optional;

import net.dehydration.DehydrationMain;
import net.dehydration.access.ThirstManagerAccess;
import net.dehydration.init.ConfigInit;
import net.dehydration.init.EffectInit;
import net.dehydration.misc.ThirstTooltipData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.Items;
import net.minecraft.item.consume.UseAction;
import net.minecraft.item.tooltip.TooltipData;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

public class WaterBowlItem extends Item {

    private final boolean hasThirstChance;

    public WaterBowlItem(net.minecraft.item.Item.Settings settings, boolean hasThirstChance) {
        super(settings);
        this.hasThirstChance = hasThirstChance;
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        ItemStack itemStack = super.finishUsing(stack, world, user);

        if (user instanceof PlayerEntity playerEntity) {
            int thirstQuench = 0;
            for (int i = 0; i < DehydrationMain.HYDRATION_TEMPLATES.size(); i++) {
                if (DehydrationMain.HYDRATION_TEMPLATES.get(i).containsItem(stack.getItem())) {
                    thirstQuench = DehydrationMain.HYDRATION_TEMPLATES.get(i).getHydration();
                    break;
                }
            }
            if (thirstQuench == 0) {
                thirstQuench = ConfigInit.CONFIG.water_bowl_quench;
            }
            ((ThirstManagerAccess) user).getThirstManager().add(thirstQuench);

            if (!world.isClient() && this.hasThirstChance && world.getRandom().nextFloat() >= ConfigInit.CONFIG.water_bowl_thirst_chance) {
                user.addStatusEffect(new StatusEffectInstance(EffectInit.THIRST, ConfigInit.CONFIG.potion_bad_thirst_duration / 2, 0, false, false, true));
            }

            if (playerEntity.isCreative()) {
                return itemStack;
            }
        }
        return new ItemStack(Items.field_8428);
    }

    @Override
    public int getMaxUseTime(ItemStack stack, LivingEntity user) {
        return 32;
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.field_8946;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        BlockHitResult hitResult = raycast(world, user, RaycastContext.FluidHandling.field_1345);
        BlockPos blockPos = hitResult.getBlockPos();

        FluidState fluidState = world.getFluidState(blockPos);
        if (hitResult.getType() == HitResult.Type.field_1332 && world.canEntityModifyAt(user, blockPos) && fluidState.isIn(FluidTags.field_15517) && user.isSneaking()) {
            world.playSound(user, user.getX(), user.getY(), user.getZ(), SoundEvents.field_14834, SoundCategory.field_15254, 1.0F, 1.0F);
            return TypedActionResult.consume(new ItemStack(Items.field_8428));
        }

        return ItemUsage.consumeHeldItem(world, user, hand);
    }

    @Override
    public Optional<TooltipData> getTooltipData(ItemStack stack) {
        if (ConfigInit.CONFIG.thirst_preview) {
            int thirstQuench = 0;
            for (int i = 0; i < DehydrationMain.HYDRATION_TEMPLATES.size(); i++) {
                if (DehydrationMain.HYDRATION_TEMPLATES.get(i).containsItem(stack.getItem())) {
                    thirstQuench = DehydrationMain.HYDRATION_TEMPLATES.get(i).getHydration();
                    break;
                }
            }
            if (thirstQuench == 0) {
                thirstQuench = ConfigInit.CONFIG.water_bowl_quench;
            }
            return Optional.of(new ThirstTooltipData(this.hasThirstChance ? 2 : 0, thirstQuench));
        } else {
            return super.getTooltipData(stack);
        }
    }

}
