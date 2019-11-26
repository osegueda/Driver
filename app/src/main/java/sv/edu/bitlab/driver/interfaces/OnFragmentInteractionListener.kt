package sv.edu.bitlab.driver.interfaces

import sv.edu.bitlab.driver.FragmentsIndex


interface OnFragmentInteractionListener {
    fun onFragmentInteraction(index: FragmentsIndex)
    fun onFragmentInteraction(index:FragmentsIndex, obj:Any)
    fun onFragmentInteraction(index:FragmentsIndex, obj1:Any,obj2:Any)

}