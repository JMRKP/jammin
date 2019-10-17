# jammin
Whether on stage or at the fire place: When people come together to play music, the often end up playing the same few songs, because it's so hard to agree on songs/pieces everyone knows.

That's where _jammin_ commes into play. It is an app build for _Android_ allowing you to keep track of your musical repertoire and determine commonly known songs/pieces when having a jam session by connecting participant's phones via bluetooth.

## approach
We had to think of a way to uniform the data, so that the same song has the same appearance on every device. Therefore, every time a user adds a song to the personal repertoire, information on song and artist is fetched from the [iTunes online API](https://affiliate.itunes.apple.com/resources/documentation/itunes-store-web-service-search-api).

A lot of sessions may take place at locations where there is no internet connection. So the application uses bluetooth to connect devices in a star topology with client - server architecture.

## background of the project
The app was written during winter semester 2016/2017 by Oliver Speck and Julius Plener in the context of a high school seminar called '_Mobile Application Praktikum_' at [HdM Stuttgart](https://www.hdm-stuttgart.de/).

## contribution
Julius did bluetooth connection handling and data exchange. Involved classes:

- ConnectThread
- AcceptThread
- ConnectionThread
- ConnectionManagerThread
- ToastHandler
- JoinSessionActivity
- SessionRepertoireActivity
- CustomAdapter
- ConnectedDevicesAdapter
- Repertoire
