const GATEWAY_URL = 'http://localhost:8080';

export const api = {
  getProducts: async () => {
    const res = await fetch(`${GATEWAY_URL}/v1/products`);
    if (!res.ok) throw new Error('Failed to fetch products');
    return res.json();
  },

  getCustomers: async () => {
    const res = await fetch(`${GATEWAY_URL}/v1/customers`);
    if (!res.ok) throw new Error('Failed to fetch customers');
    return res.json();
  },

  getOrders: async () => {
    const res = await fetch(`${GATEWAY_URL}/v1/orders`);
    if (!res.ok) throw new Error('Failed to fetch orders');
    return res.json();
  },

  getInventory: async () => {
    const res = await fetch(`${GATEWAY_URL}/v1/inventory`);
    if (!res.ok) throw new Error('Failed to fetch inventory');
    return res.json();
  },

  placeOrder: async (orderPayload) => {
    const res = await fetch(`${GATEWAY_URL}/v1/orders`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(orderPayload),
    });
    if (!res.ok) throw new Error('Failed to place order');
    return res.json();
  },
};