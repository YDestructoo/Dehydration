package net.dehydration.block.render;

import net.dehydration.block.entity.BambooPumpEntity;
import net.dehydration.init.BlockInit;
import net.dehydration.init.ItemInit;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.client.item.ItemModelManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class BambooPumpRenderer implements BlockEntityRenderer<BambooPumpEntity, BambooPumpRenderer.State> {

    private final ItemModelManager itemModelManager;

    public BambooPumpRenderer(BlockEntityRendererFactory.Context ctx) {
        this.itemModelManager = ctx.itemModelManager();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void updateRenderState(BambooPumpEntity blockEntity, State state, float tickProgress, Vec3d cameraPos,
            @Nullable ModelCommandRenderer.CrumblingOverlayCommand crumblingOverlay) {
        BlockEntityRenderState.updateBlockEntityRenderState(blockEntity, state, crumblingOverlay);

        ItemStack stack = blockEntity.getStack(0);
        BlockState blockState = blockEntity.getCachedState();
        state.visible = blockEntity.getWorld() != null
                && !stack.isEmpty()
                && !stack.isOf(Items.BUCKET)
                && !stack.isOf(ItemInit.PURIFIED_BUCKET)
                && blockState.isOf(BlockInit.BAMBOO_PUMP_BLOCK);

        if (!state.visible) {
            return;
        }

        state.direction = blockState.get(HorizontalFacingBlock.FACING);
        this.itemModelManager.clearAndUpdate(
                state.itemRenderState,
                stack,
                ItemDisplayContext.HEAD,
                blockEntity.getWorld(),
                null,
                (int) blockEntity.getPos().asLong());
    }

    @Override
    public void render(State state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState) {
        if (!state.visible) {
            return;
        }

        matrices.push();
        if (state.direction == Direction.NORTH) {
            matrices.translate(0.5D, -0.05D, 0.38D);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180F));
        } else if (state.direction == Direction.EAST) {
            matrices.translate(0.62D, -0.05D, 0.5D);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90F));
        } else if (state.direction == Direction.SOUTH) {
            matrices.translate(0.5D, -0.05D, 0.62D);
        } else if (state.direction == Direction.WEST) {
            matrices.translate(0.38D, -0.05D, 0.5D);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(270F));
        }
        matrices.scale(0.5F, 0.5F, 0.5F);
        state.itemRenderState.render(matrices, queue, state.lightmapCoordinates, OverlayTexture.DEFAULT_UV, 0);
        matrices.pop();
    }

    public static class State extends BlockEntityRenderState {
        public final ItemRenderState itemRenderState = new ItemRenderState();
        public Direction direction = Direction.NORTH;
        public boolean visible;
    }
}
