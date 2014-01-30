TuDelftLogger
=============

Android Logger built for the purposes of my MSc Thesis project

Welcome to the software built for the purp.. ok that's said in the subtitle. The thesis project can be found online in the TUDelft repository here: [ ...... ]

A few words to help fellow researchers use this application. The welcome website can be found in the following address:
http://www.st.ewi.tudelft.nl/~hauff/androidLogger/

Have a look at it first and then come back for more information!

The purpose of the app is to "hook" to the native browser of the Android system and log user's search queries and urls visited. At the moment the user performs one of those two actions, a series of information is captured:

- Browser related entries -> The query term(s) or url(s) visited by the user at the moment. In case of High Privacy, only a hash of those values is stored.
- Location -> Coordinates (Low privacy only), meters from and the name of a "Place" set by the user.
- Light data -> Light sensor data as drawn from the system. This can be used to determine if the location is in or outdoor one.
- Query intent: A notification is sent to the user in the event a query was spotted, asking from him to specify the topic of the query performed. We wanted to capture this information at the moment of the search.

---------

A bit more technical for fellow developers wanting to dive into the code: 
(Note: Understanding and appreciation of this part requires basic understanding of Android development.)

0. Application Code Map:

Packages are divided according to functionality. This way, when a developer is looking for a class, or wants to add his/her own, it can be done easily. 
  a) com.orestis.tudelftlogger ~ The basic package. Contains all the main -activities-. In general, when a class has the ending *Activity, it denotes, well, an activity and not a service, broadcast, etc. is involved. In this package, we spot the SplashActivity, which is displayed for 1,5 second and shows the logo of our group, Web Information Systems of TU Delft. Then, the UIActivity is the main activity, showing the logged -browser- entries sorted by date, newest top. The equivalent UILocationActivity is next, by pressing the "Loc" button. Preferences holds the settings by the user, PlacesActivity is where the user stores their most frequent whereabouts and NotificationReceiverActivity, is the intent selector (topic) at the moment of a search capture.
  b) com.orestis.tudelftlogger.service ~ Service classes, two main services are the BrowserCheckService and the LocationCheckService. They are called as described in  [1. Service orchestration.]
  c) com.orestis.tudelftlogger.database ~ Databases are pretty standard, and it essentially involves a Helper Class that translates the strings needed to fields and thus, easier to be handled. A number of custom objects are defined, all serving the purpose denoted by their name: MBrowser, MLocation, MPlace, Results (the latter helps in the population of the list in the UI).
  d) com.orestis.tudelftlogger.retrieve ~ This package helps us retrieve two sets of information: Call statistics and location information.
  e) com.orestis.tudelftlogger.sensors ~ This one holds data collected from the sensors. For the time being, it holds only implementation of the light data logger.
  f) com.orestis.tudelftlogger.util ~ Self explanatory, holds all classes that target to help small tasks, like forming the url for the communication with the server, compressing, the string, etc. Also, in this package is a part of a cool animation, as the older Android versions did not have official animation methods.

1. Service orchestration: 

Booting up the phone is caught by its broadcast and upon that, MyBootReceiver class is called.
This class does two things, in order to enable two different services: one for browser and one for location.
  a) invoke RegisterReceiverServices - through this class, set a filter to "catch" the screen going on and off. We use that to respectively, start or stop the checking of the browser databases for new entries.
    After that, spotting the screen is handled by MyScreenReceiver, which sets an Alarm for every 5 seconds, to check the databases
  b) Sets an alarm every 1hr, to check for and store location updates.
  
...
