package sv.edu.bitlab.driver

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import sv.edu.bitlab.driver.fragments.activationComponents.ActivationFragment
import sv.edu.bitlab.driver.fragments.notificationComponents.NotificationFragment
import sv.edu.bitlab.driver.fragments.reservationsComponents.ReservationFragment
import sv.edu.bitlab.driver.interfaces.OnFragmentInteractionListener
import sv.edu.bitlab.driver.geofence.GeofenceBroadcastReceiver
import sv.edu.bitlab.driver.models.OngoingReservation
import sv.edu.bitlab.driver.models.Reservation
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MainActivity : AppCompatActivity(),OnFragmentInteractionListener {

    private var listener:OnFragmentInteractionListener?=null
    private lateinit var geofencingClient: GeofencingClient
    private lateinit var  geofenceList:MutableList<Geofence>
    private lateinit  var todayDate:String
    private lateinit var  reservations:ArrayList<Reservation>
    private var firestoredb = FirebaseDatabase.getInstance().getReference("reservations")
    private lateinit var ongoingRounds:ArrayList<OngoingReservation>





    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        getDate()

        getAllReservationsOnce()
        ongoingRounds= ArrayList()
        reservations= ArrayList()
        listener=this
        init()
        getPermisions()
        notifications()
        getToken()
        geofencingClient = LocationServices.getGeofencingClient(this)
        geofenceList= mutableListOf()
        addGeofenceToList()
        Log.d("GEO-LIST","THE LIST -> $geofenceList")
        geofencingClient.addGeofences(getGeofencingRequest(), geofencePendingIntent)?.run {
            addOnSuccessListener {
                Toast.makeText(applicationContext,"SE AGREGARON LOS GEOFENCES",Toast.LENGTH_LONG).show()
                Log.d("GEOFENCING","RESULT GEO SUCCESS")
            }
            addOnFailureListener {
                Toast.makeText(applicationContext,"ERROR-> NOSE AGREGARON LOS GEOFENCES",Toast.LENGTH_LONG).show()
                Log.d("GEOFENCING","RESULT GEO FAILURE")
            }
        }

       /* geofencingClient.removeGeofences(geofencePendingIntent).run {
            addOnSuccessListener {
                Toast.makeText(applicationContext,"SE AGREGARON LOS GEOFENCES",Toast.LENGTH_LONG).show()
                Log.d("GEOFENCING","RESULT GEO SUCCESS")
            }
            addOnFailureListener {
                Toast.makeText(applicationContext,"ERROR-> NOSE AGREGARON LOS GEOFENCES",Toast.LENGTH_LONG).show()
                Log.d("GEOFENCING","RESULT GEO FAILURE")
            }
        }*/



    }



    private fun getGeofencingRequest(): GeofencingRequest {
        return GeofencingRequest.Builder().apply {
            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            addGeofences(geofenceList)
        }.build()
    }
    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(this, GeofenceBroadcastReceiver::class.java)
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // addGeofences() and removeGeofences().
        PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }


    override fun onFragmentInteraction(index: FragmentsIndex) {
       var fragment:Fragment?=null
        val builder=supportFragmentManager.beginTransaction()

        when(index){

            FragmentsIndex.KEY_FRAGMENT_ACTIVATION->{
                fragment=ActivationFragment.newInstance()


            }
            FragmentsIndex.KEY_FRAGMENT_RESERVATIONS->{
                fragment=ReservationFragment.newInstance()

            }
            FragmentsIndex.KEY_FRAGMENT_NOTIFICATIONS->{
                fragment=NotificationFragment.newInstance()

            }


        }
        builder
            .replace(R.id.container_fragments,fragment)
            .commit()

    }

    private fun init(){

        val fragment= ActivationFragment.newInstance()
        val builder= supportFragmentManager
            .beginTransaction()
            .add(R.id.container_fragments,fragment,TAG)
            .commit()


        findViewById<LinearLayout>(R.id.container_layout_activation).setOnClickListener{

            Toast.makeText(this,"ACTIVATION",Toast.LENGTH_LONG).show()
            listener?.onFragmentInteraction(FragmentsIndex.KEY_FRAGMENT_ACTIVATION)


            }
        findViewById<LinearLayout>(R.id.container_layout_reservation).setOnClickListener{
            listener?.onFragmentInteraction(FragmentsIndex.KEY_FRAGMENT_RESERVATIONS)
            Toast.makeText(this,"RESERVATION",Toast.LENGTH_LONG).show()

        }
        findViewById<LinearLayout>(R.id.container_layout_notifications).setOnClickListener{
            listener?.onFragmentInteraction(FragmentsIndex.KEY_FRAGMENT_NOTIFICATIONS)
            Toast.makeText(this,"NOTIFICATION",Toast.LENGTH_LONG).show()

        }

    }
    private fun addGeofenceToList(){

        staticGeofences.forEach{item->

            geofenceList.add(Geofence.Builder()
                // Set the request ID of the geofence. This is a string to identify this
                // geofence.
                .setRequestId(item.key)
                // Set the circular region of this geofence.
                .setCircularRegion(
                    item.value.latitude,
                    item.value.longitude,
                    GEOFENCE_RADIUS_IN_METERS
                )

                // Set the expiration duration of the geofence. This geofence gets automatically
                // removed after this period of time.
                .setExpirationDuration(GEOFENCE_EXPIRATION_IN_MILLISECONDS)

                // Set the transition types of interest. Alerts are only generated for these
                // transition. We track entry and exit transitions in this sample.
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)

                // Create the geofence.
                .build())


        }
       /* geofenceList.add(Geofence.Builder()
            // Set the request ID of the geofence. This is a string to identify this
            // geofence.
            .setRequestId("park1")
            // Set the circular region of this geofence.
            .setCircularRegion(
                13.644454,
                -89.279587,
                GEOFENCE_RADIUS_IN_METERS
            )

            // Set the expiration duration of the geofence. This geofence gets automatically
            // removed after this period of time.
            .setExpirationDuration(GEOFENCE_EXPIRATION_IN_MILLISECONDS)

            // Set the transition types of interest. Alerts are only generated for these
            // transition. We track entry and exit transitions in this sample.
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)

            // Create the geofence.
            .build())*/

    }


    private fun getPermisions(){

        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                Toast.makeText(this,"dame permissos",Toast.LENGTH_LONG).show()
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this, PERMISSION_FINE_LOCATION,PERMISSION_FINE_LOCATION_KEY)

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            // Permission has already been granted
            Toast.makeText(this,"ya te di permissos",Toast.LENGTH_LONG).show()
        }

    }
  
    private fun notifications(){

        Log.d("NOTIFICATION", "Subscribing to driver topic")
        // [START subscribe_topics]
        FirebaseMessaging.getInstance().subscribeToTopic("driver")
            .addOnCompleteListener { task ->
                var msg = "SUSCRIPTION SUCCESS"
                if (!task.isSuccessful) {
                    msg = "SUSCRIPTION FAILED"
                }
                Log.d("NOTIFICATION", msg)
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            }
        // [END subscribe_topics]

    }

    private fun getToken(){

        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w("TOKEN", "getInstanceId failed", task.exception)
                    return@OnCompleteListener
                }

                // Get new Instance ID token
                val token = task.result?.token

                // Log and toast
                val msg = "the generated token -> $token"
                Log.d("TOKEN", msg)
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            })
        // [END retrieve_current_token]

    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSION_FINE_LOCATION_KEY -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    Log.d("PERMITIONS","GRANTED")
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Log.d("PERMITIONS","NOT GRANTED")
                }
                return
            }

            // Add other 'when' lines to check for other
            // permissions this app might request.
            else -> {
                // Ignore all other requests.
            }
        }
    }

    fun writeFirstRoundOfDay(fistRound:String){

        val reservationOfDay = OngoingReservation(fistRound)
        firestoredb.child("$todayDate/ongoing_rounds").setValue(reservationOfDay)
            .addOnSuccessListener {
                Log.d("ONGOING","THE NODE ONGOING HAS BEEN CREATED")
            }
            .addOnFailureListener {

                Log.d("ONGOING","THE NODE IT WAS NOT CREATED")
            }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getDate(){

        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
        val formatted = current.format(formatter)
        todayDate=formatted
    }

    private fun getAllReservationsOnce(){

        firestoredb.child(todayDate).child("rounds").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()){
                    reservations.clear()

                    dataSnapshot.children.forEach {reservation->
                        val reserv= reservation.getValue(Reservation::class.java)
                        reservations.add(reserv!!)

                    }

                        Log.d("RESERVAS","$reservations")


                        reservations= reservations.filter { reservation ->
                            reservation.round_status.equals("available")
                        } as ArrayList<Reservation>
                        Log.d("RESERVAS-FILTER","$reservations")

                        val firstRound=reservations[0].id.toString()
                    getAllOngogingReservations(firstRound)











                }else{

                    Log.d("RESERV","NO EXISTE")

                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                println("The read failed: " + databaseError.code)
            }
        })

    }

    private fun getAllOngogingReservations(firstRound:String){


        firestoredb.child(todayDate).child("ongoing_rounds").addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()){
                    ongoingRounds.clear()
                    dataSnapshot.children.forEach {reservation->

                        val reserv= OngoingReservation(reservation.value.toString())
                        ongoingRounds.add(reserv)

                    }
                    Log.d("ONGOING-RESEV","$ongoingRounds")
                }else{
                    writeFirstRoundOfDay(firstRound)
                    Log.d("RESERV","NO EXISTE")

                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                println("The read failed: " + databaseError.code)
            }
        })

    }




}




