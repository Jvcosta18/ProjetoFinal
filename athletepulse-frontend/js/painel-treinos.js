const usuario = exigirAutenticacao("comissao");
document.getElementById("nomeUsuario").innerText = usuario.nome;

const STATUS_LABEL = { ok: "Em dia", atencao: "Atenção", alerta: "Alerta", sem_checkin: "Sem check-in" };
const STATUS_CLASSE = { ok: "status-ok", atencao: "status-atencao", alerta: "status-alerta", sem_checkin: "status-vazio" };
const INTENSIDADE_LABEL = { recuperacao: "Recuperação", leve: "Leve", moderada: "Moderada", intensa: "Intensa" };

let catalogo = [];
let elenco = [];
let editandoId = null; // null = criando novo treino; caso contrário, id do treino em edição

// ===== Catálogo =====
const formTreino = document.getElementById("formTreino");
const btnCriarTreino = document.getElementById("btnCriarTreino");
const btnCancelarEdicao = document.getElementById("btnCancelarEdicao");
const alertTreino = document.getElementById("alertTreino");
const listaCatalogo = document.getElementById("listaCatalogo");
const tituloFormTreino = document.getElementById("tituloFormTreino");

async function carregarCatalogo() {
  try {
    const resposta = await apiFetch("/treinos/catalogo");
    if (!resposta.ok) return;

    catalogo = await resposta.json();
    renderCatalogo();
    renderAtribuicoes(); // os selects de treino dependem do catálogo
  } catch (err) {
    // apiFetch já trata sessão expirada.
  }
}

function renderCatalogo() {
  if (catalogo.length === 0) {
    listaCatalogo.innerHTML = '<div class="vazio">Nenhum treino cadastrado ainda.</div>';
    return;
  }

  listaCatalogo.innerHTML = catalogo
    .map(
      (t) => `
    <div class="catalogo-item">
      <div>
        <span class="tag-intensidade ${t.intensidade}">${INTENSIDADE_LABEL[t.intensidade]}</span>
        <div class="treino-titulo">${t.titulo}</div>
        <div class="treino-descricao">${t.descricao}</div>
      </div>
      <div class="catalogo-acoes">
        <button class="btn-secundario" onclick="editarTreino(${t.id})">Editar</button>
        <button class="btn-excluir" onclick="excluirTreino(${t.id})">Excluir</button>
      </div>
    </div>
  `
    )
    .join("");
}

function editarTreino(id) {
  const treino = catalogo.find((t) => t.id === id);
  if (!treino) return;

  editandoId = id;
  document.getElementById("tituloTreino").value = treino.titulo;
  document.getElementById("descricaoTreino").value = treino.descricao;
  document.getElementById("intensidadeTreino").value = treino.intensidade;

  tituloFormTreino.innerText = "Editar treino";
  btnCriarTreino.innerText = "Salvar alterações";
  btnCancelarEdicao.style.display = "inline-block";

  formTreino.scrollIntoView({ behavior: "smooth", block: "center" });
}

function cancelarEdicao() {
  editandoId = null;
  formTreino.reset();
  clearFieldErrors(formTreino);
  tituloFormTreino.innerText = "Novo treino no catálogo";
  btnCriarTreino.innerText = "Adicionar ao catálogo";
  btnCancelarEdicao.style.display = "none";
}

async function excluirTreino(id) {
  const treino = catalogo.find((t) => t.id === id);
  if (!treino) return;

  if (!confirm(`Excluir o treino "${treino.titulo}"? Essa ação não pode ser desfeita.`)) {
    return;
  }

  try {
    const resposta = await apiFetch(`/treinos/catalogo/${id}`, { method: "DELETE" });

    if (!resposta.ok) {
      const erro = await resposta.json().catch(() => null);
      throw new Error(erro?.mensagem || "Não foi possível excluir o treino.");
    }

    if (editandoId === id) cancelarEdicao();
    carregarCatalogo();
  } catch (err) {
    alert(err.message || "Erro ao conectar com o servidor.");
  }
}

