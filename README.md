# EmotiCalm

EmotiCalm is a facial recognition-based application designed to detect stress levels and provide personalized suggestions for stress management. The project integrates cloud computing, mobile development, and machine learning to deliver a seamless experience for users.

---

## Table of Contents
1. [Project Overview](#project-overview)
2. [Architecture](#architecture)
3. [Application Design](#application-design)
4. [Machine Learning Design](#machine-learning-design)
5. [Branch Details](#branch-details)

---

## Project Overview
EmotiCalm aims to leverage advanced technology to assess stress levels from facial images and provide actionable suggestions. This project consists of three primary components:
- **Cloud Computing (API)**: Manages stress detection models and serves predictions.
- **Mobile Application**: Allows users to capture images and receive results and suggestions.
- **Machine Learning**: A TensorFlow-based model to analyze stress levels from facial features.

---

## Architecture

Below is the system architecture for the EmotiCalm project:

[Project Architecture](Image/architecture-cloud.jpg)

### Key Components:
- **Google Cloud Storage**: Stores machine learning models and user-uploaded images.
- **Cloud Run**: Hosts the API for stress prediction.
- **Firebase**: For Handle user authentication.
- **Mobile Frontend**: A user-friendly interface to interact with the system.
- **Machine Learning Model**: A TensorFlow-based neural network for facial recognition and stress detection.

---

## Application Design

The mobile application design focuses on user accessibility and clarity. The interface includes:
1. **Image Capture and Upload**: Allows users to upload facial images.
2. **Stress Analysis Display**: Shows detected stress level and corresponding suggestions.
3. **History Tab**: Keeps track of past analyses and suggestions.

### Screenshots
Include mockups or screenshots here (e.g., `![App Mockup](path/to/mockup.png)`).

---

## Machine Learning Design

The stress detection model uses a convolutional neural network (CNN) trained on annotated facial image datasets. The design involves:
1. **Data Preprocessing**: Facial image normalization and augmentation.
2. **Model Architecture**: Multi-layered CNN for feature extraction and classification.
3. **Label Mapping**:
   - 0: No Stress
   - 1: Weak Stress
   - 2: Medium Stress
   - 3: Strong Stress

### Design Diagram
![Model Design](path/to/model-design-image.png)

---

## Branch Details
To view specific implementations, refer to the following branches:
- **Cloud Computing (API)**: `branch-cc`
- **Mobile Development**: `branch-md`
- **Machine Learning**: `branch-ml`
