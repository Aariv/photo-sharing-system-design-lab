#!/bin/bash

POST_ID="YOUR_POST_ID"
BASE_URL="http://localhost:8080"
TOTAL_REQUESTS=1000

for ((i = 1; i <= TOTAL_REQUESTS; i++))
do
(
USER_ID="$(uuidgen | tr '[:upper:]' '[:lower:]')"
curl -s \
  -X PUT \
  "${BASE_URL}/api/v1/posts/${POST_ID}/likes/${USER_ID}" \
  -H "Content-Type: application/json" >/dev/null
) &
done

wait

echo "Completed ${TOTAL_REQUESTS} like requests for post ${POST_ID}"
