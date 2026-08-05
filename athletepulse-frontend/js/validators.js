// Validações e helpers de UI compartilhados por todos os formulários de autenticação.

function isEmailValido(email) {
  // Regex simples o suficiente pro front. Validação forte de verdade
  // sempre precisa acontecer no back-end também.
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.trim());
}

function setError(input, errorId, message) {
  input.classList.remove("input-success");
  input.classList.add("input-error");
  const errorEl = document.getElementById(errorId);
  if (errorEl) errorEl.innerText = message;
}

function setSuccess(input) {
  input.classList.remove("input-error");
  input.classList.add("input-success");
}

function clearFieldErrors(form) {
  form.querySelectorAll(".error").forEach((e) => (e.innerText = ""));
  form.querySelectorAll(".auth-input").forEach((i) => {
    i.classList.remove("input-error", "input-success");
  });
}

function showAlert(alertEl, message) {
  if (!alertEl) return;
  alertEl.innerText = message;
  alertEl.classList.add("show");
}

function hideAlert(alertEl) {
  if (!alertEl) return;
  alertEl.classList.remove("show");
  alertEl.innerText = "";
}

function setLoading(button, loading, textoNormal, textoCarregando) {
  button.disabled = loading;
  button.innerText = loading ? textoCarregando : textoNormal;
}
