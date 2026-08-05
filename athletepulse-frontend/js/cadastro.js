const form = document.getElementById("formCadastro");
const btnCadastrar = form.querySelector(".auth-button");
const alertEl = document.getElementById("alertCadastro");

form.addEventListener("submit", async function (e) {
  e.preventDefault();
  hideAlert(alertEl);

  const nome = document.getElementById("nome");
  const email = document.getElementById("email");
  const tipo = document.getElementById("tipo");
  const senha = document.getElementById("senha");
  const confirmar = document.getElementById("confirmarSenha");

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

  if (tipo.value === "") {
    setError(tipo, "erroTipo", "Selecione um tipo");
    valid = false;
  } else setSuccess(tipo);

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
        tipo: tipo.value,
        senha: senha.value,
      }),
    });

    if (!response.ok) {
      const erro = await response.json().catch(() => null);
      throw new Error(erro?.mensagem || "Não foi possível concluir o cadastro.");
    }

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
