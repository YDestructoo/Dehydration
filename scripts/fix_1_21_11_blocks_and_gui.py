#!/usr/bin/env python3
from pathlib import Path

ROOT = Path('src/net/dehydration')


def patch(rel, pairs):
    path = ROOT / rel
    text = path.read_text(encoding='utf-8')
    old = text
    for a, b in pairs:
        if a not in text:
            print(f'warning: pattern not found in {rel}: {a[:80]!r}')
        text = text.replace(a, b)
    if text != old:
        path.write_text(text, encoding='utf-8')
        print('updated', path)

# AbstractBlock#getPickStack gained includeData and is protected.
for rel in ['block/AbstractRainwaterCollectorBlock.java', 'block/AbstractCopperCauldronBlock.java']:
    patch(rel, [
        ('public ItemStack getPickStack(WorldView world, BlockPos pos, BlockState state) {',
         'protected ItemStack getPickStack(WorldView world, BlockPos pos, BlockState state, boolean includeData) {'),
    ])

# Collision callbacks gained a collision handler and comparator reads gained a direction.
for rel in ['block/RainwaterLeveledCollectorBlock.java', 'block/CopperLeveledCauldronBlock.java']:
    patch(rel, [
        ('import net.minecraft.entity.Entity;\n', 'import net.minecraft.entity.Entity;\nimport net.minecraft.entity.EntityCollisionHandler;\n'),
        ('import net.minecraft.util.math.BlockPos;\n', 'import net.minecraft.util.math.BlockPos;\nimport net.minecraft.util.math.Direction;\n'),
        ('import net.minecraft.world.WorldEvents;\n', 'import net.minecraft.world.WorldEvents;\nimport net.minecraft.server.world.ServerWorld;\n'),
        ('public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {',
         'protected void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity, EntityCollisionHandler handler, boolean bool) {'),
        ('if (entity.canModifyAt(world, pos)) {', 'if (world instanceof ServerWorld serverWorld && entity.canModifyAt(serverWorld, pos)) {'),
        ('public int getComparatorOutput(BlockState state, World world, BlockPos pos) {',
         'protected int getComparatorOutput(BlockState state, World world, BlockPos pos, Direction direction) {'),
    ])

# Campfire cauldron comparator callback also gained a direction.
patch('block/CampfireCauldronBlock.java', [
    ('public int getComparatorOutput(BlockState state, World world, BlockPos pos) {',
     'protected int getComparatorOutput(BlockState state, World world, BlockPos pos, Direction direction) {'),
])

# Bamboo pump: neighbor-update and replacement callbacks changed substantially.
patch('block/BambooPumpBlock.java', [
    ('import net.minecraft.world.WorldAccess;\n', ''),
    ('import net.minecraft.world.WorldView;\n', 'import net.minecraft.world.WorldView;\nimport net.minecraft.world.tick.ScheduledTickView;\nimport net.minecraft.server.world.ServerWorld;\nimport net.minecraft.util.math.random.Random;\n'),
    ('public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {\n        if (direction == Direction.DOWN && !state.canPlaceAt(world, pos)) {\n            return Blocks.AIR.getDefaultState();\n        } else if (state.get(WATERLOGGED)) {\n            world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));\n        }\n        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);\n    }',
     'protected BlockState getStateForNeighborUpdate(BlockState state, WorldView world, ScheduledTickView tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, Random random) {\n        if (direction == Direction.DOWN && !state.canPlaceAt(world, pos)) {\n            return Blocks.AIR.getDefaultState();\n        } else if (state.get(WATERLOGGED)) {\n            tickView.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));\n        }\n        return super.getStateForNeighborUpdate(state, world, tickView, pos, direction, neighborPos, neighborState, random);\n    }'),
    ('public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {\n        if (!state.isOf(newState.getBlock())) {\n            BlockEntity blockEntity = world.getBlockEntity(pos);\n            if (blockEntity instanceof Inventory) {\n                ItemScatterer.spawn(world, pos, (Inventory) blockEntity);\n                world.updateComparators(pos, this);\n            }\n\n            super.onStateReplaced(state, world, pos, newState, moved);\n        }\n    }',
     'protected void onStateReplaced(BlockState state, ServerWorld world, BlockPos pos, boolean moved) {\n        BlockEntity blockEntity = world.getBlockEntity(pos);\n        if (blockEntity instanceof Inventory inventory) {\n            ItemScatterer.spawn(world, pos, inventory);\n            world.updateComparators(pos, this);\n        }\n        super.onStateReplaced(state, world, pos, moved);\n    }'),
])

