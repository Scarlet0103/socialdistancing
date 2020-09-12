package scarlet.believe.socialdistancing.auth

import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.database.FirebaseDatabase

class AuthRepository {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()
    private val detailsRef = database.getReference("details")

    fun firebaseSignInOTP(credential : PhoneAuthCredential,userID : String, phoneNumber : String, name : String ) : MutableLiveData<User> {

        val authenticatedUserMutableLiveData : MutableLiveData<User> = MutableLiveData()
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener {

            if(it.isSuccessful){
                val  isnewUser : Boolean = it.getResult()!!.additionalUserInfo!!.isNewUser
                val firebaseUser = firebaseAuth.currentUser
                if(firebaseUser!=null){
                    val user = User(userID,name,phoneNumber,"0",null)
                    user.isNew = isnewUser
                    authenticatedUserMutableLiveData.value = user
                }
            }else{
                if (it.exception is FirebaseAuthInvalidCredentialsException) {
                    // The verification code entered was invalid
                }
            }

        }

        return authenticatedUserMutableLiveData
    }


    fun createUserInDatabaseIfNotExists(authenticatedUser : User) : MutableLiveData<User>{

        val newUserMutableLiveData : MutableLiveData<User> = MutableLiveData()
        detailsRef.child(authenticatedUser.uid!!).setValue(authenticatedUser).addOnCompleteListener {
            if(it.isSuccessful){
                authenticatedUser.isCreated = true
                detailsRef.child(authenticatedUser.uid!!).child("created").setValue(true)
                newUserMutableLiveData.value = authenticatedUser
            }else{
                //logErrorMessage(userCreationTask.getException().getMessage());
            }
        }
        return newUserMutableLiveData
    }

}