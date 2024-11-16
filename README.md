# Offline Transactions App

The **Offline Transactions App** enables users to make and track financial transactions without an internet connection. The app features Bluetooth and NFC (Near Field Communication) support, allowing data transfer for transactions. A Machine Learning (ML) model is integrated to detect potential fraud in transactions based on historical patterns.

## Features
- **Offline Transactions:** Users can process transactions without an active internet connection.
- **Fraud Detection:** An AI/ML model evaluates the transaction to detect possible fraud.
- **Bluetooth & NFC Support:** Enables secure data transfer over Bluetooth or NFC for offline transactions.
- **Database Persistence:** Stores transaction records locally using Room Database to enable offline processing and later synchronization.

## Technology Stack
- **Android**: The primary platform for the application.
- **Room Database**: For storing transaction data locally.
- **Bluetooth/NFC**: Supports Bluetooth and NFC communication for data transfer.
- **Flask API**: Hosts the AI/ML fraud detection model and communicates with the Android app.
- **Machine Learning**: Fraud detection model built with scikit-learn and Flask.

## Installation

1. Clone this repository:
   git clone https://github.com/Hemanthsammeta/offline-transactions.git

   Set up the backend:

**2. Install Python dependencies:**
    pip install flask scikit-learn

  **Start the Flask server:**
    python app.py

## Set up the Android project in Android Studio:

Build the project and deploy it to an Android device.
Grant necessary permissions for Bluetooth and NFC usage in the app settings.

## Usage
Start the Flask Server to run the fraud detection model.
Open the App on your Android device.
Enter Transaction Details and select Transfer.

**Bluetooth/NFC Data Transfer:** The app will guide you on transferring data via Bluetooth or NFC if internet connectivity is unavailable.

**Fraud Detection:** The transaction is analyzed, and the app alerts users in case of suspected fraud.

## Contributing
Contributions are welcome! Please submit a pull request.
