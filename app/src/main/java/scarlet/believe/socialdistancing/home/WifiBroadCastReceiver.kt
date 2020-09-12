package scarlet.believe.socialdistancing.home

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import scarlet.believe.socialdistancing.R
import scarlet.believe.socialdistancing.db.Users
import scarlet.believe.socialdistancing.db.UsersDao
import scarlet.believe.socialdistancing.db.UsersDatabase
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.CoroutineContext

class WifiBroadCastReceiver : BroadcastReceiver(),CoroutineScope{

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO

    private lateinit var usersDao : UsersDao

    private lateinit var builder : NotificationCompat.Builder

    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val dateToday = sdf.format(Date())
    var deviceNameSet =  mutableSetOf<String>()
    private var usersListNames = mutableListOf<String>()
    private var peers = mutableListOf<WifiP2pDevice>()

    private var SHARED_PREFS : String = "sharedPrefs"
    private var USER_ID : String = "userID"
    private var MY_RISK : String = "myrisk"
    private var PREFIX_NAME : String = "SDA-"

    override fun onReceive(p0: Context?, p1: Intent?) {

        val db = UsersDatabase.invoke(p0!!)
        usersDao =db.getUsersDao()
        launch {
            usersListNames = usersDao.getAllUsersNames().toMutableList()
        }

        builder = NotificationCompat.Builder(p0!!, "WifiDirectScanRisk")
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        val mManager: WifiP2pManager? = p0?.getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
        val mChannel = mManager?.initialize(p0, Looper.getMainLooper(),null)

        val sharedPref = p0.getSharedPreferences(SHARED_PREFS,MODE_PRIVATE)
        val risk = sharedPref.getInt(MY_RISK,0).toString()
        var name = sharedPref.getString(USER_ID,"")
        name= "$PREFIX_NAME$name-$risk"

        if(p1?.action=="WifiDirectScanAction"){
            discoverPeers(mManager,mChannel,p0)
            DeviceName().setDeviceName(mManager,mChannel,name)
        }

        if(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION==p1!!.action){
            Toast.makeText(p0,"req peers", Toast.LENGTH_SHORT).show()
            mManager?.requestPeers(mChannel) {
                if (it?.deviceList!! != peers) {
                    peers.clear()
                    peers.addAll(it.deviceList)
                    for (device in it.deviceList) {
                        if(device.deviceName.contains("SDA")){
                            deviceNameSet.add(device.deviceName)
                        }
                        Log.i("Devices", device.deviceName)
                    }
                    addUserInBackground(deviceNameSet,p0)
                }
            }
        }

    }

    private fun discoverPeers(mManager : WifiP2pManager?,mChannel : WifiP2pManager.Channel?,context: Context?){
        mManager?.discoverPeers(mChannel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                Toast.makeText(context,"Discovery Started", Toast.LENGTH_SHORT).show()
            }

            override fun onFailure(p0: Int) {
                Toast.makeText(context,"Discovery Failed - $p0", Toast.LENGTH_SHORT).show()
            }

        })
    }


    private fun addUserInBackground(deviceNameSet: MutableSet<String>,context: Context) {
        for(i in deviceNameSet){
            if(usersListNames.contains(i)){
                updateUser(i)
            }else{
                addUser(i)
            }
            if(i[15]=='1'){
                builder
                    .setContentTitle("Warning")
                    .setContentText("people around you have found to be at risk. Stay Safe and Healthy")
                with(NotificationManagerCompat.from(context)) {
                    notify(3, builder.build())
                }
            }else if(i[15]=='2'){
                builder
                    .setContentTitle("Warning")
                    .setContentText("people around you have found to be positive. Stay Safe and Healthy")
                with(NotificationManagerCompat.from(context)) {
                    notify(4, builder.build())
                }
            }

        }
    }

    private fun addUser(i: String) {
        val user = Users(i,sdf.parse(dateToday)!!)
        launch {
            Log.i("users","addUser")
            usersDao.addUser(user)
        }
    }

    private fun updateUser(i: String) {
        val user = Users(i,sdf.parse(dateToday)!!)
        launch {
            Log.i("users","updateUser")
            usersDao.updateUser(user)
        }
    }
}