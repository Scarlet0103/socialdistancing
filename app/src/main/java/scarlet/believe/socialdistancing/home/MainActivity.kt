package scarlet.believe.socialdistancing.home

import android.content.*
import android.net.wifi.WifiManager
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import scarlet.believe.socialdistancing.R
import scarlet.believe.socialdistancing.db.Users
import scarlet.believe.socialdistancing.db.UsersDao
import scarlet.believe.socialdistancing.db.UsersDatabase
import scarlet.believe.socialdistancing.fragments.TestFragment
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.CoroutineContext
import kotlin.math.abs

class MainActivity : AppCompatActivity(),CoroutineScope {

    private var SHARED_PREFS : String = "sharedPrefs"
    private var DATE : String = "date"
    private var DATE2 : String = "date2"
    private var USER_ID : String = "userID"

    val sdf = SimpleDateFormat("yyyy-MM-dd",Locale.getDefault())
    val dateToday = sdf.format(Date())
    val dateTo = sdf.parse(dateToday)


    private var showTestScreen : Boolean = false
    private var exeDailyCheck : Boolean = false
    var builder = NotificationCompat.Builder(this, "Daily Risk Check")
        .setSmallIcon(R.drawable.ic_launcher_background)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)


    private val database = FirebaseDatabase.getInstance()
    private val detailsRef = database.getReference("details")

    private lateinit var sharedPref : SharedPreferences
    private lateinit var usersList : MutableList<Users>
    private lateinit var usersNamesList : MutableList<String>
    private lateinit var usersRiskList : MutableList<String>

//    private lateinit var wifiManager : WifiManager
//    private lateinit var wifiLock : WifiManager.WifiLock
    private var mReceiver: BroadcastReceiver = WifiBroadCastReceiver()
    private lateinit var mIntentFilter: IntentFilter
    private lateinit var usersDao : UsersDao


    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initView()
        initShowTestScreen()
        initDailyCheck()
        //addUserInBackground

    }

    private fun initView(){
        usersList = mutableListOf()
        usersRiskList = mutableListOf()

        sharedPref = getSharedPreferences(SHARED_PREFS,MODE_PRIVATE)

        val db = UsersDatabase.invoke(application)
        usersDao =db.getUsersDao()


//        wifiManager = getApplicationContext().getSystemService(Context.WIFI_SERVICE) as WifiManager
//        wifiLock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF,"MyWifiLock")
//        wifiLock.acquire()
        mIntentFilter = IntentFilter()
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)

    }

    private fun initShowTestScreen(){
        if(showTestScreen){
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container,TestFragment())
                .commitNow()
        }
    }


    private fun initDailyCheck(){
            launch {
                getAllUsers()
            }
    }


    private suspend fun getAllUsers(){
        withContext(Dispatchers.IO){
            val list = usersDao.getAllUsers()
            usersList = list.toMutableList()
            for(users in list){
                usersNamesList.add(users.uid)
            }
            if(exeDailyCheck) {
                checkRisk()
            }
        }
    }

    private suspend fun checkRisk(){
        withContext(Dispatchers.IO){
            val listner = object : ValueEventListener{
                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    val risk  = snapshot.child("risk").getValue(String::class.java)
                    usersRiskList.add(risk!!)

                }
            }
            for(i in usersList){
                val days = abs(dateTo!!.time - i.date.time)
                if(days>10L){
                    deleteUser(i)
                    usersList.remove(i)
                }else{
                    detailsRef.child(i.uid).addListenerForSingleValueEvent(listner)
                }
            }
            notifyUsers()
        }
    }

    private fun notifyUsers(){
        var n = 0
        var m = 0
        for(i in usersRiskList){
            if(i=="1") n++
            if(i=="2") m++
        }
        if(n>0){
            builder
                .setContentTitle("Daily Risk Updates")
                .setContentText("$n people you have met in the past 10days are found to be at risk")
            with(NotificationManagerCompat.from(this@MainActivity)) {
                notify(1, builder.build())
            }
        }
        if(m>0){
            if(n>0){
                builder
                    .setContentTitle("Daily Risk Updates")
                    .setContentText("$m people you have met in the past 10days are tested for postive. Please make sure to be safe")
                with(NotificationManagerCompat.from(this@MainActivity)) {
                    notify(2, builder.build())
                }
            }
        }
    }

    private suspend fun deleteUser(user : Users){
        withContext(Dispatchers.IO){
            usersDao.deleteUser(user)
        }
    }

//    private var addUserInBackground = WifiBroadCastReceiver().deviceNameSetLiveData.observe(this,
//        androidx.lifecycle.Observer {
//            Log.i("tag","addUserInDB")
//               for(users in it){
//                   if(usersNamesList.contains(users)){
//                        updateUsers(users)
//                   }else{
//                        addUsers(users)
//                   }
//               }
//        }
//    )

    private suspend fun updateUsers(name : String){
        val user = Users(name,sdf.parse(dateToday)!!)
        withContext(Dispatchers.IO){
            usersDao.updateUser(user)
        }
    }

    private suspend fun addUsers(name: String){
        val user = Users(name,sdf.parse(dateToday)!!)
        withContext(Dispatchers.IO){
            usersDao.addUser(user)
        }
    }

    override fun onStart() {
        super.onStart()
        sharedPref = getSharedPreferences(SHARED_PREFS,MODE_PRIVATE)
        val date = sharedPref.getString(DATE,"")
        val date2 = sharedPref.getString(DATE2,"")
        if(date==""){
            showTestScreen = true
            initShowTestScreen()
        }else{
            val date1 = sdf.parse(date!!)
            val date2 = sdf.parse(dateToday)
            showTestScreen = date1!!.before(date2)
            initShowTestScreen()
        }
        if(date2==""){
            exeDailyCheck
        }else{
            val date1 = sdf.parse(date2!!)
            val date2 = sdf.parse(dateToday)
            exeDailyCheck = date1!!.before(date2)
        }
        Timer().scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                restartBroadCast()
            }
        }, 0, 30000) //put here time 1000 milliseconds=1 second

    }

    private fun restartBroadCast(){
        if(registerReceiver(mReceiver,mIntentFilter)!=null){
            unregisterReceiver(mReceiver)
            sendBroadcast(Intent(this,WifiBroadCastReceiver::class.java).setAction("WifiDirectScanAction"))
        }
        else
            sendBroadcast(Intent(this,WifiBroadCastReceiver::class.java).setAction("WifiDirectScanAction"))
    }



    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(mReceiver)
//        wifiLock.release()
    }
}