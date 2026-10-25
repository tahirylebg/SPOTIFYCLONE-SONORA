import type { Track } from "../api/types";

export type RepeatMode = "off" | "all" | "one";

/**
 * File de lecture (même logique que la version Java du desktop).
 * Elle travaille sur sa propre copie du contexte : le shuffle ne change jamais l'ordre affiché.
 */
export class PlaybackQueue {
  private original: Track[] = [];
  private queue: Track[] = [];
  private index = -1;
  shuffle = false;
  repeat: RepeatMode = "off";

  /** Démarre une nouvelle file à partir d'un contexte, en commençant par `start`. */
  start(context: Track[], start: Track) {
    this.original = context.some((t) => t.id === start.id) ? [...context] : [start, ...context];
    this.queue = [...this.original];
    if (this.shuffle) {
      shuffleInPlace(this.queue);
      this.moveToFront(start);
    }
    this.index = this.queue.findIndex((t) => t.id === start.id);
  }

  get current(): Track | null {
    return this.queue[this.index] ?? null;
  }

  /** Piste suivante demandée par l'utilisateur ; null en fin de file sans repeat. */
  next(): Track | null {
    if (this.queue.length === 0) return null;
    if (this.index < this.queue.length - 1) this.index++;
    else if (this.repeat !== "off") this.index = 0;
    else return null;
    return this.current;
  }

  /** Piste à enchaîner quand le morceau se termine. */
  nextAuto(): Track | null {
    return this.repeat === "one" ? this.current : this.next();
  }

  previous(): Track | null {
    if (this.queue.length === 0) return null;
    if (this.index > 0) this.index--;
    else if (this.repeat === "all") this.index = this.queue.length - 1;
    else this.index = 0;
    return this.current;
  }

  toggleShuffle() {
    const current = this.current;
    this.shuffle = !this.shuffle;
    this.queue = [...this.original];
    if (this.shuffle) {
      shuffleInPlace(this.queue);
      if (current) this.moveToFront(current);
    }
    this.index = current ? this.queue.findIndex((t) => t.id === current.id) : -1;
  }

  cycleRepeat() {
    this.repeat = this.repeat === "off" ? "all" : this.repeat === "all" ? "one" : "off";
  }

  private moveToFront(track: Track) {
    const i = this.queue.findIndex((t) => t.id === track.id);
    if (i > 0) this.queue.unshift(...this.queue.splice(i, 1));
  }
}

export function shuffleInPlace<T>(items: T[]): T[] {
  for (let i = items.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [items[i], items[j]] = [items[j], items[i]];
  }
  return items;
}
