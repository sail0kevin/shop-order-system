import request from './request'

export function getProductPage(params) { return request.get('/product/list', { params }) }
export function getProductDetail(id) { return request.get('/product/detail/' + id) }
