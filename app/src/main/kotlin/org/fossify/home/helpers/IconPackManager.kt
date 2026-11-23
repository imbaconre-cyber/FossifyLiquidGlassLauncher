package org.fossify.home.helpers

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.drawable.Drawable
import androidx.core.content.res.ResourcesCompat
import org.fossify.home.extensions.config
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory

data class IconPackInfo(val packageName: String, val label: String)

/**
 * Standard intent-based icon pack discovery and appfilter.xml parsing.
 *
 * Supports any pack that declares one of the well-known theme actions and exposes either
 * an `appfilter.xml` meta-data resource or an `assets/appfilter.xml` file.
 */
object IconPackManager {

    private val THEME_ACTIONS = listOf(
        "org.adw.launcher.THEMES",
        "com.fossify.launcher.THEMES",
        "com.anddoes.launcher.THEME",
        "com.teslacoilsw.launcher.THEME",
        "com.novalauncher.THEME",
        "com.android.launcher.THEME",
        "com.google.android.apps.nexuslauncher.THEME"
    )

    private const val META_NAME_APPFILTER = "appfilter"

    @Volatile
    private var packPackage: String = ""

    @Volatile
    private var packResources: Resources? = null

    private var mappings = LinkedHashMap<String, String>()

    fun discover(context: Context): List<IconPackInfo> {
        val packageManager = context.packageManager
        val found = LinkedHashMap<String, IconPackInfo>()
        THEME_ACTIONS.forEach { action ->
            val activities = packageManager.queryIntentActivities(Intent(action), 0)
            activities.forEach { resolveInfo ->
                val packageName = resolveInfo.activityInfo.applicationInfo.packageName
                if (packageName != context.packageName && !found.containsKey(packageName)) {
                    val label = resolveInfo.loadLabel(packageManager).toString()
                    found[packageName] = IconPackInfo(packageName, label)
                }
            }
        }
        return found.values.sortedBy { it.label.lowercase() }
    }

    fun reload(context: Context) {
        val selected = context.config.selectedIconPack
        packPackage = selected
        packResources = null
        mappings = LinkedHashMap()
        if (selected.isBlank()) return

        try {
            val packageManager = context.packageManager
            val appInfo = packageManager.getApplicationInfo(
                selected,
                PackageManager.GET_META_DATA
            )
            val resources = packageManager.getResourcesForApplication(appInfo)
            packResources = resources

            val metaResId = appInfo.metaData?.let { meta ->
                listOf(META_NAME_APPFILTER, "theme", "appFilter", "icon_pack")
                    .firstNotNullOfOrNull { key ->
                        meta.getInt(key, 0).takeIf { it != 0 }
                    }
            } ?: 0

            val parser = when {
                metaResId != 0 -> resources.getXml(metaResId)
                else -> try {
                    val stream = resources.assets.open("appfilter.xml")
                    XmlPullParserFactory.newInstance().newPullParser().apply {
                        setInput(stream, "UTF-8")
                    }
                } catch (_: Exception) {
                    null
                }
            }

            parser?.let { parseAppFilter(it) }
        } catch (_: Exception) {
            packPackage = ""
            packResources = null
            mappings = LinkedHashMap()
        }
    }

    private fun parseAppFilter(parser: XmlPullParser) {
        var event = parser.eventType
        while (event != XmlPullParser.END_DOCUMENT) {
            if (event == XmlPullParser.START_TAG &&
                (parser.name == "item" || parser.name == "img")
            ) {
                val component = parser.getAttributeValue(null, "component").orEmpty()
                val drawableName = parser.getAttributeValue(null, "drawable").orEmpty()
                    .substringBefore('.')
                if (component.isNotBlank() && drawableName.isNotBlank()) {
                    val normalized = normalizeComponent(component)
                    mappings[normalized] = drawableName
                    val packageName = normalized.substringBefore('/')
                    if (packageName.isNotBlank() && !mappings.containsKey(packageName)) {
                        mappings[packageName] = drawableName
                    }
                }
            }
            event = parser.next()
        }
    }

    private fun normalizeComponent(component: String): String =
        component.removePrefix("ComponentInfo{")
            .removeSuffix("}")
            .replace(':', '/')

    /** Returns a themed icon from the selected pack, or null when the app is not themed. */
    fun loadIcon(context: Context, packageName: String, activityName: String): Drawable? {
        val resources = packResources ?: return null
        if (packPackage.isBlank()) return null

        val keys = listOf(
            "ComponentInfo{$packageName/$activityName}",
            "$packageName/$activityName",
            packageName
        )
        val drawableName = keys.firstNotNullOfOrNull { mappings[it] } ?: return null

        val resourceId = resources.getIdentifier(drawableName, "drawable", packPackage)
            .takeIf { it != 0 }
            ?: resources.getIdentifier(drawableName, "mipmap", packPackage)
        if (resourceId == 0) return null

        return try {
            ResourcesCompat.getDrawable(resources, resourceId, context.theme)
        } catch (_: Exception) {
            try {
                resources.getDrawable(resourceId)
            } catch (_: Exception) {
                null
            }
        }
    }

    fun clear() {
        packPackage = ""
        packResources = null
        mappings = LinkedHashMap()
    }
}
