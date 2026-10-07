package np.com.edu.ismt.Sabinpokhrel.petcare.data.local

import androidx.room.TypeConverter
import np.com.edu.ismt.Sabinpokhrel.petcare.data.local.entity.Frequency
import np.com.edu.ismt.Sabinpokhrel.petcare.data.local.entity.PlaceType
import np.com.edu.ismt.Sabinpokhrel.petcare.data.local.entity.TaskCategory

class Converters {
    @TypeConverter
    fun fromTaskCategory(value: TaskCategory): String = value.name

    @TypeConverter
    fun toTaskCategory(value: String): TaskCategory = TaskCategory.valueOf(value)

    @TypeConverter
    fun fromFrequency(value: Frequency): String = value.name

    @TypeConverter
    fun toFrequency(value: String): Frequency = Frequency.valueOf(value)

    @TypeConverter
    fun fromPlaceType(value: PlaceType): String = value.name

    @TypeConverter
    fun toPlaceType(value: String): PlaceType = PlaceType.valueOf(value)
}
