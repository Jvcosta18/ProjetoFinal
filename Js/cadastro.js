const form = document.getElementById("formCadastro");

form.addEventListener("submit", function (e) {
  e.preventDefault();

  // inputs
  const nome = document.getElementById("nome");
  const email = document.getElementById("email");
  const tipo = document.getElementById("tipo");
  const senha = document.getElementById("senha");
  const confirmar = document.getElementById("confirmarSenha");

  let valid = true;

  // limpar erros
  clearErrors();

  // ===== NOME =====
  if (nome.value.trim().length < 3) {
    setError(nome, "erroNome", "Nome muito curto");
    valid = false;
  } else setSuccess(nome);

  // ===== EMAIL =====
  if (!email.value.includes("@")) {
    setError(email, "erroEmail", "Email inválido");
    valid = false;
  } else setSuccess(email);

  // ===== TIPO =====
  if (tipo.value === "") {
    setError(tipo, "erroTipo", "Selecione um tipo");
    valid = false;
  } else setSuccess(tipo);

  // ===== SENHA =====
  if (senha.value.length < 6) {
    setError(senha, "erroSenha", "Senha deve ter pelo menos 6 caracteres");
    valid = false;
  } else setSuccess(senha);

  // ===== CONFIRMAR SENHA =====
  if (confirmar.value !== senha.value || confirmar.value === "") {
    setError(confirmar, "erroConfirmar", "Senhas não conferem");
    valid = false;
  } else setSuccess(confirmar);

  // ===== SUCESSO =====
  if (valid) {
    alert("Cadastro realizado com sucesso!");
    form.reset();
  }
});

function setError(input, errorId, message) {
  input.classList.add("input-error");
  document.getElementById(errorId).innerText = message;
}

function setSuccess(input) {
  input.classList.remove("input-error");
  input.classList.add("input-success");
}

function clearErrors() {
  document.querySelectorAll(".error").forEach(e => e.innerText = "");
  document.querySelectorAll(".auth-input").forEach(i => {
    i.classList.remove("input-error", "input-success");
  });
}