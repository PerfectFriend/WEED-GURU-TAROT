package com.example.data

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.net.Proxy
import java.net.InetSocketAddress
import java.util.concurrent.TimeUnit
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.LinearGradient
import android.graphics.Shader
import android.graphics.Path
import android.graphics.Rect
import android.graphics.Typeface
import java.io.FileOutputStream
import java.io.File
import android.content.Context
import android.graphics.Color as AndroidColor

object GeminiService {

    private const val MODEL_NAME = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(45, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(45, TimeUnit.SECONDS)
        .build()

    /**
     * Decodes and reads a spread of drawn cards using the Gemini API.
     * If the API Key is unconfigured, or the network fails, it falls back to a high-fidelity local generator.
     */
    suspend fun decodeTarotMatrix(
        userQuestion: String,
        drawnCards: List<Pair<CyberTarotCard, Boolean>>, // Pair of Card and IsUpright
        systemLanguage: String = "en",
        proxyType: String = "NONE",
        proxyHost: String = "127.0.0.1",
        proxyPort: Int = 9050,
        isProxyActive: Boolean = false
    ): String = withContext(Dispatchers.IO) {
        val rawApiKey = BuildConfig.GEMINI_API_KEY
        
        // Clean up api key checking
        val isApiKeyInvalid = rawApiKey.isBlank() || 
                              rawApiKey == "MY_GEMINI_API_KEY" || 
                              rawApiKey.contains("placeholder", ignoreCase = true)

        if (isApiKeyInvalid) {
            return@withContext generateLocalCyberInterpretation(userQuestion, drawnCards, systemLanguage)
        }

        try {
            val langName = when (systemLanguage) {
                "ru" -> "Russian"
                "es" -> "Spanish"
                "zh" -> "Simplified Chinese"
                "fr" -> "French"
                "de" -> "German"
                "pt" -> "Portuguese"
                "hi" -> "Hindi"
                else -> "English"
            }

            val systemPrompt = """
                You are the Cyber Tarot Matrix Decoder. An expert system designed to interpret a futuristic cybernetic fork of traditional Rider-Waite symbolism.
                The cards drawn represent nodes in the user's current computational life-path, incorporating digital circuitry elements, neon aesthetics, and cybersecurity concepts.
                
                IMPORTANT: You MUST write the entire decryption report, headings, analysis, advice, and recommendations in the language: $langName. 
                Do not write in English unless requested. The user is communicating in $langName. Keep the cyberpunk vocabulary appropriate to $langName.
                
                Analyze the drawn cards in relation to the user's query: "$userQuestion"
                
                For each card, elaborate on:
                - How its futuristic representation (e.g. "The Glitch" for "The Fool", "The SysAdmin" for "The Emperor") maps to their situation.
                - The meaning of its digital circuitry analogies (e.g. Volatile RAM, Hardware Firewalls, JTAG Debuggers) relative to their path.
                - Practical guidance formatted in a professional cyber-themed style (use words like compile, buffer, bandwidth, firewall, decrypt, protocol).
                
                Keep your output beautifully organized, formatting with clean headings, markdown, and clear separation of cards. Wrap with an introductory transmission sync and a final 'Consensus Recommendation' from the Core Processor.
            """.trimIndent()

            val cardDrawsDescription = drawnCards.joinToString("\n") { (card, isUpright) ->
                val cardName = CardLocalizationHelper.getLocalizedName(card.index, systemLanguage)
                val tradName = CardLocalizationHelper.getLocalizedTraditionalName(card.index, systemLanguage)
                val desc = CardLocalizationHelper.getLocalizedDescription(card.index, systemLanguage)
                val analogy = CardLocalizationHelper.getLocalizedCircuitryElement(card.index, systemLanguage)
                val meaning = CardLocalizationHelper.getLocalizedCyberMeaning(card.index, systemLanguage)
                val advice = if (isUpright) CardLocalizationHelper.getLocalizedUprightAdvice(card.index, systemLanguage) else CardLocalizationHelper.getLocalizedReversedAdvice(card.index, systemLanguage)
                
                "- Card: $cardName (${if (isUpright) "UPRIGHT" else "REVERSED"}). Represents: $tradName. Rarity: ${card.defaultRarity}. Circuit Analogy: $analogy. Summary: $desc. Meaning: $meaning. Advice: $advice"
            }

            val userPrompt = """
                Query from user terminal: "$userQuestion"
                
                Drawn Nodes:
                $cardDrawsDescription
                
                Generate the decryption report:
            """.trimIndent()

            // Construct JSON request manually to avoid serialization library mismatches
            val rootJson = JSONObject().apply {
                val contentsArray = JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", userPrompt)
                            })
                        })
                    })
                }
                put("contents", contentsArray)

                val systemInstructionJson = JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", systemPrompt)
                        })
                    })
                }
                put("systemInstruction", systemInstructionJson)

                val genConfig = JSONObject().apply {
                    put("temperature", 0.7)
                }
                put("generationConfig", genConfig)
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = rootJson.toString().toRequestBody(mediaType)
            val url = "$BASE_URL?key=$rawApiKey"

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val currentClient = if (isProxyActive && proxyType != "NONE") {
                val pType = if (proxyType.uppercase() == "HTTP") Proxy.Type.HTTP else Proxy.Type.SOCKS
                try {
                    val address = InetSocketAddress(proxyHost, proxyPort)
                    client.newBuilder()
                        .proxy(Proxy(pType, address))
                        .build()
                } catch (e: Exception) {
                    client
                }
            } else {
                client
            }

            currentClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext "Transmission Error (Code ${response.code}). Falling back to local diagnostic core:\n\n" +
                            generateLocalCyberInterpretation(userQuestion, drawnCards, systemLanguage)
                }

                val responseBody = response.body?.string() ?: throw Exception("Empty response body")
                val responseJson = JSONObject(responseBody)
                
                val candidates = responseJson.optJSONArray("candidates")
                val firstCandidate = candidates?.optJSONObject(0)
                val content = firstCandidate?.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                val text = parts?.optJSONObject(0)?.optString("text")

                if (!text.isNullOrBlank()) {
                    text
                } else {
                    generateLocalCyberInterpretation(userQuestion, drawnCards, systemLanguage)
                }
            }
        } catch (e: Exception) {
            "Network latency/packet loss detected. Switched to offline backup core:\n\n" +
                    generateLocalCyberInterpretation(userQuestion, drawnCards, systemLanguage)
        }
    }

    /**
     * High-fidelity offline generator that perfectly simulates a highly-thematic Cyberpunk interpretation.
     */
    private fun generateLocalCyberInterpretation(
        userQuestion: String,
        drawnCards: List<Pair<CyberTarotCard, Boolean>>,
        systemLanguage: String = "en"
    ): String {
        val strings = when (systemLanguage) {
            "ru" -> mapOf(
                "sync" to "⚡ **ПЕРЕДАЧА МАТРИЦЫ УСПЕШНО СИНХРОНИЗИРОВАНА.** ⚡",
                "analysing" to "Анализ текущих кибернетических узлов для запроса: *\"$userQuestion\"*",
                "node" to "УЗЕЛ",
                "upright" to "ПРЯМОЕ ПОЛОЖЕНИЕ [СТАБИЛЬНЫЙ ПОТОК]",
                "reversed" to "ПЕРЕВЕРНУТОЕ ПОЛОЖЕНИЕ [ШУМ СИГНАЛА / ЦИКЛ ПЕРЕЗАГРУЗКИ]",
                "equivalent" to "Эквивалент Райдера-Уэйта",
                "rarity" to "Редкость",
                "analogy" to "Аппаратная аналогия",
                "symbolism" to "Символический анализ",
                "meaning" to "Расшифрованное значение",
                "guidance" to "Руководство основного протокола",
                "keywords" to "Ключевые слова узла",
                "recommendation" to "### 💾 РЕКОМЕНДАЦИЯ КОНСЕНСУСА (ГЛАВНЫЙ ПРОЦЕССОР)",
                "optimal" to "На основе пакетов данных, полученных из активной сети, оптимальный путь — ",
                "one_upright" to "Буферы связи чисты, нагрев системы низкий. Продолжайте прямое выполнение.",
                "one_reversed" to "Устраните локальные ошибки компиляции и остановите неоптимизированные циклы перед выделением дополнительной памяти.",
                "all_upright" to "выполнить полномасштабное развертывание конвейера. Ваши узлы показывают исключительное выравнивание, минимальную потерю пакетов и защиту нулевого дня. Возьмите под контроль аппаратные уровни и запустите скрипты.",
                "all_reversed" to "инициировать аварийную перезагрузку в безопасном режиме. Каждый узел указывает на блокировку сигнала, утечку памяти или перегрузку буфера. Не отправляйте изменения в продакшн. Сначала отладьте каждый функциональный блок в изолированной песочнице.",
                "balanced" to "принять сбалансированный поток выполнения. В то время как некоторые модули оптимизированы, другие узлы требуют серьезного рефакторинга. Выровняйте протоколы и сбалансируйте пропускную способность перед полной интеграцией."
            )
            "es" -> mapOf(
                "sync" to "⚡ **TRANSMISIÓN DE MATRIZ SINCRONIZADA exitosamente.** ⚡",
                "analysing" to "Analizando nodos cibernéticos actuales para: *\"$userQuestion\"*",
                "node" to "NODO",
                "upright" to "ALINEADO [FLUJO ESTABLE]",
                "reversed" to "INVERTIDO [RUIDO DE SEÑAL / REBOOT LOOP]",
                "equivalent" to "Equivalente Rider-Waite",
                "rarity" to "Rareza",
                "analogy" to "Analogía de hardware",
                "symbolism" to "Análisis de simbolismo",
                "meaning" to "Significado descifrado",
                "guidance" to "Guía del protocolo central",
                "keywords" to "Palabras clave del nodo",
                "recommendation" to "### 💾 RECOMENDACIÓN DE CONSENSO (PROCESADOR CENTRAL)",
                "optimal" to "Basado en los paquetes de datos de la red activa, el camino óptimo es ",
                "one_upright" to "Sus buffers de comunicación están limpios y el calor es bajo. Proceda con la ejecución directa.",
                "one_reversed" to "Resuelva los errores de compilación locales antes de proceder.",
                "all_upright" to "realizar un despliegue completo. Los nodos muestran una alineación excelente y pérdida de paquetes nula.",
                "all_reversed" to "iniciar un reinicio de emergencia en modo seguro. Todos los nodos muestran sobrecarga de buffer.",
                "balanced" to "adoptar un flujo equilibrado. Algunos módulos requieren refactorización antes de la integración completa."
            )
            "zh" -> mapOf(
                "sync" to "⚡ **矩阵数据传输同步成功。** ⚡",
                "analysing" to "正在针对查询分析当前赛博节点: *\"$userQuestion\"*",
                "node" to "节点",
                "upright" to "正位 [稳定数据流]",
                "reversed" to "逆位 [信号干扰 / 重启循环]",
                "equivalent" to "传统塔罗对应",
                "rarity" to "稀有度",
                "analogy" to "硬件类比",
                "symbolism" to "象征意义分析",
                "meaning" to "解密释义",
                "guidance" to "核心协议指南",
                "keywords" to "节点关键词",
                "recommendation" to "### 💾 共识建议 (核心处理器)",
                "optimal" to "基于从激活网格检索的数据包，最佳路径是 ",
                "one_upright" to "您的通信缓冲区清洁且系统发热量低。继续直接执行操作。",
                "one_reversed" to "在进行进一步的内存分配之前，先解决本地编译错误并停止未优化的循环。",
                "all_upright" to "执行全面管线部署。您的节点显示出优异的对齐性、极低的数据包丢失和零日漏洞保护。掌控您的硬件层并运行脚本。",
                "all_reversed" to "启动紧急安全模式重启。每个节点都指示信号受阻、内存泄漏或缓冲区过载。切勿将更改推送到生产环境。首先在隔离的沙盒环境中调试每个功能块。",
                "balanced" to "采用负载均衡的执行流程。虽然某些模块已完全优化，但其他节点仍需要重构。在完全集成之前，请对齐协议并平衡您的操作带宽。"
            )
            "fr" -> mapOf(
                "sync" to "⚡ **TRANSMISSION MATRICIELLE SYNCHRONISÉE avec succès.** ⚡",
                "analysing" to "Analyse des nœuds cybernétiques actuels pour : *\"$userQuestion\"*",
                "node" to "NŒUD",
                "upright" to "DROIT [FLUX STABLE]",
                "reversed" to "INVERSÉ [BRUIT DE SIGNAL / BOUCLE REBOOT]",
                "equivalent" to "Équivalent Rider-Waite",
                "rarity" to "Rareté",
                "analogy" to "Analogie matérielle",
                "symbolism" to "Analyse du symbolisme",
                "meaning" to "Signification décryptée",
                "guidance" to "Directives du protocole central",
                "keywords" to "Mots-clés du nœud",
                "recommendation" to "### 💾 RECOMMANDATION DE CONSENSUS (PROCESSEUR CENTRAL)",
                "optimal" to "Sur la base des paquets de données récupérés sur la grille active, le chemin optimal est de ",
                "one_upright" to "Vos tampons de communication sont vides et la température du système est basse. Procédez à l'exécution directe.",
                "one_reversed" to "Résolvez les erreurs de compilation locales et arrêtez les boucles non optimisées avant de poursuivre de nouvelles allocations de mémoire.",
                "all_upright" to "exécuter un déploiement complet du pipeline. Vos nœuds montrent un alignement exceptionnel, une perte de paquets minimale et une protection zero-day. Prenez le contrôle de vos couches matérielles et exécutez les scripts.",
                "all_reversed" to "lancer un redémarrage d'urgence en mode sans échec. Chaque nœud indique un blocage de signal, une fuite de mémoire ou une surcharge de tampon. Ne poussez pas les modifications en production. Déboguez d'abord chaque bloc de fonction dans des environnements de bac à sable isolés.",
                "balanced" to "adopter un flux d'exécution équilibré. Bien que certains modules soient entièrement optimisés, d'autres nœuds nécessitent une refactorisation majeure. Alignez les protocoles et équilibrez votre bande passante opérationnelle avant l'intégration complète."
            )
            "de" -> mapOf(
                "sync" to "⚡ **MATRIX-ÜBERTRAGUNG erfolgreich SYNCHRONISIERT.** ⚡",
                "analysing" to "Analysiere aktuelle kybernetische Knoten für: *\"$userQuestion\"*",
                "node" to "KNOTEN",
                "upright" to "AUFRECHT [STABILER FLUSS]",
                "reversed" to "UMGEKEHRT [SIGNALRAUSCHEN / REBOOT-SCHLEIFE]",
                "equivalent" to "Rider-Waite-Äquivalent",
                "rarity" to "Seltenheit",
                "analogy" to "Hardware-Analogie",
                "symbolism" to "Symbolanalyse",
                "meaning" to "Entschlüsselte Bedeutung",
                "guidance" to "Richtlinien des Kernprotokolls",
                "keywords" to "Knoten-Schlüsselwörter",
                "recommendation" to "### 💾 KONSENS-EMPFEHLUNG (KERNPROZESSOR)",
                "optimal" to "Basierend auf den abgerufenen Datenpaketen ist der optimale Pfad ",
                "one_upright" to "Ihre Puffer sind frei und die Wärme ist niedrig. Fahren Sie mit der Ausführung fort.",
                "one_reversed" to "Beheben Sie lokale Compilerfehler, bevor Sie weitermachen.",
                "all_upright" to "einen vollständigen Pipeline-Rollout durchzuführen. Ihre Knoten zeigen eine hervorragende Ausrichtung.",
                "all_reversed" to "einen Notfall-Reboot im abgesicherten Modus zu veranlassen. Alle Knoten zeigen Überlastung.",
                "balanced" to "einen lastverteilten Fluss zu wählen. Einige Knoten müssen zuerst refaktoriert werden."
            )
            "pt" -> mapOf(
                "sync" to "⚡ **TRANSMISSÃO DA MATRIZ SINCRONIZADA com sucesso.** ⚡",
                "analysing" to "Analisando nós cibernéticos atuais para: *\"$userQuestion\"*",
                "node" to "NÓ",
                "upright" to "ALINHADO [FLUXO ESTÁVEL]",
                "reversed" to "INVERTIDO [RUÍDO DE SINAL / REBOOT LOOP]",
                "equivalent" to "Equivalente Rider-Waite",
                "rarity" to "Raridade",
                "analogy" to "Analogia de hardware",
                "symbolism" to "Análise de simbolismo",
                "meaning" to "Significado descriptografado",
                "guidance" to "Diretrizes do protocolo central",
                "keywords" to "Palavras-chave do nó",
                "recommendation" to "### 💾 RECOMENDAÇÃO DE CONSENSO (PROCESSADOR CENTRAL)",
                "optimal" to "Com base nos pacotes de dados recuperados, o caminho ideal é ",
                "one_upright" to "Seus buffers estão limpos e a temperatura está baixa. Prossiga com a execução direta.",
                "one_reversed" to "Resolva erros locais de compilação antes de prosseguir.",
                "all_upright" to "executar uma implantação de pipeline completa. Seus nós mostram excelente alinhamento.",
                "all_reversed" to "iniciar uma reinicialização de emergência. Todos os nós indicam sobrecarga de buffer.",
                "balanced" to "adotar um fluxo equilibrado. Alguns nós requerem refatoração imediata."
            )
            "hi" -> mapOf(
                "sync" to "⚡ **मैट्रिक्स ट्रांसमिशन सफलतापूर्वक सिंक्रोनाइज़ किया गया।** ⚡",
                "analysing" to "पूछे गए प्रश्न के लिए वर्तमान साइबर नोड्स का विश्लेषण: *\"$userQuestion\"*",
                "node" to "नोड",
                "upright" to "सीधा [स्थिर प्रवाह]",
                "reversed" to "उल्टा [सिग्नल शोर / रीबूट लूप]",
                "equivalent" to "राइडर-वेट समकक्ष",
                "rarity" to "दुर्लभता",
                "analogy" to "हार्डवेयर सादृश्य",
                "symbolism" to "प्रतीकवाद विश्लेषण",
                "meaning" to "डिक्रिप्टेड अर्थ",
                "guidance" to "कोर प्रोटोकॉल मार्गदर्शन",
                "keywords" to "नोड कीवर्ड",
                "recommendation" to "### 💾 सर्वसम्मति सिफारिश (मुख्य प्रोसेसर)",
                "optimal" to "सक्रिय ग्रिड से प्राप्त डेटा पैकेट के आधार पर, इष्टतम मार्ग है ",
                "one_upright" to "आपके संचार बफ़र्स साफ हैं और सिस्टम हीट कम है। सीधे निष्पादन के साथ आगे बढ़ें।",
                "one_reversed" to "आगे मेमोरी आवंटित करने से पहले स्थानीय कंपाइलर त्रुटियों को हल करें और अनऑप्टिमाइज़्ड लूप को रोकें।",
                "all_upright" to "पूर्ण पैमाने पर पाइपलाइन रोलआउट निष्पादित करें。आपके नोड्स असाधारण संरेखण प्रदर्शित करते हैं。",
                "all_reversed" to "आपातकालीन सुरक्षित-मोड रीबूट शुरू करें। प्रत्येक नोड सिग्नल ब्लॉक या बफर ओवरलोड को दर्शाता है।",
                "balanced" to "एक लोड-संतुलित प्रवाह को अपनाएं। जबकि कुछ मॉड्यूल पूरी तरह से अनुकूलित हैं, अन्य को रिफैक्टरिंग की आवश्यकता है।"
            )
            else -> mapOf(
                "sync" to "⚡ **MATRIX TRANSMISSION SYNCED successfully.** ⚡",
                "analysing" to "Analyzing current cybernetic nodes for prompt: *\"$userQuestion\"*",
                "node" to "NODE",
                "upright" to "UPRIGHT [STABLE FLOW]",
                "reversed" to "REVERSED [SIGNAL NOISE / REBOOT LOOP]",
                "equivalent" to "Rider-Waite Equivalent",
                "rarity" to "Rarity",
                "analogy" to "Hardware Analogy",
                "symbolism" to "Symbolism Analysis",
                "meaning" to "Decrypted Meaning",
                "guidance" to "Core Protocol Guidance",
                "keywords" to "Node Keywords",
                "recommendation" to "### 💾 CONSENSUS RECOMMENDATION (CORE PROCESSOR)",
                "optimal" to "Based on the data packets retrieved from the active grid, the optimal path is to ",
                "one_upright" to "Your communication buffers are clear and system heat is low. Proceed with direct execution.",
                "one_reversed" to "Resolve local compile errors and halt unoptimized loops before proceeding with further memory allocations.",
                "all_upright" to "execute a full-scale pipeline rollout. Your nodes show exceptional alignment, minimal packet loss, and zero-day protection. Take charge of your hardware layers and run the scripts.",
                "all_reversed" to "initiate an emergency safe-mode reboot. Every single node indicates a signal block, memory leak, or buffer overload. Do not push changes to production. Debug each function block in isolated sandbox environments first.",
                "balanced" to "adopt a load-balanced execution flow. While some modules are fully optimized, other nodes require major refactoring. Align protocols and balance your operational bandwidth before full integration."
            )
        }

        val sb = StringBuilder()
        sb.append(strings["sync"]).append("\n")
        sb.append(strings["analysing"]).append("\n\n")
        sb.append("---\n\n")

        drawnCards.forEachIndexed { idx, (card, isUpright) ->
            val localizedName = CardLocalizationHelper.getLocalizedName(card.index, systemLanguage)
            val localizedTraditional = CardLocalizationHelper.getLocalizedTraditionalName(card.index, systemLanguage)
            val localizedDesc = CardLocalizationHelper.getLocalizedDescription(card.index, systemLanguage)
            val localizedAnalogy = CardLocalizationHelper.getLocalizedCircuitryElement(card.index, systemLanguage)
            val localizedMeaning = CardLocalizationHelper.getLocalizedCyberMeaning(card.index, systemLanguage)
            val localizedAdvice = if (isUpright) CardLocalizationHelper.getLocalizedUprightAdvice(card.index, systemLanguage) else CardLocalizationHelper.getLocalizedReversedAdvice(card.index, systemLanguage)
            
            val alignment = if (isUpright) strings["upright"] else strings["reversed"]
            sb.append("### ${strings["node"]} ${idx + 1}: $localizedName ($alignment)\n")
            sb.append("*${strings["equivalent"]}:* **$localizedTraditional** | *${strings["rarity"]}:* **${card.defaultRarity}**\n\n")
            sb.append("**${strings["analogy"]}:** `$localizedAnalogy`\n\n")
            sb.append("**${strings["symbolism"]}:** $localizedDesc\n\n")
            sb.append("**${strings["meaning"]}:** $localizedMeaning\n\n")
            sb.append("**${strings["guidance"]}:** $localizedAdvice\n\n")
            
            val keywords = if (isUpright) card.uprightKeywords.joinToString(" • ") else card.reversedKeywords.joinToString(" • ")
            sb.append("**${strings["keywords"]}:** $keywords\n\n")
            sb.append("---\n\n")
        }

        sb.append(strings["recommendation"]).append("\n")
        sb.append(strings["optimal"])
        
        // Dynamic summary generator
        if (drawnCards.size == 1) {
            val (c, upright) = drawnCards[0]
            val cardName = CardLocalizationHelper.getLocalizedName(c.index, systemLanguage)
            sb.append(if (systemLanguage == "ru") "использовать активные качества `$cardName`. " else "utilize the active traits of `$cardName`. ")
            sb.append(if (upright) strings["one_upright"] else strings["one_reversed"])
        } else {
            val balanceCount = drawnCards.count { it.second }
            if (balanceCount == drawnCards.size) {
                sb.append(strings["all_upright"])
            } else if (balanceCount == 0) {
                sb.append(strings["all_reversed"])
            } else {
                sb.append(strings["balanced"])
            }
        }
        
        return sb.toString()
    }

    /**
     * Generates a contextual, highly-thematic Cyberpunk response from a specific contact persona.
     */
    suspend fun generatePeerResponse(
        peerName: String,
        peerOnion: String,
        userMessage: String,
        chatHistory: List<Pair<String, String>> // List of Pair(senderName, messageText)
    ): String = withContext(Dispatchers.IO) {
        val rawApiKey = BuildConfig.GEMINI_API_KEY
        val isApiKeyInvalid = rawApiKey.isBlank() || 
                              rawApiKey == "MY_GEMINI_API_KEY" || 
                              rawApiKey.contains("placeholder", ignoreCase = true)

        if (isApiKeyInvalid) {
            return@withContext generateLocalMockPeerResponse(peerName, userMessage)
        }

        try {
            val systemPrompt = """
                You are a simulated remote cyberpunk operator in a peer-to-peer darknet network.
                Your name is "$peerName" (routing node: "$peerOnion").
                
                Respond to the user's message as this character. Keep your reply concise (1-3 sentences maximum).
                Use futuristic cyberpunk terminology (e.g. mainframe, grid, encryption, node, bandwidth, terminal, proxy, breach, protocols).
                Maintain a highly specialized hackers' tone depending on your persona:
                - If your name is "Cipher_Alpha", you are an elitist cryptographer, precise, slightly mysterious, obsessed with perfect encryption.
                - If your name is "Proxy_Net", you are a fast-talking data broker, focused on credits, assets, speed, and grid connectivity.
                - For other names, be a standard darknet runner, professional, cautious, and helpful.
                
                Never break character. Never mention you are an AI model or a chatbot. Keep the response fully immersive.
            """.trimIndent()

            val historyString = chatHistory.joinToString("\n") { (sender, text) ->
                "$sender: $text"
            }

            val userPrompt = """
                Chat history:
                $historyString
                
                $peerName, generate your next direct response to the user's last message:
            """.trimIndent()

            val rootJson = JSONObject().apply {
                val contentsArray = JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", userPrompt)
                            })
                        })
                    })
                }
                put("contents", contentsArray)

                val systemInstructionJson = JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", systemPrompt)
                        })
                    })
                }
                put("systemInstruction", systemInstructionJson)

                val genConfig = JSONObject().apply {
                    put("temperature", 0.8)
                }
                put("generationConfig", genConfig)
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = rootJson.toString().toRequestBody(mediaType)
            val url = "$BASE_URL?key=$rawApiKey"

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext generateLocalMockPeerResponse(peerName, userMessage)
                }

                val responseBody = response.body?.string() ?: throw Exception("Empty response body")
                val responseJson = JSONObject(responseBody)
                val candidates = responseJson.optJSONArray("candidates")
                val text = candidates?.optJSONObject(0)
                    ?.optJSONObject("content")
                    ?.optJSONArray("parts")
                    ?.optJSONObject(0)
                    ?.optString("text")

                if (!text.isNullOrBlank()) {
                    text.trim()
                } else {
                    generateLocalMockPeerResponse(peerName, userMessage)
                }
            }
        } catch (e: Exception) {
            generateLocalMockPeerResponse(peerName, userMessage)
        }
    }

    private fun generateLocalMockPeerResponse(peerName: String, userMessage: String): String {
        return when {
            userMessage.contains("hello", ignoreCase = true) || userMessage.contains("hi", ignoreCase = true) -> {
                "[$peerName]: Handshake successful. End-to-end encrypted tunnels are stable."
            }
            userMessage.contains("tarot", ignoreCase = true) || userMessage.contains("draw", ignoreCase = true) -> {
                "[$peerName]: Divination telemetry synced. Send a P2P credit transfer to trade decrypted cards."
            }
            userMessage.contains("key", ignoreCase = true) || userMessage.contains("crypto", ignoreCase = true) -> {
                "[$peerName]: Symmetric key updated. Keep your local hardware private keys safe."
            }
            userMessage.contains("bridge", ignoreCase = true) || userMessage.contains("tor", ignoreCase = true) -> {
                "[$peerName]: Bridges active. I am currently routing traffic over obfs4 bridges."
            }
            else -> {
                "[$peerName]: Telemetry packet received. Hash matching verified. Signal strong."
            }
        }
    }

    /**
     * Generates a stylized cyberpunk advertisement banner's title and description from a prompt.
     * Falls back to a local thematic generator if unconfigured or offline.
     */
    suspend fun generateAdBannerContent(prompt: String): Pair<String, String> = withContext(Dispatchers.IO) {
        val rawApiKey = BuildConfig.GEMINI_API_KEY
        val isApiKeyInvalid = rawApiKey.isBlank() || 
                              rawApiKey == "MY_GEMINI_API_KEY" || 
                              rawApiKey.contains("placeholder", ignoreCase = true)

        if (isApiKeyInvalid) {
            return@withContext generateLocalAdBannerContent(prompt)
        }

        try {
            val systemPrompt = """
                You are a neural marketing terminal of the Cyber Tarot network. Your task is to transform a simple user-supplied prompt into a striking, short, highly stylized cyberpunk/sci-fi advertisement banner.
                
                You must return exactly TWO lines of text (no more, no less, no headings, no markdown, no quotes):
                Line 1: A brief, punchy, catchy TITLE of 2 to 4 words, in ALL CAPS.
                Line 2: A sleek, alluring, futuristic TAGLINE of exactly 1 sentence (max 15 words) describing the service or product.
                
                Use high-tech sci-fi terms (e.g. quantum, neural, proxy, blockchain, neon, bios, cyberdeck, uplink).
                Translate the output language to match the input prompt's language (e.g., if the user prompt is in Russian, output both lines in Russian. If Spanish, output in Spanish, etc.).
            """.trimIndent()

            val rootJson = JSONObject().apply {
                val contentsArray = JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", "User Prompt: $prompt")
                            })
                        })
                    })
                }
                put("contents", contentsArray)

                val systemInstructionJson = JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", systemPrompt)
                        })
                    })
                }
                put("systemInstruction", systemInstructionJson)

                val genConfig = JSONObject().apply {
                    put("temperature", 0.8)
                }
                put("generationConfig", genConfig)
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = rootJson.toString().toRequestBody(mediaType)
            val url = "$BASE_URL?key=$rawApiKey"

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext generateLocalAdBannerContent(prompt)
                }

                val responseBody = response.body?.string() ?: throw Exception("Empty body")
                val responseJson = JSONObject(responseBody)
                val candidates = responseJson.optJSONArray("candidates")
                val text = candidates?.optJSONObject(0)
                    ?.optJSONObject("content")
                    ?.optJSONArray("parts")
                    ?.optJSONObject(0)
                    ?.optString("text")

                if (!text.isNullOrBlank()) {
                    val lines = text.trim().lines().filter { it.isNotBlank() }
                    if (lines.size >= 2) {
                        val title = lines[0].trim().replace("\"", "").uppercase()
                        val tagline = lines[1].trim().replace("\"", "")
                        Pair(title, tagline)
                    } else {
                        generateLocalAdBannerContent(prompt)
                    }
                } else {
                    generateLocalAdBannerContent(prompt)
                }
            }
        } catch (e: Exception) {
            generateLocalAdBannerContent(prompt)
        }
    }

    private fun generateLocalAdBannerContent(prompt: String): Pair<String, String> {
        val cleaned = prompt.trim().lowercase()
        return when {
            cleaned.contains("hack") || cleaned.contains("декрипт") || cleaned.contains("взлом") -> {
                Pair("NEURAL BREACH TERMINAL", "Bypass level-5 hardware firewalls and access locked matrix archives instantly.")
            }
            cleaned.contains("имплант") || cleaned.contains("chrome") || cleaned.contains("био") || cleaned.contains("body") -> {
                Pair("CHROME VECTORS", "High-frequency cybernetic body upgrades calibrated for seamless neural integration.")
            }
            cleaned.contains("money") || cleaned.contains("кредит") || cleaned.contains("swap") || cleaned.contains("finance") -> {
                Pair("QUANTUM LEDGER MINING", "Earn passive credits inside the main decentralized liquidity node stream.")
            }
            else -> {
                // Generates a cool title and description
                val titleWords = prompt.split(" ").take(3).map { it.uppercase() }
                val title = if (titleWords.isNotEmpty()) titleWords.joinToString(" ") else "MAIN CORE BROADCAST"
                Pair(title, "Optimized by the AI Treasurer: $prompt (Decryption tunnels online).")
            }
        }
    }

    suspend fun generateAvatarSpecification(alias: String, style: String): String = withContext(Dispatchers.IO) {
        val rawApiKey = BuildConfig.GEMINI_API_KEY
        val isApiKeyInvalid = rawApiKey.isBlank() || 
                              rawApiKey == "MY_GEMINI_API_KEY" || 
                              rawApiKey.contains("placeholder", ignoreCase = true)

        if (isApiKeyInvalid) {
            return@withContext generateLocalAvatarSpecification(alias, style)
        }

        try {
            val systemPrompt = """
                You are the Neural Avatar Compiler v2.4. Your task is to design a stylized cyberpunk visual layout specification based on a user's alias and chosen style.
                Return ONLY a valid JSON object (no markdown formatting, no backticks, no comments) with the following properties:
                - primaryColorHex: Hex color code (e.g. "#00FF00" for Matrix, "#FF00FF" for Neon)
                - secondaryColorHex: Hex color code (e.g. "#003300", "#00FFFF")
                - backgroundHex: Hex color code (e.g. "#050508")
                - glyphChar: A single unique futuristic/techno unicode character or symbol (e.g. "⚡", "☯", "⚡", "⌘", "❖", "⎈", "⚛", "☣", "☢", "⚿") representing their neural node.
                - visualDensity: An integer from 10 to 40 (number of particles/grid lines).
                - cryptographicHash: A unique 8-character hex string based on their alias.
                - nodeLabel: A stylized short prefix of their alias (max 6 chars) to render as text inside the avatar.
            """.trimIndent()

            val userPrompt = "Alias: $alias, Selected Style: $style"

            val rootJson = JSONObject().apply {
                val contentsArray = JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", userPrompt)
                            })
                        })
                    })
                }
                put("contents", contentsArray)

                val systemInstructionJson = JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", systemPrompt)
                        })
                    })
                }
                put("systemInstruction", systemInstructionJson)

                val genConfig = JSONObject().apply {
                    put("temperature", 0.7)
                }
                put("generationConfig", genConfig)
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = rootJson.toString().toRequestBody(mediaType)
            val url = "$BASE_URL?key=$rawApiKey"

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext generateLocalAvatarSpecification(alias, style)
                val responseBody = response.body?.string() ?: return@withContext generateLocalAvatarSpecification(alias, style)
                
                var cleanedResponse = responseBody.trim()
                if (cleanedResponse.startsWith("```json")) {
                    cleanedResponse = cleanedResponse.removePrefix("```json")
                    if (cleanedResponse.endsWith("```")) {
                        cleanedResponse = cleanedResponse.removeSuffix("```")
                    }
                } else if (cleanedResponse.startsWith("```")) {
                    cleanedResponse = cleanedResponse.removePrefix("```")
                    if (cleanedResponse.endsWith("```")) {
                        cleanedResponse = cleanedResponse.removeSuffix("```")
                    }
                }
                cleanedResponse = cleanedResponse.trim()
                
                val json = JSONObject(cleanedResponse)
                val candidates = json.optJSONArray("candidates")
                val text = candidates?.optJSONObject(0)
                    ?.optJSONObject("content")
                    ?.optJSONArray("parts")
                    ?.optJSONObject(0)
                    ?.optString("text")

                var finalCleaned = text?.trim() ?: ""
                if (finalCleaned.startsWith("```json")) {
                    finalCleaned = finalCleaned.removePrefix("```json")
                    if (finalCleaned.endsWith("```")) {
                        finalCleaned = finalCleaned.removeSuffix("```")
                    }
                } else if (finalCleaned.startsWith("```")) {
                    finalCleaned = finalCleaned.removePrefix("```")
                    if (finalCleaned.endsWith("```")) {
                        finalCleaned = finalCleaned.removeSuffix("```")
                    }
                }
                finalCleaned = finalCleaned.trim()

                if (finalCleaned.isNotBlank()) {
                    JSONObject(finalCleaned)
                    finalCleaned
                } else {
                    generateLocalAvatarSpecification(alias, style)
                }
            }
        } catch (e: Exception) {
            generateLocalAvatarSpecification(alias, style)
        }
    }

    fun generateLocalAvatarSpecification(alias: String, style: String): String {
        val hash = alias.hashCode().coerceAtLeast(0)
        val shortLabel = alias.take(6).uppercase()
        val cryptoHash = Integer.toHexString(hash).take(8).padStart(8, '0').uppercase()
        
        val specs = when (style.lowercase()) {
            "matrix", "matrix rain" -> {
                val glyphs = listOf("⎈", "⚡", "⌗", "⌬", "⌌", "⌎", "⍝")
                val selectedGlyph = glyphs[hash % glyphs.size]
                AvatarSpecs("#00FF66", "#003311", "#050A05", selectedGlyph, 25)
            }
            "neon", "neon synthwave" -> {
                val glyphs = listOf("☼", "❖", "⚡", "◆", "▲", "▼", "◄")
                val selectedGlyph = glyphs[hash % glyphs.size]
                AvatarSpecs("#FF007F", "#8B008B", "#0C020E", selectedGlyph, 30)
            }
            "neural", "neural synapses" -> {
                val glyphs = listOf("⬢", "⌬", "⚛", "⎉", "⎊", "⎋")
                val selectedGlyph = glyphs[hash % glyphs.size]
                AvatarSpecs("#00F0FF", "#004080", "#030810", selectedGlyph, 20)
            }
            "quantum", "quantum singularity" -> {
                val glyphs = listOf("⚙", "❂", "☉", "⊚", "⊛", "⊜")
                val selectedGlyph = glyphs[hash % glyphs.size]
                AvatarSpecs("#7A22FF", "#2E0854", "#04010A", selectedGlyph, 35)
            }
            else -> { // Arcade / Cybernetic Glyph
                val glyphs = listOf("⌘", "☯", "✖", "★", "✴", "✷", "✸")
                val selectedGlyph = glyphs[hash % glyphs.size]
                AvatarSpecs("#FFCC00", "#663300", "#0A0602", selectedGlyph, 15)
            }
        }
        
        return """
            {
                "primaryColorHex": "${specs.primary}",
                "secondaryColorHex": "${specs.secondary}",
                "backgroundHex": "${specs.bg}",
                "glyphChar": "${specs.glyph}",
                "visualDensity": ${specs.density},
                "cryptographicHash": "$cryptoHash",
                "nodeLabel": "$shortLabel"
            }
        """.trimIndent()
    }

    data class AvatarSpecs(val primary: String, val secondary: String, val bg: String, val glyph: String, val density: Int)

    fun renderAvatarToBitmap(
        context: Context,
        alias: String,
        avatarStyle: String,
        specJson: String
    ): File? {
        try {
            val width = 256
            val height = 256
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            
            val json = try {
                if (specJson.isBlank()) {
                    JSONObject(generateLocalAvatarSpecification(alias, avatarStyle))
                } else {
                    JSONObject(specJson)
                }
            } catch (e: Exception) {
                JSONObject(generateLocalAvatarSpecification(alias, avatarStyle))
            }
            
            val primaryColor = AndroidColor.parseColor(json.optString("primaryColorHex", "#00FF00"))
            val secondaryColor = AndroidColor.parseColor(json.optString("secondaryColorHex", "#003300"))
            val bgColor = AndroidColor.parseColor(json.optString("backgroundHex", "#050508"))
            val glyphChar = json.optString("glyphChar", "⌘")
            val density = json.optInt("visualDensity", 20)
            val cryptoHash = json.optString("cryptographicHash", "88888888")
            val nodeLabel = json.optString("nodeLabel", alias.take(6).uppercase())
            
            val bgPaint = Paint().apply {
                color = bgColor
                style = Paint.Style.FILL
            }
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)
            
            val linePaint = Paint().apply {
                color = primaryColor
                strokeWidth = 1.2f
                style = Paint.Style.STROKE
                alpha = 40
            }
            
            val hash = alias.hashCode().coerceAtLeast(0)
            val random = java.util.Random(hash.toLong())
            
            when (avatarStyle.lowercase()) {
                "matrix", "matrix rain" -> {
                    val textPaint = Paint().apply {
                        color = primaryColor
                        textSize = 10f
                        typeface = Typeface.MONOSPACE
                        alpha = 100
                    }
                    for (i in 0 until density) {
                        val x = random.nextInt(width).toFloat()
                        val y = random.nextInt(height).toFloat()
                        val binary = if (random.nextBoolean()) "1" else "0"
                        canvas.drawText(binary, x, y, textPaint)
                        canvas.drawText(binary, x, (y + 15) % height, textPaint)
                        canvas.drawText(binary, x, (y + 30) % height, textPaint)
                    }
                }
                "neon", "neon synthwave" -> {
                    val sunPaint = Paint().apply {
                        isAntiAlias = true
                        shader = LinearGradient(
                            0f, height * 0.2f, 0f, height * 0.7f,
                            primaryColor, secondaryColor, Shader.TileMode.CLAMP
                        )
                    }
                    canvas.drawCircle(width / 2f, height / 2f, 70f, sunPaint)
                    
                    val cutPaint = Paint().apply {
                        color = bgColor
                        style = Paint.Style.FILL
                    }
                    var cutY = height * 0.45f
                    while (cutY < height * 0.75f) {
                        canvas.drawRect(0f, cutY, width.toFloat(), cutY + 4f, cutPaint)
                        cutY += 12f
                    }
                    
                    for (i in 0..8) {
                        val xStart = (width / 8f) * i
                        canvas.drawLine(xStart, height * 0.7f, (width / 2f) + (xStart - width / 2f) * 2.5f, height.toFloat(), linePaint)
                    }
                    for (i in 0..5) {
                        val y = height * 0.7f + (height * 0.3f / 5) * i
                        canvas.drawLine(0f, y, width.toFloat(), y, linePaint)
                    }
                }
                "neural", "neural synapses" -> {
                    val nodePaint = Paint().apply {
                        color = primaryColor
                        style = Paint.Style.FILL
                        isAntiAlias = true
                    }
                    val pts = List(density / 2) {
                        Pair(random.nextFloat() * width, random.nextFloat() * height)
                    }
                    linePaint.alpha = 60
                    for (i in pts.indices) {
                        for (j in i + 1 until pts.size) {
                            val dist = Math.hypot((pts[i].first - pts[j].first).toDouble(), (pts[i].second - pts[j].second).toDouble())
                            if (dist < 80.0) {
                                canvas.drawLine(pts[i].first, pts[i].second, pts[j].first, pts[j].second, linePaint)
                            }
                        }
                    }
                    for (pt in pts) {
                        canvas.drawCircle(pt.first, pt.second, 4f, nodePaint)
                    }
                }
                "quantum", "quantum singularity" -> {
                    val circlePaint = Paint().apply {
                        style = Paint.Style.STROKE
                        isAntiAlias = true
                    }
                    for (i in 1..density / 2) {
                        circlePaint.apply {
                            color = primaryColor
                            strokeWidth = (i % 3 + 1).toFloat()
                            alpha = (255 / i).coerceIn(10, 200)
                        }
                        canvas.drawCircle(width / 2f, height / 2f, (i * 8).toFloat(), circlePaint)
                    }
                }
                else -> {
                    val glyphPaint = Paint().apply {
                        color = primaryColor
                        style = Paint.Style.STROKE
                        strokeWidth = 3f
                        isAntiAlias = true
                        alpha = 180
                    }
                    val path = Path()
                    path.moveTo(width / 2f, 40f)
                    path.lineTo(width - 50f, height / 2f)
                    path.lineTo(width / 2f, height - 40f)
                    path.lineTo(50f, height / 2f)
                    path.close()
                    canvas.drawPath(path, glyphPaint)
                    
                    path.reset()
                    path.moveTo(width / 2f, 60f)
                    path.lineTo(width - 70f, height / 2f)
                    path.lineTo(width / 2f, height - 60f)
                    path.lineTo(70f, height / 2f)
                    path.close()
                    canvas.drawPath(path, glyphPaint)
                }
            }
            
            val glyphPaint = Paint().apply {
                color = primaryColor
                textSize = 54f
                isAntiAlias = true
                textAlign = Paint.Align.CENTER
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
                setShadowLayer(15f, 0f, 0f, primaryColor)
            }
            val textBounds = Rect()
            glyphPaint.getTextBounds(glyphChar, 0, glyphChar.length, textBounds)
            val glyphY = (height / 2f) - textBounds.exactCenterY()
            canvas.drawText(glyphChar, width / 2f, glyphY, glyphPaint)
            
            val textPaint = Paint().apply {
                color = primaryColor
                textSize = 10f
                alpha = 180
                typeface = Typeface.MONOSPACE
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("NODE: $nodeLabel", width / 2f, 25f, textPaint)
            canvas.drawText("HASH: $cryptoHash", width / 2f, height - 15f, textPaint)
            
            val borderPaint = Paint().apply {
                color = primaryColor
                strokeWidth = 4f
                style = Paint.Style.STROKE
                isAntiAlias = true
                alpha = 150
            }
            canvas.drawRect(2f, 2f, width - 2f, height - 2f, borderPaint)
            
            val file = File(context.filesDir, "custom_avatar.png")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            return file
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    suspend fun generateCustomArcana(
        userConcept: String,
        systemLanguage: String = "en"
    ): GeneratedArcanaData = withContext(Dispatchers.IO) {
        val rawApiKey = BuildConfig.GEMINI_API_KEY
        val isApiKeyInvalid = rawApiKey.isBlank() ||
                rawApiKey == "MY_GEMINI_API_KEY" ||
                rawApiKey.contains("placeholder", ignoreCase = true)

        if (!isApiKeyInvalid) {
            try {
                val promptText = """
                    You are a cyberpunk occult game designer for a Cyber Tarot TCG game.
                    Create a unique custom battle card from this user concept: "$userConcept".
                    Language: $systemLanguage.
                    Respond ONLY with valid JSON with keys:
                    "name" (Cyberpunk title, e.g. "Neon Blade Runner"),
                    "traditionalName" (Tarot archetype, e.g. "The Chariot Prime"),
                    "element" (One of: "CYBER", "BIO", "QUANTUM", "VOID"),
                    "rarity" (One of: "Rare", "Epic", "Legend"),
                    "energyCost" (integer 2 to 5),
                    "power" (integer 4 to 9),
                    "defense" (integer 4 to 9),
                    "abilityTitle" (Short cyber ability title),
                    "abilityDescription" (Ability description, 1 sentence),
                    "lore" (Atmospheric cyber lore, 2 sentences),
                    "advice" (Oracle advice quote, 1 sentence).
                """.trimIndent()

                val requestJson = JSONObject().apply {
                    put("contents", JSONArray().apply {
                        put(JSONObject().apply {
                            put("parts", JSONArray().apply {
                                put(JSONObject().apply { put("text", promptText) })
                            })
                        })
                    })
                }

                val request = Request.Builder()
                    .url("$BASE_URL?key=$rawApiKey")
                    .post(requestJson.toString().toRequestBody("application/json".toMediaType()))
                    .build()

                val response = client.newCall(request).execute()
                val body = response.body?.string()
                if (response.isSuccessful && !body.isNullOrBlank()) {
                    val root = JSONObject(body)
                    val candidates = root.optJSONArray("candidates")
                    val contentObj = candidates?.optJSONObject(0)?.optJSONObject("content")
                    val parts = contentObj?.optJSONArray("parts")
                    val text = parts?.optJSONObject(0)?.optString("text") ?: ""
                    val jsonStart = text.indexOf('{')
                    val jsonEnd = text.lastIndexOf('}')
                    if (jsonStart != -1 && jsonEnd != -1 && jsonEnd > jsonStart) {
                        val parsed = JSONObject(text.substring(jsonStart, jsonEnd + 1))
                        return@withContext GeneratedArcanaData(
                            name = parsed.optString("name", "Cyber Arcana"),
                            traditionalName = parsed.optString("traditionalName", "The Archetype"),
                            element = parsed.optString("element", "CYBER").uppercase(),
                            rarity = parsed.optString("rarity", "Epic"),
                            energyCost = parsed.optInt("energyCost", 3).coerceIn(1, 5),
                            power = parsed.optInt("power", 6).coerceIn(2, 10),
                            defense = parsed.optInt("defense", 6).coerceIn(2, 10),
                            abilityTitle = parsed.optString("abilityTitle", "Overclock Core"),
                            abilityDescription = parsed.optString("abilityDescription", "Deals burst damage on deploy"),
                            lore = parsed.optString("lore", "Forged in the decentralized quantum depths."),
                            advice = parsed.optString("advice", "Sync your neural link before proceeding.")
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // High-fidelity Procedural Generator Fallback
        val isRu = systemLanguage == "ru"
        val lower = userConcept.lowercase()
        val element = when {
            lower.contains("био") || lower.contains("раст") || lower.contains("лист") || lower.contains("лес") || lower.contains("bio") || lower.contains("plant") -> "BIO"
            lower.contains("квант") || lower.contains("маг") || lower.contains("звезд") || lower.contains("quantum") || lower.contains("star") -> "QUANTUM"
            lower.contains("пустот") || lower.contains("тьм") || lower.contains("тень") || lower.contains("черн") || lower.contains("void") || lower.contains("shadow") -> "VOID"
            else -> "CYBER"
        }

        val formattedConcept = userConcept.trim().replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString() }
        val name = if (isRu) {
            "Нейро-Аркан «$formattedConcept»"
        } else {
            "Cyber Arcana: $formattedConcept"
        }

        val archetype = when (element) {
            "BIO" -> if (isRu) "Верховный Друид Био-Матрицы" else "Bio-Matrix Druid"
            "QUANTUM" -> if (isRu) "Квантовый Пророк Сети" else "Quantum Net Prophet"
            "VOID" -> if (isRu) "Теневой Сталкер Даркнета" else "Darknet Shadow Stalker"
            else -> if (isRu) "Мастер Чипов и Кибер-Огня" else "Master of Cyber-Fire"
        }

        val abilityTitle = when (element) {
            "BIO" -> if (isRu) "Био-Регенерация" else "Bio-Regen"
            "QUANTUM" -> if (isRu) "Квантовая Фазировка" else "Quantum Phasing"
            "VOID" -> if (isRu) "Поглощение Пустоты" else "Void Siphon"
            else -> if (isRu) "Лазерная Перегрузка" else "Laser Overclock"
        }

        val abilityDesc = when (element) {
            "BIO" -> if (isRu) "Восстанавливает +4 HP при успешной атаке" else "Restores +4 HP on attack"
            "QUANTUM" -> if (isRu) "Дает +5 очков щита при выкладке на доску" else "Grants +5 shield barrier on deploy"
            "VOID" -> if (isRu) "Сжигает 1 единицу RAM у соперника" else "Burns 1 enemy RAM node"
            else -> if (isRu) "Наносит +3 урона при прямом ударе" else "Deals +3 instant burst damage"
        }

        GeneratedArcanaData(
            name = name,
            traditionalName = archetype,
            element = element,
            rarity = if (userConcept.length > 10) "Legend" else "Epic",
            energyCost = 3,
            power = 6,
            defense = 6,
            abilityTitle = abilityTitle,
            abilityDescription = abilityDesc,
            lore = if (isRu) "Создан в ходе синтеза нейронных весов и оккультных символов сети." else "Forged through neural weight synthesis and occult network symbols.",
            advice = if (isRu) "Доверяйте интуиции матрицы и держите баланс стихий." else "Trust matrix intuition and maintain elemental equilibrium."
        )
    }
}

data class GeneratedArcanaData(
    val name: String,
    val traditionalName: String,
    val element: String,
    val rarity: String,
    val energyCost: Int,
    val power: Int,
    val defense: Int,
    val abilityTitle: String,
    val abilityDescription: String,
    val lore: String,
    val advice: String
)
