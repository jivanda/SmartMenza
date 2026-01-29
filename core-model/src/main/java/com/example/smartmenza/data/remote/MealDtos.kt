import com.google.gson.annotations.SerializedName

data class MealDto(
    val mealId: Int,
    val mealTypeId: Int,
    val name: String,
    val description: String?,
    val price: Double,
    val calories: Double?,
    val protein: Double?,
    val carbohydrates: Double?,
    val fat: Double?,
    @SerializedName(value = "imageUrl", alternate = ["ImageUrl"])
    val imageUrl: String
)