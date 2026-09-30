const usuario = exigirAutenticacao("medico");
document.getElementById("nomeUsuario").innerText = usuario.nome;

const STATUS_INFO = {
  sem_registro: { label: "Liberado", classe: "status-ok" },
  liberado_total: { label: "Liberado", classe: "status-ok" },
  liberado_com_restricao: { label: "Restrição", classe: "status-atencao" },
  em_recuperacao: { label: "Em recuperação", classe: "status-alerta" },
  em_avaliacao: { label: "Em avaliação", classe: "status-atencao" },
};

const ORDEM = { em_recuperacao: 0, em_avaliacao: 1, liberado_com_restricao: 2, liberado_total: 3, sem_registro: 4 };

async function carregarAtletas() {
  const grid = document.getElementById("gridAtletas");
  try {
    const resposta = await apiFetch("/medico/atletas");
    if (!resposta.ok) {
      grid.innerHTML = '<div class="vazio">Não foi possível carregar os atletas.</div>';
      return;
    }

    const atletas = await resposta.json();

    if (atletas.length === 0) {
      grid.innerHTML = '<div class="vazio">Nenhum atleta cadastrado ainda.</div>';
      return;
    }

    atletas.sort((a, b) => ORDEM[a.statusLiberacao] - ORDEM[b.statusLiberacao]);

    grid.className = "grid-elenco";
    grid.innerHTML = atletas
      .map((a) => {
        const info = STATUS_INFO[a.statusLiberacao] || STATUS_INFO.sem_registro;
        return `
        <div class="atleta-card clicavel" onclick="window.location.href='painel-medico-atleta.html?id=${a.id}'">
          <div class="atleta-card-head">
            <h3>${a.nome}</h3>
            <span class="status-badge ${info.classe}">${info.label}</span>
          </div>
          ${a.descricaoLesao ? `<p class="atleta-vazio">${a.descricaoLesao}</p>` : '<p class="atleta-vazio">Nenhuma lesão registrada.</p>'}
        </div>
      `;
      })
      .join("");
  } catch (err) {
    // apiFetch já trata sessão expirada.
  }
}

carregarAtletas();
