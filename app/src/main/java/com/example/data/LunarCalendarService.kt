package com.example.data

import java.util.Calendar
import java.util.Locale

data class LunarDayInfo(
    val dayOfMonth: Int,
    val dateString: String,
    val phaseName: String,
    val phaseIcon: String,
    val illumination: Int, // 0 to 100
    val astrologicalSign: String,
    val isFertile: Boolean,
    val recommendedAction: String,
    val detailInstructions: String
)

enum class MintingAuspiciousnessTier {
    AUSPICIOUS,     // Peak astral conductivity (Full Moon, Waxing Gibbous, Fertile Alignment)
    BALANCED,       // Stable baseline node resonance (First/Third Quarter)
    CHALLENGING     // Volatile, Void static, Eclipse flux (New Moon, Waning Crescent, Barren Void)
}

data class TarotMintingWindow(
    val tier: MintingAuspiciousnessTier,
    val title: String,
    val summary: String,
    val dropRateModifierPercent: Int, // e.g. +18, +10, 0, -10
    val gasFeeDiscountPercent: Int,    // e.g. 15 (discount) or -20 (surcharge)
    val favoredElements: List<String>,
    val patronArcanaIndices: List<Int>,
    val ritualAdvice: String,
    val astralConductivity: Float,     // 0.0f .. 1.0f
    val badgeLabel: String
)

data class TarotMintingDayForecast(
    val dayOfMonth: Int,
    val dateString: String,
    val ageFactor: Double,
    val illumination: Int,
    val phaseIcon: String,
    val phaseName: String,
    val astrologicalSign: String,
    val mintingWindow: TarotMintingWindow
)

object LunarCalendarService {

    /**
     * Approximate calculation of the lunar phase based on the date.
     * Returns a double value from 0.0 to 1.0 representing the position in the synodic cycle:
     * 0.0 = New Moon, 0.25 = First Quarter, 0.5 = Full Moon, 0.75 = Last Quarter.
     */
    fun calculateLunarAgeFactor(year: Int, month: Int, day: Int): Double {
        val c = Calendar.getInstance()
        c.set(year, month - 1, day, 12, 0, 0)
        val epochTime = c.timeInMillis / 1000L

        // Reference New Moon: Jan 11, 2024 (epoch 1704974400)
        // Synodic Month = 29.530588853 days = 2551442.8 seconds
        val referenceNewMoonEpoch = 1704974400L
        val synodicPeriod = 2551442.8

        val diffSeconds = epochTime - referenceNewMoonEpoch
        var factor = (diffSeconds % synodicPeriod) / synodicPeriod
        if (factor < 0) {
            factor += 1.0
        }
        return factor
    }

