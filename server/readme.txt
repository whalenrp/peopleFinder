Google App Engine Documentation: http://developers.google.com/appengine/

The source code contained in this directory does not provide
the total "package" required to be deployed to the Google App
Engine web service. While the implementation of the server
is all here, the App Engine SDK and other ancillary tools 
used in deployment are not included. Libraries and other 
binaries from the 'war' subdirectory are not included either.

I have used the Google Plugin for Eclipse to manage project 
creation, testing, and deployment. Other methods are of 
course available, such as using Ant. A good intro and the
necessary links can be found here: 
http://developers.google.com/appengine/docs/java/gettingstarted

The biggest and most important component of the server source
code has to do with using JDO, or Java Data Objects. JDO is 
used to interface with the App Engine's datastore. Google's 
resource on JDO and how it is used: 
http://developers.google.com/appengine/docs/java/datastore/jdo/
 