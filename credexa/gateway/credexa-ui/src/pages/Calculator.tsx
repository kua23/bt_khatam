import { useState } from 'react';
import { calculatorApi } from '@/services/api';
import type { CalculatorResponse } from '@/types';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Button } from '@/components/ui/button';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Calculator, TrendingUp } from 'lucide-react';
import { Alert, AlertDescription } from '@/components/ui/alert';

export default function FDCalculator() {
  const [standaloneForm, setStandaloneForm] = useState({
    principalAmount: '',
    interestRate: '',
    termInMonths: '',
    compoundingFrequency: 'QUARTERLY',
  });

  const [productForm, setProductForm] = useState({
    productCode: '',
    principalAmount: '',
  });

  const [result, setResult] = useState<CalculatorResponse | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleStandaloneCalculate = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    try {
      const response = await calculatorApi.calculateStandalone({
        principalAmount: parseFloat(standaloneForm.principalAmount),
        interestRate: parseFloat(standaloneForm.interestRate),
        termInMonths: parseInt(standaloneForm.termInMonths),
        compoundingFrequency: standaloneForm.compoundingFrequency,
      });
      setResult(response.data);
    } catch (err) {
      setError('Failed to calculate. Please check your inputs.');
      console.error('Error calculating:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleProductCalculate = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    try {
      const response = await calculatorApi.calculateWithProduct(
        productForm.productCode,
        parseFloat(productForm.principalAmount)
      );
      setResult(response.data);
    } catch (err) {
      setError('Failed to calculate. Please check your inputs.');
      console.error('Error calculating:', err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="p-8 space-y-6">
      <div>
        <h1 className="text-3xl font-bold text-gray-900">FD Calculator</h1>
        <p className="text-gray-600 mt-2">Calculate your fixed deposit returns</p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Calculator size={20} />
              Calculate Returns
            </CardTitle>
            <CardDescription>Choose your calculation method</CardDescription>
          </CardHeader>
          <CardContent>
            <Tabs defaultValue="standalone">
              <TabsList className="grid w-full grid-cols-2">
                <TabsTrigger value="standalone">Custom Calculation</TabsTrigger>
                <TabsTrigger value="product">With Product</TabsTrigger>
              </TabsList>

              <TabsContent value="standalone" className="space-y-4 mt-4">
                <form onSubmit={handleStandaloneCalculate} className="space-y-4">
                  <div>
                    <Label htmlFor="principal">Principal Amount (₹)</Label>
                    <Input
                      id="principal"
                      type="number"
                      step="0.01"
                      value={standaloneForm.principalAmount}
                      onChange={(e) => setStandaloneForm({ ...standaloneForm, principalAmount: e.target.value })}
                      required
                      placeholder="e.g., 100000"
                    />
                  </div>

                  <div>
                    <Label htmlFor="rate">Interest Rate (%)</Label>
                    <Input
                      id="rate"
                      type="number"
                      step="0.01"
                      value={standaloneForm.interestRate}
                      onChange={(e) => setStandaloneForm({ ...standaloneForm, interestRate: e.target.value })}
                      required
                      placeholder="e.g., 6.5"
                    />
                  </div>

                  <div>
                    <Label htmlFor="term">Term (Months)</Label>
                    <Input
                      id="term"
                      type="number"
                      value={standaloneForm.termInMonths}
                      onChange={(e) => setStandaloneForm({ ...standaloneForm, termInMonths: e.target.value })}
                      required
                      placeholder="e.g., 12"
                    />
                  </div>

                  <div>
                    <Label htmlFor="frequency">Compounding Frequency</Label>
                    <Select
                      value={standaloneForm.compoundingFrequency}
                      onValueChange={(value) => setStandaloneForm({ ...standaloneForm, compoundingFrequency: value })}
                    >
                      <SelectTrigger>
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="MONTHLY">Monthly</SelectItem>
                        <SelectItem value="QUARTERLY">Quarterly</SelectItem>
                        <SelectItem value="HALF_YEARLY">Half Yearly</SelectItem>
                        <SelectItem value="YEARLY">Yearly</SelectItem>
                      </SelectContent>
                    </Select>
                  </div>

                  <Button type="submit" className="w-full" disabled={loading}>
                    {loading ? 'Calculating...' : 'Calculate'}
                  </Button>
                </form>
              </TabsContent>

              <TabsContent value="product" className="space-y-4 mt-4">
                <form onSubmit={handleProductCalculate} className="space-y-4">
                  <div>
                    <Label htmlFor="productCode">Product Code</Label>
                    <Input
                      id="productCode"
                      value={productForm.productCode}
                      onChange={(e) => setProductForm({ ...productForm, productCode: e.target.value })}
                      required
                      placeholder="e.g., FD-STD-6M"
                    />
                  </div>

                  <div>
                    <Label htmlFor="productPrincipal">Principal Amount (₹)</Label>
                    <Input
                      id="productPrincipal"
                      type="number"
                      step="0.01"
                      value={productForm.principalAmount}
                      onChange={(e) => setProductForm({ ...productForm, principalAmount: e.target.value })}
                      required
                      placeholder="e.g., 100000"
                    />
                  </div>

                  <Button type="submit" className="w-full" disabled={loading}>
                    {loading ? 'Calculating...' : 'Calculate'}
                  </Button>
                </form>
              </TabsContent>
            </Tabs>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <TrendingUp size={20} />
              Calculation Result
            </CardTitle>
            <CardDescription>Your FD maturity details</CardDescription>
          </CardHeader>
          <CardContent>
            {error && (
              <Alert variant="destructive" className="mb-4">
                <AlertDescription>{error}</AlertDescription>
              </Alert>
            )}

            {!result ? (
              <div className="text-center text-gray-500 py-12">
                Fill in the form and calculate to see results
              </div>
            ) : (
              <div className="space-y-4">
                <div className="p-4 bg-blue-50 rounded-lg">
                  <div className="text-sm text-gray-600">Principal Amount</div>
                  <div className="text-2xl font-bold text-gray-900">
                    ₹{result.principalAmount.toLocaleString()}
                  </div>
                </div>

                <div className="p-4 bg-green-50 rounded-lg">
                  <div className="text-sm text-gray-600">Interest Earned</div>
                  <div className="text-2xl font-bold text-green-600">
                    ₹{result.interestEarned.toLocaleString()}
                  </div>
                </div>

                <div className="p-4 bg-purple-50 rounded-lg">
                  <div className="text-sm text-gray-600">Maturity Amount</div>
                  <div className="text-2xl font-bold text-purple-600">
                    ₹{result.maturityAmount.toLocaleString()}
                  </div>
                </div>

                <div className="grid grid-cols-2 gap-4 pt-4 border-t">
                  <div>
                    <div className="text-sm text-gray-600">Interest Rate</div>
                    <div className="font-semibold">{result.interestRate}%</div>
                  </div>
                  <div>
                    <div className="text-sm text-gray-600">Term</div>
                    <div className="font-semibold">{result.termInMonths} months</div>
                  </div>
                  <div>
                    <div className="text-sm text-gray-600">Compounding</div>
                    <div className="font-semibold">{result.compoundingFrequency}</div>
                  </div>
                  <div>
                    <div className="text-sm text-gray-600">Effective Rate</div>
                    <div className="font-semibold">{result.effectiveInterestRate.toFixed(2)}%</div>
                  </div>
                </div>
              </div>
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
