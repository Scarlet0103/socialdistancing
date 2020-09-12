package scarlet.believe.socialdistancing.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Users::class], version = 2)
@TypeConverters(DateConverters::class)
abstract class UsersDatabase : RoomDatabase() {

    abstract fun getUsersDao() : UsersDao

    companion object{

        @Volatile
        private var instance : UsersDatabase? = null
        private val LOCK = Any()
        operator fun invoke(context: Context) = instance ?: synchronized(LOCK){
            instance ?: buildDatabase(context).also {
                instance = it
            }
        }

        private fun buildDatabase(context: Context) = Room.databaseBuilder(
            context.applicationContext,UsersDatabase::class.java,"usersdatabase"
        ).fallbackToDestructiveMigration().build()

    }

}