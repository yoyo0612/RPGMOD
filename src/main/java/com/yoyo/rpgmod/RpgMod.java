package com.yoyo.rpgmod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.SwordItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class RpgMod implements ModInitializer {
    public static final String MOD_ID = "rpgmod";

    // 勇者の剣: ダイヤモンドの剣より強い攻撃力
    public static final Item HERO_SWORD = new SwordItem(
            HeroToolMaterial.INSTANCE,
            6,      // 攻撃ダメージ（素手ベース）
            -2.4f,  // 攻撃速度
            new Item.Settings()
    );

    @Override
    public void onInitialize() {
        Registry.register(Registries.ITEM, new Identifier(MOD_ID, "hero_sword"), HERO_SWORD);

        // クリエイティブのタブ「戦闘」に追加
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT)
                .register(entries -> entries.add(HERO_SWORD));

        System.out.println("[RPG Mod] 読み込みました");
    }
}
