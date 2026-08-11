const usuario = exigirAutenticacao("psicologo");
document.getElementById("nomeUsuario").innerText = usuario.nome;

const params = new URLSearchParams(window.location.search);
const atletaId = params.get("id");

if (!atletaId) {
  window.location.href = "painel-psicologo.html";
}

const listaNotas = document.getElementById("listaNotas");
const form = document.getElementById("formNota");
const btnSalvar = document.getElementById("btnSalvar");
const alertEl = document.getElementById("alertNota");

function formatarDataHora(iso) {
  const d = new Date(iso);
  return d.toLocaleString("pt-BR", { day: "2-digit", month: "2-digit", hour: "2-digit", minute: "2-digit" });
}

async function carregarNotas() {
  try {
    const resposta = await apiFetch(`/psicologo/atletas/${atletaId}/notas`);

    if (!resposta.ok) {
      listaNotas.innerHTML = '<div class="vazio">Não foi possível carregar as notas.</div>';
      return;
    }

    const notas = await resposta.json();

    if (notas.length === 0) {
      listaNotas.innerHTML = '<div class="vazio">Nenhuma nota registrada ainda.</div>';
      return;
    }

    listaNotas.innerHTML = notas
      .map(
        (n) => `
      <div class="nota">
        <div class="nota-head">
          <span>${n.autor}</span>
          <span>${formatarDataHora(n.criadoEm)}</span>
        </div>
        <div class="nota-texto">${n.texto}</div>
      </div>
    `
      )
      .join("");
  } catch (err) {
    // apiFetch já trata sessão expirada.
  }
}

form.addEventListener("submit", async function (e) {
  e.preventDefault();
  hideAlert(alertEl);

  const texto = document.getElementById("texto");

  if (!texto.value.trim()) {
    document.getElementById("erroTexto").innerText = "Escreva algo antes de salvar";
    return;
  }
  document.getElementById("erroTexto").innerText = "";

  setLoading(btnSalvar, true, "Salvar nota", "Salvando...");

  try {
    const resposta = await apiFetch(`/psicologo/atletas/${atletaId}/notas`, {
      method: "POST",
      body: JSON.stringify({ texto: texto.value.trim() }),
    });

    if (!resposta.ok) {
      const erro = await resposta.json().catch(() => null);
      throw new Error(erro?.mensagem || "Não foi possível salvar a nota.");
    }

    texto.value = "";
    carregarNotas();
  } catch (err) {
    showAlert(alertEl, err.message || "Erro ao conectar com o servidor.");
  } finally {
    setLoading(btnSalvar, false, "Salvar nota", "Salvando...");
  }
});

carregarNotas();
