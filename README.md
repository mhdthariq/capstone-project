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

<img src="Image/architecture-cloud.jpg" alt="Screenshot" style="max-width: 100%; height: auto;">

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
Below the screnshoot of our application

<img src="Image/Mockup_Emoticalm.png" alt="Screenshot" style="max-width: 100%; height: auto;">

---

## Machine Learning Design

The notebook is designed for training a Convolutional Neural Network (CNN) model to classify facial images into four stress levels based on emotions. It preprocesses the FER-2013 dataset, balances the data, applies data augmentation, and trains a deep learning model using Keras.

---

## Dataset
- **Name**: FER-2013
- **Structure**: Grayscale images of size 48x48 pixels.
- **Classes**:
  - Angry (0), Disgust (1), Fear (2), Happy (3), Sad (4), Surprise (5), Neutral (6).
- **Mapping to Stress Levels**:
  - No Stress: Happy (3)
  - Weak Stress: Neutral (6), Surprise (5)
  - Medium Stress: Disgust (1), Fear (2)
  - Strong Stress: Angry (0), Sad (4)
- **Splits**:
  - Training: 28,709 images
  - Testing: 3,589 images

---

## Key Components

### 1. **Data Preparation**
- The notebook reads the FER-2013 dataset from a directory structure.
- A mapping function converts the original 7 emotion categories into 4 stress categories (`no_stress`, `weak_stress`, `medium_stress`, `strong_stress`).
- Splits the training data into training (80%) and validation (20%) subsets, ensuring balanced class distribution using stratified sampling.

### 2. **Data Balancing**
- Uses **Random Oversampling** to address class imbalance in the training set, ensuring all stress categories have equal representation.

### 3. **Data Augmentation**
- Augments training images with the following transformations:
  - Rotation: up to 25 degrees
  - Horizontal and vertical shifts: up to 20%
  - Shear: up to 15%
  - Zoom: up to 20%
  - Horizontal flips
  - Nearest-fill for padding

### 4. **Data Generators**
- **Training Data Generator**: Applies augmentations and scales pixel values to the range [0, 1].
- **Validation and Test Generators**: Only scales pixel values to the range [0, 1].
- Resizes images to 224x224 pixels to fit the CNN input requirements.

### 5. **Model Training**
- Defines a CNN model architecture (not included in this README but can be found in the notebook).
- Optimizes the model using the Adam optimizer and `categorical_crossentropy` loss.
- Tracks performance metrics such as accuracy during training and validation.

---

## How to Use This Notebook

### Prerequisites
- Install required Python libraries:
  ```bash
  pip install tensorflow keras scikit-learn imbalanced-learn matplotlib pandas
  ```
- Ensure the FER-2013 dataset is properly organized in the following structure:
  ```
  data/
  ├── train/
  │   ├── angry/
  │   ├── disgust/
  │   ├── fear/
  │   ├── happy/
  │   ├── neutral/
  │   ├── sad/
  │   └── surprise/
  └── test/
      ├── angry/
      ├── disgust/
      ├── fear/
      ├── happy/
      ├── neutral/
      ├── sad/
      └── surprise/
  ```

### Running the Notebook
1. Load the dataset.
2. Follow the notebook cells sequentially to:
   - Preprocess the data.
   - Balance the dataset.
   - Augment the training data.
   - Train the CNN model.
   - Evaluate the model on the test set.

### Outputs
- Model performance metrics (accuracy, loss) during training and validation.
- Balanced training dataset distribution.
- Test set evaluation results.

---

## Notes
- The notebook uses a random seed (`random_state=42`) for reproducibility.
- Make sure GPU acceleration is enabled in your environment (e.g., Google Colab) to speed up training.

---

## Author
- **Name**: Abyan Dzakky
- **Field**: Machine Learning Path

For any questions or feedback, feel free to contact me.

### Design Diagram
![Model Design](path/to/model-design-image.png)

---

## Branch Details
To view specific implementations, refer to the following branches:
- **Cloud Computing (API)**: `branch-cc`
- **Mobile Development**: `branch-md`
- **Machine Learning**: `branch-ml`
