# АРХИТЕКТУРНАЯ СПЕЦИФИКАЦИЯ: CYBER TAROT NFT & DECENTRALIZED EXCHANGE ECOSYSTEM

**Версия спецификации:** 1.0.0-PROD  
**Стандарты токенов:** ERC-721A / ERC-1155 (EVM Compatible), EIP-712, EIP-2981  
**Экономический токен расчетов:** $WEED (ERC-20 Utility / In-Game Currency)  
**Сети развертывания:** Polygon / Arbitrum One / Base / Ethereum L2  

---

## 1. АРХИТЕКТУРА ЧЕКАНКИ (CORE MINTING LOGIC)

### 1.1. Источники генерации и триггеры чеканки
В экосистеме Cyber Tarot чеканка NFT-карт происходит через три основных игровых механизма:

1. **Гадание и Синхронизация Оракула (Divination Scan Minting):**
   * Пользователь выполняет расклад через ИИ-Оракула (Gemini API / Local Node).
   * Выпавшие карты получают статус «Синхронизированы в локальном буфере».
   * Пользователь может зафиксировать (зачеканить) любую выпавшую карту в блокчейн, уплатив комиссию в токенах `$WEED` или нативном газе (MATIC/ETH).
   * Для предотвращения мошенничества сервер Оракула подписывает данные карты криптографической подписью **EIP-712** (`cardIndex`, `entropyHash`, `userAddress`, `nonce`, `expiryTimestamp`).

