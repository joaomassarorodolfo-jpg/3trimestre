export type GameStatus = "menu" | "playing" | "paused" | "gameover";
export interface GameState {
  state: GameStatus;
  score: number;
  highScore: number;
  wave: number;
  lives: number;
  muted: boolean;
}
export interface GameHandle {
  start(): void;
  togglePause(): void;
  primaryAction(): void;
  toggleMute(): void;
  destroy(): void;
}
export interface GameOptions {
  onState?: (s: GameState) => void;
}
export function createGame(canvas: HTMLCanvasElement, opts?: GameOptions): GameHandle;
