package sv.edu.bitlab.driver

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import sv.edu.bitlab.driver.fragments.activationComponents.ActivationFragment
import sv.edu.bitlab.driver.fragments.notificationComponents.NotificationFragment
import sv.edu.bitlab.driver.fragments.reservationsComponents.ReservationFragment
import sv.edu.bitlab.driver.interfaces.OnFragmentInteractionListener

class MainActivity : AppCompatActivity(),OnFragmentInteractionListener {

private var listener:OnFragmentInteractionListener?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        listener=this
        init()
        notifications()
        getToken()




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


}
