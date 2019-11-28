package sv.edu.bitlab.driver

import android.Manifest
import sv.edu.bitlab.driver.models.Dia

import sv.edu.bitlab.driver.models.LatLang


const val TAG="ACTIVATION_FRAGMENT"
const val TAG2="DETAIL_FRAGMENT"
const val APPLICATION_NAME="sv.edu.bitlab.driver"

var staticGeofences= hashMapOf(
    "park1" to LatLang(13.644454, -89.279587),
    "park2" to LatLang(13.650160,-89.279547),
    "elanin" to LatLang(13.707566,-89.251402)


)
var times= mutableListOf("1-5","6-58")

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
const val PERMISSION_READ_STORAGE_KEY = 10
const val PERMISSION_WRITE_STORAGE_KEY = 100
const val PERMISSION_FINE_LOCATION_KEY = 5
const val PERMISSION_COARSE_LOCATION_KEY = 6

const val PERMISSION_ID_COARSE_FINE_LOCATION=7
