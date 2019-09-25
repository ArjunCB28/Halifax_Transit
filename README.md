# HalifaxTransit

This is an Android app which shows the real-time bus positions in Halifax. User can view all the buses running across Halifax or perform some filter and view specific buses and their real-time location.

The app uses Google maps to plot the real-time position of the buses in Halifax. The bus information is taken from [Halifax open data](https://www.halifax.ca/home/open-data/halifax-transit-open-data) using protocol buffer. [Gtfs-realtime library](https://github.com/google/transit/tree/master/gtfs-realtime/spec/en) is used to get the data from Halifax open data. The Gfts-realtime library returns an object that has the details of the buses, their position, delay time and all other information. These information is plotted against the Google map.

A filter option is provided which can filter the required bus from the list of all buses running in Halifax. The app remembers recently used state and loads the state when the app is opened again.