# World gamerules are server-side in 1.21.11; use the dimension registry key for Nether heat.
patch('mixin/PlayerEntityMixin.java', [
    ('import net.minecraft.world.rule.GameRules;\n', 'import net.minecraft.world.rule.GameRules;\nimport net.minecraft.server.world.ServerWorld;\n'),
    ('if (this.getEntityWorld().getDifficulty() == Difficulty.PEACEFUL && this.getEntityWorld().getGameRules().getBoolean(GameRules.NATURAL_HEALTH_REGENERATION) && this.thirstManager.hasThirst()) {',
     'if (this.getEntityWorld() instanceof ServerWorld serverWorld && serverWorld.getDifficulty() == Difficulty.PEACEFUL && serverWorld.getGameRules().getBoolean(GameRules.NATURAL_HEALTH_REGENERATION) && this.thirstManager.hasThirst()) {'),
    ('ConfigInit.CONFIG.harder_nether && this.getEntityWorld().getDimension().ultrawarm()',
     'ConfigInit.CONFIG.harder_nether && this.getEntityWorld().getRegistryKey().equals(World.NETHER)'),
])

# Vanilla removed the public FILL_WITH_WATER constant; register equivalent behavior explicitly.
path = ROOT / 'mixin/CauldronBehaviorMixin.java'
path.write_text('''package net.dehydration.mixin;\n\nimport java.util.Map;\n\nimport org.spongepowered.asm.mixin.Mixin;\nimport org.spongepowered.asm.mixin.injection.At;\nimport org.spongepowered.asm.mixin.injection.Inject;\nimport org.spongepowered.asm.mixin.injection.callback.CallbackInfo;\n\nimport net.dehydration.init.ItemInit;\nimport net.minecraft.block.Blocks;\nimport net.minecraft.block.cauldron.CauldronBehavior;\nimport net.minecraft.item.Item;\nimport net.minecraft.sound.SoundEvents;\n\n@Mixin(CauldronBehavior.class)\npublic interface CauldronBehaviorMixin {\n\n    @Inject(method = "registerBucketBehavior", at = @At("TAIL"))\n    private static void registerBucketBehaviorMixin(Map<Item, CauldronBehavior> behavior, CallbackInfo info) {\n        behavior.put(ItemInit.PURIFIED_BUCKET, (state, world, pos, player, hand, stack) ->\n                CauldronBehavior.fillCauldron(world, pos, player, hand, stack, Blocks.WATER_CAULDRON.getDefaultState(), SoundEvents.ITEM_BUCKET_EMPTY));\n    }\n}\n''', encoding='utf-8')
print('updated', path)

# Fabric's block render-layer API moved packages and now uses static methods.
path = ROOT / 'init/RenderInit.java'
path.write_text('''package net.dehydration.init;\n\nimport net.dehydration.block.render.BambooPumpRenderer;\nimport net.dehydration.misc.ThirstTooltipComponent;\nimport net.dehydration.misc.ThirstTooltipData;\nimport net.dehydration.thirst.ThirstHudRender;\nimport net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;\nimport net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;\nimport net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;\nimport net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererFactories;\nimport net.minecraft.client.render.BlockRenderLayer;\nimport net.minecraft.util.Identifier;\n\npublic class RenderInit {\n    public static final Identifier THIRST_ICON = Identifier.of("dehydration", "textures/gui/thirst.png");\n\n    public static void init() {\n        BlockRenderLayerMap.putBlock(BlockInit.RAINWATER_COLLECTOR_BLOCK, BlockRenderLayer.CUTOUT);\n        BlockRenderLayerMap.putBlock(BlockInit.RAINWATER_LEVELED_COLLECTOR_BLOCK, BlockRenderLayer.CUTOUT);\n        BlockRenderLayerMap.putBlock(BlockInit.BAMBOO_PUMP_BLOCK, BlockRenderLayer.CUTOUT);\n        BlockRenderLayerMap.putBlock(BlockInit.CAMPFIRE_CAULDRON_BLOCK, BlockRenderLayer.CUTOUT);\n        BlockRenderLayerMap.putBlock(BlockInit.COPPER_CAULDRON_BLOCK, BlockRenderLayer.CUTOUT);\n        BlockRenderLayerMap.putBlock(BlockInit.COPPER_WATER_CAULDRON_BLOCK, BlockRenderLayer.CUTOUT);\n        BlockRenderLayerMap.putBlock(BlockInit.COPPER_PURIFIED_WATER_CAULDRON_BLOCK, BlockRenderLayer.CUTOUT);\n        BlockRenderLayerMap.putFluids(BlockRenderLayer.TRANSLUCENT, FluidInit.PURIFIED_WATER, FluidInit.FLOWING_PURIFIED_WATER);\n\n        BlockEntityRendererFactories.register(BlockInit.BAMBOO_PUMP_ENTITY, BambooPumpRenderer::new);\n        TooltipComponentCallback.EVENT.register(data -> data instanceof ThirstTooltipData thirstData ? new ThirstTooltipComponent(thirstData) : null);\n        HudRenderCallback.EVENT.register((context, tickCounter) -> {\n            var client = net.minecraft.client.MinecraftClient.getInstance();\n            ThirstHudRender.renderThirstHud(context, client, client.player, context.getScaledWindowWidth(), context.getScaledWindowHeight(), client.player == null ? 0 : client.player.age);\n        });\n    }\n}\n''', encoding='utf-8')
print('updated', path)

