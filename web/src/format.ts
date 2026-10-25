import type { Track } from "./api/types";

/** "3:26" ; chaîne vide si la durée est inconnue. */
export function formatDuration(seconds: number): string {
  if (!Number.isFinite(seconds) || seconds <= 0) return "";
  const total = Math.floor(seconds);
  return `${Math.floor(total / 60)}:${String(total % 60).padStart(2, "0")}`;
}

export function plural(n: number, word: string): string {
  return `${n} ${word}${n > 1 ? "s" : ""}`;
}

/** "3 titres · 8 min" */
export function summary(tracks: Track[]): string {
  const seconds = tracks.reduce((sum, t) => sum + t.durationSeconds, 0);
  const text = plural(tracks.length, "titre");
  return seconds > 0 ? `${text} · ${Math.max(1, Math.round(seconds / 60))} min` : text;
}

export function greeting(date = new Date()): string {
  const h = date.getHours();
  if (h < 5) return "Bonne nuit";
  if (h < 12) return "Bonjour";
  if (h < 18) return "Bon après-midi";
  return "Bonsoir";
}

/** Couleur stable dérivée d'un texte (tuiles "Parcourir"). */
const COLORS = ["#e13300", "#1e3264", "#8d67ab", "#e8115b", "#148a08", "#bc5900", "#509bf5", "#af2896", "#27856a", "#477d95"];
export function colorFor(text: string): string {
  let hash = 0;
  for (const c of text) hash = (hash * 31 + c.charCodeAt(0)) | 0;
  return COLORS[Math.abs(hash) % COLORS.length];
}
