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

---

## Common Object Operations

### Uploading a Document

To upload a file to MinIO, the application uses:

```java
minioClient.putObject(...)
```

Example:

```java
minioClient.putObject(
    PutObjectArgs.builder()
        .bucket(bucketName)
        .object(objectKey)
        .stream(
            file.getInputStream(),
            file.getSize(),
            -1
        )
        .contentType(file.getContentType())
        .build()
);
```

#### What it does

* Reads the incoming file stream from Spring Boot's `MultipartFile`
* Uploads the file into the specified bucket
* Stores the file under the provided object key
* Persists the object in MinIO's underlying storage

Example:

```text
Bucket: documents

Object Key:
550e8400-e29b-41d4-a716-446655440000-resume.pdf
```

After successful execution, the file is physically stored in the object store and can later be downloaded or deleted.

---

### Generating a Download URL

To allow users to download a file without exposing MinIO credentials, the application generates a presigned URL using:

```java
minioClient.getPresignedObjectUrl(...)
```

Example:

```java
String url = minioClient.getPresignedObjectUrl(
    GetPresignedObjectUrlArgs.builder()
        .method(Method.GET)
        .bucket(bucketName)
        .object(objectKey)
        .expiry(1, TimeUnit.HOURS)
        .build()
);
```

#### What it does

* Creates a temporary URL that grants access to a specific object
* No authentication is required by the user
* The URL automatically expires after the configured duration
* Ideal for file downloads in web applications

Flow:

```text
Browser
    |
    v
Spring Boot
    |
    v
Generates Presigned URL
    |
    v
Browser Downloads Directly From MinIO
```

Benefits:

* Spring Boot does not need to stream large files through the application server
* Reduces backend memory and bandwidth usage
* Works similarly to AWS S3 presigned URLs

---

### Deleting a Document

To remove a stored object, the application uses:

```java
minioClient.removeObject(...)
```

Example:

```java
minioClient.removeObject(
    RemoveObjectArgs.builder()
        .bucket(bucketName)
        .object(objectKey)
        .build()
);
```

#### What it does

* Locates the object using its bucket and object key
* Permanently removes the object from storage
* The object can no longer be downloaded
* Any future presigned URLs referencing the object become invalid

Example:

Before deletion:

```text
documents
 ├── profile.png
 ├── resume.pdf
 └── video.mp4
```

After deleting `resume.pdf`:

```text
documents
 ├── profile.png
 └── video.mp4
```

---

### Summary

| Operation              | Method                                   |
| ---------------------- | ---------------------------------------- |
| Upload a file          | `minioClient.putObject(...)`             |
| Generate download link | `minioClient.getPresignedObjectUrl(...)` |
| Delete a file          | `minioClient.removeObject(...)`          |

These operations closely mirror their AWS S3 equivalents, making migration from MinIO to Amazon S3 straightforward.

--- 

## Metadata Storage with PostgreSQL

The actual file contents are stored in MinIO, while file metadata is stored in PostgreSQL.

This approach keeps the database lightweight and avoids storing large binary files (BLOBs) inside PostgreSQL.

Example entity:

```java
@Entity
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String fileName;

    private String objectKey;

    private Long size;
}
```

### Why Store Metadata Separately?

MinIO is optimized for storing file contents, whereas PostgreSQL is optimized for querying structured data.

The application stores:

* `id` → Unique identifier exposed to clients
* `fileName` → Original filename
* `objectKey` → Internal MinIO object identifier
* `size` → File size in bytes

Example record:

| id          | fileName   | objectKey                          | size   |
| ----------- | ---------- | ---------------------------------- | ------ |
| 550e8400... | resume.pdf | 7b1f9f43-2f6a-4f7a-8a5e-resume.pdf | 245760 |

---

## Upload Flow

When a file is uploaded:

1. Generate a unique object key
2. Upload the file to MinIO using `putObject(...)`
3. Save metadata to PostgreSQL

Flow:

```text
Client
  |
  v
Spring Boot
  |
  +--> Upload File To MinIO
  |         |
  |         v
  |     objectKey
  |
  +--> Save Metadata To PostgreSQL
            |
            v
        Document Record
```

The database never stores the actual file contents.

---

## Listing Documents

The application retrieves document metadata from PostgreSQL.

Example response:

```json
[
  {
    "id": "4a5c2b10-6f76-4f6d-9c2d-4c64d4e90c87",
    "fileName": "resume.pdf",
    "size": 245760
  },
  {
    "id": "d8b7d3a8-8f70-40d7-a84a-d730c6071c21",
    "fileName": "profile.png",
    "size": 102400
  }
]
```

---

## Generating a Download URL

To download a document:

1. Find the document metadata in PostgreSQL
2. Retrieve its `objectKey`
3. Generate a presigned URL using `getPresignedObjectUrl(...)`
4. Return the URL to the client

Flow:

```text
Client
  |
  v
Document ID
  |
  v
PostgreSQL
  |
  v
objectKey
  |
  v
MinIO Presigned URL
  |
  v
Client Downloads File
```

The `objectKey` acts as the bridge between PostgreSQL metadata and the actual object stored in MinIO.

---

## Deleting a Document

To delete a document:

1. Find the document metadata in PostgreSQL
2. Retrieve its `objectKey`
3. Delete the object from MinIO using `removeObject(...)`
4. Delete the metadata record from PostgreSQL

Flow:

```text
Document ID
  |
  v
PostgreSQL
  |
  v
objectKey
  |
  +--> Remove Object From MinIO
  |
  +--> Remove Metadata From PostgreSQL
```

This ensures that neither orphaned files nor orphaned database records remain in the system.

---

## Architecture Overview

```text
                   PostgreSQL
                 +------------+
                 | Document   |
                 | Metadata   |
                 +------------+
                        |
                        | objectKey
                        |
                        v
                    MinIO
                 +------------+
                 | File Data  |
                 +------------+

Metadata  ---> PostgreSQL
File Data ---> MinIO
```

This separation of responsibilities is the same pattern commonly used with Amazon S3 in production systems.

---