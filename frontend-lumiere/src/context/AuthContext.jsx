import { createContext, useCallback, useContext, useState } from "react";
import { authApi } from "../services/api.js";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    try {
      return JSON.parse(localStorage.getItem("user"));
    } catch {
      return null;
    }
  });

  const login = useCallback((data) => {
    // Keep only safe fields, never the password
    const safe = { id: data.id, name: data.name, email: data.email, role: data.role };
    localStorage.setItem("user", JSON.stringify(safe));
    setUser(safe);
  }, []);

  const logout = useCallback(() => {
    authApi.logout().catch(() => {});
    localStorage.removeItem("user");
    setUser(null);
  }, []);

  return (
    <AuthContext.Provider value={{ user, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export const useAuth = () => useContext(AuthContext);
