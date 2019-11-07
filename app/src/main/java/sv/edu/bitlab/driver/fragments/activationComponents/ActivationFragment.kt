package sv.edu.bitlab.driver.fragments.activationComponents

import android.content.Context
import android.net.Uri
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

import sv.edu.bitlab.driver.R
import sv.edu.bitlab.driver.R.array.schedule
import sv.edu.bitlab.driver.interfaces.OnFragmentInteractionListener
import sv.edu.bitlab.tarea6.ordenHistorial.recyclerView.ScheduleAdapter
import sv.edu.bitlab.tarea6.ordenHistorial.recyclerView.ScheduleViewHolder
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList


class ActivationFragment : Fragment(),ScheduleViewHolder.ReservationItemListener {
    override fun onItemClickReservation(position: Int) {
       Toast.makeText(requireContext(),"Clicked on #$position",Toast.LENGTH_LONG).show()
    }

    override fun onItemClickDetalle(btn_detalle: Button, position: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onTextInput(input: String, position: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private var listener: OnFragmentInteractionListener? = null
    private var recycler:RecyclerView?=null
    private var schedule:Array<String>?=null
    private var activationPressed:Boolean=false
    private var firestoredb = FirebaseDatabase.getInstance().getReference("reservations")
    private lateinit var today_date:String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        schedule=resources.getStringArray(R.array.schedule)



    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view=inflater.inflate(R.layout.fragment_activation, container, false)


        view.activation_btn.setOnClickListener{

                if (activationPressed){

                    confirmDeactivation(view.activation_btn)



                }else{
                    activationPressed=true
                    Toast.makeText(requireContext(), "Reservations Activated", Toast.LENGTH_LONG).show()
                    view.activation_btn.text="Stop service"
                    updateService(true)
                   // pushNotification()
                    view.activation_btn.setBackgroundColor(resources.getColor(android.R.color.holo_red_dark))
                }


        }
        return view
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recycler=view.findViewById(R.id.recyclerView_reservations_schedule)
        recycler?.layoutManager = LinearLayoutManager(this.context)
        recycler?.adapter = ScheduleAdapter(schedule!!,this,requireContext())

        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
        val formatted = current.format(formatter)
        today_date=formatted
        Log.d("DATE", formatted)
        Toast.makeText(requireContext(), "$ a toast $formatted", Toast.LENGTH_SHORT).show()


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
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
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
                button.text="Start service"
                button.setBackgroundColor(resources.getColor(android.R.color.holo_green_dark))
                updateService(false)
                Toast.makeText(requireContext(), "Reservations Deactivated", Toast.LENGTH_LONG).show()
            }

            .setNegativeButton("Cancel") { _, _ ->

                Toast.makeText(requireContext(), "No", Toast.LENGTH_LONG).show()

            }
        alertDialog.show()
    }



    companion object {

        @JvmStatic
        fun newInstance() = ActivationFragment()
    }
}
