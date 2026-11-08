package com.example.ui.theme

import androidx.compose.ui.graphics.Color
import com.example.R

enum class CyberSkin(
    val id: String,
    val displayName: String,
    val backgroundResId: Int,
    val primaryColor: Color,
    val secondaryColor: Color,
    val ambientGlowColor: Color,
    val description: String
) {
    ORACLE_TEMPLE(
        "oracle_temple",
        "Oracle Temple",
        R.drawable.img_bg_oracle_temple,
        Color(0xFF00FF88), // Glowing Sacred Emerald
        Color(0xFFFFD700), // Royal Sacred Gold
        Color(0x6600FF88), // Bioluminescent Dome Glow
        "Monumental bioluminescent cannabis oracle sanctuary with sacred holographic geometry."
    ),
    LUNAR_GROWER(
        "lunar_grower",
        "Lunar Grower",
        R.drawable.img_bg_lunar_calendar,
        Color(0xFFFFD700), // Celestial Moon Gold
        Color(0xFF00FF88), // Hydroponic Emerald
        Color(0x66FFD700), // Cosmic Moon Glow
        "Futuristic cybernetic celestial observatory tracking lunar grower phases and zodiac tides."
    ),
    CYBER_ALTAR(
        "cyber_altar",
        "Cyber Altar",
        R.drawable.img_bg_tarot_altar,
        Color(0xFF00F0FF), // Electric Cyber Cyan
        Color(0xFFFF007F), // Neon Magenta
        Color(0x6600F0FF), // Levitation Field Glow
        "Mystic cybernetic tarot altar with floating holographic cards and glowing Metatron runes."
    ),
    SYNTH_LAB(
        "synth_lab",
        "Synth Lab",
        R.drawable.img_bg_synth_lab,
        Color(0xFF00FFA3), // Soundwave Mint
        Color(0xFFFF00E5), // Synthwave Magenta
        Color(0x6600FFA3), // Crystal Soundboard Glow
        "High-tech organic sound synthesizer laboratory generating healing botanical frequencies."
    ),
    JAMAICA(
        "jamaica",
        "Jamaica",
        R.drawable.img_skin_jamaica_1784511085781,
        Color(0xFF39FF14), // Hyper Neon Lime Green
        Color(0xFFFF007F), // Vivid Magenta
        Color(0x5539FF14), // Green Energy Glow
        "Sunny rasta frequencies in a neon cyberpunk magic fantasy forest."
    ),
    MOROCCO(
        "morocco",
        "Morocco",
        R.drawable.img_cyber_alchemy_lab_1783983282176,
        Color(0xFFFFEE00), // Glowing Sun Gold
        Color(0xFFFF5500), // Saturated Cyber Amber
        Color(0x55FFEE00), // Golden Rune Glow
        "Ancient Moroccan alchemy meets a futuristic neon-cyber magic lab."
    ),
    BARCELONA(
        "barcelona",
        "Barcelona",
        R.drawable.img_cyber_tarot_witch_1783983295253,
        Color(0xFFE000FF), // Deep Cyber Magenta
        Color(0xFF00FFFF), // Radiant Electric Cyan
        Color(0x55E000FF), // Neon Witch Glow
        "Cyberpunk social tarot-witch reading. Saturated purple, magenta, and cyan lights."
    ),
    AMSTERDAM(
        "amsterdam",
        "Amsterdam",
        R.drawable.img_skin_amsterdam_1784511096713,
        Color(0xFFFF5500), // Vibrant Dutch Orange
        Color(0xFF00FF66), // Glowing Emerald
        Color(0x55FF5500), // Cyber Orange Glow
        "Digital neon greenhouses and flowing waterways of a fantasy cyber-capital."
    ),
    COFFESHOP(
        "coffeeshop",
        "Coffeeshop",
        R.drawable.img_skin_coffeeshop_1784511108346,
        Color(0xFFFFB900), // Bright Caramel Amber
        Color(0xFF00FF88), // Radioactive Mint Green
        Color(0x55FFB900), // Alchemist Fire Glow
        "A high-energy digital dispensary with glowing potions and emerald flora."
    ),
    BUDS(
        "buds",
        "Buds",
        R.drawable.img_skin_buds_1784511118877,
        Color(0xFF00FFA3), // Radioactive Mint Green
        Color(0xFFFF007F), // Neon Pink
        Color(0x5500FFA3), // Frosty Trichome Glow
        "Vivid fantasy buds sparkling under high-intensity ultraviolet synthwave lasers."
    ),
    GAUDI_VANGOGH(
        "gaudi_vangogh",
        "Gaudí & Van Gogh",
        R.drawable.img_cyber_gaudi_vangogh,
        Color(0xFF00F5FF), // Cyber Cyan Neon
        Color(0xFFFFB800), // Starry Gold Vortex
        Color(0x5500F5FF), // Luminescent Botanical Cyan Glow
        "Monumental cyber-cathedral with Gaudi curves, Van Gogh starry vortexes, and luminescent botanical vines."
    );

    companion object {
        fun fromId(id: String): CyberSkin = values().find { it.id == id } ?: ORACLE_TEMPLE
    }
}

