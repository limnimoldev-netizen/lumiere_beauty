import { createContext, useContext, useEffect, useMemo, useState } from "react";
import { productApi } from "../services/api";

const ShopContext = createContext(null);
export function ShopProvider({ children }) {

  const [products, setProducts] = useState([]);

  useEffect(() => {
    const fetchProducts = async () => {
      try {
        const data = await productApi.getAll();
        setProducts(data);
        console.log("Products from backend:", data);
      } catch (error) {
        console.error("Failed to fetch products:", error);
      }
    };

    fetchProducts();
  }, []);

  const [cart, setCart] = useState(() => {
    try {
      return JSON.parse(localStorage.getItem("lumiere-cart")) || [];
    } catch {
      return [];
    }
  });
  
  useEffect(
    () => localStorage.setItem("lumiere-cart", JSON.stringify(cart)),
    [cart],
  );
  const addToCart = (product) =>
    setCart((current) => {
      if (product?.id == null) return current;
      const stock = Number(product.stock);
      if (Number.isFinite(stock) && stock <= 0) return current;
      const found = current.find((item) => item.id === product.id);
      if (found) {
        if (Number.isFinite(stock) && found.quantity >= stock) return current;
        return current.map((item) =>
          item.id === product.id
            ? { ...item, quantity: item.quantity + 1 }
            : item,
        );
      }
      return [...current, { ...product, quantity: 1 }];
    });
  const updateQuantity = (id, quantity) =>
    setCart((current) => {
      if (quantity <= 0) return current.filter((item) => item.id !== id);
      return current.map((item) => {
        if (item.id !== id) return item;
        const stock = Number(item.stock);
        if (Number.isFinite(stock) && stock <= 0) return item;
        return {
          ...item,
          quantity: Number.isFinite(stock)
            ? Math.min(quantity, stock)
            : quantity,
        };
      });
    });
  const removeFromCart = (id) =>
    setCart((current) => current.filter((item) => item.id !== id));
    
  const saveProduct = async (product) => {
    try {
      let savedProduct;

      if (product.id) {
        // Update an existing product
        savedProduct = await productApi.update(product.id, product);
      } else {
        // Create a new product
        savedProduct = await productApi.create(product);
      }

      // Update the React state with the backend response
      setProducts((current) => {
        const exists = current.some(
          (item) => item.id === savedProduct.id
        );

        return exists
          ? current.map((item) =>
              item.id === savedProduct.id ? savedProduct : item
            )
          : [savedProduct, ...current];
      });

      return savedProduct;
    } catch (error) {
      console.error("Failed to save product:", error);
      throw error;
    }
  };

  const deleteProduct = async (id) => {
    try {
      // Send DELETE request to the backend
      await productApi.remove(id);

      setProducts((current) =>
        current.filter((item) => Number(item.id) !== Number(id))
      );
    } catch (error) {
      console.error("Failed to delete product:", error);
      throw error;
    }
  };

  const clearCart = () => setCart([]);

  const value = useMemo(
    () => ({
      products,
      cart,
      addToCart,
      updateQuantity,
      removeFromCart,
      clearCart,
      saveProduct,
      deleteProduct,
    }),
    [products, cart],
  );
  return <ShopContext.Provider value={value}>{children}</ShopContext.Provider>;
}
export const useShop = () => useContext(ShopContext);
