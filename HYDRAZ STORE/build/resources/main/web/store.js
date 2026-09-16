// Hydraz Store - Core Store Logic & Product Engine

const DEFAULT_PRODUCTS = {
    'lifesteal-ranks': [
        {
            id: 'ls_rank_vip',
            name: 'VIP Rank',
            subtitle: 'VIP Rank + VIP Kit (Permanent)',
            price: 38,
            originalPrice: 59,
            image: 'img/rank_vip.png',
            category: 'lifesteal-ranks',
            badge: 'POPULAR',
            badgeColor: '#10B981',
            shortDesc: 'Diamond Gear + Full VIP Kit + Chat Prefix & Homes',
            highlights: ['Full Diamond Armor & Tools', '2× God Apples & 32× Gapples', '1× /sethome & 3× /ah Listings'],
            perks: [
                'VIP Prefix in Chat and Tab',
                'Access to 1 /sethome location',
                'Access to 3 /ah auction house listings',
                'Access to /team create & join',
                'Access to VIP Kit (Permanent)',
                'Priority server queue joining'
            ],
            kitItems: [
                { name: 'Diamond Sword', desc: 'Sharpness & Unbreaking Diamond Blade', icon: '⚔️' },
                { name: 'Diamond Axe', desc: 'Battle & Utility Woodcutting Axe', icon: '🪓' },
                { name: 'Diamond Pickaxe', desc: 'Efficiency Diamond Pickaxe', icon: '⛏️' },
                { name: 'Diamond Shovel & Hoe', desc: 'Complete Diamond Toolset', icon: '🧤' },
                { name: 'Full Diamond Armor', desc: 'Protection Helmet, Chestplate, Leggings, Boots', icon: '🛡️' },
                { name: 'Trident, Bow & Crossbow', desc: 'Ranged Weapon Armory with Arrows', icon: '🔱' },
                { name: 'VIP Shield', desc: 'Reinforced Custom Shield', icon: '🛡️' },
                { name: '2× Enchanted Golden Apples', desc: 'Tier III God Apples for clutches', icon: '🍏' },
                { name: '32× Golden Apples', desc: 'Standard PvP Golden Apples', icon: '🍎' },
                { name: '64× Experience Bottles', desc: 'Instant enchanting XP boost', icon: '🧪' },
                { name: 'Totems of Undying', desc: 'Second chance life preservation', icon: '🌟' },
                { name: '64× Golden Carrots', desc: 'Max saturation combat food', icon: '🥕' }
            ]
        },
        {
            id: 'ls_rank_titan',
            name: 'TITAN Rank',
            subtitle: 'TITAN Rank + TITAN Kit (Permanent)',
            price: 97,
            originalPrice: 149,
            image: 'img/rank_titan.png',
            category: 'lifesteal-ranks',
            badge: 'BEST VALUE',
            badgeColor: '#3B82F6',
            shortDesc: 'Full Netherite Gear + TITAN Kit + Portable /craft',
            highlights: ['Full Netherite Armor & Weapons', 'God Apples, Totems & Trident', '2× /sethomes & 5× /ah Listings'],
            perks: [
                'TITAN Prefix in Chat and Tab',
                'Access to 2 /sethome locations',
                'Access to 5 /ah auction listings',
                'Access to 5 /team members limit',
                'Access to TITAN Kit (Permanent)',
                'Access to /craft anywhere'
            ],
            kitItems: [
                { name: 'TITAN Netherite Sword', desc: 'High Sharpness Netherite Blade', icon: '⚔️' },
                { name: 'TITAN Netherite Axe', desc: 'High-damage combat battleaxe', icon: '🪓' },
                { name: 'TITAN Netherite Pickaxe', desc: 'Max Efficiency Netherite Pickaxe', icon: '⛏️' },
                { name: 'TITAN Netherite Shovel & Hoe', desc: 'Durable Netherite excavation tools', icon: '🧤' },
                { name: 'Full TITAN Netherite Armor', desc: 'Reinforced Netherite Helmet, Chestplate, Leggings, Boots', icon: '🛡️' },
                { name: 'Trident, Bow & Crossbow', desc: 'Enchanted combat ranged arsenal', icon: '🔱' },
                { name: 'TITAN Custom Shield', desc: 'Unbreaking & Mending Shield', icon: '🛡️' },
                { name: 'Enchanted Golden Apples', desc: 'God Apples for high-stakes fights', icon: '🍏' },
                { name: 'Golden Apples', desc: 'Combat sustenance stack', icon: '🍎' },
                { name: 'Totems of Undying', desc: 'Emergency revival totems', icon: '🌟' },
                { name: 'XP Bottles & Golden Carrots', desc: 'Full combat consumables stack', icon: '🧪' }
            ]
        },
        {
            id: 'ls_rank_supreme',
            name: 'SUPREME Rank',
            subtitle: 'Supreme Rank + Supreme Kit (Permanent)',
            price: 194,
            originalPrice: 299,
            image: 'img/rank_supreme.png',
            category: 'lifesteal-ranks',
            badge: 'PREMIUM',
            badgeColor: '#EC4899',
            shortDesc: 'Custom Enchanted Netherite Gear + Fly in Spawn',
            highlights: ['Supreme Netherite Equipment', 'Custom Enchanted Weapons', '3× /sethomes & 8× /ah Listings'],
            perks: [
                'Supreme Prefix in Chat and Tab',
                'Access to 3 /sethome locations',
                'Access to 8 /ah auction listings',
                'Access to 7 /team members limit',
                'Access to Supreme Kit (Permanent)',
                'Access to /fly inside Server Spawn',
                'Custom join message announcement'
            ],
            kitItems: [
                { name: 'Supreme Netherite Sword', desc: 'Custom Enchanted Supreme Slasher', icon: '⚔️' },
                { name: 'Supreme Netherite Axe', desc: 'Heavy shield-breaker axe with enchants', icon: '🪓' },
                { name: 'Supreme Netherite Pickaxe', desc: 'Silk Touch & Fortune Netherite Pick', icon: '⛏️' },
                { name: 'Supreme Shovel & Hoe', desc: 'Reinforced Netherite toolset', icon: '🧤' },
                { name: 'Full Supreme Netherite Armor', desc: 'Custom high-level protection set', icon: '🛡️' },
                { name: 'Supreme Trident & Bow', desc: 'Custom enchanted high-velocity weapons', icon: '🔱' },
                { name: 'Supreme Shield & Crossbow', desc: 'Rapid reload & fortified guard', icon: '🛡️' },
                { name: 'Enchanted Golden Apples', desc: 'High-tier god apple bundle', icon: '🍏' },
                { name: 'Golden Apples & Totems', desc: 'Multiple Totems of Undying', icon: '🌟' }
            ]
        },
        {
            id: 'ls_rank_hydraz_king',
            name: 'HYDRAZ+ Rank',
            subtitle: 'Hydraz+ Rank + Hydraz+ Kit (Endgame)',
            price: 227,
            originalPrice: 349,
            image: 'img/rank_hydraz_king.png',
            category: 'lifesteal-ranks',
            badge: 'ENDGAME',
            badgeColor: '#F59E0B',
            shortDesc: 'The Ultimate Rank: Custom Models, /enderchest & Max Perks',
            highlights: ['Hydraz Mace & God Weapons', 'Full Access to /enderchest', '3× /sethome, 10× /ah & 9× /team'],
            perks: [
                'Hydraz KING Animated Prefix',
                'Access to 3 /sethome homes',
                'Access to 10 /ah auction listings',
                'Access to 9 /team members',
                'Access to Hydraz /kit (Highest Tier)',
                'Access to /enderchest anywhere',
                'Access to /craft & /fly in spawn',
                'Exclusive Discord King role & VIP lounge'
            ],
            kitItems: [
                { name: 'Hydraz Helmet', desc: 'Custom Model Data + God Protection IV', icon: '🪖' },
                { name: 'Hydraz Chestplate', desc: 'Max Defense Armor Core', icon: '🦺' },
                { name: 'Hydraz Leggings', desc: 'Fortified High-Tier Leggings', icon: '👖' },
                { name: 'Hydraz Boots', desc: 'Soul Speed & Feather Falling IV', icon: '🥾' },
                { name: 'Hydraz Sword', desc: 'Ultimate Sharpness V + Fire Aspect II', icon: '⚔️' },
                { name: 'Hydraz Axe', desc: 'Shield Breaker & High DPS Axe', icon: '🪓' },
                { name: 'Hydraz Mace', desc: 'Custom Wind Burst Smash Mace', icon: '🔨' },
                { name: 'Hydraz Shield', desc: 'Unbreakable Custom Fortress Shield', icon: '🛡️' },
                { name: 'Hydraz Pickaxe, Shovel & Hoe', desc: 'Maxed out Netherite Toolset', icon: '⛏️' },
                { name: 'Bow, Trident & Crossbow', desc: 'Full custom enchanted ranged set', icon: '🔱' },
                { name: '16× Enchanted Golden Apples', desc: 'Full stack of god apples', icon: '🍏' },
                { name: '64× Golden Apples', desc: 'Full stack of battle apples', icon: '🍎' },
                { name: 'Totems of Undying & Ender Pearls', desc: 'Emergency survivability package', icon: '🌟' }
            ]
        }
    ],
    'lifesteal-keys': [
        {
            id: 'ls_key_common',
            name: 'Common Key',
            subtitle: 'Common Key ×1 → Common Crate',
            crateName: 'Common Crate',
            price: 7,
            originalPrice: 10,
            image: 'img/key_common.png',
            category: 'lifesteal-keys',
            badge: 'TIER I',
            badgeColor: '#78716C',
            shortDesc: 'Protection II Diamond Armor, Sharp III Sword, Gapples & Resources',
            highlights: ['Protection II Diamond Armor', 'Sharpness III Sword & Axe', 'Golden Apples & Shulker Box'],
            crateRewards: [
                { name: 'Protection II Diamond Helmet', qty: '1×', icon: '🪖', rarity: 'Common' },
                { name: 'Protection II Diamond Chestplate', qty: '1×', icon: '🦺', rarity: 'Common' },
                { name: 'Protection II Diamond Leggings', qty: '1×', icon: '👖', rarity: 'Common' },
                { name: 'Protection II Diamond Boots', qty: '1×', icon: '🥾', rarity: 'Common' },
                { name: 'Sharpness III Diamond Sword', qty: '1×', icon: '⚔️', rarity: 'Common' },
                { name: 'Efficiency III + Sharpness III Axe', qty: '1×', icon: '🪓', rarity: 'Common' },
                { name: 'Efficiency III Diamond Pickaxe', qty: '1×', icon: '⛏️', rarity: 'Common' },
                { name: 'Efficiency III Diamond Shovel', qty: '1×', icon: '🧤', rarity: 'Common' },
                { name: 'Golden Apple Stack', qty: '16× or 32×', icon: '🍎', rarity: 'Common' },
                { name: 'Enchanted Golden Apple', qty: '3×', icon: '🍏', rarity: 'Rare' },
                { name: 'Diamond Stack', qty: '64×', icon: '💎', rarity: 'Common' },
                { name: 'Golden Carrots', qty: '64×', icon: '🥕', rarity: 'Common' },
                { name: 'Totem of Undying', qty: '1×', icon: '🌟', rarity: 'Rare' },
                { name: 'Strong Strength Splash Potion', qty: '1×', icon: '🧪', rarity: 'Common' },
                { name: 'Orange Shulker Box', qty: '1×', icon: '📦', rarity: 'Rare' },
                { name: 'Unbreaking II Shield', qty: '1×', icon: '🛡️', rarity: 'Common' }
            ]
        },
        {
            id: 'ls_key_epic',
            name: 'Epic Key',
            subtitle: 'Epic Key ×1 → Epic Crate',
            crateName: 'Epic Crate',
            price: 13,
            originalPrice: 20,
            image: 'img/key_epic.png',
            category: 'lifesteal-keys',
            badge: 'TIER II',
            badgeColor: '#F97316',
            shortDesc: 'Full Epic Armor, Epic Sword & Axe, 8x God Apples & Blocks',
            highlights: ['Full Epic Gear Set', '8× God Apples & 64× Gapples', 'Villager Egg & Netherite Ingots'],
            crateRewards: [
                { name: 'Epic Armor Set (Helmet, Chest, Legs, Boots)', qty: 'Full Set', icon: '🛡️', rarity: 'Epic' },
                { name: 'Epic Sword, Axe & Pickaxe', qty: '1× each', icon: '⚔️', rarity: 'Epic' },
                { name: 'Epic Shield', qty: '1×', icon: '🛡️', rarity: 'Epic' },
                { name: 'Totem of Undying', qty: '1×', icon: '🌟', rarity: 'Epic' },
                { name: 'Enchanted Golden Apples', qty: '8×', icon: '🍏', rarity: 'Epic' },
                { name: 'Golden Apples', qty: '64×', icon: '🍎', rarity: 'Common' },
                { name: 'Netherite Ingot', qty: '2×', icon: '🪙', rarity: 'Rare' },
                { name: 'Diamond Blocks', qty: '16×', icon: '💎', rarity: 'Rare' },
                { name: 'Iron Blocks', qty: '32×', icon: '🧱', rarity: 'Common' },
                { name: 'Ender Pearls', qty: '16×', icon: '🔮', rarity: 'Common' },
                { name: 'Cobwebs', qty: '32×', icon: '🕸️', rarity: 'Common' },
                { name: 'Villager Spawn Egg', qty: '1×', icon: '🥚', rarity: 'Epic' }
            ]
        },
        {
            id: 'ls_key_spawner',
            name: 'Spawner Key',
            subtitle: 'Spawner Key ×1 → Spawner Crate',
            crateName: 'Spawner Crate',
            price: 26,
            originalPrice: 40,
            image: 'img/key_spawner.png',
            category: 'lifesteal-keys',
            badge: 'SPAWNER',
            badgeColor: '#EAB308',
            shortDesc: 'Guaranteed Mob Spawner: Iron Golem, Blaze, Creeper, Enderman',
            highlights: ['12 Different Spawner Drops', 'High Chance Iron Golem Spawner', 'Blaze & Creeper Spawners'],
            crateRewards: [
                { name: "Iron Golem's Spawner", qty: '1×', icon: '🤖', rarity: 'Legendary' },
                { name: "Blaze's Spawner", qty: '1×', icon: '🔥', rarity: 'Epic' },
                { name: "Creeper's Spawner", qty: '1×', icon: '💣', rarity: 'Epic' },
                { name: "Enderman's Spawner", qty: '1×', icon: '👁️', rarity: 'Epic' },
                { name: "Skeleton's Spawner", qty: '1×', icon: '🏹', rarity: 'Rare' },
                { name: "Zombie's Spawner", qty: '1×', icon: '🧟', rarity: 'Rare' },
                { name: "Spider's Spawner", qty: '1×', icon: '🕷️', rarity: 'Rare' },
                { name: "Cave Spider's Spawner", qty: '1×', icon: '🕸️', rarity: 'Rare' },
                { name: "Phantom's Spawner", qty: '1×', icon: '🦇', rarity: 'Rare' },
                { name: "Allay's Spawner", qty: '1×', icon: '🧚', rarity: 'Rare' },
                { name: "Cow's Spawner", qty: '1×', icon: '🐄', rarity: 'Common' },
                { name: "Goat's Spawner", qty: '1×', icon: '🐐', rarity: 'Common' }
            ]
        },
        {
            id: 'ls_key_rare',
            name: 'Rare Key',
            subtitle: 'Rare Key ×1 → Rare Crate',
            crateName: 'Rare Crate',
            price: 33,
            originalPrice: 50,
            image: 'img/key_rare.png',
            category: 'lifesteal-keys',
            badge: 'TIER III',
            badgeColor: '#38BDF8',
            shortDesc: 'Rare Enchanted Armor & Weapons, Spawners & Netherite Blocks',
            highlights: ['Rare Armor & Weapons', 'Netherite & Diamond Blocks', 'Mob Spawners Included'],
            crateRewards: [
                { name: 'Rare Armor Set (Helmet, Chest, Legs, Boots)', qty: 'Full Set', icon: '🛡️', rarity: 'Rare' },
                { name: 'Rare Sword, Axe & Pickaxe', qty: '1× each', icon: '⚔️', rarity: 'Rare' },
                { name: 'Iron Golem Spawner', qty: '1×', icon: '🤖', rarity: 'Legendary' },
                { name: 'Skeleton / Zombie / Spider Spawner', qty: '1×', icon: '📦', rarity: 'Epic' },
                { name: 'Netherite Block', qty: '1×', icon: '🪙', rarity: 'Legendary' },
                { name: 'Diamond Block', qty: '4×', icon: '💎', rarity: 'Rare' },
                { name: 'Iron Block', qty: '16×', icon: '🧱', rarity: 'Common' },
                { name: 'Totem of Undying', qty: '1×', icon: '🌟', rarity: 'Rare' },
                { name: 'Enchanted Golden Apple', qty: '2×', icon: '🍏', rarity: 'Rare' },
                { name: 'Golden Apple & XP Bottles', qty: 'Stack', icon: '🍎', rarity: 'Common' }
            ]
        },
        {
            id: 'ls_key_hydraz',
            name: 'Hydraz Key',
            subtitle: 'Hydraz Key ×1 → Hydraz Crate (Highest Tier)',
            crateName: 'Hydraz Crate',
            price: 42,
            originalPrice: 65,
            image: 'img/key_hydraz.png',
            category: 'lifesteal-keys',
            badge: 'HIGHEST TIER',
            badgeColor: '#EF4444',
            shortDesc: 'God Tier Hydraz Mace, 16x God Apples, 8x Netherite Blocks & 4x Villager Eggs',
            highlights: ['Hydraz Armor, Sword & Mace', '16× God Apples & 8× Netherite Blocks', '4× Villager Spawn Eggs'],
            crateRewards: [
                { name: 'Hydraz Armor Set (Helmet, Chest, Legs, Boots)', qty: 'God Set', icon: '🛡️', rarity: 'Mythic' },
                { name: 'Hydraz Sword & Hydraz Mace', qty: '1× each', icon: '🔨', rarity: 'Mythic' },
                { name: 'Hydraz Axe, Pickaxe & Shovel', qty: '1× each', icon: '🪓', rarity: 'Mythic' },
                { name: 'Hydraz Custom Shield', qty: '1×', icon: '🛡️', rarity: 'Mythic' },
                { name: 'Enchanted Golden Apples', qty: '16×', icon: '🍏', rarity: 'Mythic' },
                { name: 'Netherite Blocks', qty: '8×', icon: '🪙', rarity: 'Mythic' },
                { name: 'Villager Spawn Eggs', qty: '4×', icon: '🥚', rarity: 'Legendary' },
                { name: 'Breeze Rods', qty: '64×', icon: '💨', rarity: 'Epic' },
                { name: 'Experience Bottles', qty: '64×', icon: '🧪', rarity: 'Rare' },
                { name: 'Totem of Undying', qty: '1×', icon: '🌟', rarity: 'Legendary' },
                { name: 'Golden Apples & Ender Pearls', qty: '64× / 16×', icon: '🍎', rarity: 'Epic' }
            ]
        }
    ],
    'lifesteal-coins': [
        {
            id: 'ls_coins_pkg_1',
            name: '700 Coins',
            subtitle: 'Coins Package #1',
            price: 59,
            originalPrice: 90,
            image: 'img/coins_pkg_1.png',
            category: 'lifesteal-coins',
            badge: 'BASIC',
            badgeColor: '#A855F7',
            shortDesc: 'Instant 700 Coins deposited directly to your in-game balance.',
            highlights: ['700 In-Game Coins', 'Instant Automatic Delivery', 'Usable in all Lifesteal markets']
        },
        {
            id: 'ls_coins_pkg_2',
            name: '1,500 Coins',
            subtitle: 'Coins Package #2',
            price: 117,
            originalPrice: 180,
            image: 'img/coins_pkg_2.png',
            category: 'lifesteal-coins',
            badge: 'POPULAR',
            badgeColor: '#3B82F6',
            shortDesc: 'Instant 1,500 Coins deposited directly to your in-game balance.',
            highlights: ['1,500 In-Game Coins', '+100 Bonus Coins Value', 'Instant Automatic Delivery']
        },
        {
            id: 'ls_coins_pkg_3',
            name: '2,800 Coins',
            subtitle: 'Coins Package #3',
            price: 244,
            originalPrice: 375,
            image: 'img/coins_pkg_3.png',
            category: 'lifesteal-coins',
            badge: 'BEST VALUE',
            badgeColor: '#10B981',
            shortDesc: 'Instant 2,800 Coins deposited directly to your in-game balance.',
            highlights: ['2,800 In-Game Coins', '+250 Bonus Coins Value', 'Instant Automatic Delivery']
        },
        {
            id: 'ls_coins_pkg_4',
            name: '5,560 Coins',
            subtitle: 'Coins Package #4',
            price: 442,
            originalPrice: 680,
            image: 'img/coins_pkg_4.png',
            category: 'lifesteal-coins',
            badge: 'MEGA PACK',
            badgeColor: '#F59E0B',
            shortDesc: 'Instant 5,560 Coins deposited directly to your in-game balance.',
            highlights: ['5,560 In-Game Coins', '+600 Bonus Coins Value', 'Maximum Value Tier']
        }
    ],
    'practice-coins': [
        {
            id: 'pr_coins_pkg_1',
            name: '700 Coins',
            subtitle: 'Practice Coins Package #1',
            price: 59,
            originalPrice: 90,
            image: 'img/coins_pkg_1.png',
            category: 'practice-coins',
            badge: 'BASIC',
            badgeColor: '#A855F7',
            shortDesc: 'Instant 700 Practice Coins for kit customization & cosmetics.',
            highlights: ['700 Practice Coins', 'Instant Delivery', 'Unlock custom kits & trails']
        },
        {
            id: 'pr_coins_pkg_2',
            name: '1,500 Coins',
            subtitle: 'Practice Coins Package #2',
            price: 117,
            originalPrice: 180,
            image: 'img/coins_pkg_2.png',
            category: 'practice-coins',
            badge: 'POPULAR',
            badgeColor: '#3B82F6',
            shortDesc: 'Instant 1,500 Practice Coins for kit customization & cosmetics.',
            highlights: ['1,500 Practice Coins', '+100 Bonus Value', 'Instant Delivery']
        },
        {
            id: 'pr_coins_pkg_3',
            name: '2,800 Coins',
            subtitle: 'Practice Coins Package #3',
            price: 244,
            originalPrice: 375,
            image: 'img/coins_pkg_3.png',
            category: 'practice-coins',
            badge: 'BEST VALUE',
            badgeColor: '#10B981',
            shortDesc: 'Instant 2,800 Practice Coins for kit customization & cosmetics.',
            highlights: ['2,800 Practice Coins', '+250 Bonus Value', 'Instant Delivery']
        },
        {
            id: 'pr_coins_pkg_4',
            name: '5,560 Coins',
            subtitle: 'Practice Coins Package #4',
            price: 442,
            originalPrice: 680,
            image: 'img/coins_pkg_4.png',
            category: 'practice-coins',
            badge: 'MEGA PACK',
            badgeColor: '#F59E0B',
            shortDesc: 'Instant 5,560 Practice Coins for kit customization & cosmetics.',
            highlights: ['5,560 Practice Coins', '+600 Bonus Value', 'Instant Delivery']
        }
    ],
    'survival-ranks': [
        {
            id: 'surv_rank_hydraz',
            name: 'Hydraz Rank',
            subtitle: 'Hydraz Rank + Hydraz Kit (Permanent)',
            price: 64,
            originalPrice: 99,
            image: 'img/rank_hydraz_king.png',
            category: 'survival-ranks',
            badge: 'POPULAR',
            badgeColor: '#10B981',
            shortDesc: 'Full Netherite Gear + Hydraz Kit + 3x Totems + 5x God Apples + Sethomes',
            highlights: ['Full Netherite Armor & Weapons', '3× Totems & 5× God Apples', '2× /sethomes & /craft Access'],
            perks: [
                'Hydraz Prefix in Chat & Tab',
                'Access to /kit hydraz (Weekly cooldown)',
                'Access to 2 /sethome locations',
                'Access to /craft & /feed anywhere',
                'Priority server queue joining'
            ],
            kitItems: [
                { name: 'Hydraz Netherite Sword', desc: 'Sharpness V, Fire Aspect II, Sweeping Edge III, Looting III, Unbreaking III, Mending', icon: '⚔️' },
                { name: 'Hydraz Netherite Axe', desc: 'Efficiency V, Sharpness V, Fortune III, Unbreaking III, Mending', icon: '🪓' },
                { name: 'Hydraz Netherite Pickaxe', desc: 'Efficiency V, Fortune III, Unbreaking III, Mending', icon: '⛏️' },
                { name: 'Hydraz Netherite Shovel', desc: 'Efficiency V, Fortune III, Unbreaking III, Mending', icon: '🧤' },
                { name: 'Hydraz Netherite Hoe', desc: 'Efficiency V, Fortune III, Unbreaking III, Mending', icon: '🌾' },
                { name: 'Full Hydraz Netherite Armor', desc: 'Prot IV, Fire Prot IV, Unbr III, Mending, Aqua Affinity, Swift Sneak, Depth Strider & Spire Trim', icon: '🛡️' },
                { name: 'Hydraz Trident', desc: 'Riptide III, Unbreaking III, Mending', icon: '🔱' },
                { name: 'Hydraz Bow & Crossbow', desc: 'Power V, Flame I, Punch II & Quick Charge IV, Piercing IV', icon: '🏹' },
                { name: 'Hydraz Custom Shield', desc: 'Unbreaking V, Mending Reinforced Shield', icon: '🛡️' },
                { name: '3× Totems of Undying', desc: 'Emergency second-chance life preservation totems', icon: '🌟' },
                { name: '5× Enchanted Golden Apples', desc: 'Supercharged Tier III God Apples', icon: '🍏' },
                { name: '64× Golden Apples', desc: 'Full stack combat golden apples', icon: '🍎' },
                { name: '16× Ender Pearls', desc: 'Tactical instant mobility pearls', icon: '🔮' },
                { name: '64× Cooked Steak', desc: 'Max saturation sustenance beef', icon: '🥩' }
            ]
        },
        {
            id: 'surv_rank_hydraz_plus',
            name: 'Hydraz+ Rank',
            subtitle: 'Hydraz+ Rank + Hydraz+ Kit (The Ultimate Survival Rank)',
            price: 194,
            originalPrice: 299,
            image: 'img/rank_hydraz_king.png',
            category: 'survival-ranks',
            badge: 'TOP TIER',
            badgeColor: '#F59E0B',
            shortDesc: 'Supreme God Armor + 4x Totems + 10x God Apples + Fly in Claims + Enderchest',
            highlights: ['Supreme Hydraz+ God Kit', '4× Totems & 10× God Apples', '/fly in Claims & /enderchest'],
            perks: [
                'Hydraz+ Special Red Prefix & Glow in Chat/Tab',
                'Access to /kit hydraz+ (Weekly cooldown)',
                'Access to 5 /sethome locations',
                'Access to /enderchest anywhere',
                'Access to /fly within claimed lands',
                'Access to /near & /heal',
                'Exclusive Discord Role'
            ],
            kitItems: [
                { name: 'Hydraz+ Netherite Sword', desc: 'Sharpness V, Fire Aspect II, Sweeping Edge III, Unbreaking III, Mending, Vanishing Curse (⭐⭐⭐⭐⭐)', icon: '⚔️' },
                { name: 'Hydraz+ Netherite Axe', desc: 'Efficiency V, Sharpness V, Fortune III, Unbreaking III, Mending (⭐⭐⭐⭐⭐)', icon: '🪓' },
                { name: 'Hydraz+ Netherite Pickaxe', desc: 'Efficiency V, Fortune III, Unbreaking III, Mending (⭐⭐⭐⭐⭐)', icon: '⛏️' },
                { name: 'Hydraz+ Netherite Shovel & Hoe', desc: 'Max Efficiency V, Fortune III, Unbreaking III, Mending Tools', icon: '⛏️' },
                { name: 'Full Hydraz+ God Armor', desc: 'Netherite Helmet, Chest, Legs & Boots (Prot IV, Fire Prot IV, Proj Prot II, Unbr III, Mending)', icon: '🛡️' },
                { name: 'Hydraz+ Trident', desc: 'Channeling I, Loyalty V, Unbreaking III, Mending (Supreme God Trident)', icon: '🔱' },
                { name: 'Hydraz+ Bow & Crossbow', desc: 'Power V, Flame I, Infinity I & Quick Charge IV, Piercing V Crossbow', icon: '🏹' },
                { name: 'Hydraz+ Custom Shield', desc: 'Unbreaking V, Mending Unbreakable Shield', icon: '🛡️' },
                { name: '4× Totems of Undying', desc: 'Four legendary revival totems', icon: '🌟' },
                { name: '10× Enchanted Golden Apples', desc: '10× God Apples for supreme combat dominance', icon: '🍏' },
                { name: '128× Golden Apples', desc: 'Two full stacks of PvP golden apples', icon: '🍎' },
                { name: '48× Ender Pearls', desc: 'Three stacks of emergency teleport pearls', icon: '🔮' }
            ]
        }
    ],
    'survival-keys': [
        {
            id: 'surv_key_amethyst',
            name: 'Amethyst Key',
            subtitle: 'Amethyst Key ×1 → Amethyst Crate',
            crateName: 'Amethyst Crate',
            price: 32,
            originalPrice: 49,
            image: 'img/blue-key.png',
            category: 'survival-keys',
            badge: 'TIER III',
            badgeColor: '#A855F7',
            shortDesc: 'Custom automated tools: Treechopper Axe, Chest Sell Axe, 3x3 Drill & Shard Booster',
            highlights: ['Treechopper & Chest Sell Axe', '3×3 Drill Pickaxe & Shovel', '4× Shard Booster (24h)'],
            crateRewards: [
                { name: 'Amethyst Treechopper Axe', qty: '1×', icon: '🪓', rarity: 'Legendary' },
                { name: 'Amethyst Sell Axe (Chest Auto-Sell)', qty: '1×', icon: '🪙', rarity: 'Mythic' },
                { name: 'Amethyst Drill Pickaxe (3×3)', qty: '1×', icon: '⛏️', rarity: 'Legendary' },
                { name: 'Amethyst Excavator Shovel (3×3)', qty: '1×', icon: '🧤', rarity: 'Epic' },
                { name: '4× Shard Production Booster (24h)', qty: '1×', icon: '🧪', rarity: 'Mythic' },
                { name: 'Amethyst Water Drain Bucket (27 Blocks)', qty: '1×', icon: '🪣', rarity: 'Epic' }
            ]
        },
        {
            id: 'surv_key_gold',
            name: 'Gold Key',
            subtitle: 'Gold Key ×1 → Gold Crate',
            crateName: 'Gold Crate',
            price: 12,
            originalPrice: 19,
            image: 'img/yellow-key.png',
            category: 'survival-keys',
            badge: 'SPAWNERS',
            badgeColor: '#EAB308',
            shortDesc: '100% Guaranteed Mob Spawner: Creeper, Blaze, Zombie Pigman, Skeleton or Spider',
            highlights: ['Guaranteed Mob Spawner', 'Creeper & Blaze Spawners', 'Zombie Pigman & Skeleton'],
            crateRewards: [
                { name: 'Creeper Spawner', qty: '1×', icon: '💣', rarity: 'Legendary' },
                { name: 'Blaze Spawner', qty: '1×', icon: '🔥', rarity: 'Legendary' },
                { name: 'Skeleton Spawner', qty: '1×', icon: '🏹', rarity: 'Epic' },
                { name: 'Zombie Pigman Spawner', qty: '1×', icon: '🐷', rarity: 'Epic' },
                { name: 'Spider Spawner', qty: '1×', icon: '🕷️', rarity: 'Rare' }
            ]
        },
        {
            id: 'surv_key_gold_bundle',
            name: 'Gold Key (3x Bundle)',
            subtitle: 'Gold Key ×3 → 3x Spins on Gold Crate',
            crateName: 'Gold Crate (Bundle)',
            price: 32,
            originalPrice: 49,
            image: 'img/yellow-key.png',
            category: 'survival-keys',
            badge: 'VALUE PACK',
            badgeColor: '#10B981',
            shortDesc: 'Triple Gold Keys bundle with discount for 3 guaranteed spawner spins',
            highlights: ['3× Guaranteed Spawner Spins', 'Discounted Rate', 'High Chance Creeper/Blaze'],
            crateRewards: [
                { name: 'Creeper Spawner', qty: '1×', icon: '💣', rarity: 'Legendary' },
                { name: 'Blaze Spawner', qty: '1×', icon: '🔥', rarity: 'Legendary' },
                { name: 'Skeleton Spawner', qty: '1×', icon: '🏹', rarity: 'Epic' },
                { name: 'Zombie Pigman Spawner', qty: '1×', icon: '🐷', rarity: 'Epic' },
                { name: 'Spider Spawner', qty: '1×', icon: '🕷️', rarity: 'Rare' }
            ]
        },
        {
            id: 'surv_key_hydraz',
            name: 'Hydraz Key',
            subtitle: 'Hydraz Key ×1 → Hydraz Crate (Highest Tier)',
            crateName: 'Hydraz Crate',
            price: 38,
            originalPrice: 59,
            image: 'img/key_hydraz.png',
            category: 'survival-keys',
            badge: 'HIGHEST TIER',
            badgeColor: '#EF4444',
            shortDesc: 'Snout Redstone Trimmed Netherite God Gear, 16x God Apples, End Crystals & Diamond Spear',
            highlights: ['Hydraz Netherite Armor Set', '16× Enchanted Golden Apples', '32× Crystals & Anchors', 'Diamond Spear Weapon'],
            crateRewards: [
                { name: 'Hydraz Netherite Helmet (Snout Trim)', qty: '1×', icon: '🪖', rarity: 'Mythic' },
                { name: 'Hydraz Netherite Chestplate (Snout Trim)', qty: '1×', icon: '🛡️', rarity: 'Mythic' },
                { name: 'Hydraz Netherite Leggings (Snout Trim)', qty: '1×', icon: '👖', rarity: 'Mythic' },
                { name: 'Hydraz Netherite Boots (Snout Trim)', qty: '1×', icon: '👢', rarity: 'Mythic' },
                { name: 'Hydraz Netherite Sword (Sharpness V)', qty: '1×', icon: '⚔️', rarity: 'Mythic' },
                { name: 'Hydraz Netherite Pickaxe & Axe', qty: '1× each', icon: '⛏️', rarity: 'Mythic' },
                { name: 'Enchanted Golden Apples', qty: '16×', icon: '🍏', rarity: 'Legendary' },
                { name: 'End Crystals & Respawn Anchors', qty: '32× / 32×', icon: '🔮', rarity: 'Legendary' },
                { name: 'Obsidian & Glowstone', qty: '32× / 32×', icon: '⬛', rarity: 'Epic' },
                { name: 'Golden Apples', qty: '32×', icon: '🍎', rarity: 'Epic' },
                { name: 'Diamond Spear Weapon', qty: '1×', icon: '🔱', rarity: 'Legendary' }
            ]
        }
    ]
};

