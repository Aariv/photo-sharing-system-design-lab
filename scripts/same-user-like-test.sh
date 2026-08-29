#!/bin/bash

POST_ID="49cb29f1-fcff-44e7-80cc-e01a52c0a820"
USER_ID="6c34b9b7-fb47-4c29-badb-c8fb48750962"

for i in {1..1000}
do
(
curl -s \
-X PUT \
"http://localhost:8080/api/v1/posts/${POST_ID}/likes/${USER_ID}") &
done

wait

echo "Completed"
