package sv.edu.bitlab.driver.fragments.reservationsComponents.recyclerview

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.list_row_confirmation.view.*
import kotlinx.android.synthetic.main.list_row_detail.view.*
import sv.edu.bitlab.driver.models.Reservation

class ReservationDetailViewHolder(itemView: View, val listener: ReservationDetailItemListener) : RecyclerView.ViewHolder(itemView) {

    var user_txt: TextView? = null

    fun bindData() {

      //  user_txt=itemView.username_txt
        user_txt=itemView.usr_txt

    }

    interface ReservationDetailItemListener{
        fun itemClickToDetail(rsv: Reservation)
    }

}