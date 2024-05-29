# ARDF-Manager-kotlin
Mobile app for managing ARDF events, written in Kotlin.
Originally a school project for MSOE - CSC 4911.

## Basic app information
This app enables event management for the sport of ARDF. 
Currently availabe functions:
 - CRUD operations on the races, categories, and competitors
 - Data readout using the SportIdent reader via USB OTG
 - Initial processing for the supporting competition formats.

## Equipment needed
- Sportident BSM 7 / BSM 8 reader
- USB to OTG adaper

## Supported competition formats
- Classics
- Foxoring
- Orienteering
- Sprint

Other formats (Long, Custom) are to be implemented.

## Supported file formats
### Category import / export
- CSV
- TLN
- IOF XML

### Competitor import / export
- CSV CHEB
- TLN
- IOF XML

### Results
- CSV CHEB
- PDF (basic or splits)
- HTML (basic or splits)
- IOF XML

## Icons
SportIdent by Gonzo from <a href="https://thenounproject.com/browse/icons/term/sportident/" target="_blank" title="SportIdent Icons">Noun Project</a> (CC BY 3.0)
