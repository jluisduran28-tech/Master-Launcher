package com.example.tvlauncher

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.media.AudioManager
import android.os.Build
import android.provider.Settings
import android.util.Base64
import android.webkit.JavascriptInterface
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream

/** Puente JS <-> Android. Se expone en la web como window.TVBridge. */
class TVBridge(private val ctx: Context) {
    private val pm = ctx.packageManager
    private val am = ctx.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    /** Apps instaladas: TV (leanback) primero y luego las normales. [{n,p,i(base64 png)}] */
    @JavascriptInterface
    fun getApps(): String {
        val seen = HashSet<String>()
        val arr = JSONArray()
        for (cat in listOf(Intent.CATEGORY_LEANBACK_LAUNCHER, Intent.CATEGORY_LAUNCHER)) {
            val q = Intent(Intent.ACTION_MAIN).addCategory(cat)
            val list = pm.queryIntentActivities(q, 0).sortedBy { it.loadLabel(pm).toString().lowercase() }
            for (ri in list) {
                val pkg = ri.activityInfo.packageName
                if (pkg == ctx.packageName || !seen.add(pkg)) continue
                arr.put(
                    JSONObject()
                        .put("n", ri.loadLabel(pm).toString())
                        .put("p", pkg)
                        .put("i", iconB64(ri.loadIcon(pm)))
                )
            }
        }
        return arr.toString()
    }

    @JavascriptInterface
    fun launch(pkg: String): Boolean {
        val i = pm.getLeanbackLaunchIntentForPackage(pkg) ?: pm.getLaunchIntentForPackage(pkg) ?: return false
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return try { ctx.startActivity(i); true } catch (e: Exception) { false }
    }

    @JavascriptInterface
    fun volume(dir: Int) {
        am.adjustStreamVolume(
            AudioManager.STREAM_MUSIC,
            if (dir > 0) AudioManager.ADJUST_RAISE else AudioManager.ADJUST_LOWER, 0
        )
    }

    @JavascriptInterface
    fun mute() = am.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_TOGGLE_MUTE, 0)

    @JavascriptInterface
    fun volState(): String = JSONObject()
        .put("v", am.getStreamVolume(AudioManager.STREAM_MUSIC))
        .put("m", am.getStreamMaxVolume(AudioManager.STREAM_MUSIC))
        .put("mu", if (Build.VERSION.SDK_INT >= 23) am.isStreamMute(AudioManager.STREAM_MUSIC) else false)
        .toString()

    /** Bloquea la pantalla si el launcher es administrador del dispositivo. Devuelve false si no puede. */
    @JavascriptInterface
    fun powerOff(): Boolean {
        val dpm = ctx.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val cn = ComponentName(ctx, AdminReceiver::class.java)
        return if (dpm.isAdminActive(cn)) { dpm.lockNow(); true } else false
    }

    /** Abre el Asistente de Google (o el asistente de voz que tenga la TV). false si no hay ninguno. */
    @JavascriptInterface
    fun assistant(): Boolean {
        for (action in listOf(
            "android.intent.action.VOICE_ASSIST",
            "android.intent.action.ASSIST",
            "android.intent.action.VOICE_COMMAND"
        )) {
            try {
                ctx.startActivity(Intent(action).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                return true
            } catch (e: Exception) { /* probar el siguiente */ }
        }
        return false
    }

    /** Fondo de pantalla actual del sistema Android (JPEG en base64) o "" si no está disponible. */
    @JavascriptInterface
    fun systemWallpaper(): String {
        return try {
            val d = android.app.WallpaperManager.getInstance(ctx).drawable ?: return ""
            val bmp = Bitmap.createBitmap(1280, 720, Bitmap.Config.ARGB_8888)
            d.setBounds(0, 0, 1280, 720)
            d.draw(Canvas(bmp))
            val bo = ByteArrayOutputStream()
            bmp.compress(Bitmap.CompressFormat.JPEG, 80, bo)
            bmp.recycle()
            Base64.encodeToString(bo.toByteArray(), Base64.NO_WRAP)
        } catch (e: Exception) { "" }
    }

    @JavascriptInterface
    fun openSettings(): Boolean = try {
        ctx.startActivity(Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)); true
    } catch (e: Exception) { false }

    @JavascriptInterface
    fun info(): String = JSONObject()
        .put("brand", Build.MANUFACTURER).put("model", Build.MODEL).put("sdk", Build.VERSION.SDK_INT)
        .put("leanback", pm.hasSystemFeature("android.software.leanback")).toString()

    private fun iconB64(d: Drawable): String {
        val s = 128
        val bmp = Bitmap.createBitmap(s, s, Bitmap.Config.ARGB_8888)
        d.setBounds(0, 0, s, s)
        d.draw(Canvas(bmp))
        val bo = ByteArrayOutputStream()
        bmp.compress(Bitmap.CompressFormat.PNG, 100, bo)
        bmp.recycle()
        return Base64.encodeToString(bo.toByteArray(), Base64.NO_WRAP)
    }
}
