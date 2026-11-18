import { useState } from "react";
import { Navigate, useNavigate } from "react-router-dom";
import { authAPI } from "../services/api.js";
import { useAuth } from "../context/AuthContext";

export default function AdminLogin() {
  const navigate = useNavigate();
  const { user, login } = useAuth();

  const [formData, setFormData] = useState({
  email: import.meta.env.DEV ? "admin@gmail.com" : "",
  password: import.meta.env.DEV ? "admin123" : "",
  });

  // already logged in as admin -> go straight to dashboard
  if (user?.role === "ADMIN") return <Navigate to="/admin" replace />;

  const handleChange = (e) =>
    setFormData({ ...formData, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const result = await authAPI.login(formData);
      console.log("Login result:", result);

      const data = result?.data ?? result;

      if (data?.role !== "ADMIN") {
        alert("Access denied. Admin accounts only.");
        return; // do NOT call login(), so nothing is saved
      }

      login(data);
      navigate("/admin", { replace: true });
    } catch (error) {
      console.error("Admin login error:", error);
      alert(error.response?.data?.message || error.message || "Login failed.");
    }
  };

  return (
    <main className="container-page flex min-h-[65vh] items-center justify-center py-14">
      <form
        onSubmit={handleSubmit}
        className="w-full max-w-md rounded-3xl border border-line p-8 shadow-soft"
      >
        <h1 className="text-center font-display text-4xl">Admin sign in</h1>

        <input
          required
          name="email"
          type="email"
          value={formData.email}
          onChange={handleChange}
          className="field mt-7"
          placeholder="Admin email"
        />
        <input
          required
          name="password"
          type="password"
          value={formData.password}
          onChange={handleChange}
          className="field mt-3"
          placeholder="Password"
        />
        <button type="submit" className="btn-primary mt-6 w-full">
          Sign in
        </button>
      </form>
    </main>
  );
}