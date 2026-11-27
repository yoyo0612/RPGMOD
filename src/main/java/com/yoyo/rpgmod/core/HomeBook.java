package com.yoyo.rpgmod.core;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * 家（ビルダーの保護範囲）の管理（docs/ELEMENTS.md の section 5「役職」）。
 *
 * - 家はビルダーが専用コマンドで登録する。対角の2点で直方体の範囲を指定する。
 * - 1人につき家は1つ。登録し直すと置き換わる。
 * - 範囲の境界は含む。
 *
 * 誰が壊してよいかの判定（チーム）は、呼び出し側（Minecraft 側）が行う。
 */
public final class HomeBook {

    /** 保護範囲（直方体・境界を含む）。 */
    public record Box(String dimension, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {

        public boolean contains(String dim, int x, int y, int z) {
            return dimension.equals(dim)
                    && x >= minX && x <= maxX
                    && y >= minY && y <= maxY
                    && z >= minZ && z <= maxZ;
        }
    }

    private final Map<UUID, Box> homes = new LinkedHashMap<>();

    /** 家を登録する（既存の家は置き換える）。2点の順序は問わない。 */
    public void set(UUID owner, String dimension,
                    int x1, int y1, int z1, int x2, int y2, int z2) {
        homes.put(owner, new Box(dimension,
                Math.min(x1, x2), Math.min(y1, y2), Math.min(z1, z2),
                Math.max(x1, x2), Math.max(y1, y2), Math.max(z1, z2)));
    }

    /** 家を消す。消せたら true。 */
    public boolean clear(UUID owner) {
        return homes.remove(owner) != null;
    }

    public Optional<Box> homeOf(UUID owner) {
        return Optional.ofNullable(homes.get(owner));
    }

    /** 指定の位置を含む家の持ち主（複数あり得る）。 */
    public List<UUID> ownersContaining(String dimension, int x, int y, int z) {
        List<UUID> owners = new ArrayList<>();
        for (Map.Entry<UUID, Box> entry : homes.entrySet()) {
            if (entry.getValue().contains(dimension, x, y, z)) {
                owners.add(entry.getKey());
            }
        }
        return owners;
    }

    /** 保存用: すべての家（持ち主 → 範囲）。 */
    public Map<UUID, Box> all() {
        return new LinkedHashMap<>(homes);
    }
}
