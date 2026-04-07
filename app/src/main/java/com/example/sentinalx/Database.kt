package com.example.sentinalx

import android.content.Context
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory

@Entity(tableName = "threats")
data class ThreatEntity(
    @PrimaryKey val id: String,
    val appName: String,
    val message: String,
    val riskLevel: String,
    val riskScore: Int,
    val confidenceScore: Int,
    val trustLevel: String,
    val timestamp: String,
    val category: String,
    val advice: String,
    val reasonsJson: String,
    val mlScore: Float = 0.0f,
    val isReported: Boolean = false
)

@Dao
interface ThreatDao {
    @Query("SELECT * FROM threats ORDER BY timestamp DESC")
    fun getAllThreats(): Flow<List<ThreatEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(threat: ThreatEntity)

    @Query("UPDATE threats SET isReported = 1 WHERE id = :id")
    suspend fun markAsReported(id: String)

    @Query("DELETE FROM threats")
    suspend fun deleteAll()
}

@Database(entities = [ThreatEntity::class], version = 3)
abstract class AppDatabase : RoomDatabase() {
    abstract fun threatDao(): ThreatDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_2_3 = object : androidx.room.migration.Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE threats ADD COLUMN isReported INTEGER NOT NULL DEFAULT 0")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            val passphrase = SQLiteDatabase.getBytes("startup-grade-sentinel-secret-2025".toCharArray())
            val factory = SupportFactory(passphrase)

            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sentinel_db"
                )
                .openHelperFactory(factory)
                .addMigrations(MIGRATION_2_3)
                .fallbackToDestructiveMigration(false)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
