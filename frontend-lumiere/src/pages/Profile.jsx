import { Navigate, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

export default function Profile() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  if (!user) return <Navigate to="/login" replace />;

  return (
    <main className="container-page py-14">
      <h1 className="font-display text-4xl">Hello, {user.name}</h1>
      <p className="mt-3 text-muted">{user.email}</p>
      <button
        className="btn-primary mt-6"
        onClick={() => {
          logout();
          navigate("/");
        }}
      >
        Log out
      </button>
    </main>
  );
}