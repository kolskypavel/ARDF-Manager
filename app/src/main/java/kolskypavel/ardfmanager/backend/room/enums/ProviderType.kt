package kolskypavel.ardfmanager.backend.room.enums

enum class ProviderType(val value: Int) {
    ROBIS(0),
    ROBIS_TEST(1),
    ORESULTS(2),
    OFEED(3);

    companion object {
        fun getByValue(value: Int) =
            ProviderType.entries.firstOrNull { it.value == value } ?: ROBIS
    }
}