fun CyberSkin.getLocalizedName(lang: String): String {
    return when (this) {
        CyberSkin.ORACLE_TEMPLE -> when (lang) {
            "ru", "uk" -> "Храм Оракула"
            "es" -> "Templo del Oráculo"
            else -> "Oracle Temple"
        }
        CyberSkin.LUNAR_GROWER -> when (lang) {
            "ru", "uk" -> "Лунный Гровер"
            "es" -> "Cultivador Lunar"
            else -> "Lunar Grower"
        }
        CyberSkin.CYBER_ALTAR -> when (lang) {
            "ru", "uk" -> "Кибер-Алтарь"
            "es" -> "Altar Cibernético"
            else -> "Cyber Altar"
        }
        CyberSkin.SYNTH_LAB -> when (lang) {
            "ru", "uk" -> "Синт-Лаборатория"
            "es" -> "Laboratorio Sintetizador"
            else -> "Synth Lab"
        }
        CyberSkin.JAMAICA -> when (lang) {
            "ru", "uk" -> "Ямайка"
            "es" -> "Jamaica"
            else -> "Jamaica"
        }
        CyberSkin.MOROCCO -> when (lang) {
            "ru", "uk" -> "Марокко"
            "es" -> "Marruecos"
            else -> "Morocco"
        }
        CyberSkin.BARCELONA -> when (lang) {
            "ru", "uk" -> "Барселона"
            "es" -> "Barcelona"
            else -> "Barcelona"
        }
        CyberSkin.AMSTERDAM -> when (lang) {
            "ru", "uk" -> "Амстердам"
            "es" -> "Ámsterdam"
            else -> "Amsterdam"
        }
        CyberSkin.COFFESHOP -> when (lang) {
            "ru", "uk" -> "Кофешоп"
            "es" -> "Coffeeshop"
            else -> "Coffeeshop"
        }
        CyberSkin.BUDS -> when (lang) {
            "ru", "uk" -> "Шишки"
            "es" -> "Cogollos"
            else -> "Buds"
        }
        CyberSkin.GAUDI_VANGOGH -> when (lang) {
            "ru", "uk" -> "Гауди & Ван Гог"
            "es" -> "Gaudí y Van Gogh"
            else -> "Gaudí & Van Gogh"
        }
    }
}

