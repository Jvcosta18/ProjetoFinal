const usuario = exigirAutenticacao("jogador");
document.getElementById("nomeUsuario").innerText = usuario.nome;

const NOMES_CANAL = { comissao: "Comissão Técnica", psicologo: "Psicologia" };

let canalAtivo = "comissao";
let intervaloPolling = null;
let ultimaQuantidade = 0;

const mensagensEl = document.getElementById("chatMensagens");
const tituloEl = document.getElementById("chatTitulo");
const form = document.getElementById("formMensagem");
const textoInput = document.getElementById("textoMensagem");
const btnEnviar = document.getElementById("btnEnviar");

function trocarCanal(canal) {
  canalAtivo = canal;
  ultimaQuantidade = 0;

  document.querySelectorAll(".chat-tab").forEach((btn) => {
    btn.classList.toggle("ativo", btn.dataset.canal === canal);
  });

  tituloEl.innerText = NOMES_CANAL[canal];
  mensagensEl.innerHTML = '<div class="vazio">Carregando...</div>';
  carregarMensagens();
}

document.querySelectorAll(".chat-tab").forEach((btn) => {
  btn.addEventListener("click", () => trocarCanal(btn.dataset.canal));
});

function formatarHora(iso) {
  return new Date(iso).toLocaleTimeString("pt-BR", { hour: "2-digit", minute: "2-digit" });
}

function renderMensagens(mensagens) {
  if (mensagens.length === 0) {
    mensagensEl.innerHTML = '<div class="vazio">Nenhuma mensagem ainda. Envie a primeira!</div>';
    return;
  }

  mensagensEl.innerHTML = mensagens
    .map(
      (m) => `
    <div class="bolha ${m.minhaMensagem ? "enviada" : "recebida"}">
      ${!m.minhaMensagem ? `<span class="bolha-autor">${m.autorNome}</span>` : ""}
      <span class="bolha-texto">${escaparHtml(m.texto)}</span>
      <span class="bolha-hora">${formatarHora(m.enviadaEm)}</span>
    </div>
  `
    )
    .join("");
}

function escaparHtml(texto) {
  const div = document.createElement("div");
  div.innerText = texto;
  return div.innerHTML;
}

async function carregarMensagens() {
  try {
    const resposta = await apiFetch(`/mensagens/meu/${canalAtivo}`);
    if (!resposta.ok) return;

    const mensagens = await resposta.json();

    // Só re-renderiza (e rola pro fim) se algo mudou - evita "piscar" a cada poll.
    if (mensagens.length !== ultimaQuantidade) {
      ultimaQuantidade = mensagens.length;
      renderMensagens(mensagens);
      mensagensEl.scrollTop = mensagensEl.scrollHeight;
    }
  } catch (err) {
    // apiFetch já trata sessão expirada.
  }
}

form.addEventListener("submit", async function (e) {
  e.preventDefault();

  const texto = textoInput.value.trim();
  if (!texto) return;

  btnEnviar.disabled = true;

  try {
    const resposta = await apiFetch(`/mensagens/meu/${canalAtivo}`, {
      method: "POST",
      body: JSON.stringify({ texto }),
    });

    if (resposta.ok) {
      textoInput.value = "";
      await carregarMensagens();
    }
  } catch (err) {
    // apiFetch já trata sessão expirada.
  } finally {
    btnEnviar.disabled = false;
    textoInput.focus();
  }
});

// Enter envia, Shift+Enter quebra linha.
textoInput.addEventListener("keydown", (e) => {
  if (e.key === "Enter" && !e.shiftKey) {
    e.preventDefault();
    form.requestSubmit();
  }
});

carregarMensagens();
intervaloPolling = setInterval(carregarMensagens, 4000);
window.addEventListener("beforeunload", () => clearInterval(intervaloPolling));
