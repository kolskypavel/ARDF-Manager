package kolskypavel.ardfmanager.backend.room.enums

enum class RaceStatus(val value: Int) : Comparable<RaceStatus> {
    VALID(0),
    NO_RANKING(1),     //Did not fulfill the min CP requirement
    DISQUALIFIED(2),
    DID_NOT_FINISH(3), //Did not finish the race
    OVER_TIME_LIMIT(4),     //Over the time limit / not enough points
    UNOFFICIAL(5),     //Did not run officially
    NOT_PROCESSED(6),    //Not processed - missing category or not assigned
    ERROR(7) //Error - missing finish record etc
}