formTreino.addEventListener("submit", async function (e) {
  e.preventDefault();
  hideAlert(alertTreino);

  const titulo = document.getElementById("tituloTreino");
  const descricao = document.getElementById("descricaoTreino");
  const intensidade = document.getElementById("intensidadeTreino");

  clearFieldErrors(formTreino);
  let valid = true;

  if (!titulo.value.trim()) {
    setError(titulo, "erroTitulo", "Informe o título");
    valid = false;
  } else setSuccess(titulo);

  if (!descricao.value.trim()) {
    setError(descricao, "erroDescricao", "Informe a descrição");
    valid = false;
  } else setSuccess(descricao);

  if (!valid) return;

  const criando = editandoId === null;
  setLoading(btnCriarTreino, true, criando ? "Adicionar ao catálogo" : "Salvar alterações", "Salvando...");

  try {
    const resposta = await apiFetch(
      criando ? "/treinos/catalogo" : `/treinos/catalogo/${editandoId}`,
      {
        method: criando ? "POST" : "PUT",
        body: JSON.stringify({
          titulo: titulo.value.trim(),
          descricao: descricao.value.trim(),
          intensidade: intensidade.value,
        }),
      }
    );

    if (!resposta.ok) {
      const erro = await resposta.json().catch(() => null);
      throw new Error(erro?.mensagem || "Não foi possível salvar o treino.");
    }

    cancelarEdicao();
    carregarCatalogo();
  } catch (err) {
    showAlert(alertTreino, err.message || "Erro ao conectar com o servidor.");
  } finally {
    setLoading(btnCriarTreino, false, criando ? "Adicionar ao catálogo" : "Salvar alterações", "Salvando...");
  }
});

// ===== Atribuição diária =====
const listaAtribuicoes = document.getElementById("listaAtribuicoes");

async function carregarElencoEAtribuicoes() {
  try {
    const [respElenco, respHoje] = await Promise.all([
      apiFetch("/checkins/elenco"),
      apiFetch("/treinos/hoje"),
    ]);

    if (!respElenco.ok || !respHoje.ok) return;

    const elencoData = await respElenco.json();
    const atribuicoesHoje = await respHoje.json();

    // Junta status/sugestão (vindo de checkins/elenco) com a atribuição atual (vindo de treinos/hoje).
    elenco = elencoData.map((atleta) => {
      const atribuicao = atribuicoesHoje.find((a) => a.atletaId === atleta.id);
      return { ...atleta, atribuicaoAtual: atribuicao && atribuicao.treino ? atribuicao : null };
    });

    renderAtribuicoes();
  } catch (err) {
    // apiFetch já trata sessão expirada.
  }
}

function renderAtribuicoes() {
  if (elenco.length === 0) {
    listaAtribuicoes.innerHTML = '<div class="vazio">Carregando elenco...</div>';
    return;
  }

  if (catalogo.length === 0) {
    listaAtribuicoes.innerHTML = '<div class="vazio">Cadastre ao menos um treino no catálogo para poder atribuir.</div>';
    return;
  }

  listaAtribuicoes.innerHTML = elenco
    .map((atleta) => {
      const opcoes = catalogo
        .map((t) => {
          const sugerido = t.intensidade === atleta.intensidadeSugerida ? " ★" : "";
          const selecionado =
            atleta.atribuicaoAtual && atleta.atribuicaoAtual.treino.id === t.id ? "selected" : "";
          return `<option value="${t.id}" ${selecionado}>${t.titulo}${sugerido}</option>`;
        })
        .join("");

      const atualTexto = atleta.atribuicaoAtual
        ? `Atual: ${atleta.atribuicaoAtual.treino.titulo}`
        : '<span class="sem-treino">Nenhum treino atribuído hoje</span>';

      return `
        <div class="atribuicao-row">
          <div>
            <div class="atribuicao-nome">${atleta.nome}</div>
            <span class="status-badge ${STATUS_CLASSE[atleta.status]}">${STATUS_LABEL[atleta.status]}</span>
          </div>
          <select class="select-input" id="select-${atleta.id}">
            <option value="">Escolher treino...</option>
            ${opcoes}
          </select>
          <div class="atribuicao-atual">${atualTexto}</div>
          <button class="btn-secundario" onclick="atribuirTreino(${atleta.id})">Atribuir</button>
        </div>
      `;
    })
    .join("");
}

async function atribuirTreino(atletaId) {
  const select = document.getElementById(`select-${atletaId}`);
  const treinoId = select.value;

  if (!treinoId) {
    alert("Escolha um treino antes de atribuir.");
    return;
  }

  try {
    const resposta = await apiFetch("/treinos/atribuir", {
      method: "POST",
      body: JSON.stringify({ atletaId, treinoId: Number(treinoId), observacoes: null }),
    });

    if (!resposta.ok) {
      const erro = await resposta.json().catch(() => null);
      throw new Error(erro?.mensagem || "Não foi possível atribuir o treino.");
    }

    carregarElencoEAtribuicoes();
  } catch (err) {
    alert(err.message || "Erro ao conectar com o servidor.");
  }
}

carregarCatalogo();
carregarElencoEAtribuicoes();
btnCancelarEdicao.addEventListener("click", cancelarEdicao);
