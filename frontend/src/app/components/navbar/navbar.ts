import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <nav class="sticky top-0 z-[100] bg-black/80 backdrop-blur-2xl border-b border-white/10 px-8 py-4">
      <div class="max-w-7xl mx-auto flex items-center justify-between">
        <!-- Logo -->
        <div class="flex items-center gap-3 cursor-pointer" routerLink="/">
          <div class="w-10 h-10 bg-gradient-to-br from-cyan-400 to-emerald-500 rounded-xl flex items-center justify-center shadow-[0_0_20px_rgba(6,182,212,0.3)]">
            <span class="text-black font-black text-xl">T</span>
          </div>
          <div>
            <h1 class="text-white font-black tracking-tighter text-lg leading-none">TradeIntel <span class="text-cyan-400">AI</span></h1>
            <p class="text-[8px] text-gray-500 uppercase tracking-widest font-bold">Financial Intelligence Portal</p>
          </div>
        </div>

        <!-- Links -->
        <div class="hidden md:flex items-center gap-8">
          <a routerLink="/dashboard" routerLinkActive="text-white bg-white/5" class="text-gray-400 hover:text-white px-4 py-2 rounded-xl text-[11px] font-black uppercase tracking-widest transition-all">Dashboard</a>
          <a routerLink="/markets" routerLinkActive="text-white bg-white/5" class="text-gray-400 hover:text-white px-4 py-2 rounded-xl text-[11px] font-black uppercase tracking-widest transition-all">Markets</a>
          <a routerLink="/crypto" routerLinkActive="text-white bg-white/5" class="text-gray-400 hover:text-white px-4 py-2 rounded-xl text-[11px] font-black uppercase tracking-widest transition-all">Crypto</a>
          <a routerLink="/health" routerLinkActive="text-white bg-white/5" class="text-gray-400 hover:text-white px-4 py-2 rounded-xl text-[11px] font-black uppercase tracking-widest transition-all">Company Health</a>
        </div>

        <!-- Search & Profile -->
        <div class="flex items-center gap-4">
          <div class="hidden lg:flex items-center bg-white/5 border border-white/10 rounded-xl px-4 py-2 text-gray-500 hover:border-white/20 cursor-text group transition-all">
            <span class="text-sm">🔍</span>
            <input type="text" placeholder="Search Symbols..." class="bg-transparent border-0 text-[10px] w-32 focus:ring-0 placeholder:text-gray-600 font-bold uppercase tracking-widest">
            <span class="text-[8px] bg-white/10 px-2 py-1 rounded-md ml-2 group-hover:bg-white/20">CTRL + K</span>
          </div>
          <div class="h-10 w-10 rounded-full border border-white/10 bg-gradient-to-br from-gray-800 to-gray-900 flex items-center justify-center cursor-pointer hover:border-cyan-500/50 transition-all p-1">
            <div class="w-full h-full rounded-full bg-cyan-500 flex items-center justify-center text-black font-black text-xs">AA</div>
          </div>
        </div>
      </div>
    </nav>
  `,
  styles: [`
    :host { display: block; }
    .router-link-active {
      color: white !important;
      background: rgba(255, 255, 255, 0.05);
      border: 1px solid rgba(255, 255, 255, 0.1);
    }
  `]
})
export class NavbarComponent {}
