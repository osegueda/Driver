package sv.edu.bitlab.driver.models

 data class Reservation(var available:Boolean?=false,
                        var date:String?="",
                        var id:String?="",
                        var pplsize:Int?=0,
                        var round:Int?=0,
                        var schedule:String?="",
                        var round_status:String?="",
                        var users:ArrayList<String> =ArrayList())