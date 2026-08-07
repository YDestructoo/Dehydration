package net.dehydration.init;

import net.dehydration.block.render.BambooPumpRenderer;
import net.dehydration.misc.ThirstTooltipComponent;
import net.dehydration.misc.ThirstTooltipData;
import net.dehydration.thirst.ThirstHudRender;
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import net.minecraft.client.render.BlockRenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.util.Identifier;

public class RenderInit {
    public static final Identifier THIRST_ICON = Identifier.of("dehydration", "textures/gui/thirst.png");

    public static void init() {
        BlockRenderLayerMap.putBlock(BlockInit.CAMPFIRE_CAULDRON_BLOCK, BlockRenderLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(BlockInit.COPPER_CAULDRON_BLOCK, BlockRenderLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(BlockInit.COPPER_WATER_CAULDRON_BLOCK, BlockRenderLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(BlockInit.COPPER_POWDERED_CAULDRON_BLOCK, BlockRenderLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(BlockInit.COPPER_PURIFIED_WATER_CAULDRON_BLOCK, BlockRenderLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(BlockInit.BAMBOO_PUMP_BLOCK, BlockRenderLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(BlockInit.RAINWATER_COLLECTOR_BLOCK, BlockRenderLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(BlockInit.RAINWATER_WATER_COLLECTOR_BLOCK, BlockRenderLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(BlockInit.RAINWATER_POWDERED_COLLECTOR_BLOCK, BlockRenderLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(BlockInit.RAINWATER_PURIFIED_WATER_COLLECTOR_BLOCK, BlockRenderLayer.CUTOUT);
        BlockRenderLayerMap.putFluids(BlockRenderLayer.TRANSLUCENT, FluidInit.PURIFIED_WATER, FluidInit.PURIFIED_FLOWING_WATER);

        BlockEntityRendererFactories.register(BlockInit.BAMBOO_PUMP_ENTITY, BambooPumpRenderer::new);
        TooltipComponentCallback.EVENT.register(data -> data instanceof ThirstTooltipData thirstData ? new ThirstTooltipComponent(thirstData) : null);
        HudRenderCallback.EVENT.register((context, tickCounter) -> {
            var client = net.minecraft.client.MinecraftClient.getInstance();
            ThirstHudRender.renderThirstHud(context, client, client.player, context.getScaledWindowWidth(), context.getScaledWindowHeight(), client.player == null ? 0 : client.player.age);
        });
    }
}
