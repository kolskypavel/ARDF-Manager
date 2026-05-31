package org.openardf.radioomanager.shared.domain

enum class ResultServiceStatus(val value: Int) {
    DISABLED(0),
    RUNNING(1),
    NO_NETWORK(2),
    UNAUTHORIZED(3),
    ERROR(4)
}
