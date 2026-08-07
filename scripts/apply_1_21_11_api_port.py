#!/usr/bin/env python3
from pathlib import Path
import re

ROOT = Path('src/net')


def write(path: Path, text: str):
    old = path.read_text(encoding='utf-8')
    if old != text:
        path.write_text(text, encoding='utf-8')
        print(f'updated {path}')


def replace_all_java():
    for path in ROOT.rglob('*.java'):
        s = path.read_text(encoding='utf-8')
        old = s

        # Hand interaction result API was unified in 1.21.11.
        if 'import net.minecraft.util.ItemActionResult;' in s:
            s = s.replace('import net.minecraft.util.ItemActionResult;', 'import net.minecraft.util.ActionResult;')
        s = re.sub(r'\bItemActionResult\b', 'ActionResult', s)
        s = s.replace('ActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION', 'ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION')
        s = s.replace('return ActionResult.success(world.isClient());',
                      'return world.isClient() ? ActionResult.SUCCESS : ActionResult.SUCCESS_SERVER;')

        if 'import net.minecraft.util.TypedActionResult;' in s:
            s = s.replace('import net.minecraft.util.TypedActionResult;', 'import net.minecraft.util.ActionResult;')
        s = s.replace('CallbackInfoReturnable<TypedActionResult<ItemStack>>', 'CallbackInfoReturnable<ActionResult>')
        s = s.replace('TypedActionResult<ItemStack>', 'ActionResult')
        s = s.replace('return TypedActionResult.consume(itemStack);',
                      'return ActionResult.CONSUME.withNewHandStack(itemStack);')
        s = s.replace('return TypedActionResult.consume(new ItemStack(Items.BOWL));',
                      'return ActionResult.CONSUME.withNewHandStack(new ItemStack(Items.BOWL));')
        s = s.replace('return TypedActionResult.pass(itemStack);', 'return ActionResult.PASS;')
        s = s.replace('return TypedActionResult.pass(player.getStackInHand(hand));', 'return ActionResult.PASS;')
        s = s.replace('return TypedActionResult.fail(user.getStackInHand(hand));', 'return ActionResult.FAIL;')
        s = s.replace('return TypedActionResult.success(user.getStackInHand(hand));',
                      'return ActionResult.SUCCESS_SERVER.withNewHandStack(user.getStackInHand(hand));')
        s = s.replace('return TypedActionResult.success(player.getStackInHand(hand), true);',
                      'return (world.isClient() ? ActionResult.SUCCESS : ActionResult.SUCCESS_SERVER).withNewHandStack(player.getStackInHand(hand));')

        # Entity#getWorld was replaced by the explicit entity-world accessor.
        s = s.replace('player.getWorld()', 'player.getEntityWorld()')
        s = s.replace('playerEntity.getWorld()', 'playerEntity.getEntityWorld()')
        s = s.replace('serverPlayerEntity.getWorld()', 'serverPlayerEntity.getEntityWorld()')
        s = s.replace('return ActionResult.success(player.getEntityWorld().isClient());',
                      'return player.getEntityWorld().isClient() ? ActionResult.SUCCESS : ActionResult.SUCCESS_SERVER;')

        # DirectionProperty was folded into EnumProperty<Direction>.
        if 'import net.minecraft.state.property.DirectionProperty;' in s:
            s = s.replace('import net.minecraft.state.property.DirectionProperty;',
                          'import net.minecraft.state.property.EnumProperty;')
            s = s.replace('DirectionProperty FACING', 'EnumProperty<Direction> FACING')

        # GameRules moved into world.rule.
        s = s.replace('import net.minecraft.world.GameRules;', 'import net.minecraft.world.rule.GameRules;')

        if s != old:
            path.write_text(s, encoding='utf-8')
            print(f'updated {path}')


