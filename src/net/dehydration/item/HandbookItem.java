package net.dehydration.item;

import java.util.List;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import vazkii.patchouli.api.PatchouliAPI;

public class HandbookItem extends Item {

    private final boolean isPatchouliLoaded = FabricLoader.getInstance().isModLoaded("patchouli");

    public HandbookItem(net.minecraft.item.Item.Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (!world.isClient() && isPatchouliLoaded) {
            PatchouliAPI.get().openBookGUI((ServerPlayerEntity) user, Identifier.of("dehydration", "dehydration"));
            return TypedActionResult.success(user.getStackInHand(hand));
        }
        return TypedActionResult.fail(user.getStackInHand(hand));
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        if (!isPatchouliLoaded) {
            tooltip.add(Text.translatable("item.dehydration.patchouli_book.tooltip"));
        }
        super.appendTooltip(stack, context, tooltip, type);
    }

}
