package net.dehydration.mixin;

import com.mojang.authlib.GameProfile;
import net.dehydration.access.ServerPlayerAccess;
import net.dehydration.access.ThirstManagerAccess;
import net.dehydration.network.ThirstServerPacket;
import net.dehydration.thirst.ThirstManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity implements ServerPlayerAccess {

    private ThirstManager thirstManager = ((ThirstManagerAccess) this).getThirstManager();
    private int syncedThirstLevel = -99999999;
    public int compatSync = 0;

    public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, gameProfile);
    }

    @Inject(method = "playerTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;tick()V", shift = Shift.AFTER))
    public void playerTickMixin(CallbackInfo info) {
        if (this.syncedThirstLevel != this.thirstManager.getThirstLevel() && this.thirstManager.hasThirst()) {
            ThirstServerPacket.writeS2CThirstUpdatePacket((ServerPlayerEntity) (Object) this);
            this.syncedThirstLevel = thirstManager.getThirstLevel();
        }
        if (compatSync > 0) {
            compatSync--;
            if (compatSync == 1) {
                ThirstServerPacket.writeS2CExcludedSyncPacket((ServerPlayerEntity) (Object) this, thirstManager.hasThirst());
            }
        }
    }

    @Inject(method = "teleport(Lnet/minecraft/server/world/ServerWorld;DDDLjava/util/Set;FFZ)Z", at = @At("TAIL"))
    private void teleportMixin(ServerWorld targetWorld, double x, double y, double z, Set<PositionFlag> flags,
            float yaw, float pitch, boolean resetCamera, CallbackInfoReturnable<Boolean> info) {
        this.syncedThirstLevel = -1;
    }

    @Override
    public void compatSync() {
        this.compatSync = 5;
    }

    @Override
    public void setSyncedThirstLevel(int syncedThirstLevel) {
        this.syncedThirstLevel = syncedThirstLevel;
    }
}