def fix_block_entities():
    path = ROOT / 'dehydration/block/entity/CampfireCauldronEntity.java'
    s = path.read_text(encoding='utf-8')
    s = s.replace('import net.minecraft.nbt.NbtCompound;\nimport net.minecraft.registry.RegistryWrapper.WrapperLookup;',
                  'import net.minecraft.storage.ReadView;\nimport net.minecraft.storage.WriteView;')
    s = s.replace('public void readData(NbtCompound tag, WrapperLookup registryLookup) {\n        super.readData(tag, registryLookup);\n        this.isBoiled = tag.getBoolean("Boiled");\n    }',
                  'protected void readData(ReadView view) {\n        super.readData(view);\n        this.isBoiled = view.getBoolean("Boiled", false);\n    }')
    s = s.replace('public void writeData(NbtCompound tag, WrapperLookup registryLookup) {\n        super.writeData(tag, registryLookup);\n        tag.putBoolean("Boiled", isBoiled);\n    }',
                  'protected void writeData(WriteView view) {\n        super.writeData(view);\n        view.putBoolean("Boiled", isBoiled);\n    }')
    write(path, s)

    path = ROOT / 'dehydration/block/entity/BambooPumpEntity.java'
    s = path.read_text(encoding='utf-8')
    s = s.replace('import net.minecraft.storage.ReadView;\nimport net.minecraft.storage.WriteView;', 'import net.minecraft.storage.ReadView;\nimport net.minecraft.storage.WriteView;')
    s = s.replace('import net.minecraft.nbt.NbtCompound;\n', 'import net.minecraft.nbt.NbtCompound;\nimport net.minecraft.storage.ReadView;\nimport net.minecraft.storage.WriteView;\n')
    s = s.replace('public void readData(NbtCompound nbt, WrapperLookup registryLookup) {\n        super.readData(nbt, registryLookup);\n        this.inventory.clear();\n        Inventories.readData(nbt, inventory, registryLookup);\n    }',
                  'protected void readData(ReadView view) {\n        super.readData(view);\n        this.inventory.clear();\n        Inventories.readData(view, inventory);\n    }')
    s = s.replace('public void writeData(NbtCompound nbt, WrapperLookup registryLookup) {\n        super.writeData(nbt, registryLookup);\n        Inventories.writeData(nbt, inventory, registryLookup);\n    }',
                  'protected void writeData(WriteView view) {\n        super.writeData(view);\n        Inventories.writeData(view, inventory);\n    }')
    s = s.replace('return this.inventory.getName(0);', 'return this.inventory.get(slot);')
    write(path, s)


def fix_tooltips():
    for rel in ['dehydration/item/LeatherFlask.java', 'dehydration/item/HandbookItem.java']:
        path = ROOT / rel
        s = path.read_text(encoding='utf-8')
        if 'import net.minecraft.component.type.TooltipDisplayComponent;' not in s:
            # place alongside other net.minecraft imports
            marker = 'import net.minecraft.entity.player.PlayerEntity;'
            s = s.replace(marker, 'import net.minecraft.component.type.TooltipDisplayComponent;\n' + marker)
        if 'import java.util.function.Consumer;' not in s:
            if 'import java.util.Optional;' in s:
                s = s.replace('import java.util.Optional;', 'import java.util.Optional;\nimport java.util.function.Consumer;')
            elif 'import java.util.List;' in s:
                s = s.replace('import java.util.List;', 'import java.util.List;\nimport java.util.function.Consumer;')
        s = s.replace('public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {',
                      'public void appendTooltip(ItemStack stack, TooltipContext context, TooltipDisplayComponent displayComponent, Consumer<Text> textConsumer, TooltipType type) {')
        s = s.replace('tooltip.add(', 'textConsumer.accept(')
        s = s.replace('super.appendTooltip(stack, context, tooltip, type);',
                      'super.appendTooltip(stack, context, displayComponent, textConsumer, type);')
        write(path, s)


