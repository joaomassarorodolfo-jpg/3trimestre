// ====== CONFIGURAÇÕES ======
const FRASE_INICIAL = "Oi, dev! Brinque comigo";
const CORES = ["#ffd166", "#ef476f", "#06d6a0", "#4cc9f0", "#f78c6b", "#c77dff"];
const GRAVIDADE = 0.6;
const QUICAR = 0.65;   // quanto da velocidade sobra ao bater
const ATRITO = 0.99;

const area = document.getElementById("area");
const dev = document.getElementById("dev");
const checkGravidade = document.getElementById("gravidade");
let letras = [];

function falar(msg) { dev.textContent = "Dev: " + msg; }

// ====== CRIAR LETRAS ======
function criarLetra(ch, x, y) {
  const el = document.createElement("div");
  el.className = "letra";
  el.textContent = ch;
  el.style.color = CORES[Math.floor(Math.random() * CORES.length)];
  area.appendChild(el);

  const l = { el, x, y, vx: 0, vy: 0, w: el.offsetWidth, h: el.offsetHeight, pegada: false };
  let offX = 0, offY = 0, inicioX = 0, inicioY = 0;

  el.addEventListener("pointerdown", (e) => {
    l.pegada = true;
    el.classList.add("pegada");
    el.setPointerCapture(e.pointerId);
    offX = e.clientX - l.x; offY = e.clientY - l.y;
    inicioX = e.clientX; inicioY = e.clientY;
    l.vx = 0; l.vy = 0;
  });

  el.addEventListener("pointermove", (e) => {
    if (!l.pegada) return;
    const nx = e.clientX - offX, ny = e.clientY - offY;
    l.vx = nx - l.x;   // a velocidade vira o "arremesso" ao soltar
    l.vy = ny - l.y;
    l.x = nx; l.y = ny;
  });

  const soltar = (e) => {
    if (!l.pegada) return;
    l.pegada = false;
    el.classList.remove("pegada");
    const moveu = Math.hypot(e.clientX - inicioX, e.clientY - inicioY);
    if (moveu < 4) {              // só um clique: dá um pulinho
      l.vy = -14;
      l.vx = (Math.random() - 0.5) * 10;
      falar("Ei, isso faz cócegas!");
    } else if (Math.hypot(l.vx, l.vy) > 25) {
      falar("Que arremesso!");
    }
  };
  el.addEventListener("pointerup", soltar);
  el.addEventListener("pointercancel", soltar);

  letras.push(l);
  return l;
}

function criarPalavra(texto, xInicial, y) {
  let x = xInicial;
  for (const ch of texto) {
    if (ch === " ") { x += 24; continue; }
    const l = criarLetra(ch, x, y);
    x += l.w + 4;
  }
}

function iniciar() {
  letras.forEach((l) => l.el.remove());
  letras = [];
  const largura = Math.min(window.innerWidth - 40, 700);
  const palavras = FRASE_INICIAL.split(" ");
  let x = 20, y = 90;
  for (const p of palavras) {
    const larguraPalavra = p.length * 40;
    if (x + larguraPalavra > largura) { x = 20; y += 70; }
    criarPalavra(p, x, y);
    x += larguraPalavra + 24;
  }
  falar("Pegue as letras, arraste e solte!");
}

// ====== LOOP DE FÍSICA ======
function atualizar() {
  const W = window.innerWidth, H = window.innerHeight;
  const g = checkGravidade.checked ? GRAVIDADE : 0;

  for (const l of letras) {
    if (l.pegada) {
      l.vx *= 0.8; l.vy *= 0.8;  // se parar de mexer, o arremesso some
    } else {
      l.vy += g;
      l.vx *= ATRITO;
      l.vy *= g ? 0.999 : ATRITO;
      l.x += l.vx;
      l.y += l.vy;
    }

    // paredes, teto e chão
    if (l.x < 0)          { l.x = 0;          l.vx *= -QUICAR; }
    if (l.x + l.w > W)    { l.x = W - l.w;    l.vx *= -QUICAR; }
    if (l.y < 0)          { l.y = 0;          l.vy *= -QUICAR; }
    if (l.y + l.h > H)    {
      l.y = H - l.h;
      l.vy *= -QUICAR;
      l.vx *= 0.95;                       // atrito no chão
      if (Math.abs(l.vy) < 1.5) l.vy = 0;  // para de quicar
    }
    l.el.style.transform = `translate(${l.x}px, ${l.y}px)`;
  }
  requestAnimationFrame(atualizar);
}

// ====== BOTÕES ======
function soltarPalavra() {
  const campo = document.getElementById("entrada");
  const texto = campo.value.trim();
  if (!texto) { falar("Digite uma palavra primeiro."); campo.focus(); return; }
  const x = Math.random() * Math.max(20, window.innerWidth - texto.length * 45);
  criarPalavra(texto, x, 70);
  campo.value = "";
  falar(`Lá vai "${texto}"!`);
}

document.getElementById("soltar").addEventListener("click", soltarPalavra);
document.getElementById("entrada").addEventListener("keydown", (e) => {
  if (e.key === "Enter") soltarPalavra();
});
document.getElementById("sacudir").addEventListener("click", () => {
  letras.forEach((l) => {
    l.vx = (Math.random() - 0.5) * 40;
    l.vy = -Math.random() * 30;
  });
  falar("Terremoto!");
});
document.getElementById("reiniciar").addEventListener("click", iniciar);
checkGravidade.addEventListener("change", () => {
  falar(checkGravidade.checked ? "A gravidade voltou." : "Agora estamos flutuando...");
});

iniciar();
atualizar();