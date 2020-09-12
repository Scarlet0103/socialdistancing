package scarlet.believe.socialdistancing.auth

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import scarlet.believe.socialdistancing.home.MainActivity
import scarlet.believe.socialdistancing.R
import java.util.concurrent.TimeUnit
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

class AuthActivity : AppCompatActivity() {

    private lateinit var auth : FirebaseAuth
    private lateinit var phoneNum_txt : TextInputEditText
    private var phoneNumber : String? = null
    private var userID : String?= null
    private lateinit var name_txt : TextInputEditText
    private var displayName : String? = null
    private lateinit var auth_Btn : ImageButton
    private lateinit var otp_txt : TextInputEditText
    private lateinit var authOTP_btn : ImageButton
    private lateinit var authViewModel: AuthViewModel
    private var codeBySystem : String? = null

    private val encrypt = charArrayOf('z','y','w','u','s','j','l','n','p','r')

    private var SHARED_PREFS : String = "sharedPrefs"
    private var USER_ID : String = "userID"

    private var countryCODE : String = "+91"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)
        initView()
        initAuthViewModel()
    }

    private fun initView() {

        auth = FirebaseAuth.getInstance()
        phoneNum_txt = findViewById(R.id.phoneNum_edtxt)
        name_txt = findViewById(R.id.name_edtxt)
        otp_txt = findViewById(R.id.otp_edtxt)
        auth_Btn = findViewById(R.id.auth_Btn)
        authOTP_btn = findViewById(R.id.auth_otp_btn)

        auth_Btn.setOnClickListener {
            if(!phoneNum_txt.text.isNullOrEmpty() && !name_txt.text.isNullOrEmpty()){
                userID = encryptPhoneNum(phoneNum_txt.text.toString())
                phoneNumber = countryCODE + phoneNum_txt.text.toString()
                displayName = name_txt.text.toString()
                PhoneAuthProvider.getInstance().verifyPhoneNumber(
                    phoneNumber!!,
                    60,
                    TimeUnit.SECONDS,
                    this,
                    callbacks)
            }else{
                Toast.makeText(this,"PhoneNumber or Name cannot be empty",Toast.LENGTH_SHORT).show()
            }
        }

        authOTP_btn.setOnClickListener {
            if(!otp_txt.text.isNullOrEmpty()){
                verifyCode(otp_txt.text.toString())
            }else{
                Toast.makeText(this,"OTP cannot be empty",Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun encryptPhoneNum(number : String): String? {
        var s : String = ""
        for(c in number){
            s += encrypt[Character.getNumericValue(c)]
        }
        return s
    }


    private fun initAuthViewModel(){
        authViewModel = ViewModelProvider(this).get(AuthViewModel::class.java)
    }


    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onCodeSent(p0: String, p1: PhoneAuthProvider.ForceResendingToken) {
            super.onCodeSent(p0, p1)
            codeBySystem = p0
        }

        override fun onVerificationCompleted(p0: PhoneAuthCredential) {
            val code = p0.smsCode
            if(code!=null){
                otp_txt.setText(code)
                verifyCode(code)
            }
        }

        override fun onVerificationFailed(p0: FirebaseException) {
            Toast.makeText(this@AuthActivity,p0.localizedMessage,Toast.LENGTH_SHORT).show()
        }

    }

    private fun verifyCode(code : String){

        val credential = PhoneAuthProvider.getCredential(codeBySystem!!, code)
        authViewModel.firebaseSignInOTP(credential,userID!!,phoneNumber!!,displayName!!)
        authViewModel.authenticatedUserLiveData.observe(this,
            Observer {
                val sharedPref = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE)
                val editor = sharedPref.edit()
                editor.putString(USER_ID,it.uid).apply()
                if (it.isNew){
                    createNewUser(it)
                }else{
                    goToMainActivity(it)
                }

            }
        )

    }

    private fun createNewUser(user : User){
        authViewModel.createUser(user)
        authViewModel.createdUserLiveData.observe(this, Observer {
            var sharedPref = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE)
            var editor = sharedPref.edit()
            editor.putString(USER_ID,it.uid).apply()
            goToMainActivity(it)
        })
    }

    private fun goToMainActivity(user : User){

        val intent = Intent(this,
            MainActivity::class.java)
        intent.putExtra("user",user)
        startActivity(intent)
        finish()

    }


}