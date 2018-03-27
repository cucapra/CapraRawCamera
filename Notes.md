## Understanding Opencamera
1. Figure out how to enable raw by default (Done)
MyApplicationInterface.useCamera2 returns
PreferenceKeys.UseCamera2PreferenceKey

2. Find out how to send data to google drive
How to get permission to send file to our data collection account
https://developers.google.com/identity/protocols/OAuth2ServiceAccount
Google drive service account (x)
Use google drive credential and refresh token to get access token from this post
https://stackoverflow.com/questions/19766912/how-do-i-authorise-an-app-web-or-installed-without-user-intervention

3. Find a place in opencamera to trigger sending data to google drive
Call google drive service (should keep uploading data event if the camera app is turned off)
