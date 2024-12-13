# README for Training Notebook

## Overview
This notebook is designed for training a Convolutional Neural Network (CNN) model to classify facial images into four stress levels based on emotions. It preprocesses the FER-2013 dataset, balances the data, applies data augmentation, and trains a deep learning model using Keras.

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

