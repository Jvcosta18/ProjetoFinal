// Funciona tanto pra comissão quanto pra psicólogo - o back-end decide o
// canal automaticamente pelo perfil de quem está logado.
const perfilAtual = sessionStorage.getItem("perfil");
const usuario = exigirAutenticacao(perfilAtual === "psicologo" ? "psicologo" : "comissao");

document.getElementById("nomeUsuario").innerText = usuario.nome;

const listaEl = document.getElementById("listaConversas");

function formatarQuando(iso) {
  const data = new Date(iso);
  const hoje = new Date();
  const mesmodia = data.toDateString() === hoje.toDateString();
  return mesmodia
    ? data.toLocaleTimeString("pt-BR", { hour: "2-digit", minute: "2-digit" })
    : data.toLocaleDateString("pt-BR", { day: "2-digit", month: "2-digit" });
}

async function carregarConversas() {
  try {
    const resposta = await apiFetch("/mensagens/conversas");
    if (!resposta.ok) {
      listaEl.innerHTML = '<div class="vazio">Não foi possível carregar as conversas.</div>';
      return;
    }

    const conversas = await resposta.json();

    if (conversas.length === 0) {
      listaEl.innerHTML = '<div class="vazio">Nenhum atleta cadastrado ainda.</div>';
      return;
    }

    // Quem mandou mensagem mais recente aparece primeiro; sem mensagem, por último.
    conversas.sort((a, b) => {
      if (a.semMensagens && b.semMensagens) return a.atletaNome.localeCompare(b.atletaNome);
      if (a.semMensagens) return 1;
      if (b.semMensagens) return -1;
      return new Date(b.ultimaMensagemEm) - new Date(a.ultimaMensagemEm);
    });

    listaEl.innerHTML = conversas
      .map((c) => {
        const preview = c.semMensagens
          ? '<span class="inbox-preview vazio-msg">Nenhuma mensagem ainda</span>'
          : `<span class="inbox-preview">${c.ultimaFoiDoAtleta ? "" : "Você: "}${escaparHtml(c.ultimaMensagem)}</span>`;

        return `
        <div class="inbox-row" onclick="window.location.href='painel-conversa.html?atletaId=${c.atletaId}'">
          <div>
            <div class="inbox-nome">${c.atletaNome}</div>
            ${preview}
          </div>
          ${c.ultimaMensagemEm ? `<div class="inbox-hora">${formatarQuando(c.ultimaMensagemEm)}</div>` : ""}
        </div>
      `;
      })
      .join("");
  } catch (err) {
    // apiFetch já trata sessão expirada.
  }
}

function escaparHtml(texto) {
  const div = document.createElement("div");
  div.innerText = texto;
  return div.innerHTML;
}

carregarConversas();
setInterval(carregarConversas, 6000);
