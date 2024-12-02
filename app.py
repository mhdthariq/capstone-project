import os
from flask import Flask, request, jsonify
from tensorflow.keras.models import load_model
from tensorflow.keras.preprocessing.image import load_img, img_to_array
from google.cloud import storage
import numpy as np
import uuid

app = Flask(__name__)

# Load model
model = load_model('best_model.keras')

# Set target image size (sesuaikan dengan input size model Anda)
TARGET_SIZE = (224, 224)  # Misalnya, untuk model yang membutuhkan gambar 224x224

# Fungsi untuk menyimpan gambar ke Google Cloud Storage
def upload_to_gcs(bucket_name, source_file_name, destination_blob_name):
    """Upload file ke Google Cloud Storage."""
    try:
        # Inisialisasi storage client
        storage_client = storage.Client()
        bucket = storage_client.bucket(bucket_name)
        blob = bucket.blob(destination_blob_name)
        blob.upload_from_filename(source_file_name)
        blob.make_public()  # Buat file dapat diakses secara publik (opsional)
        return blob.public_url
    except Exception as e:
        raise ValueError(f"Error uploading to GCS: {str(e)}")

def prepare_image(image_path):
    """
    Fungsi untuk memuat dan mempersiapkan gambar agar sesuai dengan input model.
    """
    try:
        # Memuat gambar dan mengubah ukuran sesuai model
        image = load_img(image_path, target_size=TARGET_SIZE)
        # Konversi gambar ke array numpy
        image_array = img_to_array(image)
        # Normalisasi (jika model Anda memerlukan normalisasi)
        image_array = image_array / 255.0
        # Tambahkan dimensi batch
        image_array = np.expand_dims(image_array, axis=0)
        return image_array
    except Exception as e:
        raise ValueError(f"Error in processing image: {str(e)}")

@app.route('/')
def index():
    return jsonify({
        "status": {
            "code": 200,
            "message": "API is running."
        }
    }), 200

@app.route('/predict', methods=['POST'])
def predict():
    # Log request method and content type
    print(f"Request method: {request.method}")
    print(f"Content-Type: {request.content_type}")

    # Memastikan ada file yang diunggah
    if 'file' not in request.files:
        return jsonify({
            "status": {
                "code": 400,
                "message": "No file part in the request."
            }
        }), 400

    file = request.files['file']
    print(f"Received file: {file.filename}")

    # Memastikan file telah diunggah
    if file.filename == '':
        return jsonify({
            "status": {
                "code": 400,
                "message": "No selected file."
            }
        }), 400

    try:
        # Simpan file sementara
        temp_file_path = os.path.join('temp', f"{uuid.uuid4()}_{file.filename}")
        os.makedirs('temp', exist_ok=True)
        file.save(temp_file_path)

        # Upload gambar ke Google Cloud Storage
        bucket_name = "ember-capstone"  # Ganti dengan nama bucket Anda
        destination_blob_name = f"uploads/{file.filename}"  # Path di dalam bucket
        gcs_url = upload_to_gcs(bucket_name, temp_file_path, destination_blob_name)
        print(f"Uploaded to GCS: {gcs_url}")

        # Persiapkan gambar
        image = prepare_image(temp_file_path)

        # Prediksi dengan model
        predictions = model.predict(image)
        predicted_label = np.argmax(predictions, axis=1)[0]

        # Membersihkan file sementara
        os.remove(temp_file_path)

        # Mengembalikan hasil prediksi dan URL file di GCS
        return jsonify({
            "status": {
                "code": 200,
                "message": "Prediction successful."
            },
            "data": {
                "predicted_label": int(predicted_label),
                "predictions": predictions.tolist(),
                "image_url": gcs_url  # URL gambar yang diunggah ke GCS
            }
        }), 200

    except Exception as e:
        return jsonify({
            "status": {
                "code": 500,
                "message": f"Error processing the file: {str(e)}"
            }
        }), 500

if __name__ == "__main__":
    app.run(debug=True, host="0.0.0.0", port=int(os.environ.get("PORT", 8080)))
