# Actions on Google: Name Psychic Sample

This sample demonstrates Actions on Google features for use on Google Assistant including permission requests for [user information](https://developers.google.com/actions/assistant/helpers#user_information), [surface transfer capabilities](https://developers.google.com/actions/assistant/surface-capabilities#multi-surface_conversations), user storage, SSML, unrecognized deep link fallbacks, and Google Maps Static API -- -- using the [Java client library](https://github.com/actions-on-google/actions-on-google-java) and deployed on [App Engine](https://cloud.google.com/appengine/docs/standard/java/quickstart).

Note that if the user is not [verified](https://developers.google.com/actions/assistant/guest-users), their information will
never be saved across conversations and they will encounter a guest flow (requesting permission) each time.

### Enable Billing
**Required for running this sample**
This sample uses Firebase Cloud Functions to make an HTTP request to Google Maps Static API. If you plan to run the sample, you will need to temporarily upgrade to a Firebase plan that allows for outbound networking, such as the [Blaze Plan](https://firebase.google.com/pricing/), also called Pay as you go.

### Setup Instructions
### Prerequisites
1. Download & install the [Google Cloud SDK](https://cloud.google.com/sdk/docs/)
1. [Gradle with App Engine Plugin](https://cloud.google.com/appengine/docs/flexible/java/using-gradle)
   + Run `gcloud auth application-default login` with your Gooogle account
   + Install and update the App Engine component,`gcloud components install app-engine-java`
   + Update other components, `gcloud components update`

### Configuration
#### Actions Console
1. From the [Actions on Google Console](https://console.actions.google.com/), add a new project (this will become your *Project ID*) > **Create Project** > under **More options** > **Conversational**.
1. From the left navigation menu under **Build** > **Actions** > **Add Your First Action** > **BUILD** (this will bring you to the Dialogflow console) > Select language and time zone > **CREATE**.
1. In Dialogflow, go to **Settings** ⚙ > **Export and Import** > **Restore from zip**.
   + Follow the directions to restore from the `agent.zip` file in this repo.

#### Cloud Platform Console
1. Obtain an API key through [Google Cloud Platform console](https://console.cloud.google.com) under your *Project ID*, **Menu ☰** > **APIs & Services** > **Credentials** > **Create Credentials** > **API key**
1. Replace `<YOUR_MAPS_KEY_HERE>` in `src/main/resources/config.properties` with the API key.

#### App Engine Deployment & Webhook Configuration
When a new project is created using the Actions Console, it also creates a Google Cloud project in the background.
1. Configure the gcloud CLI and set your Google Cloud project to the name of your Actions on Google Project ID, which you can find from the [Actions on Google console](https://console.actions.google.com/) under Settings ⚙
   + `gcloud init`
1. Deploy to [App Engine using Gradle](https://cloud.google.com/appengine/docs/flexible/java/using-gradle):
   + `gradle appengineDeploy` OR
   +  From within IntelliJ, open the Gradle tray and run the appEngineDeploy task.

#### Dialogflow Console
Return to the [Dialogflow Console](https://console.dialogflow.com), from the left navigation menu under **Fulfillment** > **Enable Webhook**, set the value of **URL** to `https://${YOUR_PROJECT_ID}.appspot.com` > **Save**.
1. From the left navigation menu, select **Integrations** > **Integration Settings** under Google Assistant > Enable **Auto-preview changes** >  **Test** to open the Actions on Google simulator then say or type `Talk to my test app`.

### Running this Sample
+ You can test your Action on any Google Assistant-enabled device on which the Assistant is signed into the same account used to create this project. Just say or type, “OK Google, talk to my test app”.
+ You can also use the Actions on Google Console simulator to test most features and preview on-device behavior.

### References & Issues
+ Questions? Go to [StackOverflow](https://stackoverflow.com/questions/tagged/actions-on-google), [Assistant Developer Community on Reddit](https://www.reddit.com/r/GoogleAssistantDev/) or [Support](https://developers.google.com/actions/support/).
+ For bugs, please report an issue on Github.
+ Actions on Google [Documentation](https://developers.google.com/actions/extending-the-assistant)
+ [Webhook Boilerplate Template](https://github.com/actions-on-google/dialogflow-webhook-boilerplate-java) for Actions on Google.
+ More info about [Gradle & the App Engine Plugin](https://cloud.google.com/appengine/docs/flexible/java/using-gradle).
+ More info about deploying [Java apps with App Engine](https://cloud.google.com/appengine/docs/standard/java/quickstart).
+ To learn more about [Google Maps Static API Billing](https://developers.google.com/maps/documentation/maps-static/usage-and-billing).

### Make Contributions
Please read and follow the steps in the [CONTRIBUTING.md](CONTRIBUTING.md).

### License
See [LICENSE](LICENSE).

### Terms
Your use of this sample is subject to, and by using or downloading the sample files you agree to comply with, the [Google APIs Terms of Service](https://developers.google.com/terms/).
