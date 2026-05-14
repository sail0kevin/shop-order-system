import request from './request'

export function getCartList() { return request.get('/cart/list') }
export function addCart(data) { return request.post('/cart/add', data) }
export function updateCart(data) { return request.post('/cart/update', data) }
export function removeCart(data) { return request.post('/cart/remove', data) }
