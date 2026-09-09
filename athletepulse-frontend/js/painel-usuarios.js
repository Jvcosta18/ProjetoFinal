const usuario = exigirAutenticacao("comissao");
document.getElementById("nomeUsuario").innerText = usuario.nome;

const ROTULO_PERFIL = { jogador: "Jogador", comissao: "Comissão Técnica", psicologo: "Psicóloga(o)" };
let usuarios = [];
let editandoId = null;

const listaUsuarios = document.getElementById("listaUsuarios");

async function carregarUsuarios() {
  try {
    const resposta = await apiFetch("/usuarios");
    if (!resposta.ok) {
      listaUsuarios.innerHTML = '<div class="vazio">Não foi possível carregar os usuários.</div>';
      return;
    }

    usuarios = await resposta.json();
    renderUsuarios();
  } catch (err) {
    // apiFetch já trata sessão expirada.
  }
}

function renderUsuarios() {
  if (usuarios.length === 0) {
    listaUsuarios.innerHTML = '<div class="vazio">Nenhum usuário cadastrado.</div>';
    return;
  }

  listaUsuarios.innerHTML = usuarios
    .map((u) => {
      const souEu = u.id === Number(usuario.id);
      const statusClasse = u.ativo ? "status-ok" : "status-alerta";
      const statusLabel = u.ativo ? "Ativo" : "Desativado";

      if (editandoId === u.id) {
        return `
          <div class="usuario-row usuario-editando">
            <div class="usuario-edit-campos">
              <input class="field-input" type="text" id="editNome-${u.id}" value="${u.nome}">
              <label class="toggle-ativo">
                <input type="checkbox" id="editAtivo-${u.id}" ${u.ativo ? "checked" : ""} ${souEu ? "disabled" : ""}>
                Conta ativa
              </label>
            </div>
            <div class="usuario-acoes">
              <button class="btn-secundario" onclick="salvarEdicao(${u.id})">Salvar</button>
              <button class="btn-cancelar-inline" onclick="cancelarEdicaoUsuario()">Cancelar</button>
            </div>
          </div>
        `;
      }

      return `
        <div class="usuario-row">
          <div>
            <div class="usuario-nome">${u.nome} ${souEu ? "<span class=\"voce-tag\">(você)</span>" : ""}</div>
            <div class="usuario-email">${u.email}</div>
          </div>
          <div class="usuario-tipo">${ROTULO_PERFIL[u.tipo] || u.tipo}</div>
          <span class="status-badge ${statusClasse}">${statusLabel}</span>
          <div class="usuario-acoes">
            <button class="btn-secundario" onclick="iniciarEdicao(${u.id})">Editar</button>
            <button class="btn-excluir" onclick="abrirRedefinirSenha(${u.id})">Redefinir senha</button>
          </div>
        </div>
      `;
    })
    .join("");
}

function iniciarEdicao(id) {
  editandoId = id;
  renderUsuarios();
}

function cancelarEdicaoUsuario() {
  editandoId = null;
  renderUsuarios();
}

async function salvarEdicao(id) {
  const nome = document.getElementById(`editNome-${id}`).value.trim();
  const ativoCheckbox = document.getElementById(`editAtivo-${id}`);
  const ativo = ativoCheckbox.checked;

  if (nome.length < 3) {
    alert("Nome muito curto.");
    return;
  }

  try {
    const resposta = await apiFetch(`/usuarios/${id}`, {
      method: "PUT",
      body: JSON.stringify({ nome, ativo }),
    });

    if (!resposta.ok) {
      const erro = await resposta.json().catch(() => null);
      throw new Error(erro?.mensagem || "Não foi possível salvar as alterações.");
    }

    editandoId = null;
    carregarUsuarios();
  } catch (err) {
    alert(err.message || "Erro ao conectar com o servidor.");
  }
}

async function abrirRedefinirSenha(id) {
  const usuarioAlvo = usuarios.find((u) => u.id === id);
  const novaSenha = prompt(`Nova senha para ${usuarioAlvo.nome} (mínimo 6 caracteres):`);

  if (novaSenha === null) return; // cancelou

  if (novaSenha.length < 6) {
    alert("A senha deve ter pelo menos 6 caracteres.");
    return;
  }

  try {
    const resposta = await apiFetch(`/usuarios/${id}/senha`, {
      method: "PUT",
      body: JSON.stringify({ novaSenha }),
    });

    if (!resposta.ok) {
      const erro = await resposta.json().catch(() => null);
      throw new Error(erro?.mensagem || "Não foi possível redefinir a senha.");
    }

    alert(`Senha de ${usuarioAlvo.nome} redefinida. Informe a nova senha a essa pessoa por fora do sistema.`);
  } catch (err) {
    alert(err.message || "Erro ao conectar com o servidor.");
  }
}

carregarUsuarios();
