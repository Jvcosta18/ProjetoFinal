// Roda em toda página interna (painel-*.html) para garantir que só
// usuários autenticados acessem, e redireciona pra tela de login certa
// se o token não existir (usuário não logado ou sessão expirada).

function exigirAutenticacao(perfilEsperado) {
  const token = sessionStorage.getItem("token");
  const perfil = sessionStorage.getItem("perfil");

  if (!token || perfil !== perfilEsperado) {
    window.location.href = "login.html";
    return null;
  }

  inicializarNotificacoes();

  return {
    token,
    perfil,
    id: sessionStorage.getItem("id"),
    nome: sessionStorage.getItem("nome") || "",
  };
}

// Igual a exigirAutenticacao, mas aceita qualquer perfil - usado em páginas
// compartilhadas por todos os tipos de usuário, como "Meu Perfil".
function exigirQualquerAutenticacao() {
  const token = sessionStorage.getItem("token");
  const perfil = sessionStorage.getItem("perfil");

  if (!token || !perfil) {
    window.location.href = "login.html";
    return null;
  }

  inicializarNotificacoes();

  return {
    token,
    perfil,
    id: sessionStorage.getItem("id"),
    nome: sessionStorage.getItem("nome") || "",
  };
}

// Devolve o usuário para a página inicial do seu próprio painel.
function paginaDoPainel(perfil) {
  const paginas = {
    jogador: "painel-jogador.html",
    comissao: "painel-comissao.html",
    psicologo: "painel-psicologo.html",
  };
  return paginas[perfil] || "login.html";
}

function fazerLogout() {
  sessionStorage.clear();
  window.location.href = "login.html";
}

// Wrapper de fetch que já injeta o header Authorization.
async function apiFetch(caminho, opcoes = {}) {
  const token = sessionStorage.getItem("token");

  const resposta = await fetch(`${API_BASE_URL}${caminho}`, {
    ...opcoes,
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`,
      ...(opcoes.headers || {}),
    },
  });

  // Token expirado/invalido - joga de volta pro login.
  if (resposta.status === 401) {
    sessionStorage.clear();
    window.location.href = "login.html";
    throw new Error("Sessão expirada. Faça login novamente.");
  }

  return resposta;
}

// =====================================================================
// NOTIFICAÇÕES - sininho injetado automaticamente em toda página interna
// que chama exigirAutenticacao/exigirQualquerAutenticacao, sem precisar
// editar o HTML de cada painel individualmente.
// =====================================================================

function inicializarNotificacoes() {
  const container = document.querySelector(".topbar-user");
  if (!container || document.getElementById("sininhoNotificacoes")) return;

  const wrapper = document.createElement("div");
  wrapper.className = "notif-wrapper";
  wrapper.innerHTML = `
    <button class="notif-sino" id="sininhoNotificacoes" title="Notificações" type="button">
      🔔<span class="notif-badge" id="notifBadge"></span>
    </button>
    <div class="notif-dropdown" id="notifDropdown">
      <div class="notif-dropdown-head">
        <span>Notificações</span>
        <button class="notif-marcar-todas" id="notifMarcarTodas" type="button">Marcar todas como lidas</button>
      </div>
      <div class="notif-lista" id="notifLista"><div class="vazio">Carregando...</div></div>
    </div>
  `;
  container.insertBefore(wrapper, container.firstChild);

  const sino = document.getElementById("sininhoNotificacoes");
  const dropdown = document.getElementById("notifDropdown");
  const badge = document.getElementById("notifBadge");
  const lista = document.getElementById("notifLista");

  sino.addEventListener("click", (e) => {
    e.stopPropagation();
    const aberto = dropdown.classList.toggle("show");
    if (aberto) carregarListaNotificacoes();
  });

  document.addEventListener("click", (e) => {
    if (!wrapper.contains(e.target)) dropdown.classList.remove("show");
  });

  document.getElementById("notifMarcarTodas").addEventListener("click", async (e) => {
    e.stopPropagation();
    try {
      await apiFetch("/notificacoes/lidas", { method: "PUT" });
      carregarContagem();
      carregarListaNotificacoes();
    } catch (err) {
      // apiFetch já trata sessão expirada.
    }
  });

  function formatarQuando(iso) {
    const data = new Date(iso);
    const diffMin = Math.floor((new Date() - data) / 60000);
    if (diffMin < 1) return "agora";
    if (diffMin < 60) return `${diffMin} min`;
    const diffH = Math.floor(diffMin / 60);
    if (diffH < 24) return `${diffH}h`;
    return data.toLocaleDateString("pt-BR", { day: "2-digit", month: "2-digit" });
  }

  async function carregarContagem() {
    try {
      const resposta = await apiFetch("/notificacoes/nao-lidas/contagem");
      if (!resposta.ok) return;

      const dados = await resposta.json();
      if (dados.naoLidas > 0) {
        badge.style.display = "flex";
        badge.innerText = dados.naoLidas > 9 ? "9+" : dados.naoLidas;
      } else {
        badge.style.display = "none";
      }
    } catch (err) {
      // apiFetch já trata sessão expirada.
    }
  }

  async function carregarListaNotificacoes() {
    try {
      const resposta = await apiFetch("/notificacoes");
      if (!resposta.ok) return;

      const notificacoes = await resposta.json();

      if (notificacoes.length === 0) {
        lista.innerHTML = '<div class="vazio">Nenhuma notificação ainda.</div>';
        return;
      }

      lista.innerHTML = notificacoes
        .map(
          (n) => `
        <div class="notif-item ${n.lida ? "" : "nao-lida"}" onclick="abrirNotificacao(${n.id}, '${n.link}')">
          <div class="notif-item-titulo">${n.titulo}</div>
          <div class="notif-item-msg">${n.mensagem}</div>
          <div class="notif-item-hora">${formatarQuando(n.criadaEm)}</div>
        </div>
      `
        )
        .join("");
    } catch (err) {
      // apiFetch já trata sessão expirada.
    }
  }

  window.abrirNotificacao = async function (id, link) {
    try {
      await apiFetch(`/notificacoes/${id}/lida`, { method: "PUT" });
    } catch (err) {
      // segue o clique mesmo se falhar ao marcar como lida.
    }
    window.location.href = link;
  };

  carregarContagem();
  setInterval(carregarContagem, 15000);
}
