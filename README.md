# AR Interior Design App

Project for the Mobile Application and Cloud Computing course (University "La Sapienza", Master in Engineering in Computer Science, A.Y. 2023/2024) conducted by Professor Roberto Beraldi.

## Description

AR Interior Design is a mobile application developed as part of the Mobile Application and Cloud Computing course. The application aims to provide users with a platform for creating, sharing, and collaborating on augmented reality (AR) projects in real-time.

## Functionalities

### Creation of a Personal Project

Users can create their own AR projects by selecting 3D models from a library, positioning them in a real-world environment using AR technology, and saving the project to their account.

### Resolving a Personal Project

Users can access their saved projects and resume working on them by resolving the stored anchors and loading the previously placed 3D models into the AR scene.

### Sharing a Personal Project

Users have the option to share their AR projects with others by generating a shareable link or inviting specific users to collaborate on the project.

### Session Between Two or More Users

The application supports real-time collaborative sessions where multiple users can join a shared AR environment. The session owner has full control over positioning and interacting with objects, while other participants can view the modifications in real-time.

## Libraries Used

- SceneView: A library for rendering 3D scenes and enabling AR experiences on mobile devices.
- ARCore: Google's platform for building augmented reality experiences on Android devices.
- Firebase: A mobile and web application development platform provided by Google. Used for storing GLB models, managing user authentication, and facilitating real-time database communication for collaborative sessions.
- Firebase OAuth: Used for user authentication and authorization.
- PythonAnywhere: A cloud hosting service used for the backend to handle user login sessions and fetching projects.

## Components Used

- CloudAnchorNode: Used to host anchors for saving projects and resolving them during collaborative sessions.

## Installation

To run the Mobile-Computing-App project, follow these steps:

1. Clone the repository: `git clone https://github.com/Cristian-Santaroni/Mobile-Computing-App.git`
2. Open the project in Android Studio.
3. Build and run the application on an Android device or emulator.

## Students

- Fortunato Francesco 1848527
- Santaroni Cristian 

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

