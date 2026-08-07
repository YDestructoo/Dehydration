package net.dehydration.init;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.dehydration.effect.*;

public class EffectInit {

    public final static RegistryEntry<StatusEffect> THIRST = register("dehydration:thirst_effect", new ThirstEffect(StatusEffectCategory.HARMFUL, 3062757));
    public final static RegistryEntry<StatusEffect> HYDRATION = register("dehydration:hydration_effect", new HydrationEffect(StatusEffectCategory.BENEFICIAL, 3062757));

    public static void init() {
    }

    private static RegistryEntry<StatusEffect> register(String id, StatusEffect statusEffect) {
        return Registry.registerReference(Registries.STATUS_EFFECT, Identifier.of(id), statusEffect);
    }

}
