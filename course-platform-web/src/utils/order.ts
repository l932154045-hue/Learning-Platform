/** 订单状态标签映射 */
export function getOrderStatusTag(status: number) {
  const map: Record<number, { type: string; text: string }> = {
    0: { type: 'warning', text: '待支付' },
    1: { type: 'success', text: '已支付' },
    2: { type: 'info', text: '已取消' },
    3: { type: 'danger', text: '已退款' },
  }
  return map[status] || { type: 'info', text: '未知' }
}
