# Android Regression Test Results - 2026-05-31

Status: passed

Branch: `codex/multiplatform-foundation`

Device: moto g 5G - 2024 (`fogo`)

## Scope

This pass verified that the Android app still performs the core race-day
SportIdent download workflow after the shared Kotlin foundation refactor. The
test focused on the existing Android behavior, not new desktop functionality.

## Hardware

- SPORTident USB download box detected by Android as:
  - manufacturer: `SPORTident GmbH`
  - product: `SPORTident USB to UART Bridge Controller`
  - serial: `554896`
  - vendor/product: `4292/32778`
- Moto connected to the download box through USB OTG.

## Result

- App installed and launched successfully.
- Android USB permission was granted after reconnecting the download box.
- `SIReaderService` started and remained active as a foreground service.
- App status reached `SI station 554896 connected`.
- A temporary race was created and selected for readout testing.
- First SI card download succeeded:
  - card: `2005010`
  - result rows: `1`
  - punch rows: `17`
  - UI showed `SI station 554896, read card 2005010`.
- Bulk SI card download succeeded:
  - additional unique cards read: `15`
  - duplicate reads attempted: `1`
  - final result rows: `16`
  - final punch rows: `128`
  - duplicate card event did not create an extra result row.
- Focused log checks found no Radio-O-Manager crash, ANR, force-finish, or
  process-death signatures.

## Notes

- Three downloaded cards produced `ERROR` result status because their card data
  lacked a complete start/finish pair. This matched the stored card data and was
  not treated as an app regression.
- Aggregate readout counters stayed at `0/0` because the temporary test race had
  no competitors or categories configured. Individual readout rows and database
  records were created correctly.
- One temporary ADB offline state occurred while the phone was connected through
  the OTG/download-box setup. Reconnecting ADB restored device access; the app
  and SI reader service remained healthy.
