@import java.util.List
@import kolskypavel.ardfmanager.backend.room.entitity.Race
@import kolskypavel.ardfmanager.backend.wrappers.ResultDisplayWrapper

@param Race race
@param String titleResults
@param List<ResultDisplayWrapper> results
----------------------------------------------------------------------------------------------------
${titleResults}
----------------------------------------------------------------------------------------------------
${title_name}: ${race.getName()}
${title_date}: ${race.getStartDateTime().toLocalDate().toString()}
${title_start_time}: ${race.getStartDateTime().toLocalTime().toString()}
${title_level}: ${race.getRaceLevel()}
${title_band}: ${race.getRaceBand()}
${title_limit}: ${race.getTimeLimit()}
----------------------------------------------------------------------------------------------------
@for(var result:results)
    ${result.component1().getName()}
@endfor