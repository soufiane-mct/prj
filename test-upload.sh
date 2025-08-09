#!/bin/bash

# Test file upload endpoint
curl -X POST \
  http://localhost:8081/api/v1/books/1/images/upload \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: multipart/form-data" \
  -F "files=@test-image.jpg" \
  -F "firstAsCover=true"
