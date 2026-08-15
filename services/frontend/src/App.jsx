import React, { useState, useEffect } from 'react';
import { api } from './services/api';
import { ShoppingCart, Package, Users, Clock, CheckCircle, RefreshCw, AlertCircle } from 'lucide-react';

export default function App() {
  const [products, setProducts] = useState([]);
  const [customers, setCustomers] = useState([]);
  const [orders, setOrders] = useState([]);
  const [inventory, setInventory] = useState({});
  const [selectedCustomerId, setSelectedCustomerId] = useState(1);
  const [cart, setCart] = useState([]);
  const [loading, setLoading] = useState(false);
  const [msg, setMsg] = useState(null);

  const loadData = async () => {
    try {
      setLoading(true);
      const [prodData, custData, ordData, invData] = await Promise.all([
        api.getProducts().catch(() => []),
        api.getCustomers().catch(() => []),
        api.getOrders().catch(() => []),
        api.getInventory().catch(() => [])
      ]);

      setProducts(prodData);
      setCustomers(custData);
      setOrders(ordData);

      // Map inventory SKU to quantity object
      const invMap = {};
      invData.forEach(item => {
        invMap[item.sku] = item;
      });
      setInventory(invMap);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
    const interval = setInterval(loadData, 8000); // Poll every 8s for live order & inventory updates
    return () => clearInterval(interval);
  }, []);

  const addToCart = (product) => {
    const existing = cart.find(item => item.sku === product.sku);
    if (existing) {
      setCart(cart.map(item => item.sku === product.sku ? { ...item, quantity: item.quantity + 1 } : item));
    } else {
      setCart([...cart, { sku: product.sku, name: product.name, price: product.price, quantity: 1 }]);
    }
  };

  const removeFromCart = (sku) => {
    setCart(cart.filter(item => item.sku !== sku));
  };

  const calculateTotal = () => {
    return cart.reduce((acc, item) => acc + item.price * item.quantity, 0).toFixed(2);
  };

  const handleCheckout = async () => {
    if (cart.length === 0) return;
    try {
      setLoading(true);
      const payload = {
        customerId: Number(selectedCustomerId),
        items: cart.map(item => ({
          sku: item.sku,
          quantity: item.quantity,
          price: item.price
        }))
      };

      const res = await api.placeOrder(payload);
      setMsg({ type: 'success', text: `Order created: ${res.orderNumber}! Kafka event produced.` });
      setCart([]);
      setTimeout(loadData, 1000);
    } catch (err) {
      setMsg({ type: 'error', text: 'Checkout failed. Please check Gateway status.' });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-7xl mx-auto p-6 space-y-8">
      {/* Header */}
      <header className="flex flex-col md:flex-row items-start md:items-center justify-between border-b pb-6 gap-4">
        <div>
          <h1 className="text-3xl font-extrabold tracking-tight text-slate-900">Cloud Retail Microservices</h1>
          <p className="text-sm text-slate-500">Connected to API Gateway on port 8080 (Kafka Event-Driven Backplane)</p>
        </div>
        <div className="flex items-center gap-4">
          <div className="flex items-center gap-2 bg-white px-3 py-2 border rounded-lg shadow-sm">
            <Users className="w-4 h-4 text-indigo-600" />
            <span className="text-xs font-semibold text-slate-600">Active User:</span>
            <select
              value={selectedCustomerId}
              onChange={(e) => setSelectedCustomerId(e.target.value)}
              className="text-sm font-medium bg-transparent focus:outline-none cursor-pointer"
            >
              {customers.map(c => (
                <option key={c.id} value={c.id}>{c.firstName} {c.lastName} (ID #{c.id})</option>
              ))}
            </select>
          </div>
          <button 
            onClick={loadData} 
            className="flex items-center gap-1.5 px-3 py-2 bg-slate-100 hover:bg-slate-200 text-slate-700 text-sm font-medium rounded-lg transition"
          >
            <RefreshCw className={`w-4 h-4 ${loading ? 'animate-spin' : ''}`} /> Refresh
          </button>
        </div>
      </header>

      {/* Alert Banner */}
      {msg && (
        <div className={`p-4 rounded-lg flex items-center justify-between ${msg.type === 'success' ? 'bg-emerald-50 text-emerald-800 border border-emerald-200' : 'bg-rose-50 text-rose-800 border border-rose-200'}`}>
          <div className="flex items-center gap-2 text-sm font-medium">
            {msg.type === 'success' ? <CheckCircle className="w-5 h-5 text-emerald-600" /> : <AlertCircle className="w-5 h-5 text-rose-600" />}
            {msg.text}
          </div>
          <button onClick={() => setMsg(null)} className="text-xs font-bold uppercase tracking-wider">Dismiss</button>
        </div>
      )}

      {/* Main Grid: Catalog + Cart */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        
        {/* Product Catalog (2 Cols) */}
        <div className="lg:col-span-2 space-y-4">
          <div className="flex items-center justify-between">
            <h2 className="text-xl font-bold flex items-center gap-2">
              <Package className="w-5 h-5 text-indigo-600" /> Product Catalog
            </h2>
            <span className="text-xs font-semibold text-slate-500">{products.length} Products Available</span>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            {products.map(p => {
              const inv = inventory[p.sku];
              const availableStock = inv ? inv.quantity : 0;
              const isOutOfStock = availableStock <= 0;

              return (
                <div key={p.id} className="bg-white border rounded-xl p-5 shadow-sm flex flex-col justify-between hover:border-indigo-200 transition">
                  <div className="space-y-2">
                    <div className="flex justify-between items-start">
                      <h3 className="font-bold text-slate-900 text-base">{p.name}</h3>
                      <span className="text-xs font-mono bg-slate-100 px-2 py-0.5 rounded text-slate-600">{p.sku}</span>
                    </div>
                    <p className="text-xs text-slate-500 line-clamp-2">{p.description}</p>
                  </div>

                  <div className="mt-4 pt-4 border-t flex items-center justify-between">
                    <div>
                      <div className="text-lg font-extrabold text-slate-900">${p.price}</div>
                      <div className="text-xs font-medium">
                        {isOutOfStock ? (
                          <span className="text-rose-600 font-semibold">Out of Stock</span>
                        ) : (
                          <span className="text-emerald-600">Stock: {availableStock} units</span>
                        )}
                      </div>
                    </div>
                    <button
                      onClick={() => addToCart(p)}
                      disabled={isOutOfStock}
                      className={`px-3 py-1.5 rounded-lg text-xs font-semibold shadow-sm transition ${
                        isOutOfStock 
                          ? 'bg-slate-100 text-slate-400 cursor-not-allowed'
                          : 'bg-indigo-600 hover:bg-indigo-700 text-white'
                      }`}
                    >
                      Add to Cart
                    </button>
                  </div>
                </div>
              );
            })}
          </div>
        </div>

        {/* Shopping Cart (1 Col) */}
        <div className="space-y-4">
          <h2 className="text-xl font-bold flex items-center gap-2">
            <ShoppingCart className="w-5 h-5 text-indigo-600" /> Checkout Cart
          </h2>

          <div className="bg-white border rounded-xl p-5 shadow-sm space-y-4">
            {cart.length === 0 ? (
              <p className="text-sm text-slate-400 text-center py-8">Your cart is empty. Add products from the catalog to place an order.</p>
            ) : (
              <div className="space-y-3">
                {cart.map(item => (
                  <div key={item.sku} className="flex justify-between items-center text-sm pb-3 border-b">
                    <div>
                      <div className="font-semibold text-slate-800">{item.name}</div>
                      <div className="text-xs text-slate-500">{item.quantity} × ${item.price}</div>
                    </div>
                    <div className="flex items-center gap-3">
                      <span className="font-bold">${(item.quantity * item.price).toFixed(2)}</span>
                      <button onClick={() => removeFromCart(item.sku)} className="text-xs text-rose-500 hover:underline">Remove</button>
                    </div>
                  </div>
                ))}

                <div className="pt-2 flex justify-between items-center text-base font-bold">
                  <span>Total Amount:</span>
                  <span className="text-indigo-600 text-lg">${calculateTotal()}</span>
                </div>

                <button
                  onClick={handleCheckout}
                  disabled={loading}
                  className="w-full mt-4 py-2.5 bg-slate-900 hover:bg-black text-white text-sm font-semibold rounded-lg shadow transition disabled:opacity-50"
                >
                  {loading ? 'Submitting Order...' : 'Place Order (Publish to Kafka)'}
                </button>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Orders Dashboard */}
      <section className="space-y-4 pt-4 border-t">
        <div className="flex items-center justify-between">
          <h2 className="text-xl font-bold flex items-center gap-2">
            <Clock className="w-5 h-5 text-indigo-600" /> Live Order Stream
          </h2>
          <span className="text-xs text-slate-400">Auto-refreshing via Order Service</span>
        </div>

        <div className="bg-white border rounded-xl overflow-hidden shadow-sm">
          <table className="w-full text-left text-sm">
            <thead className="bg-slate-50 border-b text-xs uppercase font-semibold text-slate-600">
              <tr>
                <th className="px-4 py-3">Order Number</th>
                <th className="px-4 py-3">Customer</th>
                <th className="px-4 py-3">Total Amount</th>
                <th className="px-4 py-3">Status</th>
                <th className="px-4 py-3">Items</th>
              </tr>
            </thead>
            <tbody className="divide-y text-slate-700">
              {orders.length === 0 ? (
                <tr>
                  <td colSpan="5" className="text-center py-6 text-slate-400">No orders recorded yet.</td>
                </tr>
              ) : (
                orders.slice().reverse().map(order => (
                  <tr key={order.id} className="hover:bg-slate-50">
                    <td className="px-4 py-3 font-mono font-bold text-indigo-600">{order.orderNumber}</td>
                    <td className="px-4 py-3">Customer #{order.customerId}</td>
                    <td className="px-4 py-3 font-semibold">${order.totalAmount}</td>
                    <td className="px-4 py-3">
                      <span className="inline-flex items-center px-2 py-0.5 rounded text-xs font-semibold bg-emerald-100 text-emerald-800">
                        {order.status}
                      </span>
                    </td>
                    <td className="px-4 py-3 text-xs text-slate-500">
                      {order.items?.map(it => `${it.sku} (×${it.quantity})`).join(', ')}
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </section>
    </div>
  );
}