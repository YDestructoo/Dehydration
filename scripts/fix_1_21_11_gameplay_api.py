#!/usr/bin/env python3
from pathlib import Path

ROOT = Path('src/net/dehydration')


def replace(path, pairs):
    p = ROOT / path
    s = p.read_text(encoding='utf-8')
    old = s
    for a, b in pairs:
        s = s.replace(a, b)
    if s != old:
        p.write_text(s, encoding='utf-8')
        print('updated', p)

# Potion now requires an explicit base name before the effect array.
replace('init/ItemInit.java', [
    ('new Potion(new StatusEffectInstance[0])', 'new Potion("purified_water", new StatusEffectInstance[0])'),
    ('new Potion(new StatusEffectInstance(EffectInit.HYDRATION, 900))', 'new Potion("hydration", new StatusEffectInstance[]{new StatusEffectInstance(EffectInit.HYDRATION, 900)})'),
])

# Entity world accessor + registry raw values.
replace('network/ThirstClientPacket.java', [
    ('context.player().getWorld()', 'context.player().getEntityWorld()'),
    ('items.add(Registries.ITEM.getEntry(payload.templateIdentifiers().get(count)));', 'items.add(Registries.ITEM.get(payload.templateIdentifiers().get(count)));'),
])
replace('data/DataLoader.java', [
    ('items.add(Registries.ITEM.getEntry(Identifier.of(jsonObject.getAsJsonArray("items").get(u).getAsString())));', 'items.add(Registries.ITEM.get(Identifier.of(jsonObject.getAsJsonArray("items").get(u).getAsString())));'),
])

# GameRules package/name and world accessors changed.
replace('mixin/PlayerEntityMixin.java', [
    ('this.getWorld()', 'this.getEntityWorld()'),
    ('GameRules.NATURAL_REGENERATION', 'GameRules.NATURAL_HEALTH_REGENERATION'),
])

# Server-side damage now requires ServerWorld; NBT primitive getters use fallback overloads.
p = ROOT / 'thirst/ThirstManager.java'
s = p.read_text(encoding='utf-8')
if 'import net.minecraft.server.world.ServerWorld;' not in s:
    s = s.replace('import net.minecraft.registry.RegistryKeys;', 'import net.minecraft.registry.RegistryKeys;\nimport net.minecraft.server.world.ServerWorld;')
s = s.replace('player.clientDamage(createDamageSource(player), ConfigInit.CONFIG.thirst_damage);',
              'if (player.getEntityWorld() instanceof ServerWorld serverWorld) {\n                        player.damage(serverWorld, createDamageSource(player), ConfigInit.CONFIG.thirst_damage);\n                    }')
s = s.replace('if (tag.contains("ThirstLevel", 99)) {', 'if (tag.contains("ThirstLevel")) {')
s = s.replace('tag.getInt("ThirstLevel")', 'tag.getInt("ThirstLevel", 20)')
s = s.replace('tag.getInt("ThirstTickTimer")', 'tag.getInt("ThirstTickTimer", 0)')
s = s.replace('tag.getFloat("ThirstExhaustionLevel")', 'tag.getFloat("ThirstExhaustionLevel", 0.0F)')
s = s.replace('tag.getBoolean("HasThirst")', 'tag.getBoolean("HasThirst", true)')
p.write_text(s, encoding='utf-8')
print('updated', p)

# Remaining unified ActionResult sites.
replace('mixin/compat/PuddleBlockMixin.java', [
    ('info.setReturnValue(ActionResult.success(world.isClient()));', 'info.setReturnValue(world.isClient() ? ActionResult.SUCCESS : ActionResult.SUCCESS_SERVER);'),
])
replace('mixin/PotionItemMixin.java', [
    ('info.setReturnValue(TypedActionResult.success(new ItemStack(Items.GLASS_BOTTLE), world.isClient()));', 'info.setReturnValue((world.isClient() ? ActionResult.SUCCESS : ActionResult.SUCCESS_SERVER).withNewHandStack(new ItemStack(Items.GLASS_BOTTLE)));'),
])

# Mixin superclass constructor changed.
replace('mixin/ServerPlayerEntityMixin.java', [
    ('super(world, pos, yaw, gameProfile);', 'super(world, gameProfile);'),
])

# Honey/milk item classes were removed; use vanilla item identity.
replace('util/ThirstHelper.java', [
    ('stack.getItem() instanceof MilkBucketItem', 'stack.isOf(Items.MILK_BUCKET)'),
    ('stack.getItem() instanceof HoneyBottleItem', 'stack.isOf(Items.HONEY_BOTTLE)'),
])

# World client flag is accessor-only.
replace('block/CampfireCauldronBlock.java', [('world.isClient ?', 'world.isClient() ?')])

# StatusEffect update callbacks now receive ServerWorld.
for rel in ['effect/HydrationEffect.java', 'effect/ThirstEffect.java']:
    p = ROOT / rel
    s = p.read_text(encoding='utf-8')
    if 'import net.minecraft.server.world.ServerWorld;' not in s:
        anchor = 'import net.minecraft.entity.effect.StatusEffectCategory;'
        s = s.replace(anchor, anchor + '\nimport net.minecraft.server.world.ServerWorld;')
    s = s.replace('public boolean applyUpdateEffect(LivingEntity entity, int amplifier)', 'public boolean applyUpdateEffect(ServerWorld world, LivingEntity entity, int amplifier)')
    s = s.replace('super.applyUpdateEffect(entity, amplifier)', 'super.applyUpdateEffect(world, entity, amplifier)')
    s = s.replace('!entity.getWorld().isClient()', '!world.isClient()')
    p.write_text(s, encoding='utf-8')
    print('updated', p)
