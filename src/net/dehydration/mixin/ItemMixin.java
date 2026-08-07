package net.dehydration.mixin;

import net.dehydration.DehydrationMain;
import net.dehydration.init.ConfigInit;
import net.dehydration.init.TagInit;
import net.dehydration.misc.ThirstTooltipData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PotionItem;
import net.minecraft.item.ThrowablePotionItem;
import net.minecraft.item.tooltip.TooltipData;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(Item.class)
public abstract class ItemMixin {

    @Shadow
    protected static BlockHitResult raycast(World world, PlayerEntity player, RaycastContext.FluidHandling fluidHandling) {
        throw new AssertionError();
    }

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void dehydration$useRegularPotion(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<ActionResult> info) {
        Item self = (Item) (Object) this;
        if (!(self instanceof PotionItem) || self instanceof ThrowablePotionItem) {
            return;
        }

        BlockHitResult hitResult = raycast(world, user, RaycastContext.FluidHandling.SOURCE_ONLY);
        if (hitResult.getType() != HitResult.Type.BLOCK) {
            return;
        }

        BlockPos blockPos = hitResult.getBlockPos();
        if (world.canEntityModifyAt(user, blockPos) && world.getFluidState(blockPos).isIn(FluidTags.WATER)) {
            world.playSound(user, user.getX(), user.getY(), user.getZ(), SoundEvents.ITEM_BOTTLE_EMPTY, SoundCategory.NEUTRAL, 1.0f, 1.0f);
            info.setReturnValue((world.isClient() ? ActionResult.SUCCESS : ActionResult.SUCCESS_SERVER)
                    .withNewHandStack(new ItemStack(Items.GLASS_BOTTLE)));
        }
    }

    @Inject(method = "getTooltipData", at = @At("HEAD"), cancellable = true)
    private void getTooltipDataMixin(ItemStack stack, CallbackInfoReturnable<Optional<TooltipData>> info) {
        if (ConfigInit.CONFIG.thirst_preview) {
            int thirstQuench = 0;
            int quality = 0;
            if (stack.isOf(Items.HONEY_BOTTLE)) {
                thirstQuench = ConfigInit.CONFIG.honey_quench;
            } else if (stack.isOf(Items.MILK_BUCKET)) {
                thirstQuench = ConfigInit.CONFIG.milk_thirst_quench;
                quality = 1;
            } else if (stack.isIn(TagInit.HYDRATING_STEW)) {
                thirstQuench = ConfigInit.CONFIG.stew_thirst_quench;
            } else if (stack.isIn(TagInit.HYDRATING_FOOD)) {
                thirstQuench = ConfigInit.CONFIG.food_thirst_quench;
            } else if (stack.isIn(TagInit.HYDRATING_DRINKS)) {
                thirstQuench = ConfigInit.CONFIG.drinks_thirst_quench;
            } else if (stack.isIn(TagInit.STRONGER_HYDRATING_STEW)) {
                thirstQuench = ConfigInit.CONFIG.stronger_stew_thirst_quench;
            } else if (stack.isIn(TagInit.STRONGER_HYDRATING_FOOD)) {
                thirstQuench = ConfigInit.CONFIG.stronger_food_thirst_quench;
            } else if (stack.isIn(TagInit.STRONGER_HYDRATING_DRINKS)) {
                thirstQuench = ConfigInit.CONFIG.stronger_drinks_thirst_quench;
            }
            for (int i = 0; i < DehydrationMain.HYDRATION_TEMPLATES.size(); i++) {
                if (DehydrationMain.HYDRATION_TEMPLATES.get(i).containsItem(stack.getItem())) {
                    thirstQuench = DehydrationMain.HYDRATION_TEMPLATES.get(i).getHydration();
                    break;
                }
            }

            if (thirstQuench > 0) {
                info.setReturnValue(Optional.of(new ThirstTooltipData(quality, thirstQuench)));
            }
        }
    }
}
