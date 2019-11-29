package sv.edu.bitlab.driver.fragments.activationComponents

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.fragment_activation.view.*
import sv.edu.bitlab.driver.APPLICATION_NAME

import sv.edu.bitlab.driver.R
import sv.edu.bitlab.driver.interfaces.OnFragmentInteractionListener
import sv.edu.bitlab.driver.static_images
import sv.edu.bitlab.driver.fragments.activationComponents.recyclerview.ScheduleAdapter
import sv.edu.bitlab.driver.fragments.activationComponents.recyclerview.ScheduleViewHolder
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class ActivationFragment : Fragment(), ScheduleViewHolder.ReservationItemListener {
    override fun onItemClickReservation(position: Int) {
       Toast.makeText(requireContext(),"Clicked on #$position",Toast.LENGTH_LONG).show()
    }


    private var listener: OnFragmentInteractionListener? = null
    private var recycler:RecyclerView?=null
    private var schedule:Array<String>?=null
    private var activationPressed:Boolean=false
    private var firestoredb = FirebaseDatabase.getInstance().getReference("reservations")
    private lateinit var today_date:String
    private  lateinit var images:MutableList<Int>
    private val sharedPreferences: SharedPreferences?
        get() = context?.getSharedPreferences(APPLICATION_NAME, Context.MODE_PRIVATE)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activationPressed=getPreference("service")!!
        schedule=resources.getStringArray(R.array.schedule)
        images= static_images



    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view=inflater.inflate(R.layout.fragment_activation, container, false)

        repaintServiceButton(view.activation_btn)

        view.activation_btn.setOnClickListener{

                if (activationPressed){

                    confirmDeactivation(view.activation_btn)



                }else{
                    activationPressed=true
                    Toast.makeText(requireContext(), "Reservations Activated", Toast.LENGTH_LONG).show()
                    view.activation_btn.text=getString(R.string.btn_service_deactivation)
                    setPreference("service",activationPressed)
                    updateService(true)
                   // pushNotification()
                    view.activation_btn.setBackgroundResource(R.drawable.input_activation_button)
                }


        }
        return view
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recycler=view.findViewById(R.id.recyclerView_reservations_schedule)
        recycler?.layoutManager = LinearLayoutManager(this.context)
        recycler?.adapter = ScheduleAdapter(images, schedule!!,this,requireContext())

        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
        val formatted = current.format(formatter)
        today_date=formatted
        Log.d("DATE", formatted)
        //Toast.makeText(requireContext(), "$ a toast $formatted", Toast.LENGTH_SHORT).show()


    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    private fun pushNotification(){

        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w("NOTIFICATION", "getInstanceId failed", task.exception)
                    return@OnCompleteListener
                }

                // Get new Instance ID token
                val token = task.result?.token

                // Log and toast
                val msg = "the token is ->$token"
                Log.d("NOTFICATION", msg)
               // Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            })

    }

    private fun updateService(activation:Boolean){
        Log.d("FECHA","TODAY DATE -> $today_date")

        firestoredb.child("$today_date/service_status/activation").setValue(activation)

    }

    private fun confirmDeactivation(button:Button) {

        val alertDialog = AlertDialog.Builder(requireContext())
            .setTitle("Cancel Reservations")
            .setMessage("Do you want to stop the service?")
            .setPositiveButton("Stop") { _, _ ->


                activationPressed=false
                setPreference("service",activationPressed)
                button.text=getString(R.string.text_start_button)
                button.setBackgroundResource(R.drawable.input_nav_button)
                updateService(false)
               Toast.makeText(requireContext(), "Reservations Deactivated", Toast.LENGTH_LONG).show()
            }

            .setNegativeButton("Cancel") { _, _ ->

               // Toast.makeText(requireContext(), "No", Toast.LENGTH_LONG).show()

            }
        alertDialog.show()
    }
    private fun setPreference(key: String, value: Boolean) {
        sharedPreferences?.edit()?.putBoolean(key, value)?.apply()
        Log.d("setPreferenceKey "," $key -> $value")
    }
    private fun getPreference(key: String): Boolean? {
        return sharedPreferences?.getBoolean(key, false)
    }

    private fun repaintServiceButton(button: Button){

        if (activationPressed) {
            button.text = getString(R.string.btn_service_deactivation)
            button.setBackgroundResource(R.drawable.input_activation_button)
        }else{

            button.text = getString(R.string.btn_service_activation)
            button.setBackgroundResource(R.drawable.input_nav_button)
        }

    }


    companion object {

        @JvmStatic
        fun newInstance() = ActivationFragment()
    }
}
