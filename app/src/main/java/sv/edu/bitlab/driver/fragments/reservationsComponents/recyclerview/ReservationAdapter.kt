package sv.edu.bitlab.driver.fragments.reservationsComponents.recyclerview

import android.content.Context
import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import sv.edu.bitlab.driver.R
import sv.edu.bitlab.driver.models.Reservation


class ReservationAdapter(var reservations:ArrayList<Reservation>, val listener: ReservationViewHolder.ReservationItemListener, var context:Context
) : RecyclerView.Adapter<ReservationViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReservationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_row_reservation, parent, false)
        return ReservationViewHolder(view, listener)
    }

    override fun onBindViewHolder(holder: ReservationViewHolder, position: Int) {
        holder.bindData()
        holder.date_txt?.visibility= View.GONE
        holder.status_txt?.visibility=View.GONE
        holder.id_txt?.text = context.resources.getString(
            R.string.two_format_string,
            "ID:",
            reservations[position].id
        )
        holder.round_txt?.text = context.resources.getString(
            R.string.two_format_string,
            "Round Number:",
            reservations[position].round.toString()
        )
        holder.schedule_txt?.text = context.resources.getString(
            R.string.two_format_string,
            "Schedule:",
            reservations[position].schedule
        )
        holder.count_txt?.text = context.resources.getString(
            R.string.two_format_string,
            "Available Slots:",
            reservations[position].pplsize.toString()
        )
        holder.status_txt?.text=context.resources.getString(
            R.string.two_format_string,
            "Round Status",
            reservations[position].round_status.toString()

        )
        Log.d("backgorund", "$reservations")


        when(reservations[position].round_status){

            "available"->{
                holder.container!!.setBackgroundColor(ContextCompat.getColor(context,android.R.color.holo_green_dark))
            }
            "finished"->{
                holder.container!!.setBackgroundColor(ContextCompat.getColor(context,android.R.color.holo_red_light))
            }
            "ongoing"->{
                holder.container!!.setBackgroundColor(ContextCompat.getColor(context,R.color.yellow))
            }
        }

        holder.container?.setOnClickListener{

            listener.onItemClickReservation(position,reservations[position].round_status!!,reservations[position].round!!,reservations[position].id!!,isOngoing())

        }

        // para el caso cuando no se cumple el minimo de reservaciones hacer check de la cantidad de personas y si es menor a 11 mostrar
        // un dialog si esta seguro que desea continuar el viaje aun con poca cantidad de personas

        //disableRounds(holder.container!!,position)

        /*if (reservations.any { reservation->reservation.round_status.equals("available")}) {
            holder.container!!.setBackgroundColor(context.resources.getColor(android.R.color.holo_green_dark))
            Log.d("USER","SI ESTA")
        } else {
            holder.container!!.setBackgroundColor(context.resources.getColor(android.R.color.holo_red_light))
            holder.container!!.isEnabled=false
            Log.d("USER","NO ESTA")
        }*/



    }


    override fun getItemCount(): Int {
        Log.d("SIZE", "${reservations.size}")
        return reservations.size

    }


    private fun isOngoing():Boolean{


        return reservations.any { reservation -> reservation.round_status.equals("ongoing") }

    }


}



