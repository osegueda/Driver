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
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

import sv.edu.bitlab.driver.R
import sv.edu.bitlab.driver.fragments.reservationsComponents.recyclerview.ReservationAdapter
import sv.edu.bitlab.driver.fragments.reservationsComponents.recyclerview.ReservationViewHolder
import sv.edu.bitlab.driver.interfaces.OnFragmentInteractionListener
import sv.edu.bitlab.driver.models.Reservation
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ReservationFragment : Fragment() ,ReservationViewHolder.ReservationItemListener{
    override fun onItemClickReservation(position: Int) {
        Toast.makeText(requireContext(),"TAP ON $position",Toast.LENGTH_LONG).show()


    }

    override fun onItemClickDetalle(btn_detalle: Button, position: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onTextInput(input: String, position: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private var listener: OnFragmentInteractionListener? = null
    private var firestoredb = FirebaseDatabase.getInstance().getReference("reservations")
    private var listView: RecyclerView?=null
    private var fragmentView:View?=null
    private lateinit  var todayDate:String
    private var reservations:ArrayList<Reservation> ?=null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

                    reservations=reservations?.filter {reservation ->
                        reservation.available==false
                    } as ArrayList<Reservation>
                    val adapter=listView?.adapter as ReservationAdapter
                    adapter.reservations=reservations!!
                    adapter.notifyDataSetChanged()


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


    companion object {

        @JvmStatic
        fun newInstance() =
            ReservationFragment()
    }
}
