const usuario = exigirQualquerAutenticacao();
document.getElementById("nomeUsuario").innerText = usuario.nome;
document.getElementById("linkVoltar").href = paginaDoPainel(usuario.perfil);

const ROTULO_PERFIL = { jogador: "Jogador", comissao: "Comissão Técnica", psicologo: "Psicóloga(o)" };

// ===== Carregar dados atuais =====
async function carregarPerfil() {
  try {
    const resposta = await apiFetch("/usuarios/me");
    if (!resposta.ok) return;

    const dados = await resposta.json();
    document.getElementById("nomeAtual").value = dados.nome;
    document.getElementById("emailAtual").innerText = dados.email;
    document.getElementById("perfilAtual").innerText = ROTULO_PERFIL[dados.tipo] || dados.tipo;
  } catch (err) {
    // apiFetch já trata sessão expirada.
  }
}

// ===== Atualizar nome =====
const formNome = document.getElementById("formNome");
const btnSalvarNome = document.getElementById("btnSalvarNome");
const alertNome = document.getElementById("alertNome");

formNome.addEventListener("submit", async function (e) {
  e.preventDefault();
  hideAlert(alertNome);

  const nomeInput = document.getElementById("nomeAtual");
  clearFieldErrors(formNome);

  if (nomeInput.value.trim().length < 3) {
    setError(nomeInput, "erroNome", "Nome muito curto");
    return;
  }
  setSuccess(nomeInput);

  setLoading(btnSalvarNome, true, "Salvar nome", "Salvando...");

  try {
    const resposta = await apiFetch("/usuarios/me", {
      method: "PUT",
      body: JSON.stringify({ nome: nomeInput.value.trim() }),
    });

    if (!resposta.ok) {
      const erro = await resposta.json().catch(() => null);
      throw new Error(erro?.mensagem || "Não foi possível salvar o nome.");
    }

    const atualizado = await resposta.json();
    sessionStorage.setItem("nome", atualizado.nome);
    document.getElementById("nomeUsuario").innerText = atualizado.nome;
    showAlert(alertNome, "Nome atualizado com sucesso.");
    alertNome.classList.add("sucesso");
  } catch (err) {
    alertNome.classList.remove("sucesso");
    showAlert(alertNome, err.message || "Erro ao conectar com o servidor.");
  } finally {
    setLoading(btnSalvarNome, false, "Salvar nome", "Salvando...");
  }
});

// ===== Trocar senha =====
const formSenha = document.getElementById("formSenha");
const btnSalvarSenha = document.getElementById("btnSalvarSenha");
const alertSenha = document.getElementById("alertSenha");

formSenha.addEventListener("submit", async function (e) {
  e.preventDefault();
  hideAlert(alertSenha);
  alertSenha.classList.remove("sucesso");

  const senhaAtual = document.getElementById("senhaAtual");
  const novaSenha = document.getElementById("novaSenha");
  const confirmarSenha = document.getElementById("confirmarNovaSenha");

  clearFieldErrors(formSenha);
  let valid = true;

  if (!senhaAtual.value) {
    setError(senhaAtual, "erroSenhaAtual", "Informe sua senha atual");
    valid = false;
  } else setSuccess(senhaAtual);

  if (novaSenha.value.length < 6) {
    setError(novaSenha, "erroNovaSenha", "Nova senha deve ter pelo menos 6 caracteres");
    valid = false;
  } else setSuccess(novaSenha);

  if (confirmarSenha.value !== novaSenha.value || confirmarSenha.value === "") {
    setError(confirmarSenha, "erroConfirmarSenha", "Senhas não conferem");
    valid = false;
  } else setSuccess(confirmarSenha);

  if (!valid) return;

  setLoading(btnSalvarSenha, true, "Trocar senha", "Salvando...");

  try {
    const resposta = await apiFetch("/usuarios/me/senha", {
      method: "PUT",
      body: JSON.stringify({ senhaAtual: senhaAtual.value, novaSenha: novaSenha.value }),
    });

    if (!resposta.ok) {
      const erro = await resposta.json().catch(() => null);
      throw new Error(erro?.mensagem || "Não foi possível trocar a senha.");
    }

    formSenha.reset();
    clearFieldErrors(formSenha);
    alertSenha.classList.add("sucesso");
    showAlert(alertSenha, "Senha alterada com sucesso.");
  } catch (err) {
    alertSenha.classList.remove("sucesso");
    showAlert(alertSenha, err.message || "Erro ao conectar com o servidor.");
  } finally {
    setLoading(btnSalvarSenha, false, "Trocar senha", "Salvando...");
  }
});

carregarPerfil();
