package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.CyberTarotDeck
import com.example.data.LunarCalendarService
import com.example.duel.ElementAffinity
import java.util.Calendar

class TarotAppWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val cal = Calendar.getInstance()
            val year = cal.get(Calendar.YEAR)
            val month = cal.get(Calendar.MONTH) + 1
            val day = cal.get(Calendar.DAY_OF_MONTH)
            val dayOfYear = cal.get(Calendar.DAY_OF_YEAR)

            val prefs = context.getSharedPreferences("neuro_tarot_prefs", Context.MODE_PRIVATE)
            val savedLang = prefs.getString("system_language", null)
            val deviceLang = java.util.Locale.getDefault().language
            val activeLang = when {
                !savedLang.isNullOrBlank() -> savedLang
                deviceLang in listOf("en", "ru", "es", "zh", "fr", "de", "pt", "hi") -> deviceLang
                else -> "en"
            }

            // Lunar phase & minting info
            val lunarInfo = LunarCalendarService.getLunarDayInfo(year, month, day, activeLang)
            val mintingWindow = LunarCalendarService.getTarotMintingWindow(year, month, day, activeLang)

            // Card of the Day
            val card = CyberTarotDeck.cards[dayOfYear % CyberTarotDeck.cards.size]
            val element = when (card.index % 4) {
                0 -> ElementAffinity.CYBER
                1 -> ElementAffinity.BIO
                2 -> ElementAffinity.QUANTUM
                else -> ElementAffinity.VOID
            }

            val views = RemoteViews(context.packageName, R.layout.widget_cyber_tarot)

            views.setTextViewText(
                R.id.widget_lunar_phase,
                "${lunarInfo.phaseIcon} ${lunarInfo.phaseName} • ${lunarInfo.illumination}%"
            )
            views.setTextViewText(
                R.id.widget_core_status,
                when (mintingWindow.tier) {
                    com.example.data.MintingAuspiciousnessTier.AUSPICIOUS -> when (activeLang) {
                        "ru" -> "МИНТ: БЛАГОПРИЯТНО ✦"
                        "es" -> "MINT: PROPICIO ✦"
                        "zh" -> "铸造: 吉兆 ✦"
                        "fr" -> "MINT: PROPICE ✦"
                        "de" -> "MINT: GÜNSTIG ✦"
                        "pt" -> "MINT: PROPÍCIO ✦"
                        "hi" -> "मिंट: शुभ ✦"
                        else -> "MINT: AUSPICIOUS ✦"
                    }
                    com.example.data.MintingAuspiciousnessTier.BALANCED -> when (activeLang) {
                        "ru" -> "МИНТ: БАЛАНС ~"
                        "es" -> "MINT: EQUILIBRIO ~"
                        "zh" -> "铸造: 平衡 ~"
                        "fr" -> "MINT: ÉQUILIBRE ~"
                        "de" -> "MINT: AUSGEGLICHEN ~"
                        "pt" -> "MINT: EQUILÍBRIO ~"
                        "hi" -> "मिंट: संतुलित ~"
                        else -> "MINT: BALANCED ~"
                    }
                    com.example.data.MintingAuspiciousnessTier.CHALLENGING -> when (activeLang) {
                        "ru" -> "МИНТ: ВОЗМУЩЕНИЕ ⚠"
                        "es" -> "MINT: INESTABLE ⚠"
                        "zh" -> "铸造: 虚空波动 ⚠"
                        "fr" -> "MINT: INSTABLE ⚠"
                        "de" -> "MINT: INSTABIL ⚠"
                        "pt" -> "MINT: INSTÁVEL ⚠"
                        "hi" -> "मिंट: अस्थिर ⚠"
                        else -> "MINT: VOID FLUX ⚠"
                    }
                }
            )
            views.setTextViewText(R.id.widget_element_icon, element.symbol)
            val localizedTitle = com.example.data.CardLocalizationHelper.getLocalizedName(card.index, activeLang)
            val localizedTraditional = com.example.data.CardLocalizationHelper.getLocalizedTraditionalName(card.index, activeLang)
            val localizedAdvice = com.example.data.CardLocalizationHelper.getLocalizedUprightAdvice(card.index, activeLang)
            val oracleLabel = when (activeLang) {
                "ru" -> "Оракул"
                "es" -> "Oráculo"
                "zh" -> "神谕"
                "fr" -> "Oracle"
                "de" -> "Orakel"
                "pt" -> "Oráculo"
                "hi" -> "ओरेकल"
                else -> "Oracle"
            }
            val openHubPrompt = when (activeLang) {
                "ru" -> "➔ ОТКРЫТЬ В ХАБЕ"
                "es" -> "➔ ABRIR EN EL HUB"
                "zh" -> "➔ 在中心打开"
                "fr" -> "➔ OUVRIR DANS LE HUB"
                "de" -> "➔ IM HUB ÖFFNEN"
                "pt" -> "➔ ABRIR NO HUB"
                "hi" -> "➔ हब में खोलें"
                else -> "➔ OPEN IN HUB"
            }

            views.setTextViewText(R.id.widget_card_title, localizedTitle)
            views.setTextViewText(
                R.id.widget_card_subtitle,
                "$localizedTraditional • ${card.defaultRarity}"
            )
            views.setTextViewText(
                R.id.widget_card_advice,
                "$oracleLabel: $localizedAdvice"
            )
            views.setTextViewText(R.id.widget_action_prompt, openHubPrompt)

            // Deep link into MainActivity
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("EXTRA_DEEP_LINK", "CARD_OF_DAY")
                putExtra("EXTRA_CARD_INDEX", card.index)
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
