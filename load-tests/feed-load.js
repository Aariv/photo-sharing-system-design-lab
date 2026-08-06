import http from "k6/http";
import { check } from "k6";

export const options = {
    vus: 50,
    duration: "30s",
};

const USER_ID = "8746a57f-0317-486a-b0ee-414353a68271";

export default function () {

    const response =
        http.get(
            `http://localhost:8080/api/v1/feed?userId=${USER_ID}`
        );

    check(response, {
        "status is 200":
            (r) => r.status === 200
    });
}