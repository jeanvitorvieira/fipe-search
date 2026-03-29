import http from 'k6/http';
import { check } from 'k6';

const HOT_KEYS = [
    { modelo: 1, ano: 2023 },
    { modelo: 2, ano: 2021 },
    { modelo: 3, ano: 2021 },
    { modelo: 4, ano: 2021 },
    { modelo: 5, ano: 2021 },
];

export const options = {
    vus: 200,
    duration: '20s',
};

const BASE_URL = 'http://app:8080/fipe';

function getRandomKey() {
    return HOT_KEYS[Math.floor(Math.random() * HOT_KEYS.length)];
}

export default function () {
    const key = getRandomKey();
    let res;

    res = http.get(`${BASE_URL}/${key.modelo}/${key.ano}`);

    check(res, { 'status is 200': (r) => r.status === 200 });
}