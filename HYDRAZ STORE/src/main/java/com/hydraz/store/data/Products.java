package com.hydraz.store.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Products {

    private static final Map<String, List<Product>> ALL_PRODUCTS = new HashMap<>();

    static {
        // Lifesteal Ranks (35% OFF)
        List<Product> lifestealRanks = new ArrayList<>();
        lifestealRanks.add(new Product("ls_rank_vip", "VIP Rank + VIP Kit", 38, 59, "img/rank_vip.png", "lifesteal-ranks", "lp user %player% parent set vip"));
        lifestealRanks.add(new Product("ls_rank_titan", "TITAN Rank + TITAN Kit", 97, 149, "img/rank_titan.png", "lifesteal-ranks", "lp user %player% parent set titan"));
        lifestealRanks.add(new Product("ls_rank_supreme", "Supreme Rank + Supreme Kit", 194, 299, "img/rank_supreme.png", "lifesteal-ranks", "lp user %player% parent set supreme"));
        lifestealRanks.add(new Product("ls_rank_hydraz_king", "Hydraz+ Rank + Hydraz+ Kit", 227, 349, "img/rank_hydraz_king.png", "lifesteal-ranks", "lp user %player% parent set hydraz+"));
        ALL_PRODUCTS.put("lifesteal-ranks", lifestealRanks);

        // Lifesteal Keys & Crates (35% OFF)
        List<Product> lifestealKeys = new ArrayList<>();
        lifestealKeys.add(new Product("ls_key_common", "Common Key (Common Crate)", 7, 10, "img/key_common.png", "lifesteal-keys", "crate key give %player% common 1"));
        lifestealKeys.add(new Product("ls_key_epic", "Epic Key (Epic Crate)", 13, 20, "img/key_epic.png", "lifesteal-keys", "crate key give %player% epic 1"));
        lifestealKeys.add(new Product("ls_key_spawner", "Spawner Key (Spawner Crate)", 26, 40, "img/key_spawner.png", "lifesteal-keys", "crate key give %player% spawner 1"));
        lifestealKeys.add(new Product("ls_key_rare", "Rare Key (Rare Crate)", 33, 50, "img/key_rare.png", "lifesteal-keys", "crate key give %player% rare 1"));
        lifestealKeys.add(new Product("ls_key_hydraz", "Hydraz Key (Hydraz Crate)", 42, 65, "img/key_hydraz.png", "lifesteal-keys", "crate key give %player% hydraz_key 1"));
        ALL_PRODUCTS.put("lifesteal-keys", lifestealKeys);

        // Lifesteal Coins (35% OFF)
        List<Product> lifestealCoins = new ArrayList<>();
        lifestealCoins.add(new Product("ls_coins_pkg_1", "700 Coins", 59, 90, "img/coins_pkg_1.png", "lifesteal-coins", "points give %player% 700"));
        lifestealCoins.add(new Product("ls_coins_pkg_2", "1500 Coins", 117, 180, "img/coins_pkg_2.png", "lifesteal-coins", "points give %player% 1500"));
        lifestealCoins.add(new Product("ls_coins_pkg_3", "2800 Coins", 244, 375, "img/coins_pkg_3.png", "lifesteal-coins", "points give %player% 2800"));
        lifestealCoins.add(new Product("ls_coins_pkg_4", "5560 Coins", 442, 680, "img/coins_pkg_4.png", "lifesteal-coins", "points give %player% 5560"));
        ALL_PRODUCTS.put("lifesteal-coins", lifestealCoins);

        // Practice Coins (35% OFF)
        List<Product> practiceCoins = new ArrayList<>();
        practiceCoins.add(new Product("pr_coins_pkg_1", "700 Coins", 59, 90, "img/coins_pkg_1.png", "practice-coins", "points give %player% 700"));
        practiceCoins.add(new Product("pr_coins_pkg_2", "1500 Coins", 117, 180, "img/coins_pkg_2.png", "practice-coins", "points give %player% 1500"));
        practiceCoins.add(new Product("pr_coins_pkg_3", "2800 Coins", 244, 375, "img/coins_pkg_3.png", "practice-coins", "points give %player% 2800"));
        practiceCoins.add(new Product("pr_coins_pkg_4", "5560 Coins", 442, 680, "img/coins_pkg_4.png", "practice-coins", "points give %player% 5560"));
        ALL_PRODUCTS.put("practice-coins", practiceCoins);

        // Survival Ranks (35% OFF)
        List<Product> survivalRanks = new ArrayList<>();
        survivalRanks.add(new Product("surv_rank_hydraz", "Hydraz Rank", 64, 99, "img/rank_hydraz_king.png", "survival-ranks", "lp user %player% parent set hydraz"));
        survivalRanks.add(new Product("surv_rank_hydraz_plus", "Hydraz+ Rank", 194, 299, "img/rank_hydraz_king.png", "survival-ranks", "lp user %player% parent set hydraz+"));
        ALL_PRODUCTS.put("survival-ranks", survivalRanks);

        // Survival Keys & Crates (35% OFF)
        List<Product> survivalKeys = new ArrayList<>();
        survivalKeys.add(new Product("surv_key_amethyst", "Amethyst Key (Amethyst Crate)", 32, 49, "img/blue-key.png", "survival-keys", "crate key give %player% amethyst 1"));
        survivalKeys.add(new Product("surv_key_gold", "Gold Key (Gold Crate)", 12, 19, "img/yellow-key.png", "survival-keys", "crate key give %player% gold 1"));
        survivalKeys.add(new Product("surv_key_gold_bundle", "Gold Key (3x Bundle)", 32, 49, "img/yellow-key.png", "survival-keys", "crate key give %player% gold 3"));
        survivalKeys.add(new Product("surv_key_hydraz", "Hydraz Key (Hydraz Crate)", 38, 59, "img/key_hydraz.png", "survival-keys", "crate key give %player% hydraz_key 1"));
        ALL_PRODUCTS.put("survival-keys", survivalKeys);
    }

    public static Map<String, List<Product>> getAllProducts() {
        return ALL_PRODUCTS;
    }

    public static Product getProductById(String id) {
        for (List<Product> categoryList : ALL_PRODUCTS.values()) {
            for (Product p : categoryList) {
                if (p.getId().equals(id)) {
                    return p;
                }
            }
        }
        return null;
    }
}
