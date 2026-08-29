import http from 'k6/http';
import { SharedArray } from 'k6/data';

const POST_ID =
    '49cb29f1-fcff-44e7-80cc-e01a52c0a820';

const BASE_URL =
    'http://localhost:8080';

// const users = new SharedArray(
//     'users',
//     function () {
//
//         return open('./test-users.csv')
//             .split('\n')
//             .filter(Boolean);
//     }
// );

const users = new SharedArray(
    "users",
    function () {

        return open("./test-users.csv")
            .split("\n")
            .map(line => line.trim())
            .filter(line => line.length > 0);
    }
);

console.log(
    JSON.stringify(users[0])
);

export const options = {

    vus: users.length,

    iterations: users.length
};

export default function () {

    const userId =
        users[__VU - 1];

    const url =
        `${BASE_URL}` +
        `/api/v1/posts/` +
        `${POST_ID}` +
        `/likes/` +
        `${userId}`;

    const response =
        http.put(
            url,
            null,
            {
                headers: {
                    'Content-Type':
                        'application/json'
                }
            }
        );

    if (
        response.status !== 200 &&
        response.status !== 201
    ) {

        console.log(
            `FAILED: ${userId}` +
            ` -> ${response.status}`
        );
    }
}