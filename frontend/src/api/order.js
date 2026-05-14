import request from './request'

export function createOrder() { return request.post('/order/create') }
export function payOrder(data) { return request.post('/order/pay', data) }
export function getOrderPage(params) { return request.get('/order/list', { params }) }
