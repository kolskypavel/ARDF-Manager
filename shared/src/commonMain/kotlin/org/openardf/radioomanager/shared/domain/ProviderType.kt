package org.openardf.radioomanager.shared.domain

enum class ProviderType(val value: Int) {
    ROBIS(0),
    ROBIS_TEST(1),
    ORESULTS(2),
    OFEED(3);

    companion object {
        fun getByValue(value: Int) =
            entries.firstOrNull { it.value == value } ?: ROBIS
    }
}
