package com.example.testapplication
import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Process
import android.provider.Settings
import android.util.Log
import java.util.Calendar
import java.util.concurrent.TimeUnit


class AppUsageManager(private val context: Context) {
    //Initialize app usage manager with context
    private val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    private val packageManager = context.packageManager

    // Check if we have permissions to access app info
    fun hasUsageStatsPermission(): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager

        val mode = appOps.noteOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName,
            null,
            null
        ) //Returns MODE_ERRORED if permission not allowed else return MODE_ALLOWED
        return mode == AppOpsManager.MODE_ALLOWED
    }

//    fun hasPackagePermission(): Boolean {
//        return context.checkSelfPermission("android.permission.QUERY_ALL_PACKAGES") ==
//                PackageManager.PERMISSION_GRANTED
//    }

    //Request permission
    fun requestUsageStatsPermission(){
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }
//    fun requestPackagePermission(activity: Activity, requestCode: Int) {
//        val intent = Intent(Settings.)
//        ActivityCompat.requestPermissions(
//                activity,
//                arrayOf("android.permission.QUERY_ALL_PACKAGES"),
//                requestCode
//            )
//    }

    //Get usage stats for last 24 hours
    fun getAppUsageStats(): List<AppUsageInfo>{
        if(!hasUsageStatsPermission()){
            return emptyList()
        }
        //Define time interval & query for data
        val calendar = Calendar.getInstance()
        val endTime = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_MONTH, -1) //Last 24h
        val startTime = calendar.timeInMillis

        val usageStats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTime,
            endTime
        )
        //Filter data and show in app
        return usageStats.filter{it.totalTimeInForeground>0}.map { stats ->
            AppUsageInfo(
                packageName = stats.packageName,
                appName = getAppName(stats.packageName),
                timeInForeground = stats.totalTimeInForeground,
                lastTimeUsed = stats.lastTimeUsed,
                grantedPermissions = getGrantedPermissions(stats.packageName)
            )
        }.sortedByDescending { it.lastTimeUsed }
    }

    // Get app name from package name
    private fun getAppName (packageName:String):String {
        return try {
            // Set multiple flags to match more packages
            val flags = PackageManager.GET_META_DATA or PackageManager.MATCH_UNINSTALLED_PACKAGES

            val theAppInfo = packageManager.getApplicationInfo(packageName, flags)
            packageManager.getApplicationLabel(theAppInfo).toString()
        }
        catch (e: PackageManager.NameNotFoundException){
            Log.e("AppNameRetrieval", "Package $packageName name not found",e )
            packageName
        } catch (e: Exception) {
            Log.e("AppNameRetrieval", "Error while getting app name for $packageName: ${e.message}", e)
            packageName
        }

    }

    // Get app permissions from package name
    private fun getGrantedPermissions (packageName:String):List<String> {
        return try {
            val pkgInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS)
            val grantedPermissions = mutableListOf<String>()
            // Check if app has permissions
            if(pkgInfo.requestedPermissions != null && pkgInfo.requestedPermissionsFlags != null){
                // Check if permission is granted for each permission
                for(index in pkgInfo.requestedPermissions!!.indices){
                    if ((pkgInfo.requestedPermissionsFlags!![index] and PackageInfo.REQUESTED_PERMISSION_GRANTED)!=0) {
                        grantedPermissions.add(pkgInfo.requestedPermissions!![index])
                    }
                }
            }
            grantedPermissions
        }
        catch (e: PackageManager.NameNotFoundException ){
            Log.e("PermissionsRetrieval", "Package $packageName not found",e )
            emptyList()
        }
        catch (e: Exception ){
            Log.e("PermissionsRetrieval", "Error while getting permissions for $packageName : ${e.message}",e )
            emptyList()
        }
    }
    fun OpenAppSettings(appPackageName: String){
        //Show the settings page
        val settings_intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        //Find app URI(location) & open settings options
        val _uri = Uri.fromParts("package", appPackageName, null)
        settings_intent.data = _uri
        context.startActivity(settings_intent)
    }

}

//Data class to hold app usage info
data class AppUsageInfo(
    val packageName: String,
    val appName: String,
    val timeInForeground: Long,
    val lastTimeUsed: Long,
    val grantedPermissions : List<String>
){
    //Convert time to hours and minutes
    fun getFormattedTime(): String {
        val hours = TimeUnit.MILLISECONDS.toHours(timeInForeground)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(timeInForeground) % 60
        return when{
            hours > 0 -> "${hours}h ${minutes}m"
            minutes > 0 -> "${minutes}m"
            else -> "Less than 1m"
        }

    }

    fun getFormattedLastTimeUsed(): String {
        val now = System.currentTimeMillis()
        val diffInMillis = now - lastTimeUsed
        val seconds = diffInMillis / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24

        return when {
            days > 0 -> {
                when {
                    days == 1L -> "1 day ago"
                    days < 7 -> "$days days ago"
                    days < 30 -> "${days / 7} weeks ago"
                    days < 365 -> "${days / 30} months ago"
                    else -> "${days / 365} years ago"
                }
            }
            hours > 0 -> {
                when {
                    hours == 1L -> "1 hour ago"
                    else -> "$hours hours ago"
                }
            }
            minutes > 0 -> {
                when {
                    minutes == 1L -> "1 minute ago"
                    else -> "$minutes minutes ago"
                }
            }
            else -> "Just now"
        }
    }
}

