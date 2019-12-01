package sv.edu.bitlab.driver.fragments.reservationsComponents

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.android.synthetic.main.fragment_activation.view.*
import kotlinx.android.synthetic.main.fragment_reservation_detail.view.*
import kotlinx.android.synthetic.main.fragment_round_confirmation.view.*
import sv.edu.bitlab.driver.FragmentsIndex
import sv.edu.bitlab.driver.R
import sv.edu.bitlab.driver.RESERVATION_MAX_CAPACITY
import sv.edu.bitlab.driver.fragments.reservationsComponents.recyclerview.ReservationDetailAdapter
import sv.edu.bitlab.driver.fragments.reservationsComponents.recyclerview.ReservationDetailViewHolder
import sv.edu.bitlab.driver.interfaces.OnFragmentInteractionListener
import sv.edu.bitlab.driver.models.Reservation
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class ReservationDetailFragment : Fragment() ,ReservationDetailViewHolder.ReservationDetailItemListener{

    override fun itemClickToDetail(rsv: Reservation) {
        Log.d("CAT","CAT")
    }



    private lateinit var rsv:Reservation
    private var listener: OnFragmentInteractionListener? = null
    private var listview:RecyclerView?=null
    private var fragmentView:View?=null
    private var firestoredb = FirebaseDatabase.getInstance().getReference("reservations")
    private var db= FirebaseFirestore.getInstance()
    private lateinit  var todayDate:String
    private lateinit var functions: FirebaseFunctions
    private  var  ongoing=true

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        functions = FirebaseFunctions.getInstance()
        getDate()
        Log.d("VALS","THE VALUE IS $ongoing")
        Log.d("DETAIL","$rsv")

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View? {
        val view =inflater.inflate(R.layout.fragment_round_confirmation, container, false)
        fragmentView= view

        view.notify_all_btn.setOnClickListener {

                notifyAll(rsv.round.toString())

        }
        val passesngers= RESERVATION_MAX_CAPACITY-rsv.pplsize!!
        view.number_passengers_txt.text=passesngers.toString()
        view.round_number_txt.text=rsv.round.toString()

        when(rsv.round_status){

            "finished"->{
                view.push_btn.text=getString(R.string.detail_fragment_finished_status)
                view.push_btn.background=ContextCompat.getDrawable(requireContext(),R.drawable.input_finished_button)
            }
            "ongoing"->{
                view.push_btn.text=getString(R.string.detail_fragment_finish_btn)
                view.push_btn.background=ContextCompat.getDrawable(requireContext(),R.drawable.input_finished_button)
            }
            "available"->{

                view.push_btn.text=getString(R.string.detail_fragment_start_btn)

            }

        }

        view.push_btn.setOnClickListener {


            when(rsv.round_status){

                "finished"->{

                    Snackbar.make(requireView(), "The round is already DONE ", Snackbar.LENGTH_LONG)
                        .setAction("OK") {  }.show()
                }
                "ongoing"->{

                    finishRound(rsv.round!!,rsv.id!!)
                }

                "available"->{

                    if(ongoing){
                        Snackbar.make(requireView(), "You have an ongoing reservation in progress", Snackbar.LENGTH_LONG)
                            .setAction("OK") {  }.show()
                    }
                    else {


                        if (rsv.pplsize==0) {
                            confirmRound(rsv.round!!, rsv.id!!,"Do you want to start the Round:")
                        }else{
                            confirmRound(rsv.round!!, rsv.id!!,
                                "The round #${rsv.round} hasn't filled the max capacity, do you want to start it anyways?")

                        }
                    }
                }
            }




        }


        view.back_btn.setOnClickListener{

            listener?.onFragmentInteraction(FragmentsIndex.KEY_FRAGMENT_RESERVATIONS)

        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listview=view.findViewById(R.id.passenger_recycler)
        listview?.layoutManager = LinearLayoutManager(this.context!!)
        listview?.adapter = ReservationDetailAdapter(rsv.users,this,requireContext())
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
                listener?.onFragmentInteraction(FragmentsIndex.KEY_FRAGMENT_RESERVATIONS)
                firestoredb.child(todayDate).child("rounds").child(id).child("round_status").setValue("ongoing")
                    .addOnCompleteListener {
                       // val adapter=listView?.adapter as ReservationAdapter
                        //adapter.reservations=reservations!!
                       // adapter.notifyDataSetChanged()
                        notifyRound(round.toString(),"ongoing")





                    }
                    .addOnFailureListener {
                        Snackbar.make(fragmentView!!, "Server Error: please try again ", Snackbar.LENGTH_LONG)
                            .setAction("Retry") {  }.show()
                    }


            }
            .setNegativeButton("Cancel") { _, _ ->
                Snackbar.make(fragmentView!!, "The round was canceled ", Snackbar.LENGTH_LONG)
                    .setAction("Cancel") {  }.show()
            }
        alertDialog.show()
    }

    private fun finishRound(round:Int,id: String) {
        val alertDialog = AlertDialog.Builder(requireContext())
            .setTitle("Finishing Round ")
            .setMessage("Do you want to finish the Round: $round")
            .setPositiveButton("Finish") { _, _ ->
                listener?.onFragmentInteraction(FragmentsIndex.KEY_FRAGMENT_RESERVATIONS)
                firestoredb.child(todayDate).child("rounds").child(id).child("round_status").setValue("finished")
                    .addOnCompleteListener {
                        //val adapter=listView?.adapter as ReservationAdapter
                       // adapter.reservations=reservations!!
                      //  adapter.notifyDataSetChanged()
                        notifyRound(round.toString(),"finished")

                       // val idReservation=reservations?.filter { reservation -> reservation.id.equals(id)}

                        allReseravationsTodb(rsv)

                    }
                    .addOnFailureListener {
                        Snackbar.make(fragmentView!!, "Server Error: please try again ", Snackbar.LENGTH_LONG)
                            .setAction("Retry") {  }.show()
                    }


            }
            .setNegativeButton("Cancel") { _, _ ->
                Snackbar.make(fragmentView!!, "End of Round cancelled ", Snackbar.LENGTH_LONG)
                    .setAction("Cancel") {  }.show()
            }
        alertDialog.show()
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun getDate(){

        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
        val formatted = current.format(formatter)
        todayDate=formatted
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

    private fun notifyAll(text: String): Task<String> {
        // Create the arguments to the callable function.
        val data = hashMapOf(
            "round" to text

        )

        return functions
            .getHttpsCallable("notifyAll")
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
        fun newInstance(rsv:Reservation,ongoing:Boolean) :ReservationDetailFragment{

            val fragment=ReservationDetailFragment()
            fragment.rsv=rsv
            fragment.ongoing=ongoing
            return fragment

        }

    }
}
