package com.yoyo.rpgmod;

import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwordItem;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(RpgMod.MOD_ID)
public class RpgMod {
    public static final String MOD_ID = "rpgmod";

    // アイテム登録用のレジストリ
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);

    // 勇者の剣: ダイヤモンドの剣より強い攻撃力
    public static final DeferredHolder<Item, SwordItem> HERO_SWORD = ITEMS.registerItem(
            "hero_sword",
            props -> new SwordItem(HeroTier.INSTANCE, props.sword(HeroTier.INSTANCE, 6, -2.4f))
    );

    public RpgMod(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
        modEventBus.addListener(this::addCreative);
    }

    // クリエイティブの「戦闘」タブに追加
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            event.accept(HERO_SWORD);
        }
    }
}
