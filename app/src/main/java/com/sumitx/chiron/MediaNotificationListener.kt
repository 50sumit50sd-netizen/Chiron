package com.sumitx.chiron

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.app.Notification

class MediaNotificationListener : NotificationListenerService() {
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val extras = sbn.notification.extras ?: return
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: return
        val artist = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString().orEmpty()
        getSharedPreferences("sumitx_media", MODE_PRIVATE)
            .edit()
            .putString("title", title)
            .putString("artist", artist)
            .putString("package", sbn.packageName)
            .apply()
    }
}
