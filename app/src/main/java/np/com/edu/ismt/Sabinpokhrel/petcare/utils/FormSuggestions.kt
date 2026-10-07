package np.com.edu.ismt.Sabinpokhrel.petcare.utils

import np.com.edu.ismt.Sabinpokhrel.petcare.data.local.entity.TaskCategory

/**
 * Static suggestion lists used to power autocomplete dropdowns on the
 * Add/Edit Pet and Add/Edit Task forms. These are just common, sensible
 * defaults the user can pick from or ignore entirely and type their own text.
 */
object FormSuggestions {

    val breeds = listOf(
        "Labrador Retriever", "Golden Retriever", "German Shepherd", "Bulldog", "Poodle",
        "Beagle", "Rottweiler", "Dachshund", "Siberian Husky", "Shih Tzu", "Chihuahua",
        "Pug", "Boxer", "Doberman", "Great Dane",
        "Domestic Shorthair", "Persian", "Siamese", "Maine Coon", "Ragdoll", "Bengal",
        "British Shorthair", "Sphynx",
        "Holland Lop Rabbit", "Dutch Rabbit", "Budgerigar", "Cockatiel", "Guinea Pig"
    )

    val dietaryPreferences = listOf(
        "Dry kibble, twice daily", "Wet food, twice daily", "Grain-free diet",
        "Raw diet (BARF)", "Prescription diet", "Portion-controlled meals",
        "Once daily feeding", "No dairy", "Vegetarian diet", "Senior formula food"
    )

    val allergies = listOf(
        "None known", "Chicken", "Beef", "Dairy", "Wheat / gluten", "Grain",
        "Pollen", "Fleas", "Certain medications", "Soy", "Seafood"
    )

    val favouriteToys = listOf(
        "Tennis ball", "Rope toy", "Squeaky toy", "Chew toy", "Feather wand",
        "Catnip mouse", "Puzzle feeder", "Frisbee", "Plush toy", "Laser pointer"
    )

    /** Suggested task titles, grouped by category. */
    fun taskTitles(category: TaskCategory): List<String> = when (category) {
        TaskCategory.FEEDING -> listOf("Morning feeding", "Evening feeding", "Breakfast", "Dinner", "Treat time")
        TaskCategory.EXERCISE -> listOf("Morning walk", "Evening walk", "Playtime", "Fetch session", "Park visit")
        TaskCategory.GROOMING -> listOf("Brushing", "Bath time", "Nail trim", "Ear cleaning", "Teeth brushing")
        TaskCategory.MEDICATION -> listOf("Flea medication", "Heartworm prevention", "Daily pill", "Vitamin supplement", "Allergy medication")
        TaskCategory.HEALTHCARE -> listOf("Vet checkup", "Vaccination appointment", "Dental checkup", "Weigh-in", "Deworming")
    }

    /** Suggested supplies, grouped by category. */
    fun taskSupplies(category: TaskCategory): List<String> = when (category) {
        TaskCategory.FEEDING -> listOf("Dry kibble", "Wet food", "Food bowl", "Measuring cup", "Treats")
        TaskCategory.EXERCISE -> listOf("Leash", "Harness", "Poop bags", "Ball", "Water bottle")
        TaskCategory.GROOMING -> listOf("Brush", "Shampoo", "Nail clippers", "Towel", "Toothbrush")
        TaskCategory.MEDICATION -> listOf("Pill dispenser", "Medication", "Syringe", "Treats (for hiding pills)")
        TaskCategory.HEALTHCARE -> listOf("Vaccination record", "Carrier", "Leash", "Medical history")
    }
}
