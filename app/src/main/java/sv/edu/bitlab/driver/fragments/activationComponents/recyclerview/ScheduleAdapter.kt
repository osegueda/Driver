package sv.edu.bitlab.tarea6.ordenHistorial.recyclerView

import android.content.Context
import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import sv.edu.bitlab.driver.R


class ScheduleAdapter(var schedule:Array<String>, val listener: ScheduleViewHolder.ReservationItemListener, var context:Context
) : RecyclerView.Adapter<ScheduleViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_row_schedule, parent, false)
        return ScheduleViewHolder(view, listener)
    }

    override fun onBindViewHolder(holder: ScheduleViewHolder, position: Int) {
        holder.bindData()
        holder.schedule_txt?.text=schedule[position]



    }


    override fun getItemCount(): Int {

        return schedule.size

    }
}



