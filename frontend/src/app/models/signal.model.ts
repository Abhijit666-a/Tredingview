/**
 * TradeSignal model interface — matches the Spring Boot entity JSON.
 */
export interface TradeSignal {
  id: number;
  stock: {
    id: number;
    symbol: string;
    name: string;
    sector: string;
  };
  signalType: 'CALL' | 'PUT';
  patternName: string;
  entryPrice: number;
  targetPrice: number;
  stopLoss: number;
  confidenceScore: number;
  generatedAt: string;  
  lotSize: number;
  investmentRequired: number;
  potentialProfit: number;
  potentialLoss: number;
}
