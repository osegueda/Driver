package sv.edu.bitlab.driver.geofence


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent


class GeofenceBroadcastReceiver : BroadcastReceiver() {


    override fun onReceive(context: Context, intent: Intent) {
        // Enqueues a JobIntentService passing the context and intent as parameters
        GeofenceTransitionsJobIntentService.enqueueWork(context, intent)
    }



}
