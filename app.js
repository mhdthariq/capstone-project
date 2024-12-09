const express = require('express');
const multer = require('multer');
const { Storage } = require('@google-cloud/storage');
const tf = require('@tensorflow/tfjs-node');
const path = require('path');
const { v4: uuidv4 } = require('uuid');
const fs = require('fs');
require('dotenv').config(); // Load environment variables

// Konfigurasi Google Cloud Storage
const storage = new Storage(); // Menggunakan kredensial dari GOOGLE_APPLICATION_CREDENTIALS
const bucketName = process.env.GCS_BUCKET_NAME;

if (!bucketName) {
  console.error('Error: GCS_BUCKET_NAME is not set in .env file.');
  process.exit(1);
}

// Konfigurasi Multer untuk mengunggah file
const upload = multer({ dest: 'uploads/' });

const app = express();
app.use(express.json());

// Endpoint untuk prediksi
app.post('/predict', upload.single('image'), async (req, res) => {
  try {
    const file = req.file;

    if (!file) {
      return res.status(400).send({ error: 'No file uploaded.' });
    }

    // Generate nama unik untuk file
    const uniqueFileName = `${uuidv4()}_${file.originalname}`;
    const gcsFilePath = path.join('images', uniqueFileName);

    // Upload ke Google Cloud Storage
    await storage.bucket(bucketName).upload(file.path, {
      destination: gcsFilePath,
    });

    // URL untuk file yang diunggah
    const publicUrl = `https://storage.googleapis.com/${bucketName}/${gcsFilePath}`;

    // Hapus file lokal setelah upload
    fs.unlinkSync(file.path);

    // Muat model TensorFlow.js
    const modelUrl = process.env.MODEL_URL;
    if (!modelUrl) {
      throw new Error('MODEL_URL is not set in .env file.');
    }
    const model = await tf.loadLayersModel(modelUrl);

    // Proses gambar untuk prediksi
    const imageBuffer = fs.readFileSync(file.path);
    const decodedImage = tf.node.decodeImage(imageBuffer, 3); // Decode sebagai RGB
    const resizedImage = tf.image.resizeBilinear(decodedImage, [224, 224]); // Resize ke [224, 224]
    const normalizedImage = resizedImage.div(255.0).expandDims(0); // Normalisasi dan tambahkan batch dimensi

    console.log('Processed Input Shape:', normalizedImage.shape); // Debugging input shape

    // Prediksi
    const predictions = model.predict(normalizedImage);
    const result = predictions.dataSync(); // Ambil hasil prediksi

    // Kirim hasil prediksi dan tautan gambar
    res.status(200).send({
      imageUrl: publicUrl,
      predictions: Array.from(result),
    });
  } catch (error) {
    console.error('Error:', error.message);
    res.status(500).send({
      error: 'Something went wrong.',
      details: error.message,
    });
  }
});

// Jalankan server
const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
  console.log(`Server is running on port ${PORT}`);
});
