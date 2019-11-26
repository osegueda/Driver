package sv.edu.bitlab.driver

import android.Manifest
import sv.edu.bitlab.driver.models.Dia

import sv.edu.bitlab.driver.models.LatLang


const val TAG="ACTIVATION_FRAGMENT"
const val APPLICATION_NAME="sv.edu.bitlab.driver"

var staticGeofences= hashMapOf(
    "park1" to LatLang(13.644454, -89.279587),
    "park2" to LatLang(13.650160,-89.279547),
    "elanin" to LatLang(13.707566,-89.251402)


)
var times= mutableListOf("1-5","6-58")
var horarios= mutableListOf(
    "07:00 - 07:30",
    "07:30 - 08:00",
    "08:00 - 08:30",
    "08:30 - 09:00",
    "09:00 - 09:30",
    "09:30 - 10:00",
    "10:00 - 10:30",
    "10:30 - 11:00",
    "11:00 - 11:30",
    "11:30 - 12:00",
    "12:00 - 12:30",
    "12:30 - 13:00",
    "13:00 - 13:30",
    "13:30 - 14:00",
    "14:00 - 14:30",
    "14:30 - 15:00",
    "15:00 - 15:30",
    "15:30 - 16:00",
    "16:00 - 16:30",
    "16:30 - 17:00",
    "17:00 - 17:30",
    "17:30 - 18:00"
)

var static_images= mutableListOf(

    R.drawable.ic_notifications,
    R.drawable.ic_arrow,
    R.drawable.ic_stat_ic_notification

)


const val PACKAGE_NAME = "com.google.android.gms.location.Geofence"

const val GEOFENCES_ADDED_KEY = "$PACKAGE_NAME.GEOFENCES_ADDED_KEY"

const val GEOFENCE_EXPIRATION_IN_HOURS: Long = 1

const val GEOFENCE_EXPIRATION_IN_MILLISECONDS = GEOFENCE_EXPIRATION_IN_HOURS * 60 * 60 * 1000
const val GEOFENCE_RADIUS_IN_METERS = 1000f // 1 mile, 1.6 km


const val JOB_ID = 573

const val GEOFENCE_TAG = "GeofenceTransitionsIS"

const val CHANNEL_ID = "channel_01"

val PERMISSION_READ_STORAGE = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
val PERMISSION_WRITE_STORAGE = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
val PERMISSION_CAMERA = arrayOf(Manifest.permission.CAMERA)
val PERMISSION_COARSE_LOCATION= arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION)
val PERMISSION_FINE_LOCATION= arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)

// PERMISSION KEYS
val PERMISSION_READ_STORAGE_KEY = 10
val PERMISSION_WRITE_STORAGE_KEY = 100
val PERMISSION_FINE_LOCATION_KEY = 5
val PERMISSION_CCOARSE_LOCATION_KEY = 6

fun getallhours():MutableList<Dia>{

    val time= mutableListOf<Dia>()
    for (x in horarios){
        val dia=Dia(x,true)
        time.add(dia)
    }


    return time
}