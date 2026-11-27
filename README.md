# RPG Mod (rpgmod)

Minecraft 1.21.1 向けの [NeoForge](https://neoforged.net/) MOD です。RPG要素を少しずつ追加していく予定の開発用リポジトリです。

## 現在の機能

- **勇者の剣** (`Hero's Sword`): ダイヤモンドの剣より攻撃力の高い剣。クリエイティブの「戦闘」タブから入手できます。
  - 耐久値 2500、修復素材はダイヤモンド

## 動作環境

- Minecraft 1.21.1
- NeoForge 21.1.x 以上
- Java 21

## ビルド方法

```sh
./gradlew build
```

生成された MOD は `build/libs/` に出力されます。

## 開発環境での起動

```sh
./gradlew runClient
```

## 導入方法

1. `build/libs/` の jar ファイルを `.minecraft/mods/` に入れます。

## ライセンス

未定です。
