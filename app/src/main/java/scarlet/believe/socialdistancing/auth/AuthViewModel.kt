package scarlet.believe.socialdistancing.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.google.firebase.auth.PhoneAuthCredential

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private var authRespository : AuthRepository = AuthRepository()
    lateinit var authenticatedUserLiveData : LiveData<User>
    lateinit var createdUserLiveData : LiveData<User>

    fun firebaseSignInOTP(credential : PhoneAuthCredential,userID : String, phoneNumber : String, name : String){
        authenticatedUserLiveData = authRespository.firebaseSignInOTP(credential,userID,phoneNumber,name)
    }

    fun createUser(authenticatedUser : User){
        createdUserLiveData = authRespository.createUserInDatabaseIfNotExists(authenticatedUser)
    }


}