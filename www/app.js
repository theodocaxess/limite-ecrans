// Limite ecrans Solal
// Application Capacitor (vanilla JS, sans framework).
// Les plugins natifs (ScreenLimit, Preferences) sont exposes automatiquement
// sur window.Capacitor.Plugins des que l'app tourne sur Android.

const DAY_LABELS = { 1: "Lun", 2: "Mar", 3: "Mer", 4: "Jeu", 5: "Ven", 6: "Sam", 7: "Dim" };
const STORAGE_KEY = "screenLimitState_v1";

const PERIODS = [
  { id: "morning", label: "Matin (semaine)", time: "7h30 - 8h00", days: [1, 2, 3, 4, 5] },
  { id: "evening", label: "Soir (tous les jours)", time: "18h00 - 20h00", days: [1, 2, 3, 4, 5, 6, 7] },
  { id: "wednesday", label: "Mercredi apres-midi", time: "13h00 - 20h00", days: [3] },
  { id: "weekend", label: "Week-end (journee)", time: "7h30 - 20h00", days: [6, 7] },
];

function getPlugins() {
  const plugins = window.Capacitor && window.Capacitor.Plugins;
  if (!plugins || !plugins.Preferences) return null;
  return plugins;
}

function log(msg) {
  const el = document.getElementById("log");
  const time = new Date().toLocaleTimeString("fr-FR");
  el.textContent = `[${time}] ${msg}\n` + el.textContent;
}

async function loadState() {
  const plugins = getPlugins();
  const defaultState = {};
  for (const period of PERIODS) {
    for (const day of period.days) defaultState[`${period.id}_${day}`] = true;
  }
  if (!plugins) return defaultState;
  try {
    const { value } = await plugins.Preferences.get({ key: STORAGE_KEY });
    if (!value) return defaultState;
    const parsed = JSON.parse(value);
    return { ...defaultState, ...parsed };
  } catch (e) {
    return defaultState;
  }
}

async function saveState(state) {
  const plugins = getPlugins();
  if (!plugins) return;
  await plugins.Preferences.set({ key: STORAGE_KEY, value: JSON.stringify(state) });
}

function render(state) {
  const container = document.getElementById("periods");
  container.innerHTML = "";
  for (const period of PERIODS) {
    const section = document.createElement("section");
    const h2 = document.createElement("h2");
    h2.textContent = period.label;
    const time = document.createElement("p");
    time.className = "time";
    time.textContent = `Rappel possible entre ${period.time}`;
    const days = document.createElement("div");
    days.className = "days";
    for (const day of period.days) {
      const key = `${period.id}_${day}`;
      const btn = document.createElement("div");
      btn.className = "day-toggle" + (state[key] ? " active" : "");
      btn.textContent = DAY_LABELS[day];
      btn.addEventListener("click", async () => {
        state[key] = !state[key];
        btn.classList.toggle("active", state[key]);
        await saveState(state);
        log(`${period.label} / ${DAY_LABELS[day]} : ${state[key] ? "actif" : "desactive"}`);
      });
      days.appendChild(btn);
    }
    section.appendChild(h2);
    section.appendChild(time);
    section.appendChild(days);
    container.appendChild(section);
  }
}

function renderMasterButton(enabled) {
  const btn = document.getElementById("masterBtn");
  btn.className = "master " + (enabled ? "disable" : "enable");
  btn.textContent = enabled ? "Desactiver la surveillance" : "Activer la surveillance";
}

async function refreshStatus() {
  const dot = document.getElementById("permDot");
  const text = document.getElementById("permText");
  const plugins = window.Capacitor && window.Capacitor.Plugins;
  if (!plugins || !plugins.ScreenLimit) {
    dot.className = "dot off";
    text.textContent = "App non installee : ouvre-la sur ton telephone Android pour activer la surveillance.";
    renderMasterButton(false);
    return false;
  }
  const status = await plugins.ScreenLimit.getStatus();
  if (status.enabled && status.notificationsGranted) {
    dot.className = "dot on";
    text.textContent = "Surveillance active : rappel a chaque deverrouillage pendant un creneau coche.";
  } else if (status.enabled && !status.notificationsGranted) {
    dot.className = "dot off";
    text.textContent = "Notifications refusees : active-les dans les reglages Android de l'app.";
  } else {
    dot.className = "dot off";
    text.textContent = "Surveillance desactivee.";
  }
  renderMasterButton(status.enabled);
  return status.enabled;
}

async function main() {
  const state = await loadState();
  render(state);
  await refreshStatus();

  document.getElementById("masterBtn").addEventListener("click", async () => {
    const plugins = window.Capacitor && window.Capacitor.Plugins;
    if (!plugins || !plugins.ScreenLimit) {
      log("Mode navigateur (hors app) : la surveillance ne peut pas demarrer ici.");
      return;
    }
    const status = await plugins.ScreenLimit.getStatus();
    if (status.enabled) {
      await plugins.ScreenLimit.stopMonitoring();
      log("Surveillance desactivee.");
    } else {
      await plugins.ScreenLimit.startMonitoring();
      log("Surveillance activee.");
    }
    await refreshStatus();
  });

  document.getElementById("resetBtn").addEventListener("click", async () => {
    const fresh = {};
    for (const period of PERIODS) {
      for (const day of period.days) fresh[`${period.id}_${day}`] = true;
    }
    await saveState(fresh);
    render(fresh);
    log("Reglages par defaut restaures (tous les creneaux coches).");
  });

  document.getElementById("allOffBtn").addEventListener("click", async () => {
    const off = {};
    for (const period of PERIODS) {
      for (const day of period.days) off[`${period.id}_${day}`] = false;
    }
    await saveState(off);
    render(off);
    log("Tous les creneaux decoches (la surveillance reste active mais ne declenchera plus rien).");
  });
}

document.addEventListener("DOMContentLoaded", main);
