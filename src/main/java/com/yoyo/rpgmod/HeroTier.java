package com.yoyo.rpgmod;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;

public enum HeroTier implements Tier {
    INSTANCE;

    @Override
    public int getUses() {
        return 2500; // 耐久値（ダイヤモンドは1561）
    }

    @Override
    public float getSpeed() {
        return 10.0f;
    }

    @Override
    public float getAttackDamageBonus() {
        return 4.0f;
    }

    @Override
    public net.minecraft.tags.TagKey<net.minecraft.world.level.block.Block> getIncorrectBlocksForDrops() {
        return BlockTags.INCORRECT_FOR_DIAMOND_TOOL; // ダイヤモンドと同じ採掘レベル
    }

    @Override
    public int getEnchantmentValue() {
        return 20;
    }

    @Override
    public Ingredient getRepairIngredient() {
        // 修復素材はダイヤモンド
        return Ingredient.of(Items.DIAMOND);
    }
}
