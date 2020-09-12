package scarlet.believe.socialdistancing.splash

import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import scarlet.believe.socialdistancing.auth.User

class SplashRepository {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private var user : User = User()
    private val database = FirebaseDatabase.getInstance()
    private val detailsRef = database.getReference("details")

    fun isUserAuthenticatedInFirebase() : MutableLiveData<User> {
        val isUserAuthenticatedInFirebaseLiveData : MutableLiveData<User> = MutableLiveData()
        val firebaseuser = firebaseAuth.currentUser
        if(firebaseuser==null){
            user.isAuthenticated = false
            isUserAuthenticatedInFirebaseLiveData.value = user
        }else{
            user.uid = firebaseuser.uid
            user.isAuthenticated = true
            isUserAuthenticatedInFirebaseLiveData.value = user
        }
        return isUserAuthenticatedInFirebaseLiveData
    }

    fun getUserFromDatabase(uid : String) : MutableLiveData<User> {
        val getUserFromDatabaseLiveData : MutableLiveData<User> = MutableLiveData()
        var user = User()
        val listner = object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                user = snapshot.getValue(User::class.java)!!
            }

        }
        detailsRef.child(uid).addListenerForSingleValueEvent(listner)
        getUserFromDatabaseLiveData.value = user
        return getUserFromDatabaseLiveData
    }

}