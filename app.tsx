import { useEffect, useRef, useState } from "react";
import { createGame, type GameHandle, type GameState } from "./game/script.js";
import { cn } from "./utils/cn";
const ENEMIES = [
  { name: "Drone", color: "#f97316", desc: "Desce em linha reta. Frágil." },
  { name: "Zigzag", color: "#a855f7", desc: "Oscila para os lados." },
  { name: "Atirador", color: "#ef4444", desc: "Para e mira em você." },
  { name: "Tanque", color: "#22c55e", desc: "Lento, mas muito resistente." },
  { name: "Kamikaze", color: "#facc15", desc: "Persegue sua nave." },
  { name: "Chefe", color: "#e11d48", desc: "A cada 5 ondas. 3 padrões de tiro." },
];
const POWERS = [
  { key: "T", name: "Tiro Triplo", color: "#38bdf8" },
  { key: "R", name: "Tiro Rápido", color: "#f472b6" },
  { key: "S", name: "Escudo", color: "#34d399" },
  { key: "H", name: "+1 Vida", color: "#f87171" },
  { key: "B", name: "Bomba", color: "#fbbf24" },
];
export default function App() {
  const canvasRef = useRef<HTMLCanvasElement>(null);
  const gameRef = useRef<GameHandle | null>(null);
  const [gs, setGs] = useState<GameState>({
    state: "menu",
    score: 0,
    highScore: 0,
    wave: 0,
    lives: 3,
    muted: false,
  });
  useEffect(() => {
    if (!canvasRef.current) return;
    const game = createGame(canvasRef.current, { onState: setGs });
    gameRef.current = game;
    return () => game.destroy();
  }, []);
  const statusLabel: Record<GameState["state"], string> = {
    menu: "No menu",
    playing: "Jogando",
    paused: "Pausado",
    gameover: "Fim de jogo",
  };
  return (
    <div className="min-h-screen bg-slate-950 text-slate-100">
      <div className="pointer-events-none fixed inset-0 bg-[radial-gradient(ellipse_at_top,rgba(99,102,241,0.18),transparent_55%),radial-gradient(ellipse_at_bottom_right,rgba(219,39,119,0.12),transparent_55%)]" />
      <header className="relative border-b border-slate-800/80">
        <div className="mx-auto flex max-w-6xl items-center justify-between gap-4 px-4 py-4 sm:px-6">
          <div className="flex items-center gap-3">
            <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-gradient-to-br from-sky-400 to-violet-600 shadow-lg shadow-violet-500/30">
              <svg className="h-6 w-6 text-white" viewBox="0 0 24 24" fill="currentColor">
                <path d="M12 2l5 14-5-3-5 3z" />
              </svg>
            </div>
            <div>
              <h1 className="text-lg font-bold tracking-tight">Nebula Defender</h1>
              <p className="text-xs text-slate-400">
                Jogo escrito em <code className="text-yellow-200">script.js</code> puro · Canvas 2D
              </p>
            </div>
          </div>
          <div className="flex items-center gap-2">
            <button
              onClick={() => gameRef.current?.toggleMute()}
              className="rounded-md border border-slate-700 bg-slate-900 px-3 py-1.5 text-xs text-slate-300 hover:border-slate-500 hover:text-white"
            >
              {gs.muted ? "🔇 Som off" : "🔊 Som on"}
            </button>
            <button
              onClick={() => gameRef.current?.togglePause()}
              disabled={gs.state === "menu" || gs.state === "gameover"}
              className="rounded-md border border-slate-700 bg-slate-900 px-3 py-1.5 text-xs text-slate-300 hover:border-slate-500 hover:text-white disabled:opacity-40"
            >
              {gs.state === "paused" ? "▶ Continuar" : "⏸ Pausar"}
            </button>
          </div>
        </div>
      </header>
      <main className="relative mx-auto grid max-w-6xl gap-6 px-4 py-6 sm:px-6 lg:grid-cols-[auto_1fr]">
        {/* Game */}
        <div className="flex flex-col items-center gap-3">
          <div className="relative rounded-2xl border border-slate-800 bg-slate-900/60 p-2 shadow-2xl shadow-indigo-950/60">
            <canvas
              ref={canvasRef}
              className="block h-auto w-full max-w-[480px] touch-none rounded-xl lg:h-[min(80vh,720px)] lg:w-auto"
              style={{ aspectRatio: "480 / 720" }}
            />
          </div>
          <div className="flex w-full max-w-[480px] items-center justify-between text-xs text-slate-400">
            <span>
              Status:{" "}
              <span
                className={cn(
                  "font-semibold",
                  gs.state === "playing" && "text-emerald-400",
                  gs.state === "paused" && "text-amber-300",
                  gs.state === "gameover" && "text-rose-400",
                )}
              >
                {statusLabel[gs.state]}
              </span>
            </span>
            {gs.state !== "playing" && (
              <button
                onClick={() => gameRef.current?.primaryAction()}
                className="rounded-md bg-gradient-to-r from-sky-500 to-violet-600 px-4 py-1.5 text-xs font-bold text-white shadow-lg shadow-violet-600/30 transition hover:brightness-110"
              >
                {gs.state === "paused" ? "Continuar" : gs.state === "gameover" ? "Jogar de novo" : "Começar"}
              </button>
            )}
          </div>
        </div>
        {/* Sidebar */}
        <aside className="flex flex-col gap-4">
          <div className="grid grid-cols-2 gap-3 sm:grid-cols-4">
            <Stat label="Pontuação" value={gs.score.toLocaleString("pt-BR")} accent="text-white" />
            <Stat label="Recorde" value={gs.highScore.toLocaleString("pt-BR")} accent="text-yellow-300" />
            <Stat label="Onda" value={String(gs.wave || "—")} accent="text-violet-300" />
            <Stat label="Vidas" value={"♥".repeat(gs.lives) || "—"} accent="text-sky-400" />
          </div>
          <Card title="Controles">
            <ul className="grid gap-2 text-sm text-slate-300 sm:grid-cols-2">
              <li><Kbd>W A S D</Kbd> ou <Kbd>← ↑ ↓ →</Kbd> mover</li>
              <li><Kbd>Espaço</Kbd> atirar (segure)</li>
              <li><Kbd>Enter</Kbd> começar / reiniciar</li>
              <li><Kbd>P</Kbd> ou <Kbd>Esc</Kbd> pausar</li>
              <li><Kbd>M</Kbd> ligar/desligar som</li>
              <li>📱 Toque e arraste: move e atira</li>
            </ul>
          </Card>
          <Card title="Inimigos">
            <ul className="grid gap-2 sm:grid-cols-2">
              {ENEMIES.map((e) => (
                <li key={e.name} className="flex items-start gap-2 text-sm">
                  <span
                    className="mt-1 h-3 w-3 shrink-0 rounded-full"
                    style={{ background: e.color, boxShadow: `0 0 10px ${e.color}` }}
                  />
                  <span>
                    <span className="font-semibold text-slate-100">{e.name}</span>
                    <span className="text-slate-400"> — {e.desc}</span>
                  </span>
                </li>
              ))}
            </ul>
          </Card>
          <Card title="Power-ups">
            <ul className="flex flex-wrap gap-2">
              {POWERS.map((p) => (
                <li
                  key={p.key}
                  className="flex items-center gap-2 rounded-lg border border-slate-800 bg-slate-950/60 px-2.5 py-1.5 text-sm"
                >
                  <span
                    className="flex h-6 w-6 items-center justify-center rounded font-mono text-xs font-black text-slate-950"
                    style={{ background: p.color }}
                  >
                    {p.key}
                  </span>
                  {p.name}
                </li>
              ))}
            </ul>
            <p className="mt-3 text-xs text-slate-500">
              Dica: abater inimigos em sequência aumenta o <span className="text-yellow-300">combo</span> (até x8).
              Levar dano zera o combo.
            </p>
          </Card>
        </aside>
      </main>
      <footer className="relative border-t border-slate-800/80 py-4 text-center text-xs text-slate-500">
        Toda a lógica do jogo está em <code className="text-yellow-200">src/game/script.js</code> — física, ondas,
        inimigos, chefes, partículas e som (Web Audio). O React apenas monta o canvas.
      </footer>
    </div>
  );
}
function Stat({ label, value, accent }: { label: string; value: string; accent: string }) {
  return (
    <div className="rounded-xl border border-slate-800 bg-slate-900/60 p-3">
      <div className="text-[11px] uppercase tracking-wider text-slate-500">{label}</div>
      <div className={cn("mt-1 truncate font-mono text-xl font-bold", accent)}>{value}</div>
    </div>
  );
}
function Card({ title, children }: { title: string; children: React.ReactNode }) {
  return (
    <div className="rounded-xl border border-slate-800 bg-slate-900/60 p-4">
      <h3 className="mb-3 text-xs font-semibold uppercase tracking-wider text-slate-400">{title}</h3>
      {children}
    </div>
  );
}
function Kbd({ children }: { children: React.ReactNode }) {
  return (
    <kbd className="rounded border border-slate-700 bg-slate-800 px-1.5 py-0.5 font-mono text-[11px] text-slate-200">
      {children}
    </kbd>
  );
}