2. **Кузница и Синтез / Слияние (Alchemy Fusion & Burn):**
   * Дефляционный механизм: сжигание низших карт для получения гарантированной высшей редкости.
   * **4 Common** ➔ Сжигание ➔ Чеканка **1 Rare** (случайный или выбранный аркан).
   * **4 Rare** ➔ Сжигание ➔ Чеканка **1 Epic**.
   * **4 Epic** ➔ Сжигание ➔ Чеканка **1 Legendary**.
   * **10 Epic** ➔ Сжигание ➔ Чеканка **1 Genesis Singularity Gold** (#22–#30 арканы Начала).
   * Смарт-контракт атомарно вызывает `_burn()` для карт-ингредиентов и `_safeMint()` для новой карты.

3. **Турнирные Награды и Победы на Арене (Duel Victory Drops):**
   * Победа в P2P или рейтинговых дуэлях с шансом 3.5% генерирует право бесплатного клейма «Кибер-Пакета» (Loot Cache Mint Pass).

---

### 1.2. Матрица распределения редкости (Drop Rate Matrix)

При открытии базового набора или чеканке через Оракула вероятность определяется с использованием псевдослучайности, защищенной от манипуляций валидаторов (Chainlink VRF или Commit-Reveal схема):

| Тир Редкости | Вероятность (Drop Rate) | Максимальный тираж на 1 аркан | Общий пул эмиссии |
| :--- | :--- | :--- | :--- |
| **Common (Обычный)** | 70.0% | 50 000 шт. | 1 550 000 шт. |
| **Rare (Редкий)** | 22.0% | 10 000 шт. | 310 000 шт. |
| **Epic (Эпический)** | 7.0% | 2 500 шт. | 77 500 шт. |
| **Legendary (Легендарный)** | 0.9% | 500 шт. | 15 500 шт. |
| **Genesis Singularity (Начало)** | 0.1% | 100 шт. (Нумерованные #001-#100) | 3 100 шт. |

---

### 1.3. Токеномика чеканки и сжигание $WEED

* **Базовая стоимость чеканки Common:** 100 $WEED + gas.
* **Стоимость чеканки Rare:** 400 $WEED + gas (или слияние 4 Common + 50 $WEED).
* **Стоимость чеканки Epic:** 1 500 $WEED + gas (или слияние 4 Rare + 150 $WEED).
* **Стоимость чеканки Legendary:** 5 000 $WEED + gas (или слияние 4 Epic + 500 $WEED).
* **Сжигание (Token Sink):**
  * **60%** потраченных на чеканку/слияние $WEED безвозвратно сжигаются (`0x000...dead`).
  * **25%** направляются в Пул Наград боевой арены (Duel Staking Vault).
  * **15%** поступают в Пул Автоматической Ликвидности Диспенсера (DEX AMM Reserve).

---

## 2. ТИРЫ РЕДКОСТИ, АТРИБУТЫ И ВИЗУАЛЬНЫЕ ШЕЙДЕРЫ

Каждый аркан обладает метаданными, боевыми параметрами и уникальным визуальным оформлением (шейдеры на клиенте Compose / GLSL / Three.js).

```
+-------------------------------------------------------------------------------+
|                             CYBER TAROT NFT TIERS                             |
+---------------+-------------------+---------------------+---------------------+
| ТИР           | ЦВЕТОВАЯ ПАЛИТРА   | ВИЗУАЛЬНЫЕ ЭФФЕКТЫ   | БОЕВЫЕ ХАРАКТЕРИСТИКИ|
+---------------+-------------------+---------------------+---------------------+
| COMMON        | Неоновый изумруд  | • Тонкие сканлайны  | • Базовая сила 2-5  |
|               | #00FF66 / Черный  | • Матовая подложка  | • Базовый щит 2-4   |
|               |                   | • Без рамочного свеч| • RAM 1-3           |
+---------------+-------------------+---------------------+---------------------+
| RARE          | Электрик Циан     | • Пульсирующие треки| • Сила 4-7          |
|               | #00F5FF / Индиго  | • Неоновый ореол    | • Щит 4-6           |
|               |                   | • 1 слот улучшения  | • RAM 2-4           |
+---------------+-------------------+---------------------+---------------------+
| EPIC          | Ультрафиолет      | • Хроматич. аберрац.| • Сила 6-10         |
|               | #BD00FF / Пурпур  | • Голографич. фольга| • Щит 6-9           |
|               |                   | • Парящие частицы   | • 1 Спецспособность |
+---------------+-------------------+---------------------+---------------------+
| LEGENDARY     | Солнечный янтарь  | • Золотая фрактал-  | • Сила 9-14         |
|               | #FFD700 / Обсидиан|   решетка Гауди     | • Щит 8-12          |
|               |                   | • Вихри Ван Гога    | • -1 RAM навсегда   |
|               |                   | • 3D гиро-отклик    | • Двойной контрудар |
+---------------+-------------------+---------------------+---------------------+
| GENESIS       | Сингулярный Белый | • Динамический свет | • Максимальные статы|
| SINGULARITY   | #FFFFFF / Радужный| • Эффект витража    | • Пробитие щитов    |
| (Эра Начал)   | Prism Trencadís   | • Авторская печать  | • Пассивный доход   |
+---------------+-------------------+---------------------+---------------------+
```

### 2.1. Спецификация JSON-метаданных (OpenSea / ERC-721 Standard)

Каждый NFT ссылается на децентрализованный URI (IPFS / Arweave):

```json
{
  "name": "The High Priestess of Cyber Botanica #0042",
  "description": "Аркан II: Хранительница квантовых нейросетей и биолюминесцентных оранжерей. Обладает стихией Квантовой Сингулярности.",
  "image": "ipfs://bafybeic.../card_02_priestess_legendary.jpg",
  "animation_url": "ipfs://bafybeic.../card_02_hologram.mp4",
  "external_url": "https://cybertarot.app/cards/2/42",
  "attributes": [
    { "trait_type": "Arcana Number", "value": 2 },
    { "trait_type": "Traditional Name", "value": "The High Priestess" },
    { "trait_type": "Cyber Name", "value": "Quantum Net Oracle" },
    { "trait_type": "Rarity Tier", "value": "Legendary" },
    { "trait_type": "Element Affinity", "value": "QUANTUM" },
    { "trait_type": "Power (ATK)", "value": 11, "max_value": 20 },
    { "trait_type": "Shield (DEF)", "value": 10, "max_value": 20 },
    { "trait_type": "RAM Cost", "value": 3, "max_value": 6 },
    { "trait_type": "Special Ability", "value": "NEURAL_LEECH" },
    { "trait_type": "Skin Matrix", "value": "GOLDEN_CIRCUIT_GAUDI" },
    { "trait_type": "Mint Serial", "value": 42 },
    { "trait_type": "Total Edition Cap", "value": 500 }
  ]
}
```

---

## 3. СПЕЦИФИКАЦИЯ СМАРТ-КОНТРАКТОВ (ON-CHAIN ARCHITECTURE)

### 3.1. Смарт-контракт NFT: `CyberTarotNFT.sol` (ERC-721A + EIP-712 + EIP-2981)

```solidity
// SPDX-License-Identifier: MIT
pragma solidity ^0.8.24;

import "@openzeppelin/contracts/token/ERC721/extensions/ERC721Enumerable.sol";
import "@openzeppelin/contracts/access/Ownable.sol";
import "@openzeppelin/contracts/utils/cryptography/ECDSA.sol";
import "@openzeppelin/contracts/utils/cryptography/EIP712.sol";
import "@openzeppelin/contracts/token/common/ERC2981.sol";
import "@openzeppelin/contracts/utils/ReentrancyGuard.sol";

interface IWeedToken {
    function burnFrom(address account, uint256 amount) external returns (bool);
    function transferFrom(address sender, address recipient, uint256 amount) external returns (bool);
}

contract CyberTarotNFT is ERC721Enumerable, EIP712, ERC2981, Ownable, ReentrancyGuard {
    using ECDSA for bytes32;

    enum Rarity { COMMON, RARE, EPIC, LEGENDARY, GENESIS }

    struct CardMeta {
        uint16 cardIndex;      // 0..30
        Rarity rarity;         // Common..Genesis
        uint32 mintNumber;     // Serial number within rarity
        uint32 power;
        uint32 defense;
        uint16 ramCost;
        uint64 mintTimestamp;
    }

    // Mapping from tokenId to Card Metadata
    mapping(uint256 => CardMeta) public cardMetadata;
    
    // Serial trackers: cardIndex => rarity => count
    mapping(uint16 => mapping(uint8 => uint32)) public mintedCounts;

    // Hard caps per card index per rarity
    mapping(uint8 => uint32) public rarityCaps;

    IWeedToken public weedToken;
    address public oracleSigner;
    string private _baseTokenURI;

    // EIP-712 TypeHash
    bytes32 public constant MINT_TYPEHASH = keccak256(
        "MintRequest(address recipient,uint16 cardIndex,uint8 rarity,uint256 nonce,uint256 expiry)"
    );

    mapping(uint256 => bool) public usedNonces;

    event CardMinted(uint256 indexed tokenId, address indexed owner, uint16 cardIndex, Rarity rarity, uint32 serial);
    event CardsFused(address indexed owner, uint256[] burnedIds, uint256 indexed newCardId, Rarity newRarity);

    constructor(
        address _weedTokenAddress,
        address _oracleSignerAddress,
        string memory initialBaseURI
    ) ERC721("Cyber Tarot TCG", "CYBER") EIP712("CyberTarotOracle", "1.0") Ownable(msg.sender) {
        weedToken = IWeedToken(_weedTokenAddress);
        oracleSigner = _oracleSignerAddress;
        _baseTokenURI = initialBaseURI;

        // Caps per arcana:
        rarityCaps[uint8(Rarity.COMMON)] = 50000;
        rarityCaps[uint8(Rarity.RARE)] = 10000;
        rarityCaps[uint8(Rarity.EPIC)] = 2500;
        rarityCaps[uint8(Rarity.LEGENDARY)] = 500;
        rarityCaps[uint8(Rarity.GENESIS)] = 100;

        // Default 2.5% secondary royalties to treasury (EIP-2981)
        _setDefaultRoyalty(msg.sender, 250);
    }

    /**
     * @notice Чеканка карты на основе верифицированного расклада Оракула
     */
    function mintFromOracle(
        uint16 cardIndex,
        uint8 rarity,
        uint256 nonce,
        uint256 expiry,
        uint256 weedCost,
        bytes calldata signature
    ) external nonReentrant returns (uint256) {
        require(block.timestamp <= expiry, "Signature expired");
        require(!usedNonces[nonce], "Nonce already used");
        require(cardIndex <= 30, "Invalid card index");
        require(mintedCounts[cardIndex][rarity] < rarityCaps[rarity], "Rarity cap reached");

        // Verify EIP-712 Signature
        bytes32 structHash = keccak256(
            abi.encode(MINT_TYPEHASH, msg.sender, cardIndex, rarity, nonce, expiry)
        );
        bytes32 hash = _hashTypedDataV4(structHash);
        address recoveredSigner = ECDSA.recover(hash, signature);
        require(recoveredSigner == oracleSigner, "Invalid oracle signature");

        usedNonces[nonce] = true;

        // Burn $WEED mint fee
        if (weedCost > 0) {
            weedToken.burnFrom(msg.sender, weedCost);
        }

        uint256 nextTokenId = totalSupply() + 1;
        uint32 currentSerial = ++mintedCounts[cardIndex][rarity];

        cardMetadata[nextTokenId] = CardMeta({
            cardIndex: cardIndex,
            rarity: Rarity(rarity),
            mintNumber: currentSerial,
            power: calculateBasePower(cardIndex, Rarity(rarity)),
            defense: calculateBaseDefense(cardIndex, Rarity(rarity)),
            ramCost: calculateRamCost(cardIndex, Rarity(rarity)),
            mintTimestamp: uint64(block.timestamp)
        });

        _safeMint(msg.sender, nextTokenId);
        emit CardMinted(nextTokenId, msg.sender, cardIndex, Rarity(rarity), currentSerial);

        return nextTokenId;
    }

    /**
     * @notice Слияние (Алхимический Синтез) 4 карт одного тира в 1 карту высшего тира
     */
    function fuseCards(
        uint256[] calldata burnTokenIds,
        uint16 targetCardIndex,
        uint256 weedFee
    ) external nonReentrant returns (uint256) {
        require(burnTokenIds.length == 4, "Must burn exactly 4 cards");
        Rarity baseRarity = cardMetadata[burnTokenIds[0]].rarity;
        require(baseRarity < Rarity.GENESIS, "Cannot fuse Genesis");

        for (uint256 i = 0; i < 4; i++) {
            require(ownerOf(burnTokenIds[i]) == msg.sender, "Not token owner");
            require(cardMetadata[burnTokenIds[i]].rarity == baseRarity, "Rarities must match");
            _burn(burnTokenIds[i]);
        }

        if (weedFee > 0) {
            weedToken.burnFrom(msg.sender, weedFee);
        }

        Rarity newRarity = Rarity(uint8(baseRarity) + 1);
        uint256 nextTokenId = totalSupply() + 1;
        uint32 newSerial = ++mintedCounts[targetCardIndex][uint8(newRarity)];

        cardMetadata[nextTokenId] = CardMeta({
            cardIndex: targetCardIndex,
            rarity: newRarity,
            mintNumber: newSerial,
            power: calculateBasePower(targetCardIndex, newRarity),
            defense: calculateBaseDefense(targetCardIndex, newRarity),
            ramCost: calculateRamCost(targetCardIndex, newRarity),
            mintTimestamp: uint64(block.timestamp)
        });

        _safeMint(msg.sender, nextTokenId);
        emit CardsFused(msg.sender, burnTokenIds, nextTokenId, newRarity);

        return nextTokenId;
    }

    function calculateBasePower(uint16 index, Rarity r) internal pure returns (uint32) {
        uint32 bonus = uint32(r) * 3;
        return 2 + (uint32(index) % 4) + bonus;
    }

    function calculateBaseDefense(uint16 index, Rarity r) internal pure returns (uint32) {
        uint32 bonus = uint32(r) * 3;
        return 2 + ((uint32(index) + 1) % 4) + bonus;
    }

    function calculateRamCost(uint16 index, Rarity r) internal pure returns (uint16) {
        if (r == Rarity.LEGENDARY || r == Rarity.GENESIS) return 3;
        return uint16(1 + (index % 3));
    }

    function _baseURI() internal view override returns (string memory) {
        return _baseTokenURI;
    }

    function supportsInterface(bytes4 interfaceId)
        public view override(ERC721Enumerable, ERC2981) returns (bool) {
        return super.supportsInterface(interfaceId);
    }
}
```

---

### 3.2. Автоматизированная Торговая Площадка (DEX + P2P Escrow): `CyberTarotMarketplace.sol`

Контракт объединяет две модели:
1. **P2P Order Book:** Пользователи выставляют карты за фиксированную цену в `$WEED`.
2. **Instant AMM Liquidity Curve (Автоматический Скупщик/Выкупщик):** Гарантирует мгновенную ликвидность по алгоритмической формуле цены даже при отсутствии покупателей.

```solidity
// SPDX-License-Identifier: MIT
pragma solidity ^0.8.24;

import "@openzeppelin/contracts/token/ERC721/IERC721.sol";
import "@openzeppelin/contracts/token/ERC20/IERC20.sol";
import "@openzeppelin/contracts/access/Ownable.sol";
import "@openzeppelin/contracts/utils/ReentrancyGuard.sol";

contract CyberTarotMarketplace is Ownable, ReentrancyGuard {
    IERC721 public immutable nftContract;
    IERC20 public immutable weedToken;

    struct Listing {
        address seller;
        uint256 price; // in WEED
        bool active;
    }

    // tokenId => Listing
    mapping(uint256 => Listing) public listings;

    // AMM Bonding Curve State: rarity => basePrice
    mapping(uint8 => uint256) public ammFloorPrice;
    uint256 public constant PROTOCOL_FEE_BPS = 250; // 2.5%
    address public treasury;

    event Listed(uint256 indexed tokenId, address indexed seller, uint256 price);
    event Bought(uint256 indexed tokenId, address indexed buyer, address indexed seller, uint256 price);
    event ListingCancelled(uint256 indexed tokenId, address indexed seller);
    event AmmInstantSold(uint256 indexed tokenId, address indexed seller, uint256 payout);

    constructor(address _nft, address _weed, address _treasury) Ownable(msg.sender) {
        nftContract = IERC721(_nft);
        weedToken = IERC20(_weed);
        treasury = _treasury;

        // Initial floor prices for instant AMM cashout
        ammFloorPrice[0] = 50 * 1e18;   // Common floor: 50 WEED
        ammFloorPrice[1] = 200 * 1e18;  // Rare floor: 200 WEED
        ammFloorPrice[2] = 800 * 1e18;  // Epic floor: 800 WEED
        ammFloorPrice[3] = 2500 * 1e18; // Legendary floor: 2500 WEED
    }

    function listCard(uint256 tokenId, uint256 priceInWeed) external nonReentrant {
        require(priceInWeed > 0, "Price must be > 0");
        require(nftContract.ownerOf(tokenId) == msg.sender, "Not owner");
        require(
            nftContract.isApprovedForAll(msg.sender, address(this)) ||
            nftContract.getApproved(tokenId) == address(this),
            "Market not approved"
        );

        listings[tokenId] = Listing({
            seller: msg.sender,
            price: priceInWeed,
            active: true
        });

        emit Listed(tokenId, msg.sender, priceInWeed);
    }

    function buyCard(uint256 tokenId) external nonReentrant {
        Listing memory item = listings[tokenId];
        require(item.active, "Listing inactive");
        require(msg.sender != item.seller, "Cannot buy own card");

        listings[tokenId].active = false;

        uint256 fee = (item.price * PROTOCOL_FEE_BPS) / 10000;
        uint256 sellerAmount = item.price - fee;

        require(weedToken.transferFrom(msg.sender, treasury, fee), "Fee transfer failed");
        require(weedToken.transferFrom(msg.sender, item.seller, sellerAmount), "Payment failed");

        nftContract.safeTransferFrom(item.seller, msg.sender, tokenId);

        emit Bought(tokenId, msg.sender, item.seller, item.price);
    }

    function cancelListing(uint256 tokenId) external nonReentrant {
        require(listings[tokenId].seller == msg.sender, "Not your listing");
        require(listings[tokenId].active, "Not active");
        listings[tokenId].active = false;
        emit ListingCancelled(tokenId, msg.sender);
    }

    /**
     * @notice Мгновенная сдача карты в AMM Пул Ликвидности Диспенсера
     */
    function instantCashoutToAmm(uint256 tokenId, uint8 rarity) external nonReentrant {
        require(nftContract.ownerOf(tokenId) == msg.sender, "Not owner");
        uint256 payout = ammFloorPrice[rarity];
        require(payout > 0, "No AMM floor");
        require(weedToken.balanceOf(address(this)) >= payout, "AMM pool dry");

        nftContract.transferFrom(msg.sender, address(this), tokenId);
        require(weedToken.transfer(msg.sender, payout), "Payout failed");

        emit AmmInstantSold(tokenId, msg.sender, payout);
    }
}
```

---

## 4. ПОЛЬЗОВАТЕЛЬСКИЙ ИНТЕРФЕЙС И UX (ХРАНИЛИЩЕ И ОБМЕН)

### 4.1. Экран Хранилища (Vault / Collection UI)
* **Адаптивная сетка карт:** 2 колонки на смартфонах, 3–4 на планшетах.
* **Фильтры быстрого переключения:** «Все», «Common», «Rare», «Epic», «Legendary», а также стихийный фильтр (Cyber, Bio, Quantum, Void).
* **Интерактивная карточка 3D/Голограммы:**
  * Тап переворачивает карту на 180° с анимацией ребра толщиной 1.5dp.
  * Нажатие на «Инспектировать» открывает полноэкранный диалог с детализацией: серийный номер (#0042/500), процент редкости, формула урона, история владельцев в блокчейне.
  * Прямая кнопка **«Выставить на маркет»** с быстрым выбором цены (по пресетам 100 / 250 / 500 / 1000 $WEED).

### 4.2. Экран Автоматизированного Рынка (DEX Market UI)
* **Вкладка 1: «Активные Листинги» (P2P Order Book):**
  * Карточки от реальных игроков и ботов-ликвидаторов.
  * Сортировка по возрастанию цены, редкости или новизне.
  * Однокнопочная покупка с мгновенным списанием $WEED и зачислением карты в боевую колоду.
* **Вкладка 2: «Мгновенный Диспенсер» (Instant AMM Liquidity):**
  * Возможность сдать любую карту в резерв пула за гарантированный выкуп по формуле пола (Floor Price).
  * Нулевое время ожидания: игрок получает $WEED моментально.
* **Вкладка 3: «P2P Barter Hub» (Обмен Карта-на-Карту):**
  * Позволяет выбрать карту из своего инвентаря и запросить обмен на желаемый аркан соперника по защищенному P2P-каналу без комиссий.

---

## 5. ИТОГОВАЯ ТАБЛИЦА ГОТОВНОСТИ МОДУЛЕЙ

| Модуль | Компонент в коде приложения | Статус реализации |
| :--- | :--- | :--- |
| **Room БД сущностей** | `MintedCard`, `MarketListing`, `TransactionLog` | Готово (`TarotEntities.kt`) |
| **Логика чеканки и слияния** | `TarotRepository.mintTarotNFT()`, `fuseCards()` | Готово (`TarotRepository.kt`) |
| **Интерфейс Хранилища** | `VaultScreen`, `InteractiveCyberCard` | Готово (`CyberTarotScreens.kt`) |
| **Рыночный интерфейс** | `MarketScreen`, `FilterBar`, `ListingCard` | Готово (`CyberTarotScreens.kt`) |
| **Смарт-контракты** | `CyberTarotNFT.sol`, `CyberTarotMarketplace.sol` | Специфицировано в ТЗ (раздел 3) |
| **On-Chain интеграция** | `OnChainNftPanel`, `P2pLedgerPanel` | Готово (`NetOpsScreen`) |
