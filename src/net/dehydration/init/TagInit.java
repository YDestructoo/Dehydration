package net.dehydration.init;

import net.dehydration.DehydrationMain;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;

public class TagInit {

    public static final TagKey<Item> HYDRATING_FOOD = TagKey.of(RegistryKeys.field_41197, DehydrationMain.identifierOf("hydrating_food"));
    public static final TagKey<Item> STRONGER_HYDRATING_FOOD = TagKey.of(RegistryKeys.field_41197, DehydrationMain.identifierOf("stronger_hydrating_food"));
    public static final TagKey<Item> HYDRATING_STEW = TagKey.of(RegistryKeys.field_41197, DehydrationMain.identifierOf("hydrating_stew"));
    public static final TagKey<Item> STRONGER_HYDRATING_STEW = TagKey.of(RegistryKeys.field_41197, DehydrationMain.identifierOf("stronger_hydrating_stew"));
    public static final TagKey<Item> HYDRATING_DRINKS = TagKey.of(RegistryKeys.field_41197, DehydrationMain.identifierOf("hydrating_drinks"));
    public static final TagKey<Item> STRONGER_HYDRATING_DRINKS = TagKey.of(RegistryKeys.field_41197, DehydrationMain.identifierOf("stronger_hydrating_drinks"));

    public static final TagKey<Fluid> PURIFIED_WATER = TagKey.of(RegistryKeys.field_41270, DehydrationMain.identifierOf("purified_water"));

    public static void init() {
    }

}
