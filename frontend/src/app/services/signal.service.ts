import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, interval, switchMap, startWith, catchError, of } from 'rxjs';
import { TradeSignal } from '../models/signal.model';

/**
 * Service that fetches real-time trade signals from Spring Boot.
 * Uses absolute URLs to ensure 100% connectivity.
 */
@Injectable({ providedIn: 'root' })
export class SignalService {
  private readonly http = inject(HttpClient);
  
  // Base URL for API
  private readonly BASE_URL = 'http://localhost:8080/api';

  /**
   * Real-time polling: Fetches latest signals every 1 second.
   */
  getSignalsLive(): Observable<TradeSignal[]> {
    return interval(1000).pipe(
      startWith(0),
      switchMap(() => this.fetchSignals()),
    );
  }

  /**
   * MILLISECOND STREAM: Listens to Server-Sent Events from the Webhook.
   */
  streamSignalsRealtime(): Observable<TradeSignal> {
    return new Observable<TradeSignal>(observer => {
      const eventSource = new EventSource(`${this.BASE_URL}/signals/stream`);
      
      eventSource.addEventListener('trade-signal', (event: any) => {
        const signal = JSON.parse(event.data) as TradeSignal;
        observer.next(signal);
      });

      eventSource.onerror = (error) => {
        observer.error(error);
        eventSource.close();
      };

      return () => eventSource.close();
    });
  }

  /**
   * LIVE PRICE TICK STREAM: Listens for millisecond price updates.
   */
  streamPriceTicks(): Observable<any> {
    return new Observable<any>(observer => {
      const eventSource = new EventSource(`${this.BASE_URL}/signals/stream`);
      
      eventSource.addEventListener('price-tick', (event: any) => {
        const tick = JSON.parse(event.data);
        observer.next(tick);
      });

      eventSource.onerror = (error) => {
        observer.error(error);
        eventSource.close();
      };

      return () => eventSource.close();
    });
  }

  fetchSignals(): Observable<TradeSignal[]> {
    return this.http.get<TradeSignal[]>(`${this.BASE_URL}/signals`).pipe(
      catchError((err) => {
        console.error('❌ Connection Error to Analysis Engine:', err);
        return of([]);
      }),
    );
  }

  fetchNews(symbol: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.BASE_URL}/v1/news/stock/${symbol}`).pipe(
      catchError(() => of([]))
    );
  }

  fetchMonthlyReport(): Observable<any[]> {
    return this.http.get<any[]>(`${this.BASE_URL}/analysis/monthly-report`).pipe(
      catchError(() => of([]))
    );
  }

  fetchHealthData(symbol: string): Observable<any> {
    return this.http.get<any>(`${this.BASE_URL}/analysis/health/${symbol}`).pipe(
      catchError(() => of({}))
    );
  }

  fetchMarketQuotes(symbols: string[]): Observable<any[]> {
    const syms = symbols.join(',');
    return this.http.get<any[]>(`${this.BASE_URL}/analysis/quotes`, {
      params: { symbols: syms }
    }).pipe(
      catchError(() => of([]))
    );
  }

  submitSymbol(symbol: string): Observable<any> {
    return this.http.post(`${this.BASE_URL}/analysis/scan`, null, {
      params: { symbol: symbol }
    });
  }

  fetchOptionChain(symbol: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.BASE_URL}/analysis/option-chain`, {
      params: { symbol: symbol }
    });
  }
}
