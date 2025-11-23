## [0.1.0] - 2025-23-11
### Added
- Changed name to Radio-O Manager
- Added SHORT and OTHER to race options 
- Added edit dialog to race data import
- Added duplicate SI card read options (ignore/overwrite/create new)
- Added delay between printing two finish tickets
- Added XML import of categories and export of results
- Added OResults and OFeed to result export services, delay can now be configured
- Added sounds after readout for error and rented SI cards
- Added SI rent overview to competitor table

### Removed
- Removed automatic category assignment
- Removed stop on invalid from data import options

### Fixed
- Fixed freezing when printing
- Fixed readout notification
- Moved result recalculation to menu to prevent freezing
- Adjusted export to new version of ARDF JSON

## [0.0.3] - 2025-09-29
### Added
- Added JSON export to results
- Added swipe up action to recalculate results
- Added Race data import validation
- Control aliases can now be 6 letters long and contain a slash (/)
- Added multiple tests

### Fixed
- Fixed calculation of run times and time to limit
- Fixed display of data among races
- Fixed SI5 time calculation with drawn times
- Fixed sorting to be locale based
- Stability improvements

## [0.0.2] - 2025-09-06
### Added
- Added double result ticket printing option
- Added crashlytics reports

### Fixed
- Fixed alias printing and printer setup
- Fixed start time loading from startlist
- Fixed wrong time limit evaluation
- Fixed time input for race start time
- Added null checks in retrieving current race to prevent app crashes

## [0.0.1] - 2025-08-25
### Added
- Initial release of the app - beta version

---