def fix_fluid():
    path = ROOT / 'dehydration/fluid/PurifiedWaterFluid.java'
    s = path.read_text(encoding='utf-8')
    if 'import net.minecraft.server.world.ServerWorld;' not in s:
        s = s.replace('import net.minecraft.registry.tag.FluidTags;',
                      'import net.minecraft.registry.tag.FluidTags;\nimport net.minecraft.server.world.ServerWorld;')
    s = s.replace('protected boolean isInfinite(World var1)', 'protected boolean isInfinite(ServerWorld world)')
    write(path, s)


def fix_block_entity_types():
    path = ROOT / 'dehydration/init/BlockInit.java'
    s = path.read_text(encoding='utf-8')
    if 'FabricBlockEntityTypeBuilder' not in s:
        s = s.replace('import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;',
                      'import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;\nimport net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;')
    s = s.replace('BlockEntityType.Builder.create(CampfireCauldronEntity::new, CAMPFIRE_CAULDRON_BLOCK).build(null)',
                  'FabricBlockEntityTypeBuilder.create(CampfireCauldronEntity::new, CAMPFIRE_CAULDRON_BLOCK).build()')
    s = s.replace('BlockEntityType.Builder.create(BambooPumpEntity::new, BAMBOO_PUMP_BLOCK).build(null)',
                  'FabricBlockEntityTypeBuilder.create(BambooPumpEntity::new, BAMBOO_PUMP_BLOCK).build()')
    write(path, s)


def fix_commands_and_sound():
    path = ROOT / 'dehydration/init/CommandInit.java'
    s = path.read_text(encoding='utf-8')
    s = s.replace('CommandManager.literal("thirst").requires((serverCommandSource) -> {\n                return serverCommandSource.hasPermissionLevel(3);\n            })',
                  'CommandManager.literal("thirst").requires(CommandManager.requirePermissionLevel(CommandManager.ADMINS_CHECK))')
    write(path, s)

    path = ROOT / 'dehydration/init/EventInit.java'
    s = path.read_text(encoding='utf-8')
    s = s.replace('player.playSound(SoundEvents.ENTITY_GENERIC_DRINK,',
                  'player.playSound(SoundEvents.ENTITY_GENERIC_DRINK.value(),')
    write(path, s)


def fold_honey_milk_tooltips():
    path = ROOT / 'dehydration/mixin/ItemMixin.java'
    s = path.read_text(encoding='utf-8')
    if 'import net.minecraft.item.Items;' not in s:
        s = s.replace('import net.minecraft.item.ItemStack;', 'import net.minecraft.item.ItemStack;\nimport net.minecraft.item.Items;')
    s = s.replace('int thirstQuench = 0;\n            if (stack.isIn(TagInit.HYDRATING_STEW)) {',
                  'int thirstQuench = 0;\n            int quality = 0;\n            if (stack.isOf(Items.HONEY_BOTTLE)) {\n                thirstQuench = ConfigInit.CONFIG.honey_quench;\n            } else if (stack.isOf(Items.MILK_BUCKET)) {\n                thirstQuench = ConfigInit.CONFIG.milk_thirst_quench;\n                quality = 1;\n            } else if (stack.isIn(TagInit.HYDRATING_STEW)) {')
    s = s.replace('info.setReturnValue(Optional.of(new ThirstTooltipData(0, thirstQuench)));',
                  'info.setReturnValue(Optional.of(new ThirstTooltipData(quality, thirstQuench)));')
    write(path, s)

    mixins = Path('src/main/resources/dehydration.mixins.json')
    m = mixins.read_text(encoding='utf-8')
    m = m.replace('    "MilkBucketItemMixin",\n', '')
    m = m.replace('    "HoneyBottleItemMixin",\n', '')
    write(mixins, m)
    for rel in ['dehydration/mixin/HoneyBottleItemMixin.java', 'dehydration/mixin/MilkBucketItemMixin.java']:
        p = ROOT / rel
        if p.exists():
            p.unlink()
            print(f'deleted {p}')


def main():
    replace_all_java()
    fix_block_entities()
    fix_tooltips()
    fix_fluid()
    fix_block_entity_types()
    fix_commands_and_sound()
    fold_honey_milk_tooltips()

if __name__ == '__main__':
    main()
