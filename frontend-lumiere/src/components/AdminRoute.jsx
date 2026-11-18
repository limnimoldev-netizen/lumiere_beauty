import { useEffect, useState } from "react";
import { Navigate, Outlet } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { authApi } from "../services/api.js";

export default function AdminRoute() {
  const { user, login, logout } = useAuth();
  const [verified, setVerified] = useState(false);

  useEffect(() => {
    if (user?.role?.toUpperCase() !== "ADMIN") return;
    let cancelled = false;
    setVerified(false);
    authApi.me().then(account => {
      if (cancelled) return;
      if (account.role?.toUpperCase() !== "ADMIN" || account.active === false) {
        logout();
        return;
      }
      login(account);
      setVerified(true);
    }).catch(() => {
      if (!cancelled) logout();
    });
    return () => { cancelled = true; };
  }, [user?.id, user?.role, login, logout]);

  if (!user || user.role?.toUpperCase() !== "ADMIN") {
    return <Navigate to="/admin/login" replace />;
  }
  if (!verified) return <p role="status" className="p-8 text-center text-sm text-muted">Checking administrator access…</p>;
  return <Outlet />;
}
