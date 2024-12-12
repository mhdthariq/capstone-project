# Stress Management API

This API is designed to predict stress levels based on images using a TensorFlow model. The API is hosted using Flask and integrates Google Cloud Storage for image uploads and model management. The model is stored in Google Cloud Storage, and the API is deployed on Google Cloud Run.

## Features
- Predict stress levels based on images.
- Suggestions provided for stress management based on predictions.
- Automatic image upload to Google Cloud Storage with public URL generation.

## Table of Contents
1. [Technologies Used](#technologies-used)
2. [Setup and Installation](#setup-and-installation)
3. [Endpoints](#endpoints)
4. [Deployment on Google Cloud](#deployment-on-google-cloud)
5. [Environment Variables](#environment-variables)

## Technologies Used
- Python 3.12
- Flask 2.3.2
- TensorFlow 2.18.0
- Google Cloud Storage
- Google Cloud Run
- Gunicorn for production WSGI server

## Setup and Installation

### Prerequisites
1. Install [Python 3.12](https://www.python.org/downloads/release/python-3120/).
2. Install [Docker](https://www.docker.com/get-started/) if you plan to use the Docker setup.
3. Create a Google Cloud project and download the service account JSON key. Save it as `key.json` in your project root.
4. Enable the following Google Cloud APIs:
   - Cloud Storage API
   - Cloud Run API

### Installation Steps
1. Clone the repository:
   ```bash
   git clone -b cc https://github.com/mhdthariq/capstone-project.git
   cd capstone project
   ```
2. Install dependencies:
   ```bash
   pip install -r requirements.txt
   ```
3. Set the environment variable for Google Cloud credentials:
   ```bash
   export GOOGLE_APPLICATION_CREDENTIALS="<path-to-key.json>"
   ```
4. Run the application locally:
   ```bash
   python app.py
   ```

## Endpoints

### `GET /`
- **Description**: Health check endpoint.
- **Response**:
  ```json
  {
      "status": {
          "code": 200,
          "message": "API is running."
      }
  }
  ```

### `POST /predict`
- **Description**: Predict stress level from an image.
- **Request**:
  - `file`: Image file to be processed.
- **Response**:
  ```json
  {
      "status": {
          "code": 200,
          "message": "Prediction successful."
      },
      "data": {
          "predicted_label": "<label>",
          "predictions": [<probabilities>],
          "suggestion": "<suggestion>",
          "image_url": "<gcs-public-url>"
      }
  }
  ```

### Error Handling
- **400**: Missing or invalid file.
- **500**: Server error during processing.

## Deployment on Google Cloud

### Steps to Deploy
1. **Deploy Model to Google Cloud Storage**:
   - Upload the TensorFlow model (`converted_model.h5`) to a Google Cloud Storage bucket.
   - Ensure the bucket and model file have appropriate permissions.
2. **Deploy API to Google Cloud Run**:
   - Build the Docker image:
     ```bash
     docker build -t gcr.io/<project-id>/stress-management-api .
     ```
   - Push the image to Google Container Registry:
     ```bash
     docker push gcr.io/<project-id>/stress-management-api
     ```
   - Deploy to Cloud Run:
     ```bash
     gcloud run deploy stress-management-api \
       --image gcr.io/<project-id>/stress-management-api \
       --platform managed \
       --region <region> \
       --allow-unauthenticated \
       --set-env-vars GOOGLE_APPLICATION_CREDENTIALS=/app/key.json
     ```
3. **Configure Environment Variables**:
   - Set `GOOGLE_APPLICATION_CREDENTIALS` to the path of the service account JSON in the Cloud Run configuration.

### Accessing the API
After deployment, you will receive a URL for the Cloud Run service. Use this URL to interact with the API.

## Environment Variables
- `GOOGLE_APPLICATION_CREDENTIALS`: Path to the Google Cloud service account JSON file.
- `PORT` (optional): Port to run the application (default is `8080`).
