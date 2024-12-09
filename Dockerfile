
FROM python:3.12


COPY key.json /app/key.json


ENV GOOGLE_APPLICATION_CREDENTIALS=/app/key.json


RUN apt-get update && apt-get install -y --no-install-recommends \
    python3-distutils \
    python3-setuptools \
    python3-pip \
    && rm -rf /var/lib/apt/lists/*


RUN python3 -m pip install --upgrade pip setuptools wheel


ENV APP_HOME=/app


WORKDIR $APP_HOME


COPY . .


RUN pip install --no-cache-dir -r requirements.txt


CMD ["gunicorn", "--bind", "0.0.0.0:8080", "--workers", "1", "--threads", "8", "--timeout", "0", "app:app"]
