@import android.content.Context
@import java.util.List
@import kolskypavel.ardfmanager.R
@import kolskypavel.ardfmanager.backend.room.entity.Race
@import kolskypavel.ardfmanager.backend.wrappers.ResultWrapper

@param Race race
@param Context context
@param List<ResultWrapper> results
----------------------------------------------------------------------------------------------------
${context.getString(R.string.general_results)}
----------------------------------------------------------------------------------------------------
${context.getString(R.string.general_race)}: ${race.getName()}
${context.getString(R.string.general_date)}: ${race.getStartDateTime().toLocalDate().toString()}
${context.getString(R.string.general_start_time)}: ${race.getStartDateTime().toLocalTime().toString()}
${context.getString(R.string.race_level)}: ${race.getRaceLevel()}
${context.getString(R.string.general_band)}: ${race.getRaceBand()}
----------------------------------------------------------------------------------------------------
@for(var category:results)
    ${category.component1().component3()}\t${context.getString(R.string.general_limit)}:
    ${context.getString(R.string.race_limit_hint)}: ${race.getTimeLimit().toMinutes()} ${context.getString(R.string.general_minutes)}
    \n
    ${context.getString(R.string.placement)}\t${context.getString(R.string.general_name)}
    ----------------------------------------------------------------------------------------------------
    @for(var cd:category.component3())
    <%--        ${cd.component1().component1().}--%>
    @endfor
@endfor