    fun getLunarDayInfo(year: Int, month: Int, day: Int, lang: String): LunarDayInfo {
        val ageFactor = calculateLunarAgeFactor(year, month, day)
        val illumination = if (ageFactor <= 0.5) {
            (ageFactor * 2.0 * 100).toInt()
        } else {
            ((1.0 - ageFactor) * 2.0 * 100).toInt()
        }

        val phaseName: String
        val phaseIcon: String
        val recommendedAction: String
        val detailInstructions: String

        val activeLang = if (lang in listOf("en", "ru", "es")) lang else "en"

        when {
            ageFactor < 0.03 || ageFactor > 0.97 -> {
                phaseIcon = "🌑"
                phaseName = when (activeLang) {
                    "ru" -> "Новолуние"
                    "es" -> "Luna Nueva"
                    else -> "New Moon"
                }
                recommendedAction = when (activeLang) {
                    "ru" -> "Уход за Корнями и Почвой"
                    "es" -> "Cuidado de Raíces y Suelo"
                    else -> "Root Care & Soil Aeration"
                }
                detailInstructions = when (activeLang) {
                    "ru" -> "Низкое гравитационное притяжение. Избегайте посадки и клонирования. Идеальное время для аэрации почвы, подготовки к пересадкам и внесения мягких корневых стимуляторов."
                    "es" -> "Baja atracción gravitacional. Evite plantar o clonar. Ideal para la aireación del suelo, preparación de trasplantes y aplicación de bioestimulantes radiculares."
                    else -> "Low gravitational pull. Avoid planting or cloning. Best time for soil aeration, transplanting preparation, and applying mild organic root stimulants."
                }
            }
            ageFactor >= 0.03 && ageFactor < 0.22 -> {
                phaseIcon = "🌒"
                phaseName = when (activeLang) {
                    "ru" -> "Растущий Серп"
                    "es" -> "Luna Creciente"
                    else -> "Waxing Crescent"
                }
                recommendedAction = when (activeLang) {
                    "ru" -> "Посев семян и Проращивание"
                    "es" -> "Siembra de Semillas y Germinación"
                    else -> "Seed Sowing & Germination"
                }
                detailInstructions = when (activeLang) {
                    "ru" -> "Растущая гравитация Луны помогает поднимать влагу вверх. Идеальное время для проращивания регулярных или автоцветущих семян. Смочите ватные диски теплой водой с pH 6.2."
                    "es" -> "La gravedad lunar ascendente ayuda a elevar la humedad. Momento ideal para germinar semillas regulares o autoflorecientes. Humedezca servilletas con agua tibia a pH 6.2."
                    else -> "Rising moon gravity helps pull moisture upward. Ideal time for germinating regular or autoflower seeds. Moisten cotton pads or paper towels with warm pH 6.2 water."
                }
            }
            ageFactor >= 0.22 && ageFactor < 0.28 -> {
                phaseIcon = "🌓"
                phaseName = when (activeLang) {
                    "ru" -> "Первая Четверть"
                    "es" -> "Cuarto Creciente"
                    else -> "First Quarter"
                }
                recommendedAction = when (activeLang) {
                    "ru" -> "Клонирование и Вегетативное Питание"
                    "es" -> "Clonación y Nutrición Vegetativa"
                    else -> "Cloning & Veg Feeding"
                }
                detailInstructions = when (activeLang) {
                    "ru" -> "Высокий сокодвижение в вегетативных стеблях. Срежьте здоровые клоны с материнского растения, обмакните в гель для клонирования и поместите под цикл света 18/6."
                    "es" -> "Alto flujo de savia en tallos vegetativos. Corte clones sanos de la Planta Madre, sumérjalos en gel de enraizamiento y colóquelos bajo un ciclo de luz 18/6."
                    else -> "High sap flow in vegetative stems. Cut healthy clones from the Mother Plant, dip in organic cloning gel, and place under 18/6 vegetative light."
                }
            }
            ageFactor >= 0.28 && ageFactor < 0.47 -> {
                phaseIcon = "🌔"
                phaseName = when (activeLang) {
                    "ru" -> "Растущая Луна"
                    "es" -> "Luna Gibosa Creciente"
                    else -> "Waxing Gibbous"
                }
                recommendedAction = when (activeLang) {
                    "ru" -> "Тренировки LST и Подгибание Ветвей"
                    "es" -> "Entrenamiento LST y Doblado"
                    else -> "LST Training & Bending"
                }
                detailInstructions = when (activeLang) {
                    "ru" -> "Стебли очень гибкие и упругие. Отличная фаза для тренировок с низким стрессом (LST) и аккуратного подгибания верхушек (кол) для расширения световой кроны."
                    "es" -> "Los tallos son muy flexibles y resistentes. Fase perfecta para Entrenamiento de Bajo Estrés (LST) y doblar colas suavemente para maximizar la penetración de luz."
                    else -> "Stems are highly flexible and resilient. Perfect phase for Low Stress Training (LST) and bending colas to expand the canopy and increase light penetration."
                }
            }
            ageFactor >= 0.47 && ageFactor < 0.53 -> {
                phaseIcon = "🌕"
                phaseName = when (activeLang) {
                    "ru" -> "Полнолуние"
                    "es" -> "Luna Plena"
                    else -> "Full Moon"
                }
                recommendedAction = when (activeLang) {
                    "ru" -> "Насыщенный Полив и Подкормка"
                    "es" -> "Riego Abundante y Nutrición"
                    else -> "Watering & Rich Feeding"
                }
                detailInstructions = when (activeLang) {
                    "ru" -> "Максимальное поглощение воды и корневое давление. Дайте обильную подкормку органическими удобрениями, аминокислотами и углеводами (мелассой). Избегайте любой обрезки!"
                    "es" -> "Máxima absorción de agua y presión radicular. Dé una alimentación rica en nutrientes orgánicos, aminoácidos y carbohidratos (melaza). ¡Evite cualquier tipo de poda!"
                    else -> "Maximum water uptake and root pressure. Give a rich feeding of organic nutrients, amino acids, and plant molasses. Avoid any cutting, topping, or pruning today."
                }
            }
            ageFactor >= 0.53 && ageFactor < 0.72 -> {
                phaseIcon = "🌖"
                phaseName = when (activeLang) {
                    "ru" -> "Убывающая Луна"
                    "es" -> "Luna Gibosa Menguante"
                    else -> "Waning Gibbous"
                }
                recommendedAction = when (activeLang) {
                    "ru" -> "Дефолиация и Подрезка Листьев"
                    "es" -> "Defoliación y Poda de Hojas"
                    else -> "Defoliation & Leaf Pruning"
                }
                detailInstructions = when (activeLang) {
                    "ru" -> "Энергия смещается вниз к корням и формирующимся шишкам. Срежьте желтеющие веерные листья из нижней трети куста для улучшения обдува и циркуляции воздуха."
                    "es" -> "La energía se desplaza hacia abajo, a las raíces y flores en desarrollo. Podar las hojas de abanico del tercio inferior para mejorar el flujo de aire y luz."
                    else -> "Energy is shifting downwards to roots and developing buds. Prune yellowing fan leaves from the lower third of the canopy to improve airflow and direct power to buds."
                }
            }
            ageFactor >= 0.72 && ageFactor < 0.78 -> {
                phaseIcon = "🌗"
                phaseName = when (activeLang) {
                    "ru" -> "Третья Четверть"
                    "es" -> "Cuarto Menguante"
                    else -> "Third Quarter"
                }
                recommendedAction = when (activeLang) {
                    "ru" -> "Органическая Борьба с Вредителями"
                    "es" -> "Control de Plagas y Prevención"
                    else -> "Organic Pest Defense"
                }
                detailInstructions = when (activeLang) {
                    "ru" -> "Уменьшение влажности в верхних листьях. Идеальная фаза для профилактического опрыскивания органическим маслом нима для защиты от паутинных клещей и трипсов."
                    "es" -> "Disminución de humedad en hojas superiores. Fase excelente para rociar preventivamente con aceite de neem orgánico para defenderse de arañas rojas y trips."
                    else -> "Decreasing moisture in top leaves. Best phase for spraying neem oil organic solutions or beneficial microbes to defend against red spider mites and thrips."
                }
            }
            else -> {
                phaseIcon = "🌘"
                phaseName = when (activeLang) {
                    "ru" -> "Стареющий Серп"
                    "es" -> "Luna Menguante"
                    else -> "Waning Crescent"
                }
                recommendedAction = when (activeLang) {
                    "ru" -> "Промывка Субстрата и Сбор Урожая"
                    "es" -> "Lavado de Raíces y Cosecha"
                    else -> "Substrate Flushing & Harvest"
                }
                detailInstructions = when (activeLang) {
                    "ru" -> "Соки опускаются вниз. Идеальное время для промывки субстрата чистой водой для удаления солей, просушки корней и проведения финального сбора урожая в темноте."
                    "es" -> "La savia desciende al suelo. Momento ideal para lavar las raíces con agua pura, secar el sustrato y realizar la cosecha final en la oscuridad."
                    else -> "Sap flow goes down. Ideal time to flush your soil/coco with pure RO water to leach remaining salts, dry the roots, and perform the final harvest under dark hours."
                }
            }
        }

        // Astrological Zodiac cycle estimate (changes roughly every 2.46 days)
        val zodiacSigns = listOf(
            Triple("Cancer", "Рак", "Cáncer") to true,        // Fertile
            Triple("Leo", "Лев", "Leo") to false,             // Barren
            Triple("Virgo", "Дева", "Virgo") to false,         // Semi-fertile
            Triple("Libra", "Весы", "Libra") to true,         // Fertile
            Triple("Scorpio", "Скорпион", "Escorpio") to true,  // Fertile
            Triple("Sagittarius", "Стрелец", "Sagitario") to false, // Barren
            Triple("Capricorn", "Козерог", "Capricornio") to true, // Fertile
            Triple("Aquarius", "Водолей", "Acuario") to false, // Barren
            Triple("Pisces", "Рыбы", "Piscis") to true,       // Fertile
            Triple("Aries", "Овен", "Aries") to false,         // Barren
            Triple("Taurus", "Телец", "Tauro") to true,        // Fertile
            Triple("Gemini", "Близнецы", "Géminis") to false   // Barren
        )

        // Map epoch days to 29.5 days zodiac offset
        val dayIndex = (day + month * 30 + (year % 10) * 365) % 12
        val (zodiacNames, isFertile) = zodiacSigns[dayIndex]
        val astrologicalSign = when (activeLang) {
            "ru" -> zodiacNames.second
            "es" -> zodiacNames.third
            else -> zodiacNames.first
        }

        val calendar = Calendar.getInstance()
        calendar.set(year, month - 1, day)
        val dateString: String
        if (activeLang == "ru") {
            val monthsRu = listOf("янв", "фев", "мар", "апр", "май", "июн", "июл", "авг", "сен", "окт", "ноя", "дек")
            dateString = String.format(Locale.getDefault(), "%02d %s", day, monthsRu[(month - 1).coerceIn(0, 11)])
        } else if (activeLang == "es") {
            val monthsEs = listOf("ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "sep", "oct", "nov", "dic")
            dateString = String.format(Locale.getDefault(), "%02d %s", day, monthsEs[(month - 1).coerceIn(0, 11)])
        } else {
            dateString = String.format(Locale.getDefault(), "%02d %s", day, calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.US))
        }

        return LunarDayInfo(
            dayOfMonth = day,
            dateString = dateString,
            phaseName = phaseName,
            phaseIcon = phaseIcon,
            illumination = illumination,
            astrologicalSign = astrologicalSign,
            isFertile = isFertile,
            recommendedAction = recommendedAction,
            detailInstructions = detailInstructions
        )
    }

