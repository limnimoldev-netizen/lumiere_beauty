import { createContext, useContext, useEffect, useMemo, useState } from 'react'
import { initialProducts } from '../data/products'

const ShopContext = createContext(null)
export function ShopProvider({ children }) {
  const [products, setProducts] = useState(() => {
    try { return JSON.parse(localStorage.getItem('lumiere-products')) || initialProducts }
    catch { return initialProducts }
  })
  const [cart, setCart] = useState(() => {
    try { return JSON.parse(localStorage.getItem('lumiere-cart')) || [] }
    catch { return [] }
  })
  useEffect(() => localStorage.setItem('lumiere-products', JSON.stringify(products)), [products])
  useEffect(() => localStorage.setItem('lumiere-cart', JSON.stringify(cart)), [cart])
  const addToCart = (product) => setCart(current => {
    if (product?.id == null) return current
    const stock = Number(product.stock)
    if (Number.isFinite(stock) && stock <= 0) return current
    const found = current.find(item => item.id === product.id)
    if (found) {
      if (Number.isFinite(stock) && found.quantity >= stock) return current
      return current.map(item => item.id === product.id ? { ...item, quantity: item.quantity + 1 } : item)
    }
    return [...current, { ...product, quantity: 1 }]
  })
  const updateQuantity = (id, quantity) => setCart(current => {
    if (quantity <= 0) return current.filter(item => item.id !== id)
    return current.map(item => {
      if (item.id !== id) return item
      const stock = Number(item.stock)
      if (Number.isFinite(stock) && stock <= 0) return item
      return { ...item, quantity: Number.isFinite(stock) ? Math.min(quantity, stock) : quantity }
    })
  })
  const removeFromCart = (id) => setCart(current => current.filter(item => item.id !== id))
  const saveProduct = (product) => setProducts(current => {
    const exists = current.some(item => item.id === product.id)
    return exists ? current.map(item => item.id === product.id ? product : item) : [product, ...current]
  })
  const deleteProduct = (id) => setProducts(current => current.filter(item => item.id !== id))
  const value = useMemo(() => ({ products, cart, addToCart, updateQuantity, removeFromCart, saveProduct, deleteProduct }), [products, cart])
  return <ShopContext.Provider value={value}>{children}</ShopContext.Provider>
}
export const useShop = () => useContext(ShopContext)
