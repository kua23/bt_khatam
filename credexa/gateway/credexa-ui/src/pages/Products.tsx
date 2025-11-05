import { useEffect, useState } from 'react';
import { productApi } from '@/services/api';
import type { Product } from '@/types';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { Badge } from '@/components/ui/badge';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { Input } from '@/components/ui/input';
import { Search } from 'lucide-react';

export default function Products() {
  const [products, setProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [searchTerm, setSearchTerm] = useState('');

  useEffect(() => {
    fetchProducts();
  }, []);

  const fetchProducts = async () => {
    try {
      setLoading(true);
      setError(null);
      const response = await productApi.getAllProducts();
      setProducts(response.data);
    } catch (err) {
      setError('Failed to fetch products. Please ensure all services are running.');
      console.error('Error fetching products:', err);
    } finally {
      setLoading(false);
    }
  };

  const filteredProducts = products.filter((product) =>
    product.productName.toLowerCase().includes(searchTerm.toLowerCase()) ||
    product.productCode.toLowerCase().includes(searchTerm.toLowerCase())
  );

  if (loading) {
    return (
      <div className="flex items-center justify-center h-full">
        <div className="text-lg text-gray-600">Loading products...</div>
      </div>
    );
  }

  return (
    <div className="p-8 space-y-6">
      <div>
        <h1 className="text-3xl font-bold text-gray-900">Fixed Deposit Products</h1>
        <p className="text-gray-600 mt-2">Browse available FD products and their terms</p>
      </div>

      {error && (
        <Alert variant="destructive">
          <AlertDescription>{error}</AlertDescription>
        </Alert>
      )}

      <Card>
        <CardHeader>
          <CardTitle>Product Catalog</CardTitle>
          <div className="flex items-center gap-2 mt-4">
            <Search className="text-gray-400" size={20} />
            <Input
              placeholder="Search products..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="max-w-sm"
            />
          </div>
        </CardHeader>
        <CardContent>
          {filteredProducts.length === 0 ? (
            <div className="text-center text-gray-500 py-8">
              No products found
            </div>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Product Code</TableHead>
                  <TableHead>Product Name</TableHead>
                  <TableHead>Interest Rate</TableHead>
                  <TableHead>Term (Months)</TableHead>
                  <TableHead>Amount Range</TableHead>
                  <TableHead>Compounding</TableHead>
                  <TableHead>Status</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {filteredProducts.map((product) => (
                  <TableRow key={product.productId}>
                    <TableCell className="font-mono font-medium">{product.productCode}</TableCell>
                    <TableCell>{product.productName}</TableCell>
                    <TableCell className="font-semibold text-green-600">{product.interestRate}%</TableCell>
                    <TableCell>
                      {product.minTermMonths === product.maxTermMonths 
                        ? product.minTermMonths
                        : `${product.minTermMonths} - ${product.maxTermMonths}`}
                    </TableCell>
                    <TableCell>
                      ₹{product.minPrincipalAmount.toLocaleString()} - ₹{product.maxPrincipalAmount.toLocaleString()}
                    </TableCell>
                    <TableCell>{product.compoundingFrequency}</TableCell>
                    <TableCell>
                      <Badge variant={product.isActive ? 'default' : 'secondary'}>
                        {product.isActive ? 'Active' : 'Inactive'}
                      </Badge>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
