import request from './request'

export function register(data) { return request.post('/user/register', data) }
export function login(data) { return request.post('/user/login', data) }
export function getUserInfo() { return request.get('/user/info') }
export function refreshToken(refreshToken) { return request.post('/user/refresh', { refreshToken }) }
