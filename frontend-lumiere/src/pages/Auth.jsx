import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { authApi } from "../services/api.js";
import { useAuth } from "../context/AuthContext";

export default function Auth({ register = false }) {
  const navigate = useNavigate();
  const { login } = useAuth();

  const title = register ? "Create your account" : "Welcome back";

  const [formData, setFormData] = useState({
    name: "",
    email: "",
    password: "",
  });

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value,
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    try {
      const result = register
        ? await authApi.register(formData)
        : await authApi.login({
            email: formData.email,
            password: formData.password,
          });

      login(result.data ?? result);
      navigate("/");
    } catch (error) {
      console.error(error);

      alert(
        error.response?.data?.message ||
          (register
            ? "Registration failed. Please try again."
            : "Login failed. Please try again.")
      );
    }
  };

  return (
    <main className="container-page flex min-h-[65vh] items-center justify-center py-14">
      <form
        onSubmit={handleSubmit}
        className="w-full max-w-md rounded-3xl border border-line p-8 shadow-soft"
      >
        <p className="eyebrow text-center text-rose">Lumière account</p>

        <h1 className="mt-3 text-center font-display text-4xl">{title}</h1>

        <p className="mt-3 text-center text-sm text-muted">
          {register
            ? "Join us for your everyday beauty ritual."
            : "Sign in to view your account and orders."}
        </p>

        {register && (
          <input
            required
            name="name"
            value={formData.name}
            onChange={handleChange}
            className="field mt-7"
            placeholder="Full name"
          />
        )}

        <input
          required
          name="email"
          type="email"
          value={formData.email}
          onChange={handleChange}
          className="field mt-5"
          placeholder="Email address"
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
          {register ? "Create account" : "Sign in"}
        </button>

        <p className="mt-5 text-center text-xs text-muted">
          {register ? "Already have an account?" : "New to Lumière?"}{" "}
          <Link
            className="font-semibold text-rose"
            to={register ? "/login" : "/register"}
          >
            {register ? "Sign in" : "Create an account"}
          </Link>
        </p>
      </form>
    </main>
  );
}