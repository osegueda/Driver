package sv.edu.bitlab.driver.fragments.activationComponents.recyclerview

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import sv.edu.bitlab.driver.R


class ScheduleAdapter(var images:MutableList<Int>, var schedule:Array<String>, val listener: ScheduleViewHolder.ReservationItemListener, var context:Context
) : RecyclerView.Adapter<ScheduleViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_row_schedule, parent, false)
        return ScheduleViewHolder(view, listener)
    }

    override fun onBindViewHolder(holder: ScheduleViewHolder, position: Int) {
        holder.bindData()
        holder.schedule_txt?.text=schedule[position]
        //holder.image?.setImageResource(images[position])



    }


    override fun getItemCount(): Int {

        return schedule.size

    }
}



