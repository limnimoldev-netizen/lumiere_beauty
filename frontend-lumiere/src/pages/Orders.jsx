import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { useShop } from "../context/ShopContext";

const API_URL = "http://localhost:8080/api/orders/my";

export default function Orders() {
  const { user } = useAuth();
  const { products } = useShop();
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    if (!user?.email) return;
    const load = async () => {
      try {
        const res = await fetch(`${API_URL}?email=${encodeURIComponent(user.email)}`);
        if (!res.ok) throw new Error(await res.text());
        setOrders(await res.json());
      } catch (err) {
        console.error(err);
        setError("Could not load your orders.");
      } finally {
        setLoading(false);
      }
    };
    load();
  }, [user?.email]);

  const productName = (id) =>
    products.find((p) => Number(p.id) === Number(id))?.name ?? `Product #${id}`;

  return (
    <main className="container-page min-h-[55vh] py-16">
      <p className="eyebrow text-rose">Your account</p>
      <h1 className="mt-3 font-display text-4xl">Order history</h1>

      {loading && <p className="mt-8 text-muted">Loading your orders...</p>}
      {error && <p className="mt-8 text-sm text-red-600">{error}</p>}

      {!loading && !error && orders.length === 0 && (
        <div className="mt-8 rounded-2xl border border-line p-8 text-center">
          <p className="text-muted">You haven't placed any orders yet.</p>
          <Link to="/shop" className="btn-primary mt-5">Start shopping</Link>
        </div>
      )}

      <div className="mt-8 space-y-5">
        {orders.map((o) => (
          <article key={o.id} className="rounded-2xl border border-line p-6">
            <div className="flex flex-wrap items-center justify-between gap-2">
              <p className="font-semibold">Order #{o.id}</p>
              <span className="rounded-full bg-cream px-3 py-1 text-xs">{o.status}</span>
            </div>
            <p className="mt-1 text-xs text-muted">
              {o.createdAt ? new Date(o.createdAt).toLocaleString() : ""}
            </p>
            <ul className="mt-4 space-y-1 text-sm">
              {o.items.map((it) => (
                <li key={it.id} className="flex justify-between">
                  <span>{productName(it.productId)} × {it.quantity}</span>
                  <span>${Number(it.subtotal).toFixed(2)}</span>
                </li>
              ))}
            </ul>
            <p className="mt-4 text-right font-semibold">
              Total: ${Number(o.totalAmount).toFixed(2)}
            </p>
          </article>
        ))}
      </div>
    </main>
  );
}