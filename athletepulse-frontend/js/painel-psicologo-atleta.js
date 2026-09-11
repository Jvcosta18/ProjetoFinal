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

const ESCALA_TEXTO = { 1: "Muito baixo", 2: "Baixo", 3: "Médio", 4: "Bom", 5: "Ótimo" };

function formatarDataCurta(isoDate) {
  const [ano, mes, dia] = isoDate.split("-");
  return `${dia}/${mes}`;
}

// ===== Gráfico de evolução emocional =====
async function carregarGraficoEmocional() {
  try {
    const [respAtletas, respHistorico] = await Promise.all([
      apiFetch("/psicologo/atletas"),
      apiFetch(`/psicologo/atletas/${atletaId}/emocional`),
    ]);

    if (!respHistorico.ok) return;

    const atletas = respAtletas.ok ? await respAtletas.json() : [];
    const atletaAtual = atletas.find((a) => String(a.id) === String(atletaId));

    if (atletaAtual) {
      document.getElementById("tituloPagina").innerText = `Histórico — ${atletaAtual.nome}`;

      if (atletaAtual.quedaConsecutiva) {
        const banner = document.getElementById("bannerAlerta");
        banner.classList.add("show");
        banner.innerText = "⚠ Este atleta está com estado emocional baixo há 3 dias seguidos. Pode ser um bom momento para uma conversa.";
      }
    }

    const historico = (await respHistorico.json()).slice(0, 14).reverse();

    if (historico.length === 0) {
      document.getElementById("blocoGrafico").style.display = "none";
      return;
    }

    const ctx = document.getElementById("graficoEmocional").getContext("2d");
    new Chart(ctx, {
      type: "line",
      data: {
        labels: historico.map((p) => formatarDataCurta(p.data)),
        datasets: [
          {
            label: "Estado emocional (1-5)",
            data: historico.map((p) => p.estadoEmocional),
            borderColor: "#2f5f4f",
            backgroundColor: "#2f5f4f",
            tension: 0.3,
          },
        ],
      },
      options: {
        responsive: true,
        scales: { y: { min: 1, max: 5, ticks: { stepSize: 1 } } },
        plugins: { legend: { display: false } },
      },
    });
  } catch (err) {
    // apiFetch já trata sessão expirada.
  }
}

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

carregarGraficoEmocional();
carregarNotas();
