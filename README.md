# GMapSample

 **It is a google map sample app**

## Features
- It has login and sign up page. save User login date on [Realtime FirebaseFirestore database].
- You can select your profile picture.

     | ![loginShot] | ![signUpShot] | ![profile_picture] |
     |:-------------|:--------------|:-------------------|

- This app has foreground service that get location periodically when app is in foreground and save location on cloud database.
If app go in background service will stop and if app come to foreground and you be on map activity service will start.

   | ![map_fragment] | ![notification] |
   |:----------------|:----------------|

- It uses `ACCESS_FINE_LOCATION` and `FOREGROUND_SERVICE` permissions and has logout capability that send you to login activity.

   | ![location_permission] | ![logout_dialog] |
   |:-----------------------|:-----------------|

- Also retrieve other user location every 3 second and update their location on map. thus track other user locations.
- It has two activity with multiple fragments that replace with each other.
- It use google map and location api.

[Realtime FirebaseFirestore database]: https://console.firebase.google.com/u/0/
[loginShot]: app/src/main/res/drawable/login_shot.png
[map_fragment]: app/src/main/res/drawable/map_fragment.png
[notification]: app/src/main/res/drawable/notification.png
[signUpShot]: app/src/main/res/drawable/sign_up_shot.png
[profile_picture]: app/src/main/res/drawable/select_profile_pic_shot.png
[location_permission]: app/src/main/res/drawable/location_permision.png
[logout_dialog]: app/src/main/res/drawable/logout_dialog.png