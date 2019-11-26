package sv.edu.bitlab.driver.fragments.reservationsComponents

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
import android.widget.FrameLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions

import sv.edu.bitlab.driver.R
import sv.edu.bitlab.driver.fragments.reservationsComponents.recyclerview.ReservationAdapter
import sv.edu.bitlab.driver.fragments.reservationsComponents.recyclerview.ReservationViewHolder
import sv.edu.bitlab.driver.interfaces.OnFragmentInteractionListener
import sv.edu.bitlab.driver.models.Reservation
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ReservationFragment : Fragment() ,ReservationViewHolder.ReservationItemListener{
    override fun onItemClickReservation(position: Int, status: String,round:Int,id:String,ongoing:Boolean
    ,pplsize:Int) {

        when(status){

            "finished"->{
                Snackbar.make(requireView(), "The round is already DONE ", Snackbar.LENGTH_LONG)
                    .setAction("OK") {  }.show()
            }
            "ongoing"->{

                finishRound(round,id)
            }

            "available"->{

                if(ongoing){
                    Snackbar.make(requireView(), "You have an ongoing reservation in progress", Snackbar.LENGTH_LONG)
                        .setAction("OK") {  }.show()
                }
                else {


                    if (pplsize==0) {
                        confirmRound(round, id,"Do you want to start the Round:")
                    }else{
                        confirmRound(round, id,
                            "The round #$round hasn't filled the max capacity, do you want to start it anyways?")

                    }
                }
            }
        }
       // Toast.makeText(requireContext(),"TAP ON $position",Toast.LENGTH_LONG).show()
    }



    private var listener: OnFragmentInteractionListener? = null
    private var firestoredb = FirebaseDatabase.getInstance().getReference("reservations")
    private var db=FirebaseFirestore.getInstance()
    private var listView: RecyclerView?=null
    private var fragmentView:View?=null
    private lateinit  var todayDate:String
    private var reservations:ArrayList<Reservation> ?=null
    private lateinit var functions: FirebaseFunctions
// ...


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        functions = FirebaseFunctions.getInstance()
        getDate()
        reservations= ArrayList()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view=inflater.inflate(R.layout.fragment_reservation, container, false)
        fragmentView=view

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        listView=view.findViewById(R.id.recycler_view_reservations)
        listView?.layoutManager = LinearLayoutManager(this.context!!)
        listView?.adapter = ReservationAdapter(reservations!!,this,requireContext())

        getAllReservations()
    }
    private fun getAllReservations(){

        firestoredb.child(todayDate).child("rounds").addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()){
                    reservations?.clear()

                    dataSnapshot.children.forEach {reservation->
                        val reserv= reservation.getValue(Reservation::class.java)
                        reservations?.add(reserv!!)

                    }
                //aqui se hace el filter para no mostrar las available
                    /*reservations=reservations?.filter {reservation ->
                        reservation.available==false
                    } as ArrayList<Reservation>*/
                    val adapter=listView?.adapter as ReservationAdapter
                    adapter.reservations=reservations!!
                    adapter.notifyDataSetChanged()

                    val anim =fragmentView?.findViewById<ConstraintLayout>(R.id.animation_xml)
                    anim?.visibility=View.GONE




                    Log.d("RESERVAS","$reservations")




                }else{

                    Log.d("RESERV","NO EXISTE")

                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                println("The read failed: " + databaseError.code)
            }
        })

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getDate(){

        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
        val formatted = current.format(formatter)
        todayDate=formatted
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

    private fun confirmRound(round:Int,id: String,msg:String ) {
        val alertDialog = AlertDialog.Builder(requireContext())
            .setTitle("Confirmation Round Start")
            .setMessage(msg)
            .setPositiveButton("Start") { _, _ ->

                firestoredb.child(todayDate).child("rounds").child(id).child("round_status").setValue("ongoing")
                    .addOnCompleteListener {
                        val adapter=listView?.adapter as ReservationAdapter
                        adapter.reservations=reservations!!
                        adapter.notifyDataSetChanged()
                        notifyRound(round.toString(),"ongoing")





                    }
                    .addOnFailureListener {
                        Snackbar.make(requireView(), "Server Error: please try again ", Snackbar.LENGTH_LONG)
                            .setAction("Retry") {  }.show()
                    }


            }
            .setNegativeButton("Cancel") { _, _ ->
                Snackbar.make(requireView(), "The round was canceled ", Snackbar.LENGTH_LONG)
                    .setAction("Cancel") {  }.show()
            }
        alertDialog.show()
    }

    private fun finishRound(round:Int,id: String) {
        val alertDialog = AlertDialog.Builder(requireContext())
            .setTitle("Finishing Round ")
            .setMessage("Do you want to finish the Round: $round")
            .setPositiveButton("Finish") { _, _ ->

                firestoredb.child(todayDate).child("rounds").child(id).child("round_status").setValue("finished")
                    .addOnCompleteListener {
                        val adapter=listView?.adapter as ReservationAdapter
                        adapter.reservations=reservations!!
                        adapter.notifyDataSetChanged()
                        notifyRound(round.toString(),"finished")

                        val idReservation=reservations?.filter { reservation -> reservation.id.equals(id)}

                        if (idReservation!!.isNotEmpty()){
                            allReseravationsTodb(idReservation[0])
                        }

                    }
                    .addOnFailureListener {
                        Snackbar.make(requireView(), "Server Error: please try again ", Snackbar.LENGTH_LONG)
                            .setAction("Retry") {  }.show()
                    }


            }
            .setNegativeButton("Cancel") { _, _ ->
                Snackbar.make(requireView(), "End of Round cancelled ", Snackbar.LENGTH_LONG)
                    .setAction("Cancel") {  }.show()
            }
        alertDialog.show()
    }

    private fun notifyRound(text: String,status: String): Task<String> {
        // Create the arguments to the callable function.
        val data = hashMapOf(
            "round" to text,
            "status" to status
        )

        return functions
            .getHttpsCallable("notifyRound")
            .call(data)
            .continueWith { task ->
                // This continuation runs on either success or failure, but if the task
                // has failed then result will throw an Exception which will be
                // propagated down.
                val result = task.result?.data as String
                Log.d("CALL","THE RESULT IS -> $result")
                result
            }
    }

    private fun allReseravationsTodb(finishedReservation:Reservation){

        Log.d("USERS","the users are ->${finishedReservation.users}")

        finishedReservation.users.forEach{ user->

            val reservation= hashMapOf(

                "date" to  finishedReservation.date,
                "schedule" to finishedReservation.schedule,
                "round" to  finishedReservation.round
            )

            db.collection("users").document(user).collection("reservations").add(reservation)
                .addOnSuccessListener {
                    Log.d("TO-FIRESTORE","all reservations success")

                }
                .addOnFailureListener{
                    Log.d("TO-FIRESTORE","reservations failure")

                }

        }


    }

    companion object {

        @JvmStatic
        fun newInstance() =
            ReservationFragment()
    }
}