    fun getLunarCalendarForCurrentMonth(lang: String): List<LunarDayInfo> {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1 // 1-indexed
        val maxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        val list = mutableListOf<LunarDayInfo>()
        for (day in 1..maxDays) {
            list.add(getLunarDayInfo(year, month, day, lang))
        }
        return list
    }

    fun getTarotMintingWindow(year: Int, month: Int, day: Int, lang: String = "en"): TarotMintingWindow {
        val ageFactor = calculateLunarAgeFactor(year, month, day)
        val activeLang = if (lang in listOf("en", "ru", "es")) lang else "en"

        return when {
            // FULL MOON: PEAK ASTRAL CONVERGENCE (90% - 100% illumination)
            ageFactor in 0.44..0.56 -> {
                val title = when (activeLang) {
                    "ru" -> "ПИК АСТРАЛЬНОГО РЕЗОНАНСА (ПОЛНОЛУНИЕ)"
                    "es" -> "CONVERGENCIA ASTRAL MÁXIMA (LUNA LLENA)"
                    else -> "PEAK ASTRAL RESONANCE (FULL MOON)"
                }
                val summary = when (activeLang) {
                    "ru" -> "Максимальная лунная гравитация. Смарт-контракт генерирует энтропию повышенной чистоты. Шанс выпадения Epic/Legendary карт увеличен на 18%."
                    "es" -> "Máxima gravedad lunar. El contrato inteligente genera entropía de máxima pureza. Probabilidad aumentada en +18% para cartas Epic y Legendary."
                    else -> "Maximum lunar tidal force. Smart contract entropy seeds reach peak diversity. Dramatically heightened (+18%) probability of Epic and Legendary cards."
                }
                val advice = when (activeLang) {
                    "ru" -> "Идеальный момент для чеканки высших арканов (XVIII Луна, II Жрица) и выполнения алхимического слияния 4:1 без потерь."
                    "es" -> "Momento cumbre para acuñar arcanos mayores (XVIII La Luna, II La Sacerdotisa) y realizar fusiones alquímicas 4:1."
                    else -> "Prime window for minting Major Arcana nodes (XVIII The Moon, II The High Priestess) and zero-loss 4:1 Alchemy Fusions."
                }
                val badge = when (activeLang) {
                    "ru" -> "БЛАГОПРИЯТНО ДЛЯ ЧЕКАНКИ"
                    "es" -> "PROPICIO PARA ACUÑAR"
                    else -> "AUSPICIOUS MINT WINDOW"
                }
                TarotMintingWindow(
                    tier = MintingAuspiciousnessTier.AUSPICIOUS,
                    title = title,
                    summary = summary,
                    dropRateModifierPercent = 18,
                    gasFeeDiscountPercent = 15,
                    favoredElements = listOf("QUANTUM", "BIO", "CELESTIAL"),
                    patronArcanaIndices = listOf(18, 2, 19),
                    ritualAdvice = advice,
                    astralConductivity = 0.98f,
                    badgeLabel = badge
                )
            }
            // WAXING GIBBOUS: ASCENDING ENERGY
            ageFactor in 0.28..0.44 -> {
                val title = when (activeLang) {
                    "ru" -> "ВОСХОДЯЩИЙ ИМПУЛЬС (РАСТУЩАЯ ЛУНА)"
                    "es" -> "IMPULSO ASCENDENTE (LUNA GIBOSA)"
                    else -> "ASCENDING TIDAL PULSE (WAXING GIBBOUS)"
                }
                val summary = when (activeLang) {
                    "ru" -> "Растущий потенциал усиливает базовую силу (ATK) и щиты чеканимых карт. Благоприятное время для колодостроения."
                    "es" -> "El potencial ascendente aumenta el ATK y la defensa de las cartas acuñadas. Momento muy favorable para la forja."
                    else -> "Ascending tide bolsters base ATK and Shield metrics on newly minted cards. High synergy for battle decks."
                }
                val advice = when (activeLang) {
                    "ru" -> "Рекомендуется чеканка карт стихий BIO и CYBER. Сниженная комиссия сети \$WEED (-10%)."
                    "es" -> "Se recomienda acuñar cartas con afinidades BIO y CYBER. Tarifas reducidas en \$WEED (-10%)."
                    else -> "Recommended for minting BIO and CYBER affinity nodes with discounted (-10%) \$WEED network gas."
                }
                val badge = when (activeLang) {
                    "ru" -> "БЛАГОПРИЯТНЫЙ ЦИКЛ"
                    "es" -> "CICLO PROPICIO"
                    else -> "AUSPICIOUS RISING"
                }
                TarotMintingWindow(
                    tier = MintingAuspiciousnessTier.AUSPICIOUS,
                    title = title,
                    summary = summary,
                    dropRateModifierPercent = 10,
                    gasFeeDiscountPercent = 10,
                    favoredElements = listOf("BIO", "CYBER"),
                    patronArcanaIndices = listOf(1, 3, 14),
                    ritualAdvice = advice,
                    astralConductivity = 0.82f,
                    badgeLabel = badge
                )
            }
            // NEW MOON / DARK MOON: VOID STATIC & ECLIPSE FLUX
            ageFactor < 0.05 || ageFactor > 0.95 -> {
                val title = when (activeLang) {
                    "ru" -> "ШУМ ПУСТОТЫ И ЗАТМЕНИЕ (НОВОЛУНИЕ)"
                    "es" -> "ESTÁTICA DEL VACÍO (LUNA NUEVA)"
                    else -> "VOID STATIC ECLIPSE (NEW MOON)"
                }
                val summary = when (activeLang) {
                    "ru" -> "Астральное затмение. Стандартные арканы испытывают помехи. Однако темные арканы Пустоты получают 2.5x бонус к вероятности."
                    "es" -> "Ocultación astral. Los arcanos comunes sufren interferencias, pero las cartas del Vacío obtienen una tasa 2.5x."
                    else -> "Total astral occultation. High noise in quantum links for standard cards, but Void/Shadow arcana gain a 2.5x singularity boost."
                }
                val advice = when (activeLang) {
                    "ru" -> "Осторожно при обычной чеканке. Если охотитесь за XIII Смерть или XVI Башня — идеальное окно для вызова темной сингулярности."
                    "es" -> "Precaución con la forja estándar. Momento óptimo para invocar arcanos oscuros como XIII La Muerte o XVI La Torre."
                    else -> "Volatile for ordinary cards. Ideal occult threshold if hunting for dark singularity nodes (XIII Death, XVI The Tower)."
                }
                val badge = when (activeLang) {
                    "ru" -> "ОПАСНОСТЬ: ШУМ ПУСТОТЫ"
                    "es" -> "ALERTA: FLUJO DE VACÍO"
                    else -> "CHALLENGING: VOID FLUX"
                }
                TarotMintingWindow(
                    tier = MintingAuspiciousnessTier.CHALLENGING,
                    title = title,
                    summary = summary,
                    dropRateModifierPercent = -10,
                    gasFeeDiscountPercent = -20, // surcharge / congestion
                    favoredElements = listOf("VOID", "SHADOW"),
                    patronArcanaIndices = listOf(13, 15, 16),
                    ritualAdvice = advice,
                    astralConductivity = 0.22f,
                    badgeLabel = badge
                )
            }
            // WANING CRESCENT: ENTROPY & HARVEST
            ageFactor in 0.75..0.95 -> {
                val title = when (activeLang) {
                    "ru" -> "ФАЗА ЭНТРОПИИ (СТАРЕЮЩИЙ СЕРП)"
                    "es" -> "FASE DE ENTROPÍA (LUNA MENGUANTE)"
                    else -> "ENTROPY RECESSION (WANING CRESCENT)"
                }
                val summary = when (activeLang) {
                    "ru" -> "Энергетический спад. Повышена вероятность выпадения Common карт. Лучше копить \$WEED или торговать на DEX маркете."
                    "es" -> "Disminución de energía astral. Mayor probabilidad de cartas Common. Recomendado ahorrar \$WEED o comerciar."
                    else -> "Receding astral resonance. Common tier pull rates are heightened. Best to accumulate \$WEED or trade on the DEX."
                }
                val advice = when (activeLang) {
                    "ru" -> "Отложите крупную чеканку до растущей Луны. Проведите ревизию холодного хранилища или настройте боевую колоду."
                    "es" -> "Posponga la forja de alto nivel hasta la luna creciente. Revise su bóveda fría o ajuste su mazo de batalla."
                    else -> "Postpone high-tier mints until the next Waxing Moon. Reorganize your Vault or list cards on the DEX AMM."
                }
                val badge = when (activeLang) {
                    "ru" -> "СЛОЖНЫЙ ПЕРИОД: ЭНТРОПИЯ"
                    "es" -> "PERÍODO DIFÍCIL: ENTROPÍA"
                    else -> "CHALLENGING: LOW RESONANCE"
                }
                TarotMintingWindow(
                    tier = MintingAuspiciousnessTier.CHALLENGING,
                    title = title,
                    summary = summary,
                    dropRateModifierPercent = -5,
                    gasFeeDiscountPercent = -10,
                    favoredElements = listOf("CYBER", "VOID"),
                    patronArcanaIndices = listOf(9, 12, 20),
                    ritualAdvice = advice,
                    astralConductivity = 0.38f,
                    badgeLabel = badge
                )
            }
            // BALANCED / NEUTRAL CYCLES (First Quarter, Waning Gibbous, Waxing Crescent)
            else -> {
                val title = when (activeLang) {
                    "ru" -> "БАЛАНСИРОВАННЫЙ ЭФИР"
                    "es" -> "ÉTER EQUILIBRADO"
                    else -> "BALANCED HARMONIC ETHER"
                }
                val summary = when (activeLang) {
                    "ru" -> "Стабильная сеть блокчейна. Равномерное распределение шансов редкости и нейтральный уровень комиссий."
                    "es" -> "Red blockchain estable. Distribución equilibrada de probabilidades y tarifas de gas estables."
                    else -> "Stable network hash rate and balanced astral resonance. Standard baseline probability across all rarity tiers."
                }
                val advice = when (activeLang) {
                    "ru" -> "Подходит для сбалансированной чеканки базовых карт и участия в стандартных дуэлях на P2P арене."
                    "es" -> "Adecuado para la forja equilibrada de cartas base y duelos en la arena P2P."
                    else -> "Reliable window for consistent deck minting and casual testing in P2P arena duels."
                }
                val badge = when (activeLang) {
                    "ru" -> "НЕЙТРАЛЬНЫЙ БАЛАНС"
                    "es" -> "BALANCE NEUTRAL"
                    else -> "BALANCED BASELINE"
                }
                TarotMintingWindow(
                    tier = MintingAuspiciousnessTier.BALANCED,
                    title = title,
                    summary = summary,
                    dropRateModifierPercent = 0,
                    gasFeeDiscountPercent = 0,
                    favoredElements = listOf("CYBER", "QUANTUM"),
                    patronArcanaIndices = listOf(4, 7, 10),
                    ritualAdvice = advice,
                    astralConductivity = 0.60f,
                    badgeLabel = badge
                )
            }
        }
    }

    fun getTarotMintingForecast(daysAhead: Int = 14, lang: String = "en"): List<TarotMintingDayForecast> {
        val list = mutableListOf<TarotMintingDayForecast>()
        for (i in 0 until daysAhead) {
            val futureCal = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, i)
            }
            val y = futureCal.get(Calendar.YEAR)
            val m = futureCal.get(Calendar.MONTH) + 1
            val d = futureCal.get(Calendar.DAY_OF_MONTH)

            val lunarDay = getLunarDayInfo(y, m, d, lang)
            val mintingWindow = getTarotMintingWindow(y, m, d, lang)
            val ageFactor = calculateLunarAgeFactor(y, m, d)

            list.add(
                TarotMintingDayForecast(
                    dayOfMonth = d,
                    dateString = lunarDay.dateString,
                    ageFactor = ageFactor,
                    illumination = lunarDay.illumination,
                    phaseIcon = lunarDay.phaseIcon,
                    phaseName = lunarDay.phaseName,
                    astrologicalSign = lunarDay.astrologicalSign,
                    mintingWindow = mintingWindow
                )
            )
        }
        return list
    }
}
