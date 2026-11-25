// Texto que será exibido na animação
const texto = "Olá! Bem-vindo à animação de texto.";
const elemento = document.getElementById("texto-animado");

let indice = 0;
let apagando = false;

function animarTexto() {
    if (!apagando) {
        // Efeito de digitação: adiciona uma letra por vez
        elemento.textContent = texto.substring(0, indice + 1);
        indice++;

        if (indice === texto.length) {
            // Texto completo: pausa antes de começar a apagar
            apagando = true;
            setTimeout(animarTexto, 2000);
            return;
        }

        setTimeout(animarTexto, 100); // Velocidade de digitação (ms)
    } else {
        // Efeito de apagar: remove uma letra por vez
        elemento.textContent = texto.substring(0, indice - 1);
        indice--;

        if (indice === 0) {
            // Texto apagado: pausa antes de digitar novamente
            apagando = false;
            setTimeout(animarTexto, 500);
            return;
        }

        setTimeout(animarTexto, 50); // Velocidade ao apagar (ms)
    }
}

// Inicia a animação quando a página é carregada
document.addEventListener("DOMContentLoaded", animarTexto);