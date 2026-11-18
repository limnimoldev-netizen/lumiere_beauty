import { useEffect, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { ArrowLeft, Save } from "lucide-react";
import { useShop } from "../context/ShopContext";
import { brandApi, categoryApi } from "../services/api";

const empty = {
  id: "",
  name: "",
  brandId: "",
  categoryId: "",
  price: "",
  stock: "",
  status: "ACTIVE",
  imageUrl: "",
  description: "",
  featured: false,
  bestseller: false,
};
export default function AdminProductForm() {
  const { id } = useParams();
  const editing = Boolean(id);
  const { products, saveProduct } = useShop();
  const navigate = useNavigate();
  const [form, setForm] = useState(empty);
  const [error, setError] = useState("");
  
  const [brands, setBrands] = useState([]);
  const [categories, setCategories] = useState([]);
  
  useEffect(() => {
    const fetchOptions = async () => {
      try {
        const brandsData = await brandApi.getAll();
        const categoriesData = await categoryApi.getAll();

        setBrands(brandsData);
        setCategories(categoriesData);
      } catch (error) {
        console.error("Failed to fetch brands and categories:", error);
      }
    };

  fetchOptions();
}, []);

  useEffect(() => {
    if (editing) {
      const found = products.find((p) => String(p.id) === String(id));
      if (found) setForm({ ...found });
    }
  }, [id, editing, products]);
  const change = (e) => {
    const { name, value, type, checked } = e.target;
    setForm((f) => ({ ...f, [name]: type === "checkbox" ? checked : value }));
  };
  
  const submit = async (e) => {
    e.preventDefault();
    setError("");

    // Validation
    if (
      !form.name.trim() ||
      !form.brandId ||
      !form.categoryId ||
      form.price === "" ||
      form.stock === ""
    ) {
      setError("Please complete all required fields.");
      return;
    }

    // Product data
    const product = {
      name: form.name.trim(),
      brandId: Number(form.brandId),
      categoryId: Number(form.categoryId),
      price: Number(form.price),
      stock: Number(form.stock),
      status: form.status,
      imageUrl: form.imageUrl,
      description: form.description,

      // ADD THESE TWO LINES
      featured: form.featured,
      bestseller: form.bestseller,
    };
    console.log("Product data:", product);
    try {
      await saveProduct(
        editing ? { ...product, id: Number(id) } : product
      );

      navigate("/admin/products");
    } catch (error) {
      setError("Unable to save product. Please try again.");
    }
  };

  return (
    <>
      <Link
        to="/admin/products"
        className="inline-flex items-center gap-2 text-sm text-muted"
      >
        <ArrowLeft size={15} /> Back to products
      </Link>
      <div className="mt-5">
        <p className="eyebrow text-rose">
          {editing ? "Catalogue update" : "New catalogue item"}
        </p>
        <h1 className="mt-2 font-display text-4xl">
          {editing ? "Edit product" : "Add product"}
        </h1>
        <p className="mt-2 text-sm text-muted">
          Enter the product information below.
        </p>
      </div>
      <form
        onSubmit={submit}
        className="mt-8 max-w-3xl rounded-2xl border border-line bg-white p-6 md:p-8"
      >
        <div className="grid gap-5 md:grid-cols-2">
          <label className="text-xs font-semibold">
            Product name *
            <input
              className="field mt-2"
              name="name"
              value={form.name}
              onChange={change}
              required
              placeholder="e.g. Daily Hydrating Serum"
            />
          </label>
          <label className="text-xs font-semibold">
            Brand
            <select className="field mt-2" name="brandId" value={form.brandId} onChange={change} required >
              <option value="">Select brand</option>
              {brands.map((brand) => (
                <option key={brand.id} value={brand.id}>
                  {brand.name}
                </option>
              ))}
            </select>

          </label>
          <label className="text-xs font-semibold">
            Category
            <select className="field mt-2" name="categoryId" value={form.categoryId} onChange={change} required>
              <option value="">Select category</option>
              {categories.map((category) => (
                <option key={category.id} value={category.id}>
                  {category.name}
                </option>
              ))}
            </select>
          </label>

          <label className="text-xs font-semibold">
            Price (USD) *
            <input
              className="field mt-2"
              type="number"
              min="0"
              step="0.01"
              name="price"
              value={form.price}
              onChange={change}
              required
              placeholder="0.00"
            />
          </label>
          <label className="text-xs font-semibold">
            Stock quantity *
            <input
              className="field mt-2"
              type="number"
              min="0"
              name="stock"
              value={form.stock}
              onChange={change}
              required
              placeholder="0"
            />
          </label>
          <label className="text-xs font-semibold">
            Status
            <select
              className="field mt-2"
              name="status"
              value={form.status}
              onChange={change}
            >
              <option value="ACTIVE">Active</option>
              <option value="DRAFT">Draft</option>
            </select>
          </label>

          <label className="flex items-center gap-2">
            <input
              type="checkbox"
              name="featured"
              checked={form.featured}
              onChange={change}
            />
            Featured Product
          </label>

          <label className="flex items-center gap-2">
            <input
              type="checkbox"
              name="bestseller"
              checked={form.bestseller}
              onChange={change}
            />
            Bestseller
          </label>
          <label className="text-xs font-semibold md:col-span-2">
            Image URL
            <input
              className="field mt-2"
              name="imageUrl"
              value={form.imageUrl}
              onChange={change}
              placeholder="https://example.com/product.jpg"
            />
          </label>
          <label className="text-xs font-semibold md:col-span-2">
            Description
            <textarea
              className="field mt-2 min-h-28"
              name="description"
              value={form.description}
              onChange={change}
              placeholder="Describe the product..."
            />
          </label>
          
        </div>
        {error && <p className="mt-4 text-sm text-rose">{error}</p>}
        <div className="mt-7 flex flex-wrap gap-3 border-t border-line pt-6">
          <button className="btn-primary">
            <Save size={16} /> Save product
          </button>
          <Link to="/admin/products" className="btn-outline">
            Cancel
          </Link>
        </div>
      </form>
    </>
  );
}
