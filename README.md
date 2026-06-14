# Spring Boot + MinIO Object Storage

## What is an Object Store?

An object store is a storage system designed for storing files (objects) such as images, videos, PDFs, and documents.

Unlike a traditional filesystem that organizes data using folders and blocks, object storage stores:

* The file data itself
* Metadata about the file
* A unique object key

Example:

```text
Bucket: documents

Object Key:
550e8400-e29b-41d4-a716-446655440000-resume.pdf
```

Popular object storage solutions include:

* Amazon S3
* MinIO
* Google Cloud Storage
* Azure Blob Storage

Object storage is commonly used in modern backend systems because it scales well for large files and high volumes of data.

---

## Why MinIO?

MinIO is a self-hosted, S3-compatible object storage server.

Since MinIO implements the Amazon S3 API, code written against MinIO can later be migrated to AWS S3 with minimal or no business logic changes.

Typical migration involves changing only configuration such as:

```yaml
endpoint
access-key
secret-key
bucket-name
```

This makes MinIO ideal for local development and learning object storage concepts before moving to cloud providers.

---

## Spring Boot Integration

### Dependency

Gradle:

```gradle
implementation 'io.minio:minio:8.5.17'
```

---

### Configuration Properties

```yaml
minio:
  endpoint: http://localhost:9000
  access-key: admin
  secret-key: password123
  bucket-name: documents
```

---

### MinIO Bean Configuration

```java
@Configuration
public class MinioConfig {

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint("http://localhost:9000")
                .credentials("admin", "password123")
                .build();
    }
}
```

The `MinioClient` bean can then be injected into services responsible for uploading, downloading, and deleting objects.

---

## Running MinIO Locally

### Docker Compose

```yaml
services:
  minio:
    image: minio/minio
    command: server /data --console-address ":9001"
    ports:
      - "9000:9000"
      - "9001:9001"
    environment:
      MINIO_ROOT_USER: admin
      MINIO_ROOT_PASSWORD: password123
```

Start MinIO:

```bash
docker compose up -d
```

---

## Why `--console-address ":9001"`?

MinIO exposes:

* Port `9000` → S3 API endpoint
* Port `9001` → Web Console

Without:

```bash
--console-address ":9001"
```

the MinIO web UI is not exposed separately and cannot be accessed via:

```text
http://localhost:9001
```

The web console is useful for:

* Creating buckets
* Uploading test files
* Viewing stored objects
* Managing users and access policies

---

## Accessing MinIO

### S3 API

```text
http://localhost:9000
```

### MinIO Console

```text
http://localhost:9001
```

Credentials:

```text
Username: admin
Password: password123
```

---

## Bucket and Object Terminology

```text
Bucket
 ├── profile.png
 ├── resume.pdf
 └── video.mp4
```

A bucket is similar to a top-level container.

Files are stored as objects inside buckets and are identified by object keys.

Example:

```text
Bucket: documents

Object Key:
9f6c0f1a-0c8a-4d4f-b94d-c83f7fd8d55e-resume.pdf
```

The object key uniquely identifies the stored object within a bucket.
