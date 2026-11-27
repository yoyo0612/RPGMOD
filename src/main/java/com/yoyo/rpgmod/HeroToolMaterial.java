package com.yoyo.rpgmod;

import net.minecraft.item.ToolMaterial;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;

public enum HeroToolMaterial implements ToolMaterial {
    INSTANCE;

    @Override
    public int getDurability() {
        return 2500; // ダイヤモンドは1561
    }

    @Override
    public float getMiningSpeedMultiplier() {
        return 10.0f;
    }

    @Override
    public float getAttackDamage() {
        return 4.0f;
    }

    @Override
    public int getMiningLevel() {
        return 3; // ダイヤモンドと同じ
    }

    @Override
    public int getEnchantability() {
        return 20;
    }

    @Override
    public Ingredient getRepairIngredient() {
        // 修復素材はダイヤモンド
        return Ingredient.ofItems(Items.DIAMOND);
    }
}
