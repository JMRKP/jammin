# jammin
An app build for android allowing musicians to keep track of their musical repertoire and determine their commonly known songs/pieces when having a jam session. Whether on stage or at the fire: When people come together to play music, the often end up playing the same few songs, because of the difficulty to determine the common song / piece repertoire. 

That's where _jammin_ commes into play. It is an app build for Android allowing you to keep track of your musical repertoire and determine commonly known songs/pieces when having a jam session by connecting participant's phones via bluetooth.

## approach
We had to think of a way to uniform the data, so that the same song has the same appearance on every device. Therefore every time a user adds a song to the personal repertoire, information on song and artist is fetched from the iTunes online API.

A lot of session take place where there is no internet connection. So the application uses bluetooth to connect devices in a star topology with client - server architecture.

## background of the project
The app was written during winter semester 16/17 by Oliver Speck and Julius Plener in the context of a high school seminar called 'Mobile Application Praktikum' at HdM Stuttgart.

## contribution
Julius: bluetooth connection handling and data exchange. classes:

ConnectThread
AcceptThread
ConnectionThread
ConnectionManagerThread
ToastHandler
JoinSessionActivity
SessionRepertoireActivity
CustomAdapter
ConnectedDevicesAdapter
Repertoire
