package com.example.data

import androidx.compose.runtime.Immutable

@Immutable
data class CyberTarotCard(
    val index: Int,
    val name: String,
    val traditionalName: String,
    val description: String,
    val uprightKeywords: List<String>,
    val reversedKeywords: List<String>,
    val circuitryElement: String,
    val cyberMeaning: String,
    val uprightAdvice: String,
    val reversedAdvice: String,
    val defaultRarity: String, // "Common", "Rare", "Epic", "Legend", "Genesis"
    val series: String = "Cyber Botanica"
) {
    val rarityColorHex: String
        get() = when (defaultRarity) {
            "Genesis" -> "#FFD700" // Bright Genesis Gold
            "Legend" -> "#FFA500"  // Neon Gold/Orange
            "Epic" -> "#FF00FF"    // Neon Magenta/Purple
            "Rare" -> "#00FFFF"    // Neon Cyan
            else -> "#39FF14"      // Neon Green (Common)
        }
}

object CyberTarotDeck {
    val cards = listOf(
        CyberTarotCard(
            index = 0,
            name = "The Sprout",
            traditionalName = "The Fool",
            description = "A tiny cotyledon leaf breaking through moist organic soil into the unknown. A novice grower planting their very first seed with absolute hope and zero experience.",
            uprightKeywords = listOf("New Seedling", "Fresh Start", "Pure Potential", "Beginner Grow"),
            reversedKeywords = listOf("Overwatering", "Damping Off", "Neglect", "Bad Genetics"),
            circuitryElement = "Rockwool Plugs / Rapid Rooter Starters",
            cyberMeaning = "A fresh grow cycle, a new strain experiment, or starting from seed. Embracing the journey without fear of mistakes.",
            uprightAdvice = "Plant the seed, maintain high humidity, and let nature take its course. Don't over-analyze the soil.",
            reversedAdvice = "Watch out for seedling rot and overwatering. Let the growing medium dry out slightly before watering again.",
            defaultRarity = "Common"
        ),
        CyberTarotCard(
            index = 1,
            name = "The Budtender",
            traditionalName = "The Magician",
            description = "A master herbalist surrounded by dual vaporizers, precision scales, amber jars, and glass grinders, mixing custom terpene profiles to manifest positive vibrations.",
            uprightKeywords = listOf("Terpene Mastery", "Resourcefulness", "Curing Secrets", "Perfect Synergy"),
            reversedKeywords = listOf("Adulterated Herbs", "Poor Advice", "Unskilled Trimming", "Wasted Bud"),
            circuitryElement = "Glass Herb Grinder & Vaporizer Coils",
            cyberMeaning = "Mastery of the four pillars of cultivation: Genetics, Environment, Nutrients, and Curing. Creating craft flower profiles with skill.",
            uprightAdvice = "You have all the organic amendments and grow equipment you need. Run your setup and materialize your vision.",
            reversedAdvice = "Avoid cheap synthetic nutrients and unverified booster formulas. Stick to pure horticultural fundamentals.",
            defaultRarity = "Rare"
        ),
        CyberTarotCard(
            index = 2,
            name = "The Hash Queen",
            traditionalName = "The High Priestess",
            description = "A serene herbal priestess sitting behind a curtain of icy, frozen bubble bags, holding a golden block of solventless ice water hash.",
            uprightKeywords = listOf("Ice Water Wash", "Subtle Terpenes", "Intuitive Cure", "Silent Frost"),
            reversedKeywords = listOf("Chlorophyll Leak", "Moldy Buds", "Ignored Warnings", "Harsh Smoke"),
            circuitryElement = "Bubble Bags / 73-Micron Mesh Screens",
            cyberMeaning = "Deep, quiet, solventless extraction. Trusting the tactile feel of freezing trichomes and cold temperatures rather than surface metrics.",
            uprightAdvice = "Keep your extraction secrets to yourself for now. Let the resin heads cure slowly in cold storage.",
            reversedAdvice = "High moisture levels pose a risk of bud rot. Check the center of your densest colas immediately.",
            defaultRarity = "Epic"
        ),
        CyberTarotCard(
            index = 3,
            name = "Mother Plant",
            traditionalName = "The Empress",
            description = "A majestic, heavily branched female cannabis mother plant in a climate-controlled room, surrounded by dozens of healthy, thriving clones.",
            uprightKeywords = listOf("Cloning Abundance", "Flourishing Veg", "Vigorous Growth", "Rich Harvest"),
            reversedKeywords = listOf("Nutrient Lockout", "Root Rot", "Stunted Cuttings", "Forgotten Mother"),
            circuitryElement = "Aeroponic Cloning Manifolds & Rooting Gel",
            cyberMeaning = "Creation, botanical abundance, healthy rooting, and endless vegetative vigor. Propagating prime genetics.",
            uprightAdvice = "Take healthy cuttings. Your vegetative setup is in peak health and will produce heavy canopies.",
            reversedAdvice = "Check for root rot and stagnant water. Give your root zones plenty of oxygen and clean the reservoir.",
            defaultRarity = "Epic"
        ),
        CyberTarotCard(
            index = 4,
            name = "The Grow Master",
            traditionalName = "The Emperor",
            description = "An experienced grower sitting on a throne of soil bags, overlooking a massive commercial greenroom with automated carbon filters humming softly.",
            uprightKeywords = listOf("Climate Control", "Strict VPD", "Grow Room Discipline", "Genetic Selection"),
            reversedKeywords = listOf("Light Leaks", "Power Outage", "Tyrannical Overfeeding", "System Chaos"),
            circuitryElement = "Inline Exhaust Fans & Charcoal Carbon Filters",
            cyberMeaning = "Strict light scheduling, precise Vapor Pressure Deficit (VPD) regulation, and clean room rules. Defensive measures against pests.",
            uprightAdvice = "Enforce strict 12/12 light schedules and maintain a clean environment. Establish control over heat and humidity.",
            reversedAdvice = "A light leak during the dark cycle could cause herming. Check your grow tent seams and zip up securely.",
            defaultRarity = "Rare"
        ),
        CyberTarotCard(
            index = 5,
            name = "The Old-School Grower",
            traditionalName = "The Hierophant",
            description = "An elder grower teaching apprentices the ancient methods of organic living soil, composting, and companion planting with marigolds.",
            uprightKeywords = listOf("Living Soil", "Compost Teas", "Traditional Wisdom", "Organic Heritage"),
            reversedKeywords = listOf("Synthetic Overdose", "Burnt Leaves", "Dogmatic Mistakes", "Pest Attraction"),
            circuitryElement = "Mycorrhizae Fungi & Living Soil Microbes",
            cyberMeaning = "Adherence to organic cultivation methods, natural microbial life, and respecting the biodiversity of the soil food web.",
            uprightAdvice = "Feed the soil, not the plant. Rely on living microbes and mycorrhizae instead of heavy mineral bottle feeds.",
            reversedAdvice = "Your soil has become sterile from chemical salts. Flush the medium and inoculate with fresh compost tea.",
            defaultRarity = "Rare"
        ),
        CyberTarotCard(
            index = 6,
            name = "Terpene Symbiosis",
            traditionalName = "The Lovers",
            description = "Two distinct cannabis strains, Sativa and Indica, growing side-by-side in a hybrid system, intertwining their branches under a single light.",
            uprightKeywords = listOf("Perfect Hybrid", "Synergistic Terpenes", "Genetic Blend", "Balanced Canopy"),
            reversedKeywords = listOf("Genetic Incompatibility", "Crowded Canopy", "Shaded Buds", "Strain Competition"),
            circuitryElement = "Full-Spectrum LED Quantum Board Drivers",
            cyberMeaning = "Harmonious balancing of contrasting elements. Selecting the perfect hybrid ratio to stimulate both mind and body.",
            uprightAdvice = "A balanced hybrid approach is best. Align your Sativa growth with Indica canopy levels for a uniform yield.",
            reversedAdvice = "One vigorous strain is choking out the other. Prune the aggressive Sativa branches to let light reach the Indica buds.",
            defaultRarity = "Common"
        ),
        CyberTarotCard(
            index = 7,
            name = "Autoflower Rush",
            traditionalName = "The Chariot",
            description = "A rapid auto-flowering seedling shooting through vegetative stage and exploding into flower, fueled by 20 hours of continuous light.",
            uprightKeywords = listOf("Rapid Veg", "Explosive Flowering", "Fast Harvest", "High Momentum"),
            reversedKeywords = listOf("Stunted Growth", "Nutrient Shock", "Delayed Cycle", "Missed Window"),
            circuitryElement = "Automated Drip Irrigation Feed Lines",
            cyberMeaning = "Unmatched growing speed, short cycles, and pushing light limits to achieve a rapid, successful harvest.",
            uprightAdvice = "Keep the light cycles long and the nutrition light. Maintain high momentum as auto-flowering plants wait for no one.",
            reversedAdvice = "Stress during the early days of an autoflower will permanently stunt its size. Minimize training and transplanting.",
            defaultRarity = "Rare"
        ),
        CyberTarotCard(
            index = 8,
            name = "Super Cropping",
            traditionalName = "Strength",
            description = "A skilled hand gently pinching and bending a stiff cannabis branch without breaking the outer skin, creating a strong knuckle to boost yields.",
            uprightKeywords = listOf("High-Stress Training", "Strong Knuckles", "Gentle Power", "Resilience"),
            reversedKeywords = listOf("Snapped Branch", "Plant Shock", "Too Much Force", "Stunted Veg"),
            circuitryElement = "Silica Root Supplements & Grafting Tape",
            cyberMeaning = "Overcoming rigid plant growth with deliberate, gentle training. Optimizing canopy structure through patience and physical resilience.",
            uprightAdvice = "Apply controlled pressure. Bend the tall colas to match the canopy; the plant's internal cell wall silica will heal them.",
            reversedAdvice = "You have snapped a major branch! Use grafting tape immediately to seal the wound and support the limb.",
            defaultRarity = "Common"
        ),
        CyberTarotCard(
            index = 9,
            name = "The Curing Room",
            traditionalName = "The Hermit",
            description = "A grower standing in a cool, pitch-black room, surrounded by hanging cannabis branches drying slowly at 60 degrees and 60% humidity.",
            uprightKeywords = listOf("Slow Dry", "Terpene Preservation", "Darkness", "Patience"),
            reversedKeywords = listOf("Flash Dried", "Hay Smell", "Over-Dried Buds", "Impatience"),
            circuitryElement = "Dehumidifier Units & Oscillating Fans",
            cyberMeaning = "Disconnecting from the rush of harvest. Hanging in darkness to allow starches and chlorophyll to break down into smooth smoke.",
            uprightAdvice = "Turn off the lights. Let your crop dry slowly in darkness for at least 10 days to preserve the delicate monoterpenes.",
            reversedAdvice = "You have dried your buds too fast, locking in the harsh green chlorophyll. Use humidity packs to slowly restore cure.",
            defaultRarity = "Common"
        ),
        CyberTarotCard(
            index = 10,
            name = "Phenotype Hunt",
            traditionalName = "Wheel of Fortune",
            description = "A row of 20 young cannabis seedlings germinated from regular seeds, showcasing a beautiful variance of leaf structures and terpene aromas.",
            uprightKeywords = listOf("Keeper Pheno", "Genetic Jackpot", "Lucky Seed", "Horticultural Fortune"),
            reversedKeywords = listOf("Hermaphrodite", "Male Plants", "Unstable F1s", "Weak Phenotype"),
            circuitryElement = "Regular Breeding Seeds & Strain Clones",
            cyberMeaning = "The natural variance of plant genetics. Spotting a rare purple-leaf phenotype or finding an elite mother cut.",
            uprightAdvice = "Keep close notes on every seed. A legendary mother plant is hidden in this batch; clone every single node before flowering.",
            reversedAdvice = "A pollen sack has opened on a hermie plant! Inspect the lower nodes immediately to prevent seeding the entire tent.",
            defaultRarity = "Rare"
        ),
        CyberTarotCard(
            index = 11,
            name = "Organic Balance",
            traditionalName = "Justice",
            description = "A digital pH pen showing a perfect 6.3 reading inside an organic living soil bed, indicating perfect microbial balance.",
            uprightKeywords = listOf("Optimal Soil pH", "Microbe Balance", "Perfect Feed", "Buffer Harmony"),
            reversedKeywords = listOf("pH Swings", "Nutrient Burn", "Toxic Soil", "Salt Build-up"),
            circuitryElement = "Digital pH & EC Waterproof Pens",
            cyberMeaning = "Immutable soil metrics and nutrient absorption laws. Achieving perfect soil chemistry through natural compost buffering.",
            uprightAdvice = "Keep your water inputs around pH 6.2 - 6.5. Let the living soil microbes buffer the nutrients naturally.",
            reversedAdvice = "Your soil's pH is drifting out of range, causing nutrient lockout. Flush with clean, de-chlorinated water.",
            defaultRarity = "Common"
        ),
        CyberTarotCard(
            index = 12,
            name = "The Flushing Stage",
            traditionalName = "The Hanged Man",
            description = "A dense, mature bud plant suspended in an alternate dark state, receiving only pure demineralized water to flush out nutrient salts.",
            uprightKeywords = listOf("Pure Water Flush", "Clean White Ash", "Yellowing Leaves", "Purging Nutrients"),
            reversedKeywords = listOf("Harsh Minerals", "Black Ash Smoke", "Salty Buds", "Premature Chop"),
            circuitryElement = "Reverse Osmosis (RO) Filtration Systems",
            cyberMeaning = "Intentionally starving the plant of external nutrition during its final week to force it to consume internal chlorophyll stores.",
            uprightAdvice = "Stop feeding. Give your roots nothing but pure water; let the large fan leaves turn yellow as they surrender energy.",
            reversedAdvice = "Your flowers are holding too much residual nitrogen, leading to a harsh taste. Extend the clean-water flush by 5 days.",
            defaultRarity = "Common"
        ),
        CyberTarotCard(
            index = 13,
            name = "The Harvest",
            traditionalName = "Death",
            description = "A clean pruning shear cutting through the thick stalk of a fully mature, milky-trichomed cannabis plant, signifying the end of a beautiful cycle.",
            uprightKeywords = listOf("Chop Day", "Cycle Completion", "Trimming Canopy", "Fresh Beginnings"),
            reversedKeywords = listOf("Rotten Harvest", "Premature Chopping", "Ruined Resin", "Delayed Chopping"),
            circuitryElement = "Stainless Steel Trim Shears & Trays",
            cyberMeaning = "The final termination of plant growth. Cutting away obsolete vegetative material and prep-work for the dry room.",
            uprightAdvice = "Chop when the trichomes are 15% amber. Do not hesitate to prune away old leaves to let the buds dry clean.",
            reversedAdvice = "You are harvesting too early out of anxiety! Wait for the pistils to recede and the resin heads to turn milky.",
            defaultRarity = "Rare"
        ),
        CyberTarotCard(
            index = 14,
            name = "Climate Controller",
            traditionalName = "Temperance",
            description = "An automated environmental monitor regulating fresh air intake and exhaust fans, keeping the humidity sweet and stable.",
            uprightKeywords = listOf("Stable VPD", "Perfect Airflow", "No Stagnation", "Happy Leaves"),
            reversedKeywords = listOf("High Humidity", "Bud Rot Threat", "Stagnant Hotspots", "Wind Burn"),
            circuitryElement = "VPD Temperature & Humidity Sensors",
            cyberMeaning = "Moderation of heat, balanced airflow, sweet temperature drops at night, and preventing high-humidity hazards.",
            uprightAdvice = "Distribute your airflow evenly. Place small clip fans below the canopy to prevent stagnant humid hotspots.",
            reversedAdvice = "Your humidity levels are spiking dangerously in the dark hours. Increase exhaust fan speed and activate the dehumidifier.",
            defaultRarity = "Common"
        ),
        CyberTarotCard(
            index = 15,
            name = "Spider Mite Attack",
            traditionalName = "The Devil",
            description = "A magnification glass revealing tiny red spider mites spinning white silky webs across sparkling green cannabis trichomes.",
            uprightKeywords = listOf("Pest Infestation", "Overfeeding Nitrogen", "Cloned Pests", "Addictive Boosters"),
            reversedKeywords = listOf("Pest Eradication", "Organic Neem Wash", "Clean Tent", "Lessons Learned"),
            circuitryElement = "Organic Neem Oil & Predator Mites",
            cyberMeaning = "Ignoring simple sanitation practices, bringing dirty clones into a clean room, or relying on chemical pesticide short-cuts.",
            uprightAdvice = "Acknowledge the environmental laxity that let the pests enter. Treat your room with organic beneficial predator mites.",
            reversedAdvice = "You are successfully purging your grow space of spider mites or toxic chemical sprays. Re-establish absolute cleanliness.",
            defaultRarity = "Epic"
        ),
        CyberTarotCard(
            index = 16,
            name = "Crop Collapse",
            traditionalName = "The Tower",
            description = "A collapsed grow tent with a shattered light bulb lying on the floor, and a complete light-leak causing a beautiful canopy to hermie.",
            uprightKeywords = listOf("Hermie Outbreak", "Shattered Ballast", "Total Crop Failure", "Grow Tent Crash"),
            reversedKeywords = listOf("Saved Cuttings", "Emergency Power", "Avoided Outage", "Rebuilding Room"),
            circuitryElement = "Emergency Generator Backups & Timers",
            cyberMeaning = "A sudden, catastrophic failure of core grow equipment. Power outages during peak flower, ballast explosions, or mold outbreak.",
            uprightAdvice = "Let the ruined crop go. Throw out the seeded, moldy buds. Clean the entire tent with bleach and start fresh with organic seeds.",
            reversedAdvice = "A close-call power failure. Instantly activate backup generators, secure the dark cycle, and check for stress signs.",
            defaultRarity = "Legend"
        ),
        CyberTarotCard(
            index = 17,
            name = "Trichome Sparkle",
            traditionalName = "The Star",
            description = "A single, magnificent macro view of a mature trichome stalk, its bulbous resin head shining like a clear crystal star full of rich terpenes.",
            uprightKeywords = listOf("Milky Resin", "Abundant Terpenes", "Grower\\'s Hope", "Golden Trichomes"),
            reversedKeywords = listOf("Oxidized Resin", "Cloudy Haze", "Lost Aroma", "Burnt Buds"),
            circuitryElement = "LED Handheld Trichome Microscopes",
            cyberMeaning = "The promise of therapeutic potency, milky white resin heads, incredible aromas, and a stellar crop curing cleanly.",
            uprightAdvice = "Keep doing what you\\'re doing. Your plants are entering peak resin production; ensure clean water inputs and plenty of love.",
            reversedAdvice = "Your terpenes are evaporating due to excessive room heat. Lower your grow light intensity and check your canopy temperatures.",
            defaultRarity = "Rare"
        ),
        CyberTarotCard(
            index = 18,
            name = "Guerrilla Growing",
            traditionalName = "The Moon",
            description = "A stealth grower checking a secret forest cannabis plot under the pale silver light of a crescent moon, hiding from shadows.",
            uprightKeywords = listOf("Stealth Cultivation", "Secret Plots", "Wild Camouflage", "High Intuition"),
            reversedKeywords = listOf("Spotted Plot", "Foraged Buds", "Anxiety", "Clarity Gained"),
            circuitryElement = "Stealth Camouflage Mesh & Soil Bags",
            cyberMeaning = "Stealth operations, keeping secrets, wild organic grows without power grids, and relying on pure intuition.",
            uprightAdvice = "Keep your grow plot absolutely secret. Don\\'t tell a single soul where your plants are hidden in the woods.",
            reversedAdvice = "The anxiety of stealth growing is taking its toll. It is time to transition to a safe, secure, legal indoor grow tent.",
            defaultRarity = "Epic"
        ),
        CyberTarotCard(
            index = 19,
            name = "1000W Grow Light",
            traditionalName = "The Sun",
            description = "A blazing, powerful 1000-watt double-ended HPS grow bulb emitting a blinding golden light spectrum, pushing plants to maximum bloom.",
            uprightKeywords = listOf("Maximum Yield", "Thick Resin", "Horticultural Success", "Vigorous Bloom"),
            reversedKeywords = listOf("Heat Stress", "Light Bleaching", "Over-illumination", "Foxtailing"),
            circuitryElement = "High-Pressure Sodium (HPS) Ballasts",
            cyberMeaning = "Ultimate horticultural success, incredible terpene content, massive heavy colas, and pride in sharing your crop.",
            uprightAdvice = "Harvest the fruits of your labor! Share your premium organic buds with friends, and celebrate a highly successful crop.",
            reversedAdvice = "Your plants are showing light-bleaching from being too close to the bulb. Raise the light fixture or dim the ballast.",
            defaultRarity = "Legend"
        ),
        CyberTarotCard(
            index = 20,
            name = "Cup Submission",
            traditionalName = "Judgement",
            description = "A sparkling glass jar of perfectly cured organic buds being judged at a prestigious cannabis cup, receiving a gold award ribbon.",
            uprightKeywords = listOf("Cannabis Cup Winner", "Legendary Strain", "Expert Cure", "Horticultural Release"),
            reversedKeywords = listOf("Rejected Bud", "Hay Scent", "Under-Cured", "Harsh Taste"),
            circuitryElement = "Amber Glass Jars & Humidity Packs",
            cyberMeaning = "Reaching the absolute pinnacle as a grower. Your curing protocols are verified, and your premium flowers are legendary.",
            uprightAdvice = "Your curing process is flawless. Submit your entries with pride; you have earned your place among master growers.",
            reversedAdvice = "Your buds smell like grass clipping because they were jarred too early. Open the lids to burp the jars and restore cure.",
            defaultRarity = "Rare"
        ),
        CyberTarotCard(
            index = 21,
            name = "The Green Planet",
            traditionalName = "The World",
            description = "A breathtaking panoramic view of a lush, green, sun-drenched valley filled with legal, organic hemp and medical cannabis fields.",
            uprightKeywords = listOf("Horticultural Flow", "Complete Harvest", "Abundant Supply", "Green Synthesis"),
            reversedKeywords = listOf("Unfinished Cure", "Delayed Trim", "Local Frost", "Incomplete Loop"),
            circuitryElement = "Organic Composting & Living Soil Buffers",
            cyberMeaning = "The final successful synthesis. A complete, bountiful harvest, fully cured buds, secure seeds, and absolute botanical flow.",
            uprightAdvice = "Your harvest is complete and fully cured. Take a deep breath of clean fresh air, and enjoy the fruits of your craft.",
            reversedAdvice = "A localized frost is threatening your late-flowering outdoor plants. Protect the buds tonight; you are so close to the finish line.",
            defaultRarity = "Legend"
        ),
        CyberTarotCard(
            index = 22,
            name = "Genesis #01 - Electron",
            traditionalName = "The Origin Force",
            description = "The fundamental subatomic carrier of electric charge, the catalyst of cellular energy, photosynthesis, and neural light.",
            uprightKeywords = listOf("Genesis Spark", "Electric Charge", "Fundamental Force", "Subatomic Light"),
            reversedKeywords = listOf("Static Noise", "Discharged Cell", "Energy Leak", "Unstable Orbit"),
            circuitryElement = "Subatomic Quantum Spin Accelerator",
            cyberMeaning = "The origin of all physical energy in the universe. The foundational electric spark that powers cellular life.",
            uprightAdvice = "Ignite the primary spark. Small subatomic actions lead to cosmic reactions.",
            reversedAdvice = "Ground your electric connections. Prevent static build-up from disrupting your core circuits.",
            defaultRarity = "Genesis"
        ),
        CyberTarotCard(
            index = 23,
            name = "Genesis #02 - Proton Core",
            traditionalName = "Nuclear Anchor",
            description = "The heavy positive nuclear core holding subatomic matter together through pure nuclear attraction.",
            uprightKeywords = listOf("Nuclear Cohesion", "Unshakable Core", "Atomic Mass", "Core Gravity"),
            reversedKeywords = listOf("Atomic Decay", "Nuclear Instability", "Friction", "Loss of Core"),
            circuitryElement = "Strong Nuclear Binding Matrix",
            cyberMeaning = "The dense, unwavering anchor of atomic existence. Unshakable internal core strength.",
            uprightAdvice = "Hold your central position. Let your core gravity pull all necessary resources inward.",
            reversedAdvice = "Stabilize your internal nuclear force before trying to expand outward.",
            defaultRarity = "Genesis"
        ),
        CyberTarotCard(
            index = 24,
            name = "Genesis #03 - Hydrogen Atom",
            traditionalName = "First Element",
            description = "The primordial element that powers stars, water, and organic chemistry across the universe.",
            uprightKeywords = listOf("First Element", "Starlight Engine", "Water Genesis", "Universal Fuel"),
            reversedKeywords = listOf("Volatile Gas", "Combustion Risk", "Resource Scarcity", "Cold Vacuum"),
            circuitryElement = "Proton-Electron Valence Loop",
            cyberMeaning = "The simple, perfect synthesis of electron and proton. The raw fuel of every living sun.",
            uprightAdvice = "Combine your elemental energy into living water and light.",
            reversedAdvice = "Handle primal energy with respect. Keep volatile elements under clear pressure control.",
            defaultRarity = "Genesis"
        ),
        CyberTarotCard(
            index = 25,
            name = "Genesis #04 - Primordial DNA",
            traditionalName = "Biological Blueprint",
            description = "The golden double helix encoding ancestral terpenes, living memory, and botanical intelligence.",
            uprightKeywords = listOf("Ancestral Blueprint", "Golden Helix", "Living Code", "Terpene Genetics"),
            reversedKeywords = listOf("Genetic Mutation", "Corrupted Helix", "Memory Loss", "Degraded Strain"),
            circuitryElement = "Nucleotide Helix Transceiver",
            cyberMeaning = "The eternal living code passed down through eons. Pure genetic memory and digital organic alignment.",
            uprightAdvice = "Honor your genetic blueprint. The instructions for perfection are already encoded in your DNA.",
            reversedAdvice = "Protect your pure genetics from environmental corruption or degradation.",
            defaultRarity = "Genesis"
        ),
        CyberTarotCard(
            index = 26,
            name = "Genesis #05 - Quantum Singularity",
            traditionalName = "Origin of All",
            description = "The infinite golden origin point - Начало Начал - from which all light, matter, space, and consciousness expand.",
            uprightKeywords = listOf("Infinite Origin", "Singularity Event", "Absolute Mastery", "Cosmic Creation"),
            reversedKeywords = listOf("Black Hole Collapse", "Overwhelming Density", "Cosmic Void", "Dimensional Shift"),
            circuitryElement = "Genesis Zero-Point Quantum Core",
            cyberMeaning = "The supreme origin of all existence. The ultimate pinnacle of creation where all threads converge.",
            uprightAdvice = "You have unlocked the origin point of creation. Channel infinite energy with total awareness.",
            reversedAdvice = "Remain balanced in the presence of infinite power; do not let cosmic gravity crush your focus.",
            defaultRarity = "Genesis",
            series = "Genesis Series"
        ),
        CyberTarotCard(
            index = 27,
            name = "Amber #01 - Baltic Resin",
            traditionalName = "Petrified Light",
            description = "Ancient petrified golden tree resin preserving pre-historic flora in flawless crystal clarity.",
            uprightKeywords = listOf("Eternal Preservation", "Golden Light", "Ancient Memory", "Resin Crystal"),
            reversedKeywords = listOf("Stagnation", "Trapped In Past", "Brittle Shell", "Opaque Vision"),
            circuitryElement = "Petrified Amber Optical Matrix",
            cyberMeaning = "Timeless preservation of energy and knowledge across millennia.",
            uprightAdvice = "Crystalline clarity preserves your best work. Let time refine your value.",
            reversedAdvice = "Do not allow yourself to become trapped in frozen habits.",
            defaultRarity = "Epic",
            series = "Amber Series"
        ),
        CyberTarotCard(
            index = 28,
            name = "Amber #02 - Liquid Trichomes",
            traditionalName = "Golden Nectar",
            description = "Concentrated golden amber droplets of pure terpene resin glistening under neon UV rays.",
            uprightKeywords = listOf("Terpene Gold", "Liquid Concentrate", "Pure Potency", "Golden Nectar"),
            reversedKeywords = listOf("Sticky Trap", "Over-concentration", "Resin Clog", "Viscous Stall"),
            circuitryElement = "Terpene Resin Extraction Core",
            cyberMeaning = "The purest essence distilled into liquid gold.",
            uprightAdvice = "Focus on essence over volume. Quality distills down to gold.",
            reversedAdvice = "Be careful not to get stuck in overly complex details.",
            defaultRarity = "Legend",
            series = "Amber Series"
        ),
        CyberTarotCard(
            index = 29,
            name = "Ocean #01 - Coral Reef Matrix",
            traditionalName = "Bioluminescent Sanctuary",
            description = "A teeming underwater coral reef pulsing with blue bioluminescent energy and symbiotic marine life.",
            uprightKeywords = listOf("Symbiotic Reef", "Ocean Pulse", "Bioluminescence", "Deep Ecosystem"),
            reversedKeywords = listOf("Bleached Reef", "Polluted Waters", "Isolation", "Oxygen Depletion"),
            circuitryElement = "Deep Sea Hydro-Photonic Node",
            cyberMeaning = "A vibrant interconnected ecosystem where diversity breeds strength.",
            uprightAdvice = "Build supportive communities and thrive through mutual symbiosis.",
            reversedAdvice = "Cleanse toxic influences from your immediate environment.",
            defaultRarity = "Rare",
            series = "Seas & Oceans"
        ),
        CyberTarotCard(
            index = 30,
            name = "Ocean #02 - Deep Abyss Leviathan",
            traditionalName = "Sovereign of the Deep",
            description = "A massive cybernetic leviathan navigating the abyssal trenches with calm, unstoppable majesty.",
            uprightKeywords = listOf("Abyssal Power", "Unstoppable Force", "Deep Wisdom", "Leviathan Tide"),
            reversedKeywords = listOf("Crushing Pressure", "Deep Dark", "Unseen Monster", "Tidal Surge"),
            circuitryElement = "Sub-Oceanic Pressure Quantum Engine",
            cyberMeaning = "Mastery over the deepest emotional and intuitive depths.",
            uprightAdvice = "Navigate deep waters with quiet confidence. You command your own depths.",
            reversedAdvice = "Do not fear the dark depths; build pressure resistance within your core.",
            defaultRarity = "Legend",
            series = "Seas & Oceans"
        )
    )

    fun getCard(index: Int): CyberTarotCard {
        return cards.firstOrNull { it.index == index } ?: cards[0]
    }
}