# Legacy ModelPredicateProviderRegistry no longer exists. The dynamic item model is migrated separately as a resource.
path = ROOT / 'init/ModelProviderInit.java'
path.write_text('''package net.dehydration.init;\n\n/** Minecraft 1.21.11 removed the legacy ModelPredicateProviderRegistry. */\npublic class ModelProviderInit {\n    public static void init() {\n        // Item model selection is data-driven in 1.21.11.\n    }\n}\n''', encoding='utf-8')
print('updated', path)

# DrinkItem: FoodComponent consumption moved away from LivingEntity#eatFood.
patch('api/DrinkItem.java', [
    ('                    user.eatFood(world, stack, foodComponent);',
     '                    playerEntity.getHungerManager().eat(foodComponent);\n                    stack.decrementUnlessCreative(1, playerEntity);'),
])

# Tooltip component API now passes TextRenderer to height and dimensions to drawItems.
path = ROOT / 'misc/ThirstTooltipComponent.java'
path.write_text('''package net.dehydration.misc;\n\nimport net.dehydration.init.RenderInit;\nimport net.fabricmc.api.EnvType;\nimport net.fabricmc.api.Environment;\nimport net.minecraft.client.font.TextRenderer;\nimport net.minecraft.client.gl.RenderPipelines;\nimport net.minecraft.client.gui.DrawContext;\nimport net.minecraft.client.gui.tooltip.TooltipComponent;\n\n@Environment(EnvType.CLIENT)\npublic class ThirstTooltipComponent implements TooltipComponent {\n    private final int thirstQuench;\n    private final int quality;\n\n    public ThirstTooltipComponent(ThirstTooltipData data) {\n        this.thirstQuench = data.getThirstQuench();\n        this.quality = data.getDrinkQuality();\n    }\n\n    @Override\n    public int getHeight(TextRenderer textRenderer) {\n        return 11;\n    }\n\n    @Override\n    public int getWidth(TextRenderer textRenderer) {\n        return this.thirstQuench * 9 / 2 + (this.thirstQuench % 2 != 0 ? 9 : 0);\n    }\n\n    @Override\n    public void drawItems(TextRenderer textRenderer, int x, int y, int width, int height, DrawContext context) {\n        for (int i = 0; i < this.thirstQuench / 2; i++) {\n            context.drawTexture(RenderPipelines.GUI_TEXTURED, RenderInit.THIRST_ICON, x + i * 9 - 1, y, 0, 0, 9, 9, 256, 256);\n            context.drawTexture(RenderPipelines.GUI_TEXTURED, RenderInit.THIRST_ICON, x + i * 9 - 1, y, this.quality * 18, 9, 9, 9, 256, 256);\n        }\n        if (this.thirstQuench % 2 != 0) {\n            context.drawTexture(RenderPipelines.GUI_TEXTURED, RenderInit.THIRST_ICON, x + this.thirstQuench / 2 * 9 - 1, y, 0, 0, 9, 9, 256, 256);\n            context.drawTexture(RenderPipelines.GUI_TEXTURED, RenderInit.THIRST_ICON, x + this.thirstQuench / 2 * 9 - 1, y, this.quality * 18 + 9, 9, 9, 9, 256, 256);\n        }\n    }\n}\n''', encoding='utf-8')
print('updated', path)

# HUD rendering now supplies an explicit GUI render pipeline; color overloads replace shader-global alpha state.
path = ROOT / 'thirst/ThirstHudRender.java'
s = path.read_text(encoding='utf-8')
s = s.replace('import com.mojang.blaze3d.systems.RenderSystem;\n', '')
s = s.replace('import net.minecraft.client.gui.DrawContext;\n', 'import net.minecraft.client.gui.DrawContext;\nimport net.minecraft.client.gl.RenderPipelines;\n')
s = s.replace('context.drawTexture(RenderInit.THIRST_ICON,', 'context.drawTexture(RenderPipelines.GUI_TEXTURED, RenderInit.THIRST_ICON,')
s = s.replace('                                RenderSystem.enableBlend();\n                                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, flashAlpha);\n', '')
s = s.replace('                                RenderSystem.disableBlend();\n                                RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);\n', '')
s = s.replace('                            RenderSystem.enableBlend();\n                            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, playerEntity.getFreezingScale());\n', '')
s = s.replace('                            RenderSystem.disableBlend();\n                            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);\n', '')
path.write_text(s, encoding='utf-8')
print('updated', path)
