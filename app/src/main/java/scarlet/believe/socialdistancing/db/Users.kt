package scarlet.believe.socialdistancing.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable
import java.util.*

@Entity
data class Users(@PrimaryKey
                 var uid : String,
                 var date : Date) : Serializable

