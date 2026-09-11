const usuario = exigirAutenticacao("psicologo");

document.getElementById("nomeUsuario").innerText = usuario.nome;

const grid = document.getElementById("gridAtletas");
const resumo = document.getElementById("resumoAtletas");

const STATUS_INFO = {
  ok: { label: "Em dia", classe: "status-ok" },
  atencao: { label: "Atenção", classe: "status-atencao" },
  alerta: { label: "Alerta emocional", classe: "status-alerta" },
  sem_dado: { label: "Sem check-in hoje", classe: "status-vazio" },
};

const ESCALA_TEXTO = { 1: "Muito baixo", 2: "Baixo", 3: "Médio", 4: "Bom", 5: "Ótimo" };
const ORDEM_STATUS = { alerta: 0, atencao: 1, sem_dado: 2, ok: 3 };

async function carregarAtletas() {
  try {
    const resposta = await apiFetch("/psicologo/atletas");

    if (!resposta.ok) {
      grid.innerHTML = '<div class="vazio">Não foi possível carregar os atletas.</div>';
      return;
    }

    const atletas = await resposta.json();

    if (atletas.length === 0) {
      grid.innerHTML = '<div class="vazio">Nenhum atleta cadastrado ainda.</div>';
      resumo.innerText = "";
      return;
    }

    atletas.sort((a, b) => {
      const diff = ORDEM_STATUS[a.status] - ORDEM_STATUS[b.status];
      if (diff !== 0) return diff;
      return (b.quedaConsecutiva ? 1 : 0) - (a.quedaConsecutiva ? 1 : 0);
    });

    const alertas = atletas.filter((a) => a.status === "alerta").length;
    resumo.innerText = `${atletas.length} atletas · ${alertas} em alerta emocional`;

    grid.innerHTML = atletas.map(renderCard).join("");
  } catch (err) {
    // apiFetch já trata sessão expirada redirecionando.
  }
}

function renderCard(atleta) {
  const info = STATUS_INFO[atleta.status];

  const estadoTexto =
    atleta.ultimoEstadoEmocional != null
      ? `<p class="atleta-vazio">Estado emocional hoje: <strong>${ESCALA_TEXTO[atleta.ultimoEstadoEmocional]}</strong></p>`
      : `<p class="atleta-vazio">Nenhum check-in enviado ainda.</p>`;

  return `
    <div class="atleta-card clicavel" onclick="window.location.href='painel-psicologo-atleta.html?id=${atleta.id}'">
      <div class="atleta-card-head">
        <h3>${atleta.nome}</h3>
        <span class="status-badge ${info.classe}">${info.label}</span>
      </div>
      ${atleta.quedaConsecutiva ? '<div class="tag-consecutivo">⚠ Emocional baixo há 3 dias seguidos</div>' : ""}
      ${estadoTexto}
    </div>
  `;
}

carregarAtletas();