document.addEventListener('DOMContentLoaded', () => {
    const storeBody = document.getElementById('store-body');
    const navLinks = document.querySelectorAll('.store-nav .nav-item');

    let products = JSON.parse(JSON.stringify(DEFAULT_PRODUCTS));
    let cart = loadCart();

    // Init session header & cart count
    if (typeof renderHeaderActions === 'function') renderHeaderActions(false);
    updateCartCount();

    const searchInput = document.getElementById('store-search-input');
    const searchClear = document.getElementById('store-search-clear');
    let searchDebounceTimer = null;
    let currentSearchTerm = '';

    function clearSearchState(triggerRender = false) {
        clearTimeout(searchDebounceTimer);
        currentSearchTerm = '';
        if (searchInput) {
            searchInput.value = '';
            searchInput.defaultValue = '';
        }
        if (searchClear) {
            searchClear.style.display = 'none';
        }
        if (triggerRender) {
            renderProducts(getCategoryFromHash(), '');
        }
    }

    // Immediately reset search state
    clearSearchState(false);

    // Prevent browser autofill or BFCache from restoring old search terms
    window.addEventListener('pageshow', () => {
        clearSearchState(false);
    });
    window.addEventListener('load', () => {
        clearSearchState(false);
        setTimeout(() => clearSearchState(false), 50);
        setTimeout(() => clearSearchState(false), 200);
    });

    if (searchInput) {
        searchInput.addEventListener('input', (e) => {
            const val = e.target.value.trim();
            if (searchClear) searchClear.style.display = val.length > 0 ? 'block' : 'none';
            clearTimeout(searchDebounceTimer);
            searchDebounceTimer = setTimeout(() => {
                renderProducts(getCategoryFromHash(), val);
            }, 180);
        });

        searchInput.addEventListener('keydown', (e) => {
            if (e.key === 'Enter') {
                e.preventDefault();
                clearTimeout(searchDebounceTimer);
                renderProducts(getCategoryFromHash(), searchInput.value.trim());
            }
        });
    }

    if (searchClear) {
        searchClear.addEventListener('click', () => {
            clearSearchState(true);
            if (searchInput) searchInput.focus();
        });
    }

    function loadCart() {
        try {
            const parsed = JSON.parse(localStorage.getItem('cart') || '[]');
            if (!Array.isArray(parsed)) return [];
            return parsed
                .filter(item => item && typeof item.id === 'string' && typeof item.name === 'string')
                .map(item => ({
                    id: item.id,
                    name: item.name,
                    price: Number(item.price) || 0,
                    quantity: Math.min(99, Math.max(1, parseInt(item.quantity, 10) || 1))
                }))
                .filter(item => item.price > 0);
        } catch {
            localStorage.removeItem('cart');
            return [];
        }
    }

    function saveCart() {
        localStorage.setItem('cart', JSON.stringify(cart));
    }

    function updateCartCount() {
        const el = document.getElementById('cart-item-count');
        if (!el) return;
        const count = cart.reduce((n, i) => n + i.quantity, 0);
        el.textContent = count;
        el.setAttribute('aria-label', `${count} items in cart`);
    }

    async function fetchProducts() {
        storeBody.innerHTML = `
            <div class="product-grid" style="width:100%;">
                ${Array(8).fill('<div class="skeleton product-card-skeleton"></div>').join('')}
            </div>
        `;
        try {
            const response = await fetch('/api/products', { cache: 'no-store' });
            if (!response.ok) throw new Error(`Product API returned ${response.status}`);
            const apiProducts = await response.json();
            if (apiProducts && Object.keys(apiProducts).length > 0) {
                // Merge API data with rich details from DEFAULT_PRODUCTS
                for (const cat in DEFAULT_PRODUCTS) {
                    if (apiProducts[cat] && Array.isArray(apiProducts[cat]) && apiProducts[cat].length > 0) {
                        products[cat] = apiProducts[cat].map(apiItem => {
                            const def = (DEFAULT_PRODUCTS[cat] || []).find(d => d.id === apiItem.id) || {};
                            return { ...def, ...apiItem };
                        });
                    } else {
                        products[cat] = DEFAULT_PRODUCTS[cat];
                    }
                }
                for (const cat in apiProducts) {
                    if (!products[cat] && Array.isArray(apiProducts[cat])) {
                        products[cat] = apiProducts[cat];
                    }
                }
            } else {
                products = JSON.parse(JSON.stringify(DEFAULT_PRODUCTS));
            }
        } catch (error) {
            console.error('Error fetching products, using fallback:', error);
            products = JSON.parse(JSON.stringify(DEFAULT_PRODUCTS));
        } finally {
            renderProducts(getCategoryFromHash());
        }
    }

    function renderProducts(category, searchTerm = '') {
        currentSearchTerm = searchTerm ? searchTerm.trim().toLowerCase() : '';
        let productsToRender = [];
        
        const validCategories = ['lifesteal-ranks', 'lifesteal-keys', 'lifesteal-coins', 'survival-ranks', 'survival-keys', 'practice-coins'];
        const targetCategory = validCategories.includes(category) ? category : 'lifesteal-ranks';
        
        const safeCategory = (products[targetCategory] && products[targetCategory].length > 0) ? targetCategory 
                             : (DEFAULT_PRODUCTS[targetCategory] && DEFAULT_PRODUCTS[targetCategory].length > 0) ? targetCategory 
                             : targetCategory;
        
        if (currentSearchTerm) {
            const allProducts = Object.values(products).flat();
            const uniqueMap = new Map();
            allProducts.forEach(p => uniqueMap.set(p.id, p));
            
            productsToRender = Array.from(uniqueMap.values()).filter(p => 
                (p.name && p.name.toLowerCase().includes(currentSearchTerm)) ||
                (p.id && p.id.toLowerCase().includes(currentSearchTerm)) ||
                (p.shortDesc && p.shortDesc.toLowerCase().includes(currentSearchTerm)) ||
                (p.price && String(p.price).includes(currentSearchTerm))
            );
        } else {
            productsToRender = products[safeCategory] || DEFAULT_PRODUCTS[safeCategory] || [];
        }

        if (!currentSearchTerm) setActiveNav(safeCategory);

        let topDonatorsHtml = '';
        if (safeCategory === 'lifesteal-ranks' && !currentSearchTerm) {
            topDonatorsHtml = `
                <div class="top-donators-section">
                    <div class="section-heading">
                        <span><i class="fas fa-trophy" style="color:#FBBF24;margin-right:8px;"></i>Top Donators</span>
                    </div>
                    <div id="top-donators-list" class="top-donators-grid">
                        ${Array(3).fill('<div class="skeleton donator-card" style="height:54px; max-width:220px; flex:1 1 180px;"></div>').join('')}
                    </div>
                </div>
            `;
        }

        let gridContent = '';
        if (productsToRender.length === 0) {
            gridContent = `
                <div class="empty-state" style="grid-column: 1 / -1; text-align: center; padding: 60px 20px; color: #8892A4;">
                    <i class="fas fa-search" style="font-size: 48px; opacity: 0.3; margin-bottom: 16px; display: block;"></i>
                    <h3 style="color: #fff; font-size: 20px; font-weight: 800; margin-bottom: 8px;">No items found</h3>
                    <p style="font-size: 14px;">We couldn't find anything matching "${escapeHtml(searchTerm)}". Try searching for "VIP", "King", "Key", or "Coins".</p>
                </div>
            `;
        } else {
            gridContent = productsToRender.map(product => {
                const badgeHtml = product.badge ? `
                    <span class="product-badge" style="background:${product.badgeColor || 'var(--primary)'};">${escapeHtml(product.badge)}</span>
                ` : '';

                const highlightsHtml = (product.highlights && product.highlights.length > 0) ? `
                    <ul class="product-highlights">
                        ${product.highlights.map(h => `<li><i class="fas fa-check-circle"></i> ${escapeHtml(h)}</li>`).join('')}
                    </ul>
                ` : '';

                const isCrate = product.category === 'lifesteal-keys' || product.category === 'survival-keys';
                const hasDetails = (product.kitItems || product.crateRewards || product.perks);
                const detailsBtnText = isCrate ? '<i class="fas fa-box-open"></i> View Crate Rewards' : '<i class="fas fa-info-circle"></i> View Details';

                return `
                    <article class="product-card" data-product-id="${escapeHtml(product.id)}">
                        <div class="product-card-top">
                            ${badgeHtml}
                            <div class="product-image">
                                <img src="${escapeHtml(product.image)}" alt="${escapeHtml(product.name)}" width="140" height="140" loading="lazy" decoding="async">
                            </div>
                        </div>
                        <div class="product-info">
                            <h3 class="product-name">${escapeHtml(product.name)}</h3>
                            ${product.subtitle ? `<p class="product-subtitle">${escapeHtml(product.subtitle)}</p>` : ''}
                            ${product.shortDesc ? `<p class="product-short-desc">${escapeHtml(product.shortDesc)}</p>` : ''}
                            ${highlightsHtml}
                            <div class="price-container">
                                <span class="price-label">Price:</span>
                                <div style="display:flex;align-items:center;gap:8px;">
                                    ${(product.originalPrice && product.originalPrice > product.price) ? `
                                        <span style="color:#94A3B8;text-decoration:line-through;font-size:14px;font-weight:600;">₹${Number(product.originalPrice).toFixed(0)}</span>
                                        <span style="background:rgba(239,68,68,0.2);color:#F87171;border:1px solid rgba(239,68,68,0.4);font-size:11px;font-weight:800;padding:2px 6px;border-radius:6px;">-35%</span>
                                    ` : ''}
                                    <p class="product-price">₹${Number(product.price).toFixed(0)}</p>
                                </div>
                            </div>
                        </div>
                        <div class="product-actions">
                            ${hasDetails ? `
                                <button class="view-details-btn" data-id="${escapeHtml(product.id)}">
                                    ${detailsBtnText}
                                </button>
                            ` : ''}
                            <button class="add-to-cart-btn" data-id="${escapeHtml(product.id)}" title="Add ${escapeHtml(product.name)} to Cart">
                                <i class="fas fa-shopping-cart" aria-hidden="true"></i>
                                <span>Add to Cart</span>
                            </button>
                        </div>
                    </article>
                `;
            }).join('');
        }

        storeBody.innerHTML = `
            ${topDonatorsHtml}
            <div class="section-heading">
                <span>${currentSearchTerm ? `Search results for "${escapeHtml(searchTerm)}"` : getCategoryTitle(safeCategory)}</span>
                <span class="item-count">${productsToRender.length} item${productsToRender.length !== 1 ? 's' : ''}</span>
            </div>
            <div class="product-grid">
                ${gridContent}
            </div>
        `;

        // Bind Add to Cart buttons
        document.querySelectorAll('.add-to-cart-btn').forEach(button => {
            button.addEventListener('click', (e) => {
                e.stopPropagation();
                addToCart(button.dataset.id, safeCategory);
                const originalHtml = button.innerHTML;
                button.innerHTML = '<i class="fas fa-check"></i> <span>Added to Cart!</span>';
                button.style.background = '#10B981';
                button.style.borderColor = '#10B981';
                button.style.color = '#fff';
                setTimeout(() => {
                    button.innerHTML = originalHtml;
                    button.style.background = '';
                    button.style.borderColor = '';
                    button.style.color = '';
                }, 1200);
            });
        });

        // Bind View Details / Crate Rewards buttons
        document.querySelectorAll('.view-details-btn').forEach(button => {
            button.addEventListener('click', (e) => {
                e.stopPropagation();
                openItemModal(button.dataset.id);
            });
        });

        if (safeCategory === 'lifesteal-ranks' && !currentSearchTerm) fetchTopDonators();

        updateCartCount();

        const revealElements = storeBody.querySelectorAll('.product-card');
        revealElements.forEach((el, index) => {
            el.style.animation = `slideUp 0.35s ease ${index * 0.04}s backwards`;
        });
    }

    /* ── Item & Crate Details Modal ── */
    function openItemModal(id) {
        const allProducts = Object.values(products).flat();
        const item = allProducts.find(p => p.id === id);
        if (!item) return;

        const overlay = document.getElementById('item-modal-overlay');
        const header = document.getElementById('item-modal-header');
        const body = document.getElementById('item-modal-body');
        const footer = document.getElementById('item-modal-footer');
        if (!overlay || !header || !body || !footer) return;

        const isCrate = item.category === 'lifesteal-keys' || item.category === 'survival-keys';

        header.innerHTML = `
            <div class="modal-header-flex">
                <img src="${escapeHtml(item.image)}" alt="${escapeHtml(item.name)}" class="modal-item-icon">
                <div>
                    <span class="modal-badge" style="background:${item.badgeColor || 'var(--primary)'}">${escapeHtml(item.badge || (isCrate ? 'CRATE KEY' : 'RANK'))}</span>
                    <h2 class="modal-item-title">${escapeHtml(item.name)}</h2>
                    <p class="modal-item-sub">${escapeHtml(item.subtitle || item.shortDesc || '')}</p>
                </div>
            </div>
        `;

        let bodyHtml = '';

        // Perks Section
        if (item.perks && item.perks.length > 0) {
            bodyHtml += `
                <div class="modal-section">
                    <h4 class="modal-section-title"><i class="fas fa-crown" style="color:#F59E0B;"></i> Rank Perks & Commands</h4>
                    <div class="modal-perks-grid">
                        ${item.perks.map(p => `
                            <div class="modal-perk-item">
                                <i class="fas fa-check" style="color:#10B981;"></i>
                                <span>${escapeHtml(p)}</span>
                            </div>
                        `).join('')}
                    </div>
                </div>
            `;
        }

        // Kit Items Section
        if (item.kitItems && item.kitItems.length > 0) {
            bodyHtml += `
                <div class="modal-section">
                    <h4 class="modal-section-title"><i class="fas fa-shield-alt" style="color:#3B82F6;"></i> Kit Contents & Equipment</h4>
                    <div class="modal-items-grid">
                        ${item.kitItems.map(k => `
                            <div class="modal-kit-item">
                                <span class="modal-kit-icon">${k.icon || '📦'}</span>
                                <div class="modal-kit-info">
                                     <span class="modal-kit-name">${escapeHtml(k.name)}</span>
                                     <span class="modal-kit-desc">${escapeHtml(k.desc || '')}</span>
                                </div>
                            </div>
                        `).join('')}
                    </div>
                </div>
            `;
        }

        // Crate Rewards Section
        if (item.crateRewards && item.crateRewards.length > 0) {
            bodyHtml += `
                <div class="modal-section">
                    <h4 class="modal-section-title"><i class="fas fa-gem" style="color:#A855F7;"></i> ${escapeHtml(item.crateName || 'Crate')} Rewards & Drops</h4>
                    <div class="modal-crate-grid">
                        ${item.crateRewards.map(r => `
                            <div class="modal-crate-item rarity-${(r.rarity || 'common').toLowerCase()}">
                                <span class="modal-crate-icon">${r.icon || '💎'}</span>
                                <div class="modal-crate-info">
                                    <div class="modal-crate-name-row">
                                        <span class="modal-crate-name">${escapeHtml(r.name)}</span>
                                        <span class="modal-crate-qty">${escapeHtml(r.qty || '1×')}</span>
                                    </div>
                                    <span class="modal-crate-rarity">${escapeHtml(r.rarity || 'Standard')}</span>
                                </div>
                            </div>
                        `).join('')}
                    </div>
                </div>
            `;
        }

        body.innerHTML = bodyHtml;

        footer.innerHTML = `
            <div class="modal-footer-price">
                <span>Total:</span>
                <div style="display:flex;align-items:center;gap:8px;">
                    ${(item.originalPrice && item.originalPrice > item.price) ? `
                        <span style="color:#94A3B8;text-decoration:line-through;font-size:15px;font-weight:600;">₹${Number(item.originalPrice).toFixed(0)}</span>
                        <span style="background:rgba(239,68,68,0.2);color:#F87171;border:1px solid rgba(239,68,68,0.4);font-size:11px;font-weight:800;padding:2px 6px;border-radius:6px;">-35% OFF</span>
                    ` : ''}
                    <span class="price-val">₹${Number(item.price).toFixed(0)}</span>
                </div>
            </div>
            <div class="modal-footer-actions">
                <button class="modal-add-cart-btn" id="modal-buy-btn" data-id="${escapeHtml(item.id)}">
                    <i class="fas fa-shopping-cart"></i> Add to Cart
                </button>
            </div>
        `;

        const modalBuyBtn = document.getElementById('modal-buy-btn');
        if (modalBuyBtn) {
            modalBuyBtn.onclick = (e) => {
                e.preventDefault();
                e.stopPropagation();
                addToCart(item.id, item.category);
                closeItemModal();
                if (typeof openCartDrawer === 'function') {
                    openCartDrawer();
                }
            };
        }

        overlay.classList.add('open');
        if (typeof toggleBodyScroll === 'function') toggleBodyScroll(true);
    }

    function closeItemModal() {
        const overlay = document.getElementById('item-modal-overlay');
        if (overlay) overlay.classList.remove('open');
        if (typeof toggleBodyScroll === 'function') toggleBodyScroll(false);
    }

    const modalCloseBtn = document.getElementById('item-modal-close');
    const modalOverlay = document.getElementById('item-modal-overlay');
    if (modalCloseBtn) modalCloseBtn.onclick = closeItemModal;
    if (modalOverlay) {
        modalOverlay.onclick = (e) => {
            if (e.target === modalOverlay) closeItemModal();
        };
    }

    async function fetchTopDonators() {
        const container = document.getElementById('top-donators-list');
        if (!container) return;
        try {
            const response = await fetch('/api/top-donators', { cache: 'no-store' });
            if (!response.ok) throw new Error('Failed to fetch');
            const donators = await response.json();
            if (!donators || donators.length === 0) {
                container.innerHTML = '<div class="orders-empty">No donators yet. Be the first!</div>';
                return;
            }
            const rankIcons = ['🥇', '🥈', '🥉', '4th', '5th'];
            const rankClasses = ['gold', 'silver', 'bronze', '', ''];
            container.innerHTML = donators.map((d, i) => `
                <div class="donator-card ${rankClasses[i] || ''}">
                    <div class="donator-rank">${rankIcons[i] || (i + 1)}</div>
                    <img src="${escapeHtml(d.avatar)}" alt="${escapeHtml(d.ign)}" class="donator-head" width="36" height="36" onerror="this.src='https://mc-heads.net/avatar/MHF_Steve/64'">
                    <div class="donator-info">
                        <span class="donator-ign">${escapeHtml(d.ign)}</span>
                        <span class="donator-orders">${d.orders} order${d.orders !== 1 ? 's' : ''}</span>
                    </div>
                </div>
            `).join('');
        } catch {
            container.innerHTML = '<div class="orders-empty">Could not load top donators.</div>';
        }
    }

    function addToCart(id, category) {
        cart = loadCart();
        const allProducts = Object.values(products).flat();
        const product = allProducts.find(item => item.id === id);
        if (!product) { showToast('This item is no longer available.', 'error'); return; }

        const existing = cart.find(item => item.id === product.id);
        if (existing) {
            existing.quantity = Math.min(99, (Number(existing.quantity) || 1) + 1);
        } else {
            cart.push({ id: product.id, name: product.name, price: Number(product.price) || 0, quantity: 1 });
        }

        saveCart();
        updateCartCount();

        if (typeof renderCartDrawerItems === 'function') {
            renderCartDrawerItems();
        }

        const cartBtn = document.getElementById('cart-summary-btn');
        if (cartBtn) {
            cartBtn.classList.remove('cart-bounce');
            void cartBtn.offsetWidth;
            cartBtn.classList.add('cart-bounce');
        }
        
        if (typeof showCartToast === 'function') {
            showCartToast(product.name);
        } else if (typeof showToast === 'function') {
            showToast(`${product.name} added to cart! ✓`);
        }
    }

    function getCategoryFromHash() {
        const hash = window.location.hash.replace('#', '').trim();
        const validCategories = ['lifesteal-ranks', 'lifesteal-keys', 'lifesteal-coins', 'survival-ranks', 'survival-keys', 'practice-coins'];
        return validCategories.includes(hash) ? hash : 'lifesteal-ranks';
    }

    function setActiveNav(category) {
        document.querySelectorAll('.store-nav .nav-item').forEach(link => {
            const href = link.getAttribute('href');
            if (!href || !href.includes('#')) return;
            const hash = href.replace(/^.*#/, '').trim();
            link.classList.toggle('active', hash === category);
        });
    }

    function getCategoryTitle(category) {
        const titles = { 
            'lifesteal-ranks': '<i class="fas fa-crown" style="color:#F59E0B;margin-right:8px;"></i> Lifesteal Ranks & Kits', 
            'lifesteal-keys': '<i class="fas fa-key" style="color:#10B981;margin-right:8px;"></i> Lifesteal Keys & Crates', 
            'lifesteal-coins': '<i class="fas fa-coins" style="color:#EAB308;margin-right:8px;"></i> Lifesteal Coin Packages', 
            'survival-ranks': '<i class="fas fa-crown" style="color:#A855F7;margin-right:8px;"></i> Survival Ranks', 
            'survival-keys': '<i class="fas fa-key" style="color:#EC4899;margin-right:8px;"></i> Survival Keys & Crates', 
            'practice-coins': '<i class="fas fa-coins" style="color:#3B82F6;margin-right:8px;"></i> Practice Coin Packages' 
        };
        return titles[category] || '<i class="fas fa-star" style="color:#F59E0B;margin-right:8px;"></i> Featured Items';
    }

    document.querySelectorAll('.store-nav .nav-item').forEach(link => {
        link.addEventListener('click', (e) => {
            const href = link.getAttribute('href') || '';
            if (href.includes('#')) {
                e.preventDefault();
                const hash = href.replace(/^.*#/, '').trim();
                if (hash) {
                    clearSearchState(false);
                    setActiveNav(hash);
                    renderProducts(hash, '');
                    if (window.location.hash !== '#' + hash) {
                        try {
                            history.replaceState(null, '', '#' + hash);
                        } catch (_) {
                            window.location.hash = hash;
                        }
                    }
                }
            }
        });
    });

    window.addEventListener('hashchange', () => {
        const hash = getCategoryFromHash();
        clearSearchState(false);
        setActiveNav(hash);
        renderProducts(hash, '');
    });

    fetchProducts();
});

function escapeHtml(str) {
    if (!str) return '';
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');
}
