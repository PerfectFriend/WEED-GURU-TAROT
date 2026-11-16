package com.example.data

import android.content.Context
import com.example.BuildConfig
import com.example.ui.util.AvatarGenerator
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.net.InetSocketAddress
import java.net.Proxy
import java.util.concurrent.TimeUnit

object GeminiService {

    private const val DEFAULT_MODEL_NAME = "gemini-2.5-flash"

    @Volatile
    private var configuredModelName: String = DEFAULT_MODEL_NAME

    fun configureModel(modelName: String) {
        if (modelName.isNotBlank()) {
            configuredModelName = modelName.trim()
        }
    }

    internal fun buildEndpointUrl(modelName: String = configuredModelName): String =
        "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(45, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(45, TimeUnit.SECONDS)
        .build()

    private fun resolveConfiguredApiKey(): String? {
        val raw = BuildConfig.GEMINI_API_KEY
        if (raw.isBlank() || raw == "MY_GEMINI_API_KEY" || raw.contains("placeholder", ignoreCase = true)) {
            return null
        }
        return raw
    }

    private fun buildClient(
        proxyType: String = "NONE",
        proxyHost: String = "127.0.0.1",
        proxyPort: Int = 9050,
        isProxyActive: Boolean = false
    ): OkHttpClient {
        if (!isProxyActive || proxyType == "NONE") return client
        val pType = if (proxyType.uppercase() == "HTTP") Proxy.Type.HTTP else Proxy.Type.SOCKS
        // Fail closed when user enabled proxy so client IP never leaks on direct fallback (M5)
        val address = InetSocketAddress(proxyHost, proxyPort)
        return client.newBuilder()
            .proxy(Proxy(pType, address))
            .build()
    }

