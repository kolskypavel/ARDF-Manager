package kolskypavel.ardfmanager.backend.room.enums

enum class EvaluationStatus {
    OK,
    DISQUALIFIED,
    NO_RANKING,     //Did not fulfill the min CP requirement
    DID_NOT_FINISH, //Did not finish the race
    UNOFFICIAL,     //Did not run officially
    NOT_ASSIGNED,   //Readout not assigned to a competitor
    NOT_EVALUATED   //Not evaluated - missing category
}