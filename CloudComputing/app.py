import os
from flask import Flask, request, jsonify
from google.cloud import storage
from tensorflow.keras.models import load_model
from tensorflow.keras.preprocessing.image import load_img, img_to_array
import numpy as np
import uuid

app = Flask(__name__)

# Konfigurasi Google Cloud Storage
BUCKET_NAME = "capstone-bucket12"  
MODEL_BLOB_NAME = "model/converted_model.h5"  # Path model 
LOCAL_MODEL_PATH = "/tmp/converted_model.h5"  # Path lokal untuk menyimpan model

# Mapping label ke deskripsi
LABELS = {
    0: "no stress",
    1: "weak stress",
    2: "medium stress",
    3: "strong stress"
}

# Mapping label ke suggestion
SUGGESTIONS = {
    0: "Anda sedang dalam kondisi yang sangat baik! Untuk mempertahankan keseimbangan ini, coba tambahkan variasi pada rutinitas Anda. Mungkin Anda bisa mencoba kelas memasak baru atau bergabung dengan komunitas membaca. Jangan lupa untuk selalu bersyukur atas hal-hal kecil dalam hidup. Ingat, menjaga kesehatan mental sama pentingnya dengan kesehatan fisik.",
    1: "Mungkin Anda mulai merasa sedikit kewalahan. Cobalah untuk meluangkan waktu setiap hari untuk melakukan hal-hal yang Anda nikmati, seperti mendengarkan musik atau berjalan-jalan di alam. Teknik pernapasan dalam juga sangat efektif untuk mengurangi stres ringan. Jangan ragu untuk meminta bantuan teman atau keluarga jika Anda merasa perlu didengarkan.",
    2: "Stres yang Anda alami saat ini cukup signifikan. Selain menerapkan teknik relaksasi, pertimbangkan untuk membuat jurnal. Menuliskan pikiran dan perasaan Anda dapat membantu Anda memahami sumber stres Anda dengan lebih baik. Jika Anda merasa kesulitan mengelola stres sendiri, jangan ragu untuk berkonsultasi dengan seorang terapis.",
    3: "Stres yang Anda alami saat ini sangat memengaruhi kualitas hidup Anda. Sangat penting untuk mencari bantuan profesional sesegera mungkin. Seorang psikolog atau psikiater dapat memberikan diagnosis yang akurat dan menyusun rencana perawatan yang tepat. Selain itu, bergabung dengan kelompok dukungan dapat memberikan Anda rasa komunitas dan dukungan dari orang-orang yang memahami apa yang Anda alami."
}

# Fungsi untuk mengunduh model dari bucket
def download_model(bucket_name, source_blob_name, destination_file_name):
    try:
        # Buat direktori jika belum ada
        if not os.path.exists(os.path.dirname(destination_file_name)):
            os.makedirs(os.path.dirname(destination_file_name))

        # Unduh model dari bucket
        storage_client = storage.Client()
        bucket = storage_client.bucket(bucket_name)
        blob = bucket.blob(source_blob_name)
        blob.download_to_filename(destination_file_name)
        print(f"Model downloaded to {destination_file_name}")
    except Exception as e:
        raise ValueError(f"Error downloading model from GCS: {str(e)}")

# Unduh model jika belum ada di direktori lokal
if not os.path.exists(LOCAL_MODEL_PATH):
    download_model(BUCKET_NAME, MODEL_BLOB_NAME, LOCAL_MODEL_PATH)

# Load model
model = load_model(LOCAL_MODEL_PATH, compile=False)

# Set target image size (sesuaikan dengan input size model Anda)
TARGET_SIZE = (224, 224)  # Sesuaikan ukuran dengan model Anda

# Fungsi untuk menyimpan gambar ke Google Cloud Storage
def upload_to_gcs(bucket_name, source_file_name, destination_blob_name):
    """Upload file ke Google Cloud Storage."""
    try:
        storage_client = storage.Client()
        bucket = storage_client.bucket(bucket_name)
        blob = bucket.blob(destination_blob_name)
        blob.upload_from_filename(source_file_name)
        blob.make_public()  # Buat file dapat diakses secara publik (opsional)
        return blob.public_url
    except Exception as e:
        raise ValueError(f"Error uploading to GCS: {str(e)}")

# Fungsi untuk memproses gambar
def prepare_image(image_path):
    try:
        image = load_img(image_path, target_size=TARGET_SIZE)
        image_array = img_to_array(image) / 255.0
        return np.expand_dims(image_array, axis=0)
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
    if 'file' not in request.files:
        return jsonify({
            "status": {
                "code": 400,
                "message": "No file part in the request."
            }
        }), 400

    file = request.files['file']

    if file.filename == '':
        return jsonify({
            "status": {
                "code": 400,
                "message": "No selected file."
            }
        }), 400

    try:
        # Simpan file sementara
        temp_file_path = os.path.join('/tmp', f"{uuid.uuid4()}_{file.filename}")
        if not os.path.exists(os.path.dirname(temp_file_path)):
            os.makedirs(os.path.dirname(temp_file_path))
        file.save(temp_file_path)

        # Upload gambar ke Google Cloud Storage
        destination_blob_name = f"uploads/{file.filename}"
        gcs_url = upload_to_gcs(BUCKET_NAME, temp_file_path, destination_blob_name)

        # Persiapkan gambar
        image = prepare_image(temp_file_path)

        # Prediksi dengan model
        predictions = model.predict(image)
        predicted_label = np.argmax(predictions, axis=1)[0]
        predicted_label_description = LABELS.get(predicted_label, "Unknown")
        suggestion = SUGGESTIONS.get(predicted_label, "Suggestion not available.")

        # Membersihkan file sementara
        os.remove(temp_file_path)

        return jsonify({
            "status": {
                "code": 200,
                "message": "Prediction successful."
            },
            "data": {
                "predicted_label": predicted_label_description,
                "predictions": predictions.tolist(),
                "suggestion": suggestion,
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
