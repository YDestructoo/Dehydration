package net.dehydration.block.render;

import net.dehydration.block.entity.BambooPumpEntity;
import net.dehydration.init.BlockInit;
import net.dehydration.init.ItemInit;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.Items;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;

@Environment(EnvType.CLIENT)
public class BambooPumpRenderer implements BlockEntityRenderer<BambooPumpEntity> {

    public BambooPumpRenderer(BlockEntityRendererFactory.Context ctx) {
    }

    @Override
    public void render(BambooPumpEntity blockEntity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        if (blockEntity.getWorld() != null && !blockEntity.isEmpty() && !blockEntity.getStack(0).isOf(Items.field_8550) && !blockEntity.getStack(0).isOf(ItemInit.PURIFIED_BUCKET)) {
            BlockState blockState = blockEntity.getWorld().getBlockState(blockEntity.getPos());
            if (!blockState.isAir() && blockState.isOf(BlockInit.BAMBOO_PUMP_BLOCK)) {
                Direction blockDirection = blockState.get(HorizontalFacingBlock.FACING);
                matrices.push();
                if (blockDirection == Direction.field_11043) {
                    matrices.translate(0.5D, -0.05D, 0.38D);
                    matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((180F)));
                } else if (blockDirection == Direction.field_11034) {
                    matrices.translate(0.62D, -0.05D, 0.5D);
                    matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((90F)));
                } else if (blockDirection == Direction.field_11035)
                    matrices.translate(0.5D, -0.05D, 0.62D);
                else if (blockDirection == Direction.field_11039) {
                    matrices.translate(0.38D, -0.05D, 0.5D);
                    matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((270F)));
                }
                matrices.scale(0.5F, 0.5F, 0.5F);

                MinecraftClient.getInstance().getItemRenderer().renderItem(blockEntity.getStack(0), ItemDisplayContext.field_4316, light, overlay, matrices, vertexConsumers, blockEntity.getWorld(),
                        (int) blockEntity.getPos().asLong());
                matrices.pop();
            }
        }
    }

}
