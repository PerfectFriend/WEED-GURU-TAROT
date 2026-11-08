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

            // Lunar phase & minting info
            val lunarInfo = LunarCalendarService.getLunarDayInfo(year, month, day, "ru")
            val mintingWindow = LunarCalendarService.getTarotMintingWindow(year, month, day, "ru")

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
                    com.example.data.MintingAuspiciousnessTier.AUSPICIOUS -> "MINT: AUSPICIOUS ✦"
                    com.example.data.MintingAuspiciousnessTier.BALANCED -> "MINT: BALANCED ~"
                    com.example.data.MintingAuspiciousnessTier.CHALLENGING -> "MINT: VOID FLUX ⚠"
                }
            )
            views.setTextViewText(R.id.widget_element_icon, element.symbol)
            views.setTextViewText(R.id.widget_card_title, card.name)
            views.setTextViewText(
                R.id.widget_card_subtitle,
                "${card.traditionalName} • ${card.defaultRarity}"
            )
            views.setTextViewText(
                R.id.widget_card_advice,
                "Оракул: ${card.uprightAdvice}"
            )

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
