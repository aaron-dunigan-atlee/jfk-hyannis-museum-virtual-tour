Udacity reviewer:

A few notes to help you review the app:

LOCATION CHECKING

This app is intended to be used in a specific location: The JFK Hyannis Museum.  It uses location checking and limits functionality if the user is not at the museum.  Therefore, to review the app, you must either

* spoof your emulator or test device location and set the coordinates to latitude 41.652222 and longitude -70.284139

OR

* run the debug version of the app, which does not limit functionality

BARCODE SCANNING

The app's functionality requires the user to scan some specific QR codes that will be present at the museum.  Samples of these barcodes are in the "sample_barcodes" folder in the project root directory.  You can send them to your emulator's virtual camera, or display/print them for use with a physical device.