# Phase-06 Object Storage with MinIO

## Goal

Replace static image URL placeholders with actual object storage.

Before this phase, posts stored image URLs such as:

```text
uploads/photo1.jpg
```

which did not represent real file storage.

The goal of this phase was to introduce object storage and support:

- Image uploads
- Object persistence
- Secure image access
- Presigned URL generation

---

## Problem

The application could create posts and feeds, but images were not actually stored anywhere.

```text
Post
 |
imageUrl = uploads/photo1.jpg
```

Problems:

- No binary file storage
- No upload mechanism
- No secure access control
- Not representative of a production system

---

## Solution

Introduced MinIO as an S3-compatible object storage layer.

Architecture:

```text
Client
   |
POST /api/v1/uploads
   |
Quarkus API
   |
MinIO
   |
Object Storage
```

---

## Infrastructure

### MinIO

Container:

```yaml
minio:
  image: minio/minio

  container_name: photoshare-minio

  ports:
    - "9000:9000"
    - "9001:9001"

  environment:
    MINIO_ROOT_USER: admin
    MINIO_ROOT_PASSWORD: password123

  command: server /data --console-address ":9001"
```

---

### Bucket

Bucket name:

```text
photoshare
```

---

## Upload API

Endpoint:

```http
POST /api/v1/uploads
```

Consumes:

```text
multipart/form-data
```

Request:

```text
file=<image>
```

Response:

```json
{
  "imageUrl":
  "http://localhost:9000/photoshare/7e7f1abc.jpg",
  "objectName":
  "7e7f1abc.jpg"
}
```

---

## File Storage Service

Responsibilities:

- Generate object names
- Upload files
- Generate presigned URLs
- Hide MinIO implementation details

Core operations:

```text
upload()

generatePresignedUrl()

generatePresignedUrlFromUrl()
```

---

## Presigned URLs

### Why?

Object storage buckets should not be publicly accessible.

Instead of exposing objects directly:

```text
http://localhost:9000/photoshare/image.jpg
```

the application generates temporary secure URLs.

Example:

```text
http://localhost:9000/photoshare/image.jpg
?X-Amz-Signature=...
&X-Amz-Expires=...
```

Benefits:

- Temporary access
- Fine-grained permissions
- No public bucket required

---

## Feed Integration

Updated:

```text
FeedService
```

Before:

```java
return post.imageUrl;
```

After:

```java
storageService.generatePresignedUrlFromUrl(
        post.imageUrl);
```

Returned image URL:

```text
Presigned URL
```

---

## Timeline Integration

Updated:

```text
TimelineService
```

Timeline responses now return presigned URLs.

---

## Post Integration

Updated:

```text
PostService
```

Post retrieval now returns presigned URLs instead of raw object paths.

---

## Refactoring

Initially:

```java
extractObjectName(...)
```

was duplicated across:

- FeedService
- TimelineService
- PostService

Refactored into:

```java
FileStorageService
```

New API:

```java
generatePresignedUrlFromUrl(...)
```

Benefits:

- Single source of truth
- Reduced duplication
- Easier maintenance

---

## End-to-End Flow

### Upload

```text
Client
    |
POST /uploads
    |
MinIO
    |
Object Stored
```

### Create Post

```text
Client
    |
POST /posts
    |
Stores image URL
```

### Feed

```text
Client
    |
GET /feed
    |
Generate Presigned URL
    |
Return Response
```

---

## Architecture

```text
                Client
                   |
                   v
             Quarkus API
           /      |       \
          /       |        \
         v        v         v
   PostgreSQL   Redis   Redpanda
                              |
                       Notifications

               MinIO Object Storage
```

---

## Learning Outcomes

### Object Storage

Learned:

- Object storage concepts
- Bucket management
- Binary file persistence

### S3-Compatible APIs

Learned:

- Uploading objects
- Object naming
- Object retrieval

### Secure Access

Learned:

- Presigned URLs
- Temporary authorization
- Secure object delivery

### Service Design

Learned:

- Encapsulating storage concerns
- Avoiding duplication
- Infrastructure abstraction

---

## Technical Debt / Future Improvements

### Store Object Keys Instead of Full URLs

Current:

```text
http://localhost:9000/photoshare/image.jpg
```

Preferred:

```text
image.jpg
```

Benefits:

- Storage vendor independence
- Easier migration to S3
- Easier CDN integration
- Cleaner database schema

### Add CDN Layer

Future:

```text
Client
   |
CDN
   |
Object Storage
```

### Direct Browser Uploads

Future:

```text
Client
   |
Presigned Upload URL
   |
MinIO/S3
```

without routing file uploads through the application.

---

## Success Criteria

Completed:

- ✅ MinIO Running
- ✅ Bucket Created
- ✅ Upload API
- ✅ File Stored In MinIO
- ✅ Upload Response
- ✅ Feed Integration
- ✅ Timeline Integration
- ✅ Post Integration
- ✅ Presigned URL Support
- ✅ Shared URL Generation Logic

---

## Phase Summary

Phase-06 introduced real object storage into the platform.

The application evolved from storing fake image references to storing real binary content in MinIO and serving images securely using presigned URLs.

This phase completed the media-storage layer of the platform and moved the architecture closer to a production-grade social-media backend.