package sv.edu.bitlab.driver.fragments.reservationsComponents.recyclerview

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import sv.edu.bitlab.driver.R


class ReservationDetailAdapter(var users:ArrayList<String>, val listener: ReservationDetailViewHolder.ReservationDetailItemListener, var context: Context
) : RecyclerView.Adapter<ReservationDetailViewHolder>() {
    override fun onBindViewHolder(holder: ReservationDetailViewHolder, position: Int) {
        holder.bindData()
        holder.user_txt?.text=users[position]
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReservationDetailViewHolder{
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_row_confirmation, parent, false)
        return ReservationDetailViewHolder(view, listener)
    }


    override fun getItemCount(): Int {
        return users.size

    }
}