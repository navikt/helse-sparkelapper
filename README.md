Sparkel
=======

## Legge til en ny gradlemodul

1. Lag en mappe og sørg for at det finnes en `build.gradle.kts` der

## Legge til ny app

Alle gradlemodulene bygges og releases automatisk. 
Ved hver pakke som blir lastet opp trigges en deployment workflow for den pakken.

1. Gjør 'Legge til en ny gradlemodul'. Mappenavnet korresponderer med appnavnet
2. Lag `config/[app]/[cluster].yml` for de klustrene appen skal deployes til. 
3. Push endringene