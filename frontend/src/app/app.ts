import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { NavbarComponent } from './components/navbar/navbar';
import { TickerComponent } from './components/ticker/ticker';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, NavbarComponent, TickerComponent],
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App {}
