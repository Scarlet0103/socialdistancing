package scarlet.believe.socialdistancing.db

import androidx.room.*

@Dao
interface UsersDao {

    @Insert
    fun addUser(user : Users)

    @Query("SELECT * FROM users ORDER BY date")
    fun getAllUsers() :List<Users>

    @Query("SELECT uid FROM users ORDER BY date")
    fun getAllUsersNames() :List<String>

    @Update
    fun updateUser(user : Users)

    @Delete
    fun deleteUser(user : Users)

}