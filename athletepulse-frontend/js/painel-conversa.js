const perfilAtual = sessionStorage.getItem("perfil");
const usuario = exigirAutenticacao(perfilAtual === "psicologo" ? "psicologo" : "comissao");
document.getElementById("nomeUsuario").innerText = usuario.nome;

const params = new URLSearchParams(window.location.search);
const atletaId = params.get("atletaId");

if (!atletaId) {
  window.location.href = "painel-mensagens-equipe.html";
}

const mensagensEl = document.getElementById("chatMensagens");
const form = document.getElementById("formMensagem");
const textoInput = document.getElementById("textoMensagem");
const btnEnviar = document.getElementById("btnEnviar");

let ultimaQuantidade = 0;

function formatarHora(iso) {
  return new Date(iso).toLocaleTimeString("pt-BR", { hour: "2-digit", minute: "2-digit" });
}

function escaparHtml(texto) {
  const div = document.createElement("div");
  div.innerText = texto;
  return div.innerHTML;
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

async function carregarMensagens() {
  try {
    const resposta = await apiFetch(`/mensagens/atleta/${atletaId}`);
    if (!resposta.ok) return;

    const mensagens = await resposta.json();

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
    const resposta = await apiFetch(`/mensagens/atleta/${atletaId}`, {
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

textoInput.addEventListener("keydown", (e) => {
  if (e.key === "Enter" && !e.shiftKey) {
    e.preventDefault();
    form.requestSubmit();
  }
});

carregarMensagens();
setInterval(carregarMensagens, 4000);