    internal suspend fun executeGeminiCall(
        apiKey: String,
        userPrompt: String,
        systemPrompt: String? = null,
        temperature: Double = 0.7,
        httpClient: OkHttpClient = client
    ): String? {
        val rootJson = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", userPrompt) })
                    })
                })
            })
            if (!systemPrompt.isNullOrBlank()) {
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", systemPrompt) })
                    })
                })
            }
            put("generationConfig", JSONObject().apply {
                put("temperature", temperature)
            })
        }

        val requestBody = rootJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder()
            .url(buildEndpointUrl())
            .addHeader("x-goog-api-key", apiKey)
            .post(requestBody)
            .build()

        val retryDelaysMs = longArrayOf(400L, 1000L)
        for (attempt in 0..retryDelaysMs.size) {
            try {
                httpClient.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val responseBody = response.body?.string() ?: return null
                        val responseJson = JSONObject(responseBody)
                        return responseJson.optJSONArray("candidates")
                            ?.optJSONObject(0)
                            ?.optJSONObject("content")
                            ?.optJSONArray("parts")
                            ?.optJSONObject(0)
                            ?.optString("text")
                            ?.takeIf { it.isNotBlank() }
                    }
                    val retryable = response.code == 429 || response.code >= 500
                    if (!retryable || attempt == retryDelaysMs.size) {
                        return null
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: java.io.IOException) {
                if (attempt == retryDelaysMs.size) return null
            }
            kotlinx.coroutines.delay(retryDelaysMs[attempt])
        }
        return null
    }

    private fun stripMarkdownCodeFences(raw: String): String {
        var cleaned = raw.trim()
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.removePrefix("```json")
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.removePrefix("```")
        }
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.removeSuffix("```")
        }
        return cleaned.trim()
    }

    /**
     * Decodes and reads a spread of drawn cards using the Gemini API.
     * Falls back to the local cyberpunk diagnostic generator when offline or unconfigured.
     */
    suspend fun decodeTarotMatrix(
        userQuestion: String,
        drawnCards: List<Pair<CyberTarotCard, Boolean>>,
        systemLanguage: String = "en",
        proxyType: String = "NONE",
        proxyHost: String = "127.0.0.1",
        proxyPort: Int = 9050,
        isProxyActive: Boolean = false
    ): String = withContext(Dispatchers.IO) {
        val apiKey = resolveConfiguredApiKey()
            ?: return@withContext generateLocalCyberInterpretation(userQuestion, drawnCards, systemLanguage)

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

            // Keep untrusted user query out of systemInstruction (M5)
            val systemPrompt = """
                You are the Cyber Tarot Matrix Decoder. An expert system designed to interpret a futuristic cybernetic fork of traditional Rider-Waite symbolism.
                The cards drawn represent nodes in the user's current computational life-path, incorporating digital circuitry elements, neon aesthetics, and cybersecurity concepts.

                IMPORTANT: You MUST write the entire decryption report, headings, analysis, advice, and recommendations in the language: $langName.
                Do not write in English unless requested. The user is communicating in $langName. Keep the cyberpunk vocabulary appropriate to $langName.

                For each card in the user prompt, elaborate on:
                - How its futuristic representation (e.g. "The Glitch" for "The Fool", "The SysAdmin" for "The Emperor") maps to their situation.
                - The meaning of its digital circuitry analogies (e.g. Volatile RAM, Hardware Firewalls, JTAG Debuggers) relative to their path.
                - Practical guidance formatted in a professional cyber-themed style (use words like compile, buffer, bandwidth, firewall, decrypt, protocol).

                Keep your output beautifully organized with clean headings, markdown, and clear separation of cards. Wrap with an introductory transmission sync and a final 'Consensus Recommendation' from the Core Processor.
            """.trimIndent()

            val cardDrawsDescription = drawnCards.joinToString("\n") { (card, isUpright) ->
                val cardName = CardLocalizationHelper.getLocalizedName(card.index, systemLanguage)
                val tradName = CardLocalizationHelper.getLocalizedTraditionalName(card.index, systemLanguage)
                val desc = CardLocalizationHelper.getLocalizedDescription(card.index, systemLanguage)
                val analogy = CardLocalizationHelper.getLocalizedCircuitryElement(card.index, systemLanguage)
                val meaning = CardLocalizationHelper.getLocalizedCyberMeaning(card.index, systemLanguage)
                val advice = if (isUpright) {
                    CardLocalizationHelper.getLocalizedUprightAdvice(card.index, systemLanguage)
                } else {
                    CardLocalizationHelper.getLocalizedReversedAdvice(card.index, systemLanguage)
                }
                "- Card: $cardName (${if (isUpright) "UPRIGHT" else "REVERSED"}). Represents: $tradName. Rarity: ${card.defaultRarity}. Circuit Analogy: $analogy. Summary: $desc. Meaning: $meaning. Advice: $advice"
            }

            val userPrompt = """
                Query from user terminal: "$userQuestion"

                Drawn Nodes:
                $cardDrawsDescription

                Generate the decryption report:
            """.trimIndent()

            val configuredClient = buildClient(proxyType, proxyHost, proxyPort, isProxyActive)
            executeGeminiCall(
                apiKey = apiKey,
                userPrompt = userPrompt,
                systemPrompt = systemPrompt,
                temperature = 0.7,
                httpClient = configuredClient
            ) ?: generateLocalCyberInterpretation(userQuestion, drawnCards, systemLanguage)
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            generateLocalCyberInterpretation(userQuestion, drawnCards, systemLanguage)
        }
    }

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
                "all_upright" to "पूर्ण पैमाने पर पाइपलाइन रोलआउट निष्पादित करें। आपके नोड्स असाधारण संरेखण प्रदर्शित करते हैं।",
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
            val localizedAdvice = if (isUpright) {
                CardLocalizationHelper.getLocalizedUprightAdvice(card.index, systemLanguage)
            } else {
                CardLocalizationHelper.getLocalizedReversedAdvice(card.index, systemLanguage)
            }

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

        if (drawnCards.size == 1) {
            val (c, upright) = drawnCards[0]
            val cardName = CardLocalizationHelper.getLocalizedName(c.index, systemLanguage)
            sb.append(if (systemLanguage == "ru") "использовать активные качества `$cardName`. " else "utilize the active traits of `$cardName`. ")
            sb.append(if (upright) strings["one_upright"] else strings["one_reversed"])
        } else {
            val balanceCount = drawnCards.count { it.second }
            when (balanceCount) {
                drawnCards.size -> sb.append(strings["all_upright"])
                0 -> sb.append(strings["all_reversed"])
                else -> sb.append(strings["balanced"])
            }
        }

        return sb.toString()
    }

    suspend fun generatePeerResponse(
        peerName: String,
        peerOnion: String,
        userMessage: String,
        chatHistory: List<Pair<String, String>>
    ): String = withContext(Dispatchers.IO) {
        val apiKey = resolveConfiguredApiKey()
            ?: return@withContext generateLocalMockPeerResponse(peerName, userMessage)

        try {
            val systemPrompt = """
                You are a simulated remote cyberpunk operator in a local sandbox network.
                Your name is "$peerName" (sandbox node: "$peerOnion").

                Respond to the user's message as this character. Keep your reply concise (1-3 sentences maximum).
                Use futuristic cyberpunk terminology (e.g. mainframe, grid, encryption, node, bandwidth, terminal, proxy, breach, protocols).
                Maintain a hacker tone depending on your persona:
                - If your name is "Cipher_Alpha", be a precise, mysterious cryptographer focused on encryption.
                - If your name is "Proxy_Net", be a fast-talking data broker focused on credits and routing.
                - For other names, be a cautious, helpful operator.

                Never break character. Never mention you are an AI model.
            """.trimIndent()

            val historyString = chatHistory.joinToString("\n") { (sender, text) -> "$sender: $text" }
            val userPrompt = """
                Chat history:
                $historyString

                $peerName, generate your next direct response to the user's last message:
            """.trimIndent()

            executeGeminiCall(
                apiKey = apiKey,
                userPrompt = userPrompt,
                systemPrompt = systemPrompt,
                temperature = 0.8
            )?.trim() ?: generateLocalMockPeerResponse(peerName, userMessage)
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            generateLocalMockPeerResponse(peerName, userMessage)
        }
    }

    private fun generateLocalMockPeerResponse(peerName: String, userMessage: String): String {
        return when {
            userMessage.contains("hello", ignoreCase = true) || userMessage.contains("hi", ignoreCase = true) ->
                "[$peerName]: Handshake successful. Encrypted local channel active."
            userMessage.contains("tarot", ignoreCase = true) || userMessage.contains("draw", ignoreCase = true) ->
                "[$peerName]: Divination telemetry synced. Sandbox ledger online."
            userMessage.contains("key", ignoreCase = true) || userMessage.contains("crypto", ignoreCase = true) ->
                "[$peerName]: ECDH key verification intact. Keep local keys secure."
            userMessage.contains("bridge", ignoreCase = true) || userMessage.contains("tor", ignoreCase = true) ->
                "[$peerName]: Proxy gateway telemetry checked."
            else ->
                "[$peerName]: Telemetry packet received. Hash matching verified."
        }
    }

    suspend fun generateAdBannerContent(prompt: String): Pair<String, String> = withContext(Dispatchers.IO) {
        val apiKey = resolveConfiguredApiKey()
            ?: return@withContext generateLocalAdBannerContent(prompt)

        try {
            val systemPrompt = """
                You are a neural marketing terminal of the Cyber Tarot network. Transform the user-supplied prompt into a stylized cyberpunk advertisement banner.

                Return exactly TWO lines of text (no headings, no markdown, no quotes):
                Line 1: A brief, punchy TITLE of 2 to 4 words, in ALL CAPS.
                Line 2: A futuristic TAGLINE of exactly 1 sentence (max 15 words).

                Match the language of the input prompt.
            """.trimIndent()

            val text = executeGeminiCall(
                apiKey = apiKey,
                userPrompt = "User Prompt: $prompt",
                systemPrompt = systemPrompt,
                temperature = 0.8
            )
            if (!text.isNullOrBlank()) {
                val lines = text.trim().lines().filter { it.isNotBlank() }
                if (lines.size >= 2) {
                    val title = lines[0].trim().replace("\"", "").uppercase()
                    val tagline = lines[1].trim().replace("\"", "")
                    return@withContext Pair(title, tagline)
                }
            }
            generateLocalAdBannerContent(prompt)
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            generateLocalAdBannerContent(prompt)
        }
    }

    private fun generateLocalAdBannerContent(prompt: String): Pair<String, String> {
        val cleaned = prompt.trim().lowercase()
        return when {
            cleaned.contains("hack") || cleaned.contains("декрипт") || cleaned.contains("взлом") ->
                Pair("NEURAL BREACH TERMINAL", "Bypass level-5 hardware firewalls and access locked matrix archives instantly.")
            cleaned.contains("имплант") || cleaned.contains("chrome") || cleaned.contains("био") || cleaned.contains("body") ->
                Pair("CHROME VECTORS", "High-frequency cybernetic body upgrades calibrated for seamless neural integration.")
            cleaned.contains("money") || cleaned.contains("кредит") || cleaned.contains("swap") || cleaned.contains("finance") ->
                Pair("QUANTUM LEDGER MINING", "Earn passive credits inside the local decentralized simulation node.")
            else -> {
                val titleWords = prompt.split(" ").filter { it.isNotBlank() }.take(3).map { it.uppercase() }
                val title = if (titleWords.isNotEmpty()) titleWords.joinToString(" ") else "MAIN CORE BROADCAST"
                Pair(title, "Optimized by the Cyber Core: $prompt")
            }
        }
    }

    suspend fun generateAvatarSpecification(alias: String, style: String): String = withContext(Dispatchers.IO) {
        val apiKey = resolveConfiguredApiKey()
            ?: return@withContext generateLocalAvatarSpecification(alias, style)

        try {
            val systemPrompt = """
                You are the Neural Avatar Compiler v2.4. Design a stylized cyberpunk visual layout specification based on the user prompt's alias and style.
                Return ONLY a valid JSON object (no markdown formatting, no backticks, no comments) with keys:
                - primaryColorHex, secondaryColorHex, backgroundHex, glyphChar, visualDensity (10..40), cryptographicHash (8 hex chars), nodeLabel (max 6 chars).
            """.trimIndent()

            val text = executeGeminiCall(
                apiKey = apiKey,
                userPrompt = "Alias: $alias, Selected Style: $style",
                systemPrompt = systemPrompt,
                temperature = 0.7
            ) ?: return@withContext generateLocalAvatarSpecification(alias, style)

            val cleaned = stripMarkdownCodeFences(text)
            if (cleaned.isNotBlank()) {
                JSONObject(cleaned) // Validate JSON before returning
                cleaned
            } else {
                generateLocalAvatarSpecification(alias, style)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            generateLocalAvatarSpecification(alias, style)
        }
    }

    fun generateLocalAvatarSpecification(alias: String, style: String): String =
        AvatarGenerator.generateLocalAvatarSpecification(alias, style)

    fun renderAvatarToBitmap(
        context: Context,
        alias: String,
        avatarStyle: String,
        specJson: String
    ): File? = AvatarGenerator.renderAvatarToBitmap(context, alias, avatarStyle, specJson)

    suspend fun generateCustomArcana(
        userConcept: String,
        systemLanguage: String = "en"
    ): GeneratedArcanaData = withContext(Dispatchers.IO) {
        val apiKey = resolveConfiguredApiKey()
        if (apiKey != null) {
            try {
                val systemPrompt = """
                    You are a cyberpunk occult game designer for a Cyber Tarot TCG game.
                    Respond ONLY with valid JSON with keys:
                    "name", "traditionalName", "element" (CYBER|BIO|QUANTUM|VOID),
                    "rarity" (Rare|Epic|Legend), "energyCost" (2..5), "power" (4..9), "defense" (4..9),
                    "abilityTitle", "abilityDescription" (1 sentence), "lore" (2 sentences), "advice" (1 sentence).
                """.trimIndent()

                val text = executeGeminiCall(
                    apiKey = apiKey,
                    userPrompt = "Create a unique custom battle card in language '$systemLanguage' from user concept: \"$userConcept\".",
                    systemPrompt = systemPrompt,
                    temperature = 0.7
                )
                if (!text.isNullOrBlank()) {
                    val jsonStart = text.indexOf('{')
                    val jsonEnd = text.lastIndexOf('}')
                    if (jsonStart != -1 && jsonEnd > jsonStart) {
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
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                // Fall through cleanly to local procedural generator
            }
        }

        val isRu = systemLanguage == "ru"
        val lower = userConcept.lowercase()
        val element = when {
            lower.contains("био") || lower.contains("раст") || lower.contains("лист") || lower.contains("лес") || lower.contains("bio") || lower.contains("plant") -> "BIO"
            lower.contains("квант") || lower.contains("маг") || lower.contains("звезд") || lower.contains("quantum") || lower.contains("star") -> "QUANTUM"
            lower.contains("пустот") || lower.contains("тьм") || lower.contains("тень") || lower.contains("черн") || lower.contains("void") || lower.contains("shadow") -> "VOID"
            else -> "CYBER"
        }

        val formattedConcept = userConcept.trim().replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString()
        }
        val name = if (isRu) "Нейро-Аркан «$formattedConcept»" else "Cyber Arcana: $formattedConcept"
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
