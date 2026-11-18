import { useState } from "react";
import { NavLink, Outlet, Link, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import {
  LayoutDashboard,
  Package,
  Store,
  ArrowLeft,
  Sparkles,
  Users,
  Menu,
  X,
} from "lucide-react";

export default function AdminLayout() {
  const { logout } = useAuth();
  const navigate = useNavigate();
  const [open, setOpen] = useState(false);

  const closeMenu = () => setOpen(false);

  const handleLogout = () => {
    navigate("/admin/login", { replace: true });
    logout();
  };

  const item = ({ isActive }) =>
    `flex items-center gap-3 rounded-xl px-4 py-3 text-sm font-medium transition ${
      isActive
        ? "bg-wine text-white shadow-sm"
        : "text-ink/65 hover:bg-cream hover:text-wine"
    }`;

  return (
    <div className="min-h-screen bg-[#F5F3E9]">
      {/* Dark overlay, mobile only */}
      {open && (
        <div
          onClick={closeMenu}
          className="fixed inset-0 z-30 bg-black/40 lg:hidden"
          aria-hidden="true"
        />
      )}

      {/* Sidebar: drawer on mobile, fixed on desktop */}
      <aside
        className={`fixed inset-y-0 left-0 z-40 flex w-64 flex-col overflow-y-auto border-r border-line bg-[#FBF8F2] p-5 transition-transform duration-300 lg:translate-x-0 ${
          open ? "translate-x-0" : "-translate-x-full"
        }`}
      >
        <div className="flex items-center justify-between">
          <Link
            to="/"
            onClick={closeMenu}
            className="flex items-center gap-3 px-2 py-3"
          >
            <span className="grid h-10 w-10 place-items-center rounded-full bg-blush text-rose">
              <Sparkles size={19} />
            </span>
            <span className="font-display text-2xl text-wine">Lumière.</span>
          </Link>

          <button
            onClick={closeMenu}
            aria-label="Close menu"
            className="rounded-full p-2 text-wine lg:hidden"
          >
            <X size={20} />
          </button>
        </div>

        <p className="eyebrow mb-3 mt-8 px-4 text-sage">Workspace</p>

        <nav className="space-y-1">
          <NavLink end to="/admin" className={item} onClick={closeMenu}>
            <LayoutDashboard size={17} /> Overview
          </NavLink>
          <NavLink to="/admin/products" className={item} onClick={closeMenu}>
            <Package size={17} /> Products
          </NavLink>
          <NavLink to="/admin/users" className={item} onClick={closeMenu}>
            <Users size={17} /> User management
          </NavLink>
        </nav>

        {/* Pushed to the bottom on any screen height */}
        <div className="mt-auto pt-8">
          <button onClick={handleLogout} className="btn-primary w-full">
            Log out
          </button>

          <div className="mt-4 flex items-center justify-center">
            <Link
              to="/"
              onClick={closeMenu}
              className="flex items-center gap-2 rounded-xl px-4 py-3 text-sm text-muted transition hover:bg-cream hover:text-wine"
            >
              <ArrowLeft size={16} /> Back to storefront
            </Link>
          </div>
        </div>
      </aside>

      <div className="lg:pl-64">
        <header className="flex h-[76px] items-center justify-between gap-3 border-b border-line bg-[#FBF8F2] px-4 sm:px-6 lg:px-10">
          <div className="flex items-center gap-3">
            <button
              onClick={() => setOpen(true)}
              aria-label="Open menu"
              className="rounded-full p-2 text-wine hover:bg-cream lg:hidden"
            >
              <Menu size={22} />
            </button>
            <div>
              <p className="text-sm font-semibold text-wine">Admin workspace</p>
              <p className="hidden text-xs text-muted sm:block">
                Lumière Beauty · Demo administration
              </p>
            </div>
          </div>

          <Link
            to="/"
            className="flex items-center gap-2 rounded-full border border-line px-4 py-2 text-sm text-ink transition hover:border-gold hover:text-wine"
          >
            <Store size={16} />
            <span className="hidden sm:inline">View store</span>
          </Link>
        </header>

        <main className="p-4 sm:p-5 md:p-9">
          <Outlet />
        </main>
      </div>
    </div>
  );
}