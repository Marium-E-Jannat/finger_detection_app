# Finger Motion App

A simple Android app to send images to a backend server to run detection for finger postures/gestures.
Currently, the server is running on Colab, tunneled using `ngrok`.

## Prerequisites

You will need **Android SDK & Android Studio**. Please follow this [guide](https://developer.android.com/studio) to set up your Android development environment.

The app queries Youtube API so you will need an **Google API key**. Please follow this [guide](https://developers.google.com/youtube/v3/getting-started) to set up. Once you have it:
- Set it in `local.properties`:
    ```
    API_KEY=somekey
    ```
- Register in module level `build.gradle`:
    ```
    Properties properties = new Properties()
    properties.load(new FileInputStream(project.rootProject.file('local.properties')))
    def YOUTUBE_API_KEY = properties.getProperty("API_KEY")

    android {

        ...
        defaultConfig {
            ...
            buildConfigField("String", "YOUTUBE_API_KEY", "\"" + YOUTUBE_API_KEY + "\"")
        }
    }
    ```
- Access in code via `BuildConfig`:
    ```
    BuildConfig.YOUTUBE_API_KEY
    ```

You will need a compatible Android device with a minimum **Android API 28 (Android 9)**. Please checkout the module level `build.gradle` for more information.

Image processing is done in backend server, please contact [@Marium](https://github.com/Marium-E-Jannat) for the codebase and support. Alternatively, you can build you own server (HTTP) with following message body.

- `Request body`: The image as `byte[]` is sent as a sequence of bytes in the request body.
- `Response body`: A json must be returned with necessary fields. Please checkout `BaseDetector` class for supported class name and its corresponding index as id.
    ```
    {
        class_name: some_name,
        class_id: some_id,
        y_max: some_int
        y_min: some_int,
        x_max: some_int,
        x_min: some_int
    }
    ```

## License

[MIT](https://choosealicense.com/licenses/mit/)

