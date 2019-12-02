package sv.edu.bitlab.driver

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.LinearLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.*
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.android.synthetic.main.alert_logout_dialog.view.*
import sv.edu.bitlab.driver.fragments.activationComponents.ActivationFragment
import sv.edu.bitlab.driver.fragments.historyComponents.HistoryFragment
import sv.edu.bitlab.driver.fragments.reservationsComponents.ReservationDetailFragment
import sv.edu.bitlab.driver.fragments.reservationsComponents.ReservationFragment
import sv.edu.bitlab.driver.interfaces.OnFragmentInteractionListener
import sv.edu.bitlab.driver.geofence.GeofenceBroadcastReceiver
import sv.edu.bitlab.driver.models.LatLang
import sv.edu.bitlab.driver.models.OngoingReservation
import sv.edu.bitlab.driver.models.Reservation
import sv.edu.bitlab.driver.models.User
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MainActivity : AppCompatActivity(),OnFragmentInteractionListener {

    private var listener:OnFragmentInteractionListener?=null
    lateinit var mFusedLocationClient: FusedLocationProviderClient
    private lateinit var geofencingClient: GeofencingClient
    private lateinit var  geofenceList:MutableList<Geofence>
    private lateinit  var todayDate:String
    private lateinit var  coordinate:LatLang
    private lateinit var  reservations:ArrayList<Reservation>
    private var firestoredb = FirebaseDatabase.getInstance().getReference("reservations")
    private lateinit var ongoingRounds:ArrayList<OngoingReservation>
    var fbAuth = FirebaseAuth.getInstance()
    private lateinit var user: User
    var username: String? = null
    override fun onFragmentInteraction(index: FragmentsIndex, obj1: Any, obj2: Any) {
        var fragment:Fragment?=null
        var tag:String?=null
        val builder=supportFragmentManager.beginTransaction()

        when(index){

            FragmentsIndex.KEY_FRAGMENT_RESERVATIONS_DETAIL->{
                tag= TAG2
                fragment=ReservationDetailFragment.newInstance(obj1 as Reservation,obj2 as Boolean)
            }



        }
        builder
            .replace(R.id.container_fragments,fragment!!)
            .addToBackStack(tag)
            .commit()
    }




    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Login session
        val preferences = getSharedPreferences("User details", Context.MODE_PRIVATE)
        username = preferences.getString("FirebaseUser", "NO")
        Log.i("USERNAME", "USER EMAIL: $username")
        user= User(username,fbAuth.currentUser?.uid)


        fbAuth.addAuthStateListener {
            if(fbAuth.currentUser == null){
                this.finish()
            }
        }
        //Login Session

        getDate()
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        getLastLocation()

        //getAllReservationsOnce()
        ongoingRounds= ArrayList()
        reservations= ArrayList()
         //geofenceList= mutableListOf()
        listener=this
        init()
       // getPermisions()
        notifications()
        getToken()
        //geofencingClient = LocationServices.getGeofencingClient(this)

//        Log.d("GEO-LIST","THE LIST -> $geofenceList")
//        geofencingClient.addGeofences(getGeofencingRequest(), geofencePendingIntent)?.run {
//            addOnSuccessListener {
//                Toast.makeText(applicationContext,"SE AGREGARON LOS GEOFENCES",Toast.LENGTH_LONG).show()
//                Log.d("GEOFENCING","RESULT GEO SUCCESS")
//            }
//            addOnFailureListener {
//                Toast.makeText(applicationContext,"ERROR-> NOSE AGREGARON LOS GEOFENCES",Toast.LENGTH_LONG).show()
//                Log.d("GEOFENCING","RESULT GEO FAILURE")
//            }
//        }

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
                fragment=HistoryFragment.newInstance()

            }


        }
        builder
            .replace(R.id.container_fragments,fragment!!)
            .commit()

    }

    private fun init(){

        val fragment= ActivationFragment.newInstance()
        val builder= supportFragmentManager
            .beginTransaction()
            .add(R.id.container_fragments,fragment,TAG)
            .commit()


        findViewById<LinearLayout>(R.id.container_layout_activation).setOnClickListener{

            //Toast.makeText(this,"ACTIVATION",Toast.LENGTH_LONG).show()
            listener?.onFragmentInteraction(FragmentsIndex.KEY_FRAGMENT_ACTIVATION)


            }
        findViewById<LinearLayout>(R.id.container_layout_reservation).setOnClickListener{
            listener?.onFragmentInteraction(FragmentsIndex.KEY_FRAGMENT_RESERVATIONS)
           // Toast.makeText(this,"RESERVATION",Toast.LENGTH_LONG).show()

        }
        findViewById<LinearLayout>(R.id.container_layout_notifications).setOnClickListener{
            listener?.onFragmentInteraction(FragmentsIndex.KEY_FRAGMENT_NOTIFICATIONS)
           // Toast.makeText(this,"NOTIFICATION",Toast.LENGTH_LONG).show()

        }

    }
    ///MENU Y OPCION LOGOUT

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.my_menu, menu)
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem):Boolean{
        when(item.itemId){
            R.id.logout_action -> {

                val mDialogView = LayoutInflater.from(this).inflate(R.layout.alert_logout_dialog,null)
                val mBuilder = AlertDialog.Builder(this)
                    .setView(mDialogView)

                val mAlertDialog = mBuilder.create()
                mAlertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                mAlertDialog.window?.attributes?.windowAnimations = R.style.DialogAnimation
                mAlertDialog.show()

                mDialogView.id_cancel_btn.setOnClickListener{
                    mAlertDialog.dismiss()

                }

                mDialogView.id_confirm_btn.setOnClickListener{
                    mAlertDialog.dismiss()
                    signOut()
                }
                return true
            }
            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
    }

    fun signOut(){
        val sharedPreferences = getSharedPreferences("User details", Context.MODE_PRIVATE)
        val sharedPref = sharedPreferences?.edit()
        sharedPref!!.clear()
        sharedPref.apply()
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        fbAuth.signOut()
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
       /* geofenceList.add(Geofence.Builder()git sta
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
    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION),
            PERMISSION_ID_COARSE_FINE_LOCATION
        )
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
                //Toast.makeText(this,"dame permissos",Toast.LENGTH_LONG).show()
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this, PERMISSION_FINE_LOCATION,PERMISSION_FINE_LOCATION_KEY)

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            // Permission has already been granted
           // Toast.makeText(this,"ya te di permissos",Toast.LENGTH_LONG).show()
            Log.d("PERMISOS","ya te di permissos")
        }

    }

    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            return true
        }
        return false
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
               // Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            }
        // [END subscribe_topics]

    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {

                mFusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
                    var location: Location? = task.result
                    if (location == null) {
                        requestNewLocationData()
                    } else {
                        val coordinate= LatLang(location.latitude,location.longitude)
                        writeLocation(coordinate)
                        Toast.makeText(this@MainActivity,"lat->${location.latitude} lang->${location.longitude} ",Toast.LENGTH_LONG).show()

                        Log.d("LOCATION-LONG-LAST","${location.longitude}")
                        Log.d("LOCATION-LAT-LAST","${location.latitude}")


                    }
                }
            } else {
                Toast.makeText(this, "Turn on location", Toast.LENGTH_LONG).show()
               // val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                //startActivity(intent)
            }
        } else {
            requestPermissions()
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
     /*   val mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 5000
        mLocationRequest.fastestInterval = 1000
       // mLocationRequest.numUpdates = 5*/

        val mLocationRequest = LocationRequest.create().apply {

            fastestInterval = 1000
            interval = 1000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            smallestDisplacement = 1.0f

        }

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mFusedLocationClient.requestLocationUpdates(
            mLocationRequest, mLocationCallback,
            Looper.myLooper()
        )
        
    }

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation: Location = locationResult.lastLocation



            Log.d("LOCATION-ARR","${locationResult.locations}")
            val coordinate= LatLang(mLastLocation.latitude,mLastLocation.longitude)
            writeLocation(coordinate)
            Toast.makeText(this@MainActivity,"lat->${mLastLocation.latitude} lang->${mLastLocation.longitude} ",Toast.LENGTH_LONG).show()

            Log.d("LOCATION-LONG","${mLastLocation.longitude}")
            Log.d("LOCATION-LAT","${mLastLocation.latitude}")
        }
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
              //  Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
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
            PERMISSION_ID_COARSE_FINE_LOCATION-> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {

                    Log.d("PERMITIONS","GRANTED")
                } else {

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
    fun writeLocation(coordinate:LatLang){


        firestoredb.child("$todayDate/location/longitude").setValue(coordinate.longitude)
            .addOnSuccessListener {
                Log.d("LONGITUDE-FIRE","THE NODE longitude HAS BEEN CREATED")
            }
            .addOnFailureListener {

                Log.d("LONGITUDE-FIRE","THE NODE longitude  WAS NOT CREATED")
            }
        firestoredb.child("$todayDate/location/latitude").setValue(coordinate.latitude)
            .addOnSuccessListener {
                Log.d("LATITUDE-FIRE","THE NODE latitude HAS BEEN CREATED")
            }
            .addOnFailureListener {

                Log.d("LATITUDE-FIRE","THE NODE latitude  WAS NOT CREATED")
            }

    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun getDate(){

        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
        val formatted = current.format(formatter)
        todayDate=formatted
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
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

    private fun getLocation(){


        firestoredb.child(todayDate).child("location").addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()){

                    dataSnapshot.children.forEach { reservation ->

                        val coordinates = reservation.getValue(LatLang::class.java)
                        coordinate=coordinates!!
                    }

                    Log.d("LATLANG","$ongoingRounds")
                }else{

                    Log.d("LATLANG","NO EXISTE")

                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                println("The read failed: " + databaseError.code)
            }
        })

    }




}