fun CyberSkin.getLocalizedDescription(lang: String): String {
    return when (this) {
        CyberSkin.ORACLE_TEMPLE -> when (lang) {
            "ru", "uk" -> "Величественный биолюминесцентный храм-оракул с куполом сакральной геометрии и голограммами."
            "es" -> "Santuario monumental de oráculo con cúpula bioluminiscente y geometría sagrada."
            else -> "Monumental bioluminescent oracle sanctuary with sacred holographic dome."
        }
        CyberSkin.LUNAR_GROWER -> when (lang) {
            "ru", "uk" -> "Высокотехнологичная обсерватория с гигантской золотой луной и контролем фаз роста растений."
            "es" -> "Observatorio cibernético con luna dorada gigante para el seguimiento del cultivo."
            else -> "High-tech cybernetic observatory with a giant golden moon tracking grower cycles."
        }
        CyberSkin.CYBER_ALTAR -> when (lang) {
            "ru", "uk" -> "Мистический кибер-алтарь с парящими голографическими картами Таро и неоновыми рунами Метатрона."
            "es" -> "Altar místico con cartas de tarot holográficas levitando y runas de neón."
            else -> "Mystic cyber altar with floating holographic tarot cards and glowing neon runes."
        }
        CyberSkin.SYNTH_LAB -> when (lang) {
            "ru", "uk" -> "Лаборатория органического синтезатора, генерирующая исцеляющие растительные вибрации."
            "es" -> "Laboratorio de sintetizador orgánico que genera frecuencias botánicas curativas."
            else -> "Organic synthesizer laboratory generating harmonizing botanical audio frequencies."
        }
        CyberSkin.JAMAICA -> when (lang) {
            "ru", "uk" -> "Солнечные вибрации регги. Красно-желто-зеленая раста палитра с золотым свечением."
            "es" -> "Vibraciones soleadas de reggae. Paleta rasta roja, amarilla y verde con brillo dorado."
            else -> "Sunny reggae vibrations. Red, yellow, and green rasta palette with a golden glow."
        }
        CyberSkin.MOROCCO -> when (lang) {
            "ru", "uk" -> "Теплый пряный янтарь и традиционные восточные мотивы в неоновом исполнении."
            "es" -> "Ámbar cálido y especiado con motivos orientales en neón."
            else -> "Warm, spicy amber and traditional resin vibes in futuristic neon."
        }
        CyberSkin.BARCELONA -> when (lang) {
            "ru", "uk" -> "Стильный клубный неон ассоциации гров-клубов. Фиолетовый и бирюзовый оттенки."
            "es" -> "Estilo de club social de Barcelona. Tonos morados y turquesas con vibraciones de club."
            else -> "Stylish social grow-club neon. Purple and turquoise tones with premium club vibes."
        }
        CyberSkin.AMSTERDAM -> when (lang) {
            "ru", "uk" -> "Каналы и ночные огни Северной Венеции. Глубокий синий и яркий оранжевый неон."
            "es" -> "Canales y luces nocturnas de la capital. Azul profundo y naranja vibrante."
            else -> "Canals and night lights of the capital. Deep canal blue and vibrant orange neon."
        }
        CyberSkin.COFFESHOP -> when (lang) {
            "ru", "uk" -> "Уютная атмосфера заведения с запахом свежего кофе. Шоколадный, карамельный и зеленый тона."
            "es" -> "Atmósfera acogedora con aroma a café fresco. Tonos chocolate, caramelo y verde hierba."
            else -> "Cozy cafe atmosphere with fresh herbal aromas. Chocolate, caramel, and herbal green tones."
        }
        CyberSkin.BUDS -> when (lang) {
            "ru", "uk" -> "Густые смолистые шишки, покрытые трихомами. Интенсивный липкий зеленый и салатовый неон."
            "es" -> "Flores densas y resinosas cubiertas de tricomas. Verde pegajoso intenso y brillo de resina."
            else -> "Dense resinous buds covered in frosty trichomes. Sticky green colors with lime-green glow."
        }
        CyberSkin.GAUDI_VANGOGH -> when (lang) {
            "ru", "uk" -> "Кибер-собор в органических формах Антонио Гауди под вихревым звездным небом Ван Гога."
            "es" -> "Catedral cibernética con formas orgánicas de Gaudí bajo la noche estrellada de Van Gogh."
            else -> "Monumental cyber-cathedral with Gaudí curves under Van Gogh's starry night."
        }
    }
}
