const usuario = exigirAutenticacao("comissao");
document.getElementById("nomeUsuario").innerText = usuario.nome;

const params = new URLSearchParams(window.location.search);
const atletaId = params.get("id");

if (!atletaId) {
  window.location.href = "painel-comissao.html";
}

const ESCALA_TEXTO = { 1: "Muito baixo", 2: "Baixo", 3: "Médio", 4: "Bom", 5: "Ótimo" };
const CORES = { sono: "#3e7a64", fadiga: "#e4a33b", emocional: "#2f5f4f", dor: "#c1443a" };

let graficoSono, graficoBemEstar;

function formatarDataCurta(isoDate) {
  const [ano, mes, dia] = isoDate.split("-");
  return `${dia}/${mes}`;
}

async function carregarHistorico() {
  try {
    const [respAtleta, respHistorico] = await Promise.all([
      apiFetch("/checkins/elenco"),
      apiFetch(`/checkins/atleta/${atletaId}`),
    ]);

    if (!respHistorico.ok) {
      document.getElementById("conteudo").innerHTML = '<div class="vazio">Não foi possível carregar o histórico.</div>';
      return;
    }

    const elenco = respAtleta.ok ? await respAtleta.json() : [];
    const atletaResumo = elenco.find((a) => String(a.id) === String(atletaId));
    const historico = (await respHistorico.json()).slice(0, 14).reverse(); // últimos 14, do mais antigo pro mais novo

    document.getElementById("nomeAtleta").innerText = atletaResumo ? atletaResumo.nome : "Atleta";

    if (atletaResumo && atletaResumo.alertaConsecutivo) {
      const banner = document.getElementById("bannerAlerta");
      banner.classList.add("show");
      banner.innerText = "⚠ Este atleta está em atenção/alerta há 3 dias seguidos. Considere priorizar avaliação médica ou treino de recuperação.";
    }

    if (historico.length === 0) {
      document.getElementById("conteudo").innerHTML = '<div class="vazio">Nenhum check-in enviado ainda por este atleta.</div>';
      return;
    }

    montarConteudo(historico);
  } catch (err) {
    // apiFetch já trata sessão expirada.
  }
}

function montarConteudo(historico) {
  const linhas = historico
    .slice()
    .reverse() // mais recente primeiro na tabela
    .map(
      (c) => `
    <div class="historico-row">
      <div class="data">${formatarDataCurta(c.dataCheckin)}</div>
      <div><span class="metrica-label">Sono</span>${c.horasSono}h</div>
      <div><span class="metrica-label">Fadiga</span>${ESCALA_TEXTO[c.fadiga]}</div>
      <div><span class="metrica-label">Emocional</span>${ESCALA_TEXTO[c.estadoEmocional]}</div>
      <div>${c.temDor ? `<span class="tag-dor">Dor · ${c.localDor || "não especificado"}</span>` : '<span class="tag-ok">Sem dor</span>'}</div>
    </div>
  `
    )
    .join("");

  document.getElementById("conteudo").innerHTML = `
    <div class="painel-card">
      <h2>Sono</h2>
      <span class="sub">Últimos ${historico.length} dias</span>
      <canvas id="graficoSono" height="90"></canvas>
    </div>

    <div class="painel-card">
      <h2>Fadiga e estado emocional</h2>
      <span class="sub">Últimos ${historico.length} dias</span>
      <canvas id="graficoBemEstar" height="90"></canvas>
    </div>

    <div class="painel-card">
      <h2>Histórico detalhado</h2>
      <span class="sub">Todos os check-ins</span>
      ${linhas}
    </div>
  `;

  renderGraficos(historico);
}

function renderGraficos(historico) {
  const labels = historico.map((c) => formatarDataCurta(c.dataCheckin));

  const ctxSono = document.getElementById("graficoSono").getContext("2d");
  graficoSono = new Chart(ctxSono, {
    type: "line",
    data: {
      labels,
      datasets: [
        {
          label: "Horas de sono",
          data: historico.map((c) => c.horasSono),
          borderColor: CORES.sono,
          backgroundColor: CORES.sono,
          tension: 0.3,
        },
      ],
    },
    options: {
      responsive: true,
      scales: { y: { min: 0, max: 12, title: { display: true, text: "Horas" } } },
      plugins: { legend: { display: false } },
    },
  });

  const ctxBemEstar = document.getElementById("graficoBemEstar").getContext("2d");
  graficoBemEstar = new Chart(ctxBemEstar, {
    type: "line",
    data: {
      labels,
      datasets: [
        {
          label: "Fadiga (1-5)",
          data: historico.map((c) => c.fadiga),
          borderColor: CORES.fadiga,
          backgroundColor: CORES.fadiga,
          tension: 0.3,
        },
        {
          label: "Estado emocional (1-5)",
          data: historico.map((c) => c.estadoEmocional),
          borderColor: CORES.emocional,
          backgroundColor: CORES.emocional,
          tension: 0.3,
        },
      ],
    },
    options: {
      responsive: true,
      scales: { y: { min: 1, max: 5, ticks: { stepSize: 1 } } },
      plugins: { legend: { position: "bottom" } },
    },
  });
}

carregarHistorico();
