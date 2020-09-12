package scarlet.believe.socialdistancing.auth

import com.google.firebase.database.Exclude
import java.io.Serializable

class User : Serializable {
    var uid: String? = null
    var name: String? = null
    var phone: String? = null
    var risk: String? = null
    var location: String? = null
    @Exclude
    var isAuthenticated = false
    @Exclude
    var isNew = false
    @Exclude
    var isCreated = false

    constructor() {}
    internal constructor(uid: String?, name: String?, phone: String?, risk: String?,location: String?) {
        this.uid = uid
        this.name = name
        this.phone = phone
        this.risk = risk
        this.location = location
    }
}