const usuario = exigirAutenticacao("medico");
document.getElementById("nomeUsuario").innerText = usuario.nome;

const params = new URLSearchParams(window.location.search);
const atletaId = params.get("id");
if (!atletaId) window.location.href = "painel-medico.html";

const GRAVIDADE_LABEL = { leve: "Leve", moderada: "Moderada", grave: "Grave" };
const STATUS_LABEL = {
  em_avaliacao: "Em avaliação",
  em_recuperacao: "Em recuperação",
  liberado_com_restricao: "Liberado com restrição",
  liberado_total: "Liberado total",
};

function formatarData(iso) {
  if (!iso) return "-";
  const [ano, mes, dia] = iso.split("-");
  return `${dia}/${mes}/${ano}`;
}

async function carregarNomeAtleta() {
  try {
    const resposta = await apiFetch("/medico/atletas");
    if (!resposta.ok) return;
    const atletas = await resposta.json();
    const atual = atletas.find((a) => String(a.id) === String(atletaId));
    if (atual) document.getElementById("nomeAtleta").innerText = atual.nome;
  } catch (err) {
    // apiFetch já trata sessão expirada.
  }
}

async function carregarHistorico() {
  const lista = document.getElementById("listaLesoes");
  try {
    const resposta = await apiFetch(`/medico/atletas/${atletaId}/lesoes`);
    if (!resposta.ok) return;

    const lesoes = await resposta.json();

    if (lesoes.length === 0) {
      lista.innerHTML = '<div class="vazio">Nenhum registro ainda.</div>';
      return;
    }

    lista.innerHTML = lesoes
      .map(
        (l) => `
      <div class="nota">
        <div class="nota-head">
          <span>${l.medicoNome} · ${GRAVIDADE_LABEL[l.gravidade]}</span>
          <span>${formatarData(l.dataOcorrencia)}</span>
        </div>
        <div class="nota-texto"><strong>${l.descricao}</strong></div>
        <div class="nota-texto">Status: ${STATUS_LABEL[l.status]}${l.previsaoRetorno ? ` · Retorno previsto: ${formatarData(l.previsaoRetorno)}` : ""}</div>
        ${l.observacoes ? `<div class="nota-texto">${l.observacoes}</div>` : ""}
      </div>
    `
      )
      .join("");
  } catch (err) {
    // apiFetch já trata sessão expirada.
  }
}

document.getElementById("formLesao").addEventListener("submit", async function (e) {
  e.preventDefault();
  hideAlert(document.getElementById("alertLesao"));

  const descricao = document.getElementById("descricao");
  if (!descricao.value.trim()) {
    setError(descricao, "erroDescricao", "Descreva a lesão");
    return;
  }
  setSuccess(descricao);

  const btn = document.getElementById("btnSalvar");
  setLoading(btn, true, "Salvar avaliação", "Salvando...");

  try {
    const resposta = await apiFetch(`/medico/atletas/${atletaId}/lesoes`, {
      method: "POST",
      body: JSON.stringify({
        descricao: descricao.value.trim(),
        gravidade: document.getElementById("gravidade").value,
        status: document.getElementById("status").value,
        previsaoRetorno: document.getElementById("previsaoRetorno").value || null,
        observacoes: document.getElementById("observacoes").value.trim() || null,
      }),
    });

    if (!resposta.ok) {
      const erro = await resposta.json().catch(() => null);
      throw new Error(erro?.mensagem || "Não foi possível salvar.");
    }

    document.getElementById("formLesao").reset();
    carregarHistorico();
  } catch (err) {
    showAlert(document.getElementById("alertLesao"), err.message || "Erro ao conectar com o servidor.");
  } finally {
    setLoading(btn, false, "Salvar avaliação", "Salvando...");
  }
});

carregarNomeAtleta();
carregarHistorico();
