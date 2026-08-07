package net.dehydration.mixin;

import java.util.Optional;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.dehydration.init.ItemInit;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.potion.Potion;
import net.minecraft.registry.entry.RegistryEntry;

@Mixin(PotionContentsComponent.class)
public class PotionContentsComponentMixin {

    @Shadow
    public Optional<RegistryEntry<Potion>> potion() {
        return null;
    };

    @SuppressWarnings("deprecation")
    @Inject(method = "getColor()I", at = @At("HEAD"), cancellable = true)
    private void getColorMixin(CallbackInfoReturnable<Integer> info) {
        if (this.potion() == null || !this.potion().isPresent() || this.potion().get() == null) {
            return;
        }
        if (this.potion().get().matches(ItemInit.PURIFIED_WATER)) {
            info.setReturnValue(3708358);
        }
    }

}
