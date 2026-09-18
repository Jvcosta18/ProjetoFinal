const form = document.getElementById("formCadastro");
const btnCadastrar = form.querySelector(".auth-button");
const alertEl = document.getElementById("alertCadastro");

const selectTipo = document.getElementById("tipo");
const blocoTemClube = document.getElementById("blocoTemClube");
const blocoToken = document.getElementById("blocoToken");
const blocoNomeClube = document.getElementById("blocoNomeClube");
const toggleTemClube = document.getElementById("toggleTemClube");

// null = ainda não escolheu; true = já tem clube (entra por código); false = vai criar um clube novo.
// Só é usado quando o perfil selecionado é "comissao".
let temClube = null;

// ===== Exibição dinâmica dos campos conforme o perfil =====
function atualizarCamposClube() {
  const tipo = selectTipo.value;

  blocoTemClube.classList.remove("show");
  blocoToken.classList.remove("show");
  blocoNomeClube.classList.remove("show");

  if (tipo === "jogador" || tipo === "psicologo") {
    // Atleta e psicólogo sempre entram num clube existente.
    blocoToken.classList.add("show");
    return;
  }

  if (tipo === "comissao") {
    blocoTemClube.classList.add("show");

    if (temClube === true) blocoToken.classList.add("show");
    if (temClube === false) blocoNomeClube.classList.add("show");
  }
}

selectTipo.addEventListener("change", () => {
  temClube = null;
  toggleTemClube.querySelectorAll("button").forEach((b) => b.classList.remove("selecionado"));
  atualizarCamposClube();
});

toggleTemClube.querySelectorAll("button").forEach((btn) => {
  btn.addEventListener("click", () => {
    toggleTemClube.querySelectorAll("button").forEach((b) => b.classList.remove("selecionado"));
    btn.classList.add("selecionado");
    temClube = btn.dataset.valor === "sim";
    atualizarCamposClube();
  });
});

// Deixa o código do clube sempre em maiúsculas enquanto digita.
document.getElementById("tokenClube").addEventListener("input", function () {
  this.value = this.value.toUpperCase();
});

// ===== Envio do cadastro =====
form.addEventListener("submit", async function (e) {
  e.preventDefault();
  hideAlert(alertEl);

  const nome = document.getElementById("nome");
  const email = document.getElementById("email");
  const senha = document.getElementById("senha");
  const confirmar = document.getElementById("confirmarSenha");
  const tokenClube = document.getElementById("tokenClube");
  const nomeClube = document.getElementById("nomeClube");

  clearFieldErrors(form);
  let valid = true;

  if (nome.value.trim().length < 3) {
    setError(nome, "erroNome", "Nome muito curto");
    valid = false;
  } else setSuccess(nome);

  if (!isEmailValido(email.value)) {
    setError(email, "erroEmail", "Email inválido");
    valid = false;
  } else setSuccess(email);

  if (selectTipo.value === "") {
    setError(selectTipo, "erroTipo", "Selecione um tipo");
    valid = false;
  } else setSuccess(selectTipo);

  // Validações específicas de clube
  const ehComissao = selectTipo.value === "comissao";
  const precisaToken =
    selectTipo.value === "jogador" || selectTipo.value === "psicologo" || (ehComissao && temClube === true);

  if (ehComissao && temClube === null) {
    document.getElementById("erroTemClube").innerText = "Escolha uma das opções";
    valid = false;
  } else {
    document.getElementById("erroTemClube").innerText = "";
  }

  if (precisaToken && tokenClube.value.trim().length === 0) {
    setError(tokenClube, "erroToken", "Informe o código do clube");
    valid = false;
  } else if (precisaToken) setSuccess(tokenClube);

  if (ehComissao && temClube === false && nomeClube.value.trim().length < 3) {
    setError(nomeClube, "erroNomeClube", "Informe o nome do clube");
    valid = false;
  } else if (ehComissao && temClube === false) setSuccess(nomeClube);

  if (senha.value.length < 6) {
    setError(senha, "erroSenha", "Senha deve ter pelo menos 6 caracteres");
    valid = false;
  } else setSuccess(senha);

  if (confirmar.value !== senha.value || confirmar.value === "") {
    setError(confirmar, "erroConfirmar", "Senhas não conferem");
    valid = false;
  } else setSuccess(confirmar);

  if (!valid) return;

  setLoading(btnCadastrar, true, "Cadastrar", "Enviando...");

  try {
    const response = await fetch(`${API_BASE_URL}/auth/registrar`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        nome: nome.value.trim(),
        email: email.value.trim(),
        tipo: selectTipo.value,
        senha: senha.value,
        temClube: ehComissao ? temClube : true,
        tokenClube: precisaToken ? tokenClube.value.trim().toUpperCase() : null,
        nomeClube: ehComissao && temClube === false ? nomeClube.value.trim() : null,
      }),
    });

    if (!response.ok) {
      const erro = await response.json().catch(() => null);
      throw new Error(erro?.mensagem || "Não foi possível concluir o cadastro.");
    }

    const dados = await response.json();

    // Clube novo criado: mostra o código gerado para a pessoa compartilhar.
    if (dados.tokenClube) {
      mostrarCodigoGerado(dados.nomeClube, dados.tokenClube);
      return;
    }

    // Entrou num clube existente: segue direto para o login.
    form.reset();
    clearFieldErrors(form);
    window.location.href = "login.html";
  } catch (err) {
    // Erro de rede (back-end fora do ar) cai aqui também.
    showAlert(alertEl, err.message || "Erro ao conectar com o servidor.");
  } finally {
    setLoading(btnCadastrar, false, "Cadastrar", "Enviando...");
  }
});

// ===== Tela de sucesso com o código do clube =====
function mostrarCodigoGerado(nomeDoClube, codigo) {
  document.getElementById("cardCadastro").style.display = "none";

  document.getElementById("nomeClubeCriado").innerText = nomeDoClube;
  document.getElementById("codigoGerado").innerText = codigo;
  document.getElementById("cardSucesso").classList.add("show");

  document.getElementById("btnCopiarCodigo").addEventListener("click", async function () {
    try {
      await navigator.clipboard.writeText(codigo);
      this.innerText = "Código copiado!";
      setTimeout(() => (this.innerText = "Copiar código"), 2000);
    } catch (err) {
      // Alguns navegadores bloqueiam a área de transferência; o código segue visível na tela.
      this.innerText = "Copie manualmente acima";
    }
  });
}
