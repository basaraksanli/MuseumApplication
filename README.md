# Museum App

![Latest Version](https://img.shields.io/badge/latestVersion-1.0-yellow) ![Kotlin](https://img.shields.io/badge/language-kotlin-blue) ![Minimum SDK Version](https://img.shields.io/badge/minSDK-26-orange) ![Android Gradle Version](https://img.shields.io/badge/androidGradleVersion-4.1.1-green) ![Gradle Version](https://img.shields.io/badge/gradleVersion-6.5-informational)

The Museum App consists a combination of several Huawei Kits and Services such as Account Kit, Auth Service, Location Kit, Map Kit, Site Kit, Nearby Service, Awareness Kit and Cloud Database.

<img src="/screenshots/1.jpg" width=200></img>
<img src="/screenshots/5.jpg" width=200></img>
<img src="/screenshots/8.jpg" width=200></img>
<img src="/screenshots/13.jpg" width=200></img>
<img src="/screenshots/14.jpg" width=200></img>


## Introduction

Museum App is an Android application that is developed with Huawei Mobile Services. It reveals the museums around a certain radius of the user's current location by a background notification system. This way, visiting tourists are also informed of the museums that are often overlooked and are encouraged to visit those places. The application also provides the users a virtual guide feature in the partnered museums. By doing so, the users can use their personal smartphones instead of self-guiding devices of the museums that are costly and don't comply with today's hygienic requirements due to highly contagious COVID-19 pandemic. Because these self-guiding devices are used by many people during the day and change hands many times. The partnered museums can also analyze the visitor data collected by the Bluetooth beacons. The partnered museums can observe which exhibitions are the most and least visited ones. With this feature, they can optimize the layout of exhibits. 

## About HUAWEI Account Kit

The Account Kit provides easy, safe and fast registration to the user. Users can simply touch the Sign In with HUAWEI ID button to sign in with their HUAWEI IDs in the app easily and safely rather than inserting their credentials and wasting their time for authentication. To discover more, visit: [Huawei Account Kit Guide](https://developer.huawei.com/consumer/en/doc/development/HMSCore-Guides/introduction-0000001050048870)

## About HUAWEI Auth Service

Auth service provides multiple authentication methods to help developers secure user data based on simple rules. Developers can involve one or more of the authentication methods into their applications by using the AppGallery Auth Service SDK to accomplish quick and reliable registration and sign-in for users. To discover more, visit: [Huawei Auth Service Guide](https://developer.huawei.com/consumer/en/doc/development/AppGallery-connect-Guides/agc-auth-introduction-0000001053732605)

## About HUAWEI Map Kit

Map Kit is an SDK for map development. It covers map data of more than 200 countries and regions, and supports over one hundred of languages. With this SDK, the developers can easily integrate map-based functions into their apps. To discover more, visit: [Huawei Map Kit Guide](https://developer.huawei.com/consumer/en/doc/development/HMSCore-Guides-V5/android-sdk-brief-introduction-0000001061991343-V5)

## About HUAWEI Location Kit

Location Kit combines the GNSS, Wi-Fi, and base station location functionalities into the apps to build up global positioning capabilities, allowing the developers to provide flexible location-based services for global users. Currently, it provides three main capabilities: fused location, activity identification, and geofence. You can call one or more of these capabilities as needed. To discover more, visit: [Huawei Location Kit Guide](https://developer.huawei.com/consumer/en/doc/development/HMSCore-Guides/introduction-0000001050706106)

## About HUAWEI Site Kit

With Site Kit, applications can provide users with convenient and secure access to diverse, place-related services, and therefore acquire more users. To discover more, visit: [Huawei Site Kit Guide](https://developer.huawei.com/consumer/en/doc/development/HMSCore-Guides/android-sdk-introduction-0000001050158571)

## About HUAWEI Nearby Service

Nearby Service includes Nearby Data Communication and Contact Shield. Nearby Data Communication allows apps to easily discover nearby devices and set up communication with them using technologies such as Bluetooth and Wi-Fi. HMS Core Contact Shield is a basic contact tracing service developed based on the Bluetooth low energy (BLE) technology. To discover more, visit: [Huawei Nearby Service Guide](https://developer.huawei.com/consumer/en/doc/development/HMSCore-Guides/introduction-nearby-0000001060363166)

## About HUAWEI Awareness Kit

HUAWEI Awareness Kit provides the applications with the ability to obtain contextual information including users' current time, location, behavior, audio device status, ambient light, weather, and nearby beacons. Those applications with Awaraness Kit can gain insight into a user's current situation more efficiently, making it possible to deliver a smarter, more considerate user experience. To discover more, visit: [Huawei Awareness Kit Guide](https://developer.huawei.com/consumer/en/doc/development/HMSCore-Guides/service-introduction-0000001050031140)

## About HUAWEI Cloud DB

Cloud DB is a device-cloud synergy database product that provides data synergy management capabilities between the device and cloud, unified data models, and various data management APIs. In addition to ensuring data availability, reliability, consistency, and security, CloudDB enables seamless data synchronization between the device and cloud, and supports offline application operations, helping developers quickly develop device-cloud and multi-device synergy applications. To discover more, visit: [Huawei Cloud DB Guide](https://developer.huawei.com/consumer/en/doc/development/AppGallery-connect-Guides/agc-clouddb-introduction)

## About HUAWEI Machine Learning Kit - Text to Speech
Text to speech (TTS) can convert text information into audio output in real time. Rich timbres are provided and the volume and speed can be adjusted (5x adjustment is supported for Chinese and English), thereby natural voices can be produced. This service uses the deep neural network (DNN) synthesis mode and can be quickly integrated through the on-device SDK to generate audio data in real time. It supports the download of offline models. To discover more, visit: [Huawei Machine Learning Kit -Text to Speech Guide](https://developer.huawei.com/consumer/en/doc/development/HMSCore-Guides/ml-tts-0000001050068169)

## What You Will Need

**Hardware Requirements**
- A computer that can run Android Studio.
- A Huawei Phone for debugging.

**Software Requirements**
- Android SDK package
- Android Studio 3.X-4.X
- HUAWEI HMS Core 4.0.2.300 or later
- JDK version: 1.8.211 or later
## Getting Started

Museum App uses HUAWEI services. In order to use them, you have to [create an app](https://developer.huawei.com/consumer/en/doc/distribution/app/agc-create_app) first. Before getting started, please [sign-up](https://id1.cloud.huawei.com/CAS/portal/userRegister/regbyemail.html?service=https%3A%2F%2Foauth-login1.cloud.huawei.com%2Foauth2%2Fv2%2Flogin%3Faccess_type%3Doffline%26client_id%3D6099200%26display%3Dpage%26flowID%3D6d751ab7-28c0-403c-a7a8-6fc07681a45d%26h%3D1603370512.3540%26lang%3Den-us%26redirect_uri%3Dhttps%253A%252F%252Fdeveloper.huawei.com%252Fconsumer%252Fen%252Flogin%252Fhtml%252FhandleLogin.html%26response_type%3Dcode%26scope%3Dopenid%2Bhttps%253A%252F%252Fwww.huawei.com%252Fauth%252Faccount%252Fcountry%2Bhttps%253A%252F%252Fwww.huawei.com%252Fauth%252Faccount%252Fbase.profile%26v%3D9f7b3af3ae56ae58c5cb23a5c1ff5af7d91720cea9a897be58cff23593e8c1ed&loginUrl=https%3A%2F%2Fid1.cloud.huawei.com%3A443%2FCAS%2Fportal%2FloginAuth.html&clientID=6099200&lang=en-us&display=page&loginChannel=89000060&reqClientType=89) for a HUAWEI developer account.

After creating the application, you need to [generate a signing certificate fingerprint](https://developer.huawei.com/consumer/en/codelab/HMSPreparation/index.html#3). Then you have to set this fingerprint to the application you created in AppGallery Connect.
- Go to "My Projects" in AppGallery Connect.
- Find your project from the project list and click the app on the project card.
- On the Project Setting page, set SHA-256 certificate fingerprint to the SHA-256 fingerprint you've generated.
![AGC-Fingerprint](https://communityfile-drcn.op.hicloud.com/FileServer/getFile/cmtyPub/011/111/111/0000000000011111111.20200511174103.08977471998788006824067329965155:50510612082412:2800:6930AD86F3F5AF6B2740EF666A56165E65A37E64FA305A30C5EFB998DA38D409.png?needInitFileName=true?needInitFileName=true?needInitFileName=true?needInitFileName=true)

## Using the Application

- Before you run the app, make sure that you have a working internet connection since the application uses Huawei Mobile Services. Otherwise, the app will display a "no connection" dialog until you turn the internet on.

When you first run the app, a splash screen with the application logo welcomes you and directs you to Login Screen, where the users can choose to one of the signs in methods. The app currently provides four methods to sign in such as Huawei ID, Google Sign in, Facebook Sign in, and Email registration. Google Sign In button is hidden when Google Play Services are not available. To register with email, click the "Login/Register With Email" button. This screen has the sign-in function as well as Museum Panel screen. Museum staff can insert the museum ID and password, sign in by clicking the Museum Staff Login button.

Once you sign-in as a user with one of the four methods, a map with your current location marker is shown on the home page. You can click the magnifier button to search nearby museums and set Awareness Location Barriers for each museum that is found. These barriers are set using a foreground service that can send local push notifications to the user. Whenever the user enters one of these barriers, he/she will get a local notification that encourages the user to visit the museum. After you search the nearby museums, a museum list pops up from the bottom, and you can see the relevant information of every museum. This list can be minimized by clicking the button with down arrow button. You can either click the item to focus on the map, or you can click the information button in the list item to see detailed information of the museum. In the detailed information screen, you can favorite the museum or click navigate to open Google Maps or Yandex Maps to use the navigation to museum location.

The home page also consists of a navigation drawer where you can navigate to other screens such as Explore Inside, Settings and Favorites. You can see the profile information at the top and you can log out by clicking the Logout button.

Explore Inside screen provides a virtual guide feature for the user. When you are inside a partnered museum, you can use this screen the see the information of the nearby exhibits. This feature is developed with Huawei Nearby Service. Every exhibit in the partnered museum has an Eddystone or IBeacon Bluetooth beacon. These beacons send messages through Huawei Nearby Service and processed by the application to show the information of relevant exhibit. Only the exhibit that is closest to the user among the other exhibits and closer to the user less than 2 meters(can be configured in settings page by the user) is shown in this page. The exhibit is shown can be favorited by the user. The number near the favorite button indicates how many people favorited the exhibit. User can click the play button to listen to the description of the exhibit. This feature is provided by Huawei Machine Learning Kit- Text to Speech.

Favorite page consists of two nested pages. The first screen lists the favorited museums by the user, the Second one lists the favorited exhibits by the user. In the favorited museums page, you can click to the map button in any item to focus the museum marker in the map. You can also click the star button to unfavorite the museum. In the favorited exhibit page, you can click the information button to see detailed information of relevant exhibit or you can click star button to unfavorite the exhibit. Detailed information screen of the exhibit has the same features like the virtual guide page. The difference is you can browse the exhibits while you are not in that specific museum.

You can configure several features using Settings Page according to your preference. You can change the theme of the application by turning Dark mode on or off. You can also change the theme of the map by turning Night Mode for Map on or off. Museum nearby search can be configured from 1 kilometer to 50 kilometres according to your preferences and exhibit detection range for the virtual guide can be set from 1 meter to 3 meters.

If you are a museum staff you can log in using museum ID and password through the email login page. This leads you to the museum panel which shows the statistical information of the museum and exhibits. The first screen provides general museum information at the top. Below this section, statistics section provides information such as total exhibit counts, total favorites from the visitors, total visits from the visitors. Besides, it provides detailed information on the visit counts and average length visit of the total exhibits for the last week. The second screen presents the most visited, least visited exhibits and shortest stay, longest stay exhibits on the top. Below, all the exhibits of the museum are listed. Once an item is clicked in the list, statistics of the relevant information are shown in a bottom sheet page.

## Pre-defined Museums

- Galleria Borghese

## Pre-defined Exhibits

- Hercules Head
- Sleeping Hermaphroditus



## Screenshots

<img src="/screenshots/1.jpg" width=200></img>
<img src="/screenshots/2.jpg" width=200></img>
<img src="/screenshots/3.jpg" width=200></img>
<img src="/screenshots/4.jpg" width=200></img>
<img src="/screenshots/5.jpg" width=200></img>
<img src="/screenshots/6.jpg" width=200></img>
<img src="/screenshots/7.jpg" width=200></img>
<img src="/screenshots/8.jpg" width=200></img>
<img src="/screenshots/9.jpg" width=200></img>
<img src="/screenshots/10.jpg" width=200></img>
<img src="/screenshots/11.jpg" width=200></img>
<img src="/screenshots/12.jpg" width=200></img>
<img src="/screenshots/13.jpg" width=200></img>
<img src="/screenshots/14.jpg" width=200></img>

## Project Structure

Museum App is designed with MVVM design pattern.

## Libraries

- Huawei Account Kit
- Huawei Auth Service
- Huawei Map Kit
- Huawei Location Kit
- Huawei Site Kit
- Huawei Nearby Service
- Huawei Awareness Kit
- Huawei Cloud DB
- Huawei Machine Learning Kit- Text to Speech
- LiveData
- DataBinding
- TimeIT
- Navigation
- Glide
- Spager

## Contributors

- Basar Aksanli
