const usuario = exigirAutenticacao("jogador");

document.getElementById("nomeUsuario").innerText = usuario.nome;

const form = document.getElementById("formCheckin");
const btnEnviar = document.getElementById("btnEnviar");
const alertEl = document.getElementById("alertCheckin");
const blocoForm = document.getElementById("blocoForm");
const blocoJaEnviado = document.getElementById("blocoJaEnviado");
const listaHistorico = document.getElementById("listaHistorico");

let fadigaSelecionada = null;
let emocionalSelecionado = null;
let temDorSelecionado = null;
let intensidadeSelecionada = null;

// ===== Escala de fadiga (1 a 5) =====
document.querySelectorAll("#escalaFadiga button").forEach((btn) => {
  btn.addEventListener("click", () => {
    document.querySelectorAll("#escalaFadiga button").forEach((b) => b.classList.remove("selecionado"));
    btn.classList.add("selecionado");
    fadigaSelecionada = Number(btn.dataset.valor);
  });
});

// ===== Escala de estado emocional (1 a 5) =====
document.querySelectorAll("#escalaEmocional button").forEach((btn) => {
  btn.addEventListener("click", () => {
    document.querySelectorAll("#escalaEmocional button").forEach((b) => b.classList.remove("selecionado"));
    btn.classList.add("selecionado");
    emocionalSelecionado = Number(btn.dataset.valor);
  });
});

// ===== Toggle dor (sim/não) =====
const camposDor = document.getElementById("camposDor");
document.querySelectorAll("#toggleDor button").forEach((btn) => {
  btn.addEventListener("click", () => {
    document.querySelectorAll("#toggleDor button").forEach((b) => b.classList.remove("selecionado"));
    btn.classList.add("selecionado");
    temDorSelecionado = btn.dataset.valor === "sim";
    camposDor.classList.toggle("show", temDorSelecionado);
  });
});

// ===== Escala de intensidade da dor (1 a 5) =====
document.querySelectorAll("#escalaIntensidade button").forEach((btn) => {
  btn.addEventListener("click", () => {
    document.querySelectorAll("#escalaIntensidade button").forEach((b) => b.classList.remove("selecionado"));
    btn.classList.add("selecionado");
    intensidadeSelecionada = Number(btn.dataset.valor);
  });
});

form.addEventListener("submit", async function (e) {
  e.preventDefault();
  hideAlert(alertEl);

  const horasSono = document.getElementById("horasSono");
  const observacoes = document.getElementById("observacoes");
  const localDor = document.getElementById("localDor");

  clearFieldErrors(form);
  let valid = true;

  if (!horasSono.value || horasSono.value < 0 || horasSono.value > 24) {
    setError(horasSono, "erroSono", "Informe um valor entre 0 e 24");
    valid = false;
  } else setSuccess(horasSono);

  if (fadigaSelecionada === null) {
    document.getElementById("erroFadiga").innerText = "Selecione seu nível de fadiga";
    valid = false;
  } else document.getElementById("erroFadiga").innerText = "";

  if (emocionalSelecionado === null) {
    document.getElementById("erroEmocional").innerText = "Selecione seu estado emocional";
    valid = false;
  } else document.getElementById("erroEmocional").innerText = "";

  if (temDorSelecionado === null) {
    document.getElementById("erroDor").innerText = "Informe se sente dor";
    valid = false;
  } else document.getElementById("erroDor").innerText = "";

  if (temDorSelecionado === true && intensidadeSelecionada === null) {
    document.getElementById("erroIntensidade").innerText = "Selecione a intensidade";
    valid = false;
  } else document.getElementById("erroIntensidade").innerText = "";

  if (!valid) return;

  setLoading(btnEnviar, true, "Enviar check-in", "Enviando...");

  try {
    const resposta = await apiFetch("/checkins", {
      method: "POST",
      body: JSON.stringify({
        horasSono: Number(horasSono.value),
        fadiga: fadigaSelecionada,
        estadoEmocional: emocionalSelecionado,
        temDor: temDorSelecionado,
        localDor: temDorSelecionado ? localDor.value.trim() : null,
        intensidadeDor: temDorSelecionado ? intensidadeSelecionada : null,
        observacoes: observacoes.value.trim() || null,
      }),
    });

    if (!resposta.ok) {
      const erro = await resposta.json().catch(() => null);
      throw new Error(erro?.mensagem || "Não foi possível enviar o check-in.");
    }

    blocoForm.style.display = "none";
    blocoJaEnviado.classList.add("show");
    carregarHistorico();
  } catch (err) {
    showAlert(alertEl, err.message || "Erro ao conectar com o servidor.");
  } finally {
    setLoading(btnEnviar, false, "Enviar check-in", "Enviando...");
  }
});

// ===== Histórico =====
const ESCALA_TEXTO = { 1: "Muito baixo", 2: "Baixo", 3: "Médio", 4: "Bom", 5: "Ótimo" };

function formatarData(isoDate) {
  const [ano, mes, dia] = isoDate.split("-");
  return `${dia}/${mes}`;
}

async function carregarHistorico() {
  try {
    const resposta = await apiFetch("/checkins/meus");
    if (!resposta.ok) return;

    const checkins = await resposta.json();

    if (checkins.length === 0) {
      listaHistorico.innerHTML = '<div class="vazio">Nenhum check-in enviado ainda.</div>';
      return;
    }

    // Se já existe check-in de hoje, mostra o bloco de "já enviado" em vez do formulário.
    const hojeISO = new Date().toISOString().slice(0, 10);
    if (checkins.some((c) => c.dataCheckin === hojeISO)) {
      blocoForm.style.display = "none";
      blocoJaEnviado.classList.add("show");
    }

    listaHistorico.innerHTML = checkins
      .map(
        (c) => `
      <div class="historico-row">
        <div class="data">${formatarData(c.dataCheckin)}</div>
        <div>
          <span class="metrica-label">Sono</span>
          ${c.horasSono}h
        </div>
        <div>
          <span class="metrica-label">Fadiga</span>
          ${ESCALA_TEXTO[c.fadiga]}
        </div>
        <div>
          <span class="metrica-label">Emocional</span>
          ${ESCALA_TEXTO[c.estadoEmocional]}
        </div>
        <div>
          ${c.temDor ? `<span class="tag-dor">Dor · ${c.localDor || "não especificado"}</span>` : '<span class="tag-ok">Sem dor</span>'}
        </div>
      </div>
    `
      )
      .join("");
  } catch (err) {
    // apiFetch já trata 401 redirecionando - outros erros só deixam o histórico vazio.
  }
}

carregarHistorico();
