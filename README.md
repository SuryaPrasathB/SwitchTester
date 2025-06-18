# Switch Tester Application

## 📋 Index
- [Project Description](#project-description)
- [Features](#features)
- [Technologies Used](#technologies-used)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Building the Project](#building-the-project)
  - [Running the Application](#running-the-application)
- [Configuration](#configuration)
- [Admin Credentials](#admin-credentials)
- [Contributing](#contributing)
- [License](#license)

## Project Description
The Switch Tester Application is a JavaFX-based desktop application designed to facilitate the testing of electrical switches. It provides a comprehensive dashboard for monitoring, configuration settings for managing test parameters, and dedicated screens for various aspects of the testing process, including project management, execution, reporting, and user administration.

## Features

- **Splash Screen**: A visually appealing loading screen on application startup.
- **User Authentication**: Secure login with distinct user profiles (Admin, Production, Quality, Maintenance).
- **Interactive Dashboard**: Displays real-time date/time and dummy statistics for switch testing (total, pass, fail).
- **Modular Navigation**: A sidebar for easy navigation between different application modules:
  - Dashboard
  - Projects
  - Execution
  - Reports
  - Debug Console (Placeholder)
  - Settings
  - Users
- **Role-Based Access Control (RBAC)**: Navigation items are dynamically shown or hidden based on the logged-in user's profile and assigned permissions.
- **Custom Notification System**:
  - Replaces standard `javafx.scene.control.Alert`.
  - Floating, styled notifications with type-specific icons (Info, Success, Warning, Error).
  - Auto-hide with fade-out animation and manual dismissal.
- **Execution Screen**:
  - Configurable test types (Normal Operation, Endurance, Dielectric Strength, Insulation Resistance).
  - Selectable switch voltage and current ratings.
  - Dynamic test parameters display based on selected options.
  - Placeholder controls for Start, Stop, Reset, Save, Load operations.
- **Admin Settings**:
  - Password-protected critical configurations.
  - Embedded password prompt.
- **Test Type Configuration**:
  - CRUD operations for test types.
  - Parameters saved/loaded via `test_type_configs.json`.
  - Drag-and-drop reordering.
- **User Management**:
  - View profiles as interactive cards.
  - Add/edit/delete users and their permissions.
  - Toggle visibility.
- **Single Instance Control**: Prevents multiple instances of the app from running.
- **Custom Application Icon**: Shown in taskbar and title bars.
- **Robust Dialog Handling**: Top-level JavaFX alerts for confirmations.
- **Logout Functionality**: Clears session and returns to login.

## Technologies Used

- **JavaFX**: UI development.
- **Maven**: Build automation and dependency management.
- **Jackson**: JSON serialization/deserialization.

## Project Structure

```
├── pom.xml
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com.switchtester.app
│   │   │       ├── MainApp.java
│   │   │       ├── model
│   │   │       │   ├── TestTypeConfig.java
│   │   │       │   └── User.java
│   │   │       ├── service
│   │   │       │   ├── NotificationManager.java
│   │   │       │   ├── TestTypeConfigManager.java
│   │   │       │   └── UserManager.java
│   │   │       ├── util
│   │   │       │   └── PasswordHasher.java
│   │   │       └── viewmodel
│   │   │           ├── *.java (ViewModels)
│   │   └── resources
│   │       └── com.switchtester.app
│   │           ├── images/*.png
│   │           ├── view/*.fxml
│   │           └── styles.css
│   └── test
│       └── java
└── test_type_configs.json
└── users.json
```

## Getting Started

### Prerequisites

- JDK 8 or higher
- Maven 3.x or higher
- IDE like IntelliJ IDEA or Eclipse

### Building the Project

Clone the repository and navigate to the project directory:

```bash
git clone <repository_url>
cd SwitchTesterApp
mvn clean install
```

### Running the Application

Use Maven to run:

```bash
mvn exec:java
```

Or run `MainApp.java` from your IDE.

## Configuration

Configuration files `test_type_configs.json` and `users.json` are auto-managed by the application and editable via the GUI.

## Admin Credentials

- **Username**: admin
- **Password**: password
- **Admin Settings Password**: adminpass

## Contributing

Contributions are welcome! Open issues or submit PRs.

## License

This project is licensed under the [Specify License Here, e.g., MIT License] - see the LICENSE.md file for details.
