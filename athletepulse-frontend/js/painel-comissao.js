const usuario = exigirAutenticacao("comissao");

document.getElementById("nomeUsuario").innerText = usuario.nome;

const grid = document.getElementById("gridElenco");
const resumo = document.getElementById("resumoElenco");

const STATUS_INFO = {
  ok: { label: "Em dia", classe: "status-ok" },
  atencao: { label: "Atenção", classe: "status-atencao" },
  alerta: { label: "Alerta de dor", classe: "status-alerta" },
  sem_checkin: { label: "Sem check-in hoje", classe: "status-vazio" },
};

const ESCALA_TEXTO = { 1: "Muito baixo", 2: "Baixo", 3: "Médio", 4: "Bom", 5: "Ótimo" };

// Ordena por prioridade: alerta > atencao > sem_checkin > ok
const ORDEM_STATUS = { alerta: 0, atencao: 1, sem_checkin: 2, ok: 3 };

async function carregarElenco() {
  try {
    const resposta = await apiFetch("/checkins/elenco");

    if (!resposta.ok) {
      grid.innerHTML = '<div class="vazio">Não foi possível carregar o elenco.</div>';
      return;
    }

    const atletas = await resposta.json();

    if (atletas.length === 0) {
      grid.innerHTML = '<div class="vazio">Nenhum atleta cadastrado ainda.</div>';
      resumo.innerText = "";
      return;
    }

    atletas.sort((a, b) => ORDEM_STATUS[a.status] - ORDEM_STATUS[b.status]);

    const alertas = atletas.filter((a) => a.status === "alerta").length;
    const atencoes = atletas.filter((a) => a.status === "atencao").length;
    const semCheckin = atletas.filter((a) => a.status === "sem_checkin").length;

    resumo.innerText = `${atletas.length} atletas · ${alertas} em alerta · ${atencoes} em atenção · ${semCheckin} sem check-in hoje`;

    grid.innerHTML = atletas.map(renderCard).join("");
  } catch (err) {
    // apiFetch já trata sessão expirada redirecionando.
  }
}

function renderCard(atleta) {
  const info = STATUS_INFO[atleta.status];
  const c = atleta.ultimoCheckin;

  const detalhes = c
    ? `
      <div class="atleta-metricas">
        <div><span class="metrica-label">Sono</span>${c.horasSono}h</div>
        <div><span class="metrica-label">Fadiga</span>${ESCALA_TEXTO[c.fadiga]}</div>
        <div><span class="metrica-label">Emocional</span>${ESCALA_TEXTO[c.estadoEmocional]}</div>
      </div>
      ${c.temDor ? `<div class="tag-dor">Dor · ${c.localDor || "não especificado"} (${c.intensidadeDor}/5)</div>` : ""}
    `
    : `<p class="atleta-vazio">Nenhum check-in enviado ainda.</p>`;

  return `
    <div class="atleta-card">
      <div class="atleta-card-head">
        <h3>${atleta.nome}</h3>
        <span class="status-badge ${info.classe}">${info.label}</span>
      </div>
      ${detalhes}
    </div>
  `;
}

carregarElenco();
