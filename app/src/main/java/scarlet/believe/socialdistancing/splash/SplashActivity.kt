package scarlet.believe.socialdistancing.splash

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import scarlet.believe.socialdistancing.home.MainActivity
import scarlet.believe.socialdistancing.R
import scarlet.believe.socialdistancing.auth.AuthActivity

class SplashActivity : AppCompatActivity() {

    private lateinit var splashViewModel: SplashViewModel

    private var SHARED_PREFS : String = "sharedPrefs"
    private var USER_ID : String = "userID"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        initSplashViewModel()
        checkIfUserIsAuthenticated()

    }

    private fun initSplashViewModel(){
        splashViewModel = ViewModelProvider(this).get(SplashViewModel::class.java)
    }

    private fun checkIfUserIsAuthenticated(){
        splashViewModel.checkIsUserIsAuthenticated()
        splashViewModel.isUserAuthenticated.observe(this,
            Observer {
                if(!it.isAuthenticated){
                    val intent = Intent(this, AuthActivity::class.java)
                    startActivity(intent)
                    finish()
                }else{
                    getUserFromDatabase()
                }
            })
    }

    private fun getUserFromDatabase(){
        val sharedPref = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE)
        val uid = sharedPref.getString(USER_ID,"")!!
        splashViewModel.getUserFromDatabase(uid)
        splashViewModel.userData.observe(this,
            Observer {
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("user",it)
                startActivity(intent)
                finish()
            })
    }

}