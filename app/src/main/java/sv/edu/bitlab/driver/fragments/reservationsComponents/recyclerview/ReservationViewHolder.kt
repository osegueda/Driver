package sv.edu.bitlab.driver.fragments.reservationsComponents.recyclerview

import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.list_row_reservation.view.*

class ReservationViewHolder(itemView: View, val listener: ReservationItemListener) : RecyclerView.ViewHolder(itemView)  {

    var card1:View?=null
    var card2:View?=null



    //card1
    var status_image: ImageView? = null
    var id_txt: TextView? = null
    var status_txt: TextView? =null
    var date_txt: TextView?=null
    var count_txt:TextView? =null
    var round_txt:TextView?=null
    var schedule_txt:TextView?=null
    var container:View?=null

    //card2

    var result_number:TextView?=null
    var result_txt:TextView?=null

    fun bindData() {

        card1=itemView.test1

        status_image = itemView.imageView
        id_txt=itemView.idTxt
        status_txt=itemView.statusTxt
        count_txt=itemView.countTxt
        round_txt=itemView.roundTxt
        container=itemView.item_container_reservation




    }


    interface ReservationItemListener{
       fun onItemClickReservation(position: Int,status:String,round:Int,id:String,ongoing:Boolean,pplsize:Int)

    }
}