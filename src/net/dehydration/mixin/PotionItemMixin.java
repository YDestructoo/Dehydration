package net.dehydration.mixin;

import net.dehydration.DehydrationMain;
import net.dehydration.init.ConfigInit;
import net.dehydration.misc.ThirstTooltipData;
import net.dehydration.util.ThirstHelper;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PotionItem;
import net.minecraft.item.ThrowablePotionItem;
import net.minecraft.item.tooltip.TooltipData;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Optional;

@Mixin(PotionItem.class)
public abstract class PotionItemMixin extends Item {

    public PotionItemMixin(Item.Settings settings) {
        super(settings);
    }

    @Override
    public Optional<TooltipData> getTooltipData(ItemStack stack) {
        if (ConfigInit.CONFIG.thirst_preview && !(stack.getItem() instanceof ThrowablePotionItem)) {
            int thirstQuench = 0;
            for (int i = 0; i < DehydrationMain.HYDRATION_TEMPLATES.size(); i++) {
                if (DehydrationMain.HYDRATION_TEMPLATES.get(i).containsItem(stack.getItem())) {
                    thirstQuench = DehydrationMain.HYDRATION_TEMPLATES.get(i).getHydration();
                    break;
                }
            }
            if (thirstQuench == 0) {
                thirstQuench = ConfigInit.CONFIG.potion_thirst_quench;
            }
            if (stack.getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT).potion().isPresent()
                    && ThirstHelper.isBadPotion(stack.getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT).potion().get())) {
                return Optional.of(new ThirstTooltipData(2, thirstQuench));
            }
            return Optional.of(new ThirstTooltipData(0, thirstQuench));
        }
        return Optional.empty();
    }
}
