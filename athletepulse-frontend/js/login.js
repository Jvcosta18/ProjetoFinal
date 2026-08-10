const form = document.getElementById("formLogin");
const btnEntrar = form.querySelector(".auth-button");
const alertEl = document.getElementById("alertLogin");
const perfil = form.dataset.role; // "jogador" ou "comissao", definido no HTML

form.addEventListener("submit", async function (e) {
  e.preventDefault();
  hideAlert(alertEl);

  const email = document.getElementById("email");
  const senha = document.getElementById("senha");

  clearFieldErrors(form);
  let valid = true;

  if (!isEmailValido(email.value)) {
    setError(email, "erroEmail", "Email inválido");
    valid = false;
  } else setSuccess(email);

  if (senha.value.length === 0) {
    setError(senha, "erroSenha", "Informe sua senha");
    valid = false;
  } else setSuccess(senha);

  if (!valid) return;

  setLoading(btnEntrar, true, "Entrar", "Entrando...");

  try {
    const response = await fetch(`${API_BASE_URL}/auth/login`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        email: email.value.trim(),
        senha: senha.value,
        perfil,
      }),
    });

    if (!response.ok) {
      // Mensagem genérica de propósito: não revelar se o email existe ou não.
      throw new Error("Email ou senha inválidos.");
    }

    const dados = await response.json();

    // Token guardado em memória de sessão do navegador.
    // OBS: para produção, o ideal é o back-end emitir um cookie httpOnly
    // (mais seguro contra XSS do que localStorage) — ajustamos isso
    // quando integrarmos o Spring Security de verdade.
    sessionStorage.setItem("token", dados.token);
    sessionStorage.setItem("perfil", perfil);
    sessionStorage.setItem("nome", dados.nome);

    const paginas = {
      jogador: "painel-jogador.html",
      comissao: "painel-comissao.html",
      psicologo: "painel-psicologo.html",
    };
    window.location.href = paginas[perfil] || "login.html";
  } catch (err) {
    showAlert(alertEl, err.message || "Erro ao conectar com o servidor.");
  } finally {
    setLoading(btnEntrar, false, "Entrar", "Entrando...");
  }
});
