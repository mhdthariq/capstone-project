# Gunakan Python 3.12 sebagai base image
FROM python:3.12

# Salin file kunci JSON ke dalam container
COPY key.json /app/key.json

# Set variabel lingkungan untuk kredensial GCP
ENV GOOGLE_APPLICATION_CREDENTIALS=/app/key.json

# Instal dependensi tambahan yang diperlukan
RUN apt-get update && apt-get install -y --no-install-recommends \
    python3-distutils \
    python3-setuptools \
    python3-pip \
    && rm -rf /var/lib/apt/lists/*

# Upgrade setuptools, wheel, dan pip
RUN python3 -m pip install --upgrade pip setuptools wheel

# Lokasi aplikasi di dalam kontainer
ENV APP_HOME=/app

# Pindah ke direktori aplikasi
WORKDIR $APP_HOME

# Salin file proyek ke dalam direktori kerja
COPY . .

# Instalasi paket Python
RUN pip install --no-cache-dir -r requirements.txt

# Jalankan aplikasi dengan Gunicorn
CMD ["gunicorn", "--bind", "0.0.0.0:8080", "--workers", "1", "--threads", "8", "--timeout", "0", "app:app"]
