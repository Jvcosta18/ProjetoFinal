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
