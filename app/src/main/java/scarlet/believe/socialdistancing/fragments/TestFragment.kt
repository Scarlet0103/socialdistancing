package scarlet.believe.socialdistancing.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase
import scarlet.believe.socialdistancing.R
import scarlet.believe.socialdistancing.home.HomeFragment
import java.text.SimpleDateFormat
import java.util.*


class TestFragment : Fragment() {

    private var SHARED_PREFS : String = "sharedPrefs"
    private var MY_RISK : String = "myrisk"
    private var USER_ID : String = "userID"
    private var DATE : String = "date"

    private val database = FirebaseDatabase.getInstance()
    private val detailsRef = database.getReference("details")

    private lateinit var sharedPref : SharedPreferences
    private lateinit var risk0 : Button
    private lateinit var risk1 : Button
    private lateinit var risk2 : Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_test, container, false)
        initView(view)
        return view
    }

    private fun initView(view : View){

        sharedPref = activity!!.getSharedPreferences(SHARED_PREFS,Context.MODE_PRIVATE)
        risk0 = view.findViewById(R.id.risk0)
        risk0.setOnClickListener { onClicked(0) }
        risk1 = view.findViewById(R.id.risk1)
        risk1.setOnClickListener { onClicked(1) }
        risk2 = view.findViewById(R.id.risk2)
        risk2.setOnClickListener { onClicked(2) }
    }

    private fun onClicked(risk : Int){
        updateOnFirebase(risk)
    }

    private fun updateOnFirebase(risk : Int){
        val userID = sharedPref.getString(USER_ID,"")
        detailsRef.child(userID!!).child("risk").setValue(risk.toString()).addOnCompleteListener {
            if(it.isSuccessful){
                Log.i("tag","risk updated")
                val editor = sharedPref.edit()
                val dateToday = SimpleDateFormat("yyyy-MM-dd",Locale.getDefault()).format(Date())
                editor.putInt(MY_RISK,risk).apply()
                editor.putString(DATE,dateToday).apply()
                activity?.supportFragmentManager?.beginTransaction()
                    ?.replace(R.id.fragment_container,HomeFragment())
                    ?.commitNow()
            }else{
                Toast.makeText(this.context,"Please check your internet connetion",Toast.LENGTH_SHORT).show()
                return@addOnCompleteListener
            }
        }
    }




}