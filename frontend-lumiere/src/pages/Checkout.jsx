import { useRef, useState } from "react";
import { Link } from "react-router-dom";
import { useShop } from "../context/ShopContext";

const API_URL = "http://localhost:8080/api/orders/checkout";

export default function Checkout() {
  const { cart, clearCart } = useShop(); // use whatever your context calls its clear function
  const [done, setDone] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const submittingRef = useRef(false); // blocks double submits synchronously
  const [form, setForm] = useState({
    customerName: "",
    email: "",
    phone: "",
    address: "",
    city: "",
    paymentMethod: "COD",
  });

  const total = cart.reduce((s, p) => s + p.price * p.quantity, 0);

  const handleChange = (e) =>
    setForm({ ...form, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (submittingRef.current) return; // ignore clicks while a request is in flight
    submittingRef.current = true;
    setLoading(true);
    setError("");

    const payload = {
      customerName: form.customerName,
      email: form.email,
      phone: form.phone,
      address: `${form.address}, ${form.city}`,
      paymentMethod: form.paymentMethod,
      items: cart.map((p) => ({ productId: p.id, quantity: p.quantity })),
    };

    try {
      const res = await fetch(API_URL, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload),
      });

      if (!res.ok) {
        setError(await res.text());
        return;
      }

      clearCart();
      setDone(true);
    } catch {
      setError("Cannot reach the server. Is Spring Boot running?");
    } finally {
      submittingRef.current = false;
      setLoading(false);
    }
  };

  if (done)
    return (
      <main className="container-page py-24 text-center">
        <p className="eyebrow text-rose">Thank you</p>
        <h1 className="mt-3 font-display text-4xl">Order received</h1>
        <p className="mt-4 text-muted">We'll contact you to confirm delivery.</p>
        <Link to="/shop" className="btn-primary mt-8">Continue shopping</Link>
      </main>
    );

  if (!cart.length)
    return (
      <main className="container-page py-24 text-center">
        <h1 className="font-display text-3xl">Your bag is empty</h1>
        <Link to="/shop" className="btn-primary mt-6">Shop now</Link>
      </main>
    );

  return (
    <main className="container-page max-w-3xl py-12">
      <p className="eyebrow text-rose">Almost yours</p>
      <h1 className="mt-3 font-display text-4xl">Checkout</h1>

      <form onSubmit={handleSubmit} className="mt-8 grid gap-8 md:grid-cols-[1fr_260px]">
        <div className="space-y-4">
          <h2 className="font-semibold">Contact & delivery</h2>
          <input required name="customerName" value={form.customerName}
            onChange={handleChange} className="field" placeholder="Full name" />
          <input required type="email" name="email" value={form.email}
            onChange={handleChange} className="field" placeholder="Email address" />
          <input required name="phone" value={form.phone}
            onChange={handleChange} className="field" placeholder="Phone number" />
          <input required name="address" value={form.address}
            onChange={handleChange} className="field" placeholder="Delivery address" />
          <input required name="city" value={form.city}
            onChange={handleChange} className="field" placeholder="City" />

          <label className="block text-sm font-medium">
            Payment method
            <select name="paymentMethod" value={form.paymentMethod}
              onChange={handleChange} className="field mt-2">
              <option value="COD">Cash on delivery</option>
              <option value="BANK_TRANSFER">Bank transfer</option>
            </select>
          </label>

          {error && <p className="text-sm text-red-600">{error}</p>}

          <button type="submit" disabled={loading} className="btn-primary w-full">
            {loading ? "Placing order..." : "Place order"}
          </button>
        </div>

        {/* your <aside> summary stays exactly the same */}
      </form>
    </main>
  );
}