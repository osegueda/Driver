package sv.edu.bitlab.tarea6.ordenHistorial.recyclerView

import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.list_row_schedule.view.*


class ScheduleViewHolder(itemView: View, val listener: ReservationItemListener) : RecyclerView.ViewHolder(itemView)  {


    var schedule_txt:TextView?=null
    var container:View?=null



    fun bindData() {


        schedule_txt=itemView.txt_schedule
        container=itemView.item_container_schedule





       // listener.onViewDetalleOrden(id_txt!!,fecha_txt!!,total_txt!!,status_txt!!,this.adapterPosition)
        container?.setOnClickListener{

            listener.onItemClickReservation(this.adapterPosition)

        }
        
    }


    interface ReservationItemListener{
       fun onItemClickReservation(position: Int)
        fun onItemClickDetalle(btn_detalle:Button,position: Int)
        fun onTextInput(input:String, position: Int)
    }
}