import { createContext, useCallback, useContext, useEffect, useMemo, useRef, useState, type ReactNode } from "react";
import { api } from "../api/client";
import type { Track } from "../api/types";
import { useLibrary } from "../library/LibraryProvider";
import { PlaybackQueue, shuffleInPlace, type RepeatMode } from "./PlaybackQueue";

// Au-delà de ce temps, "précédent" revient au début du morceau au lieu de changer de piste.
const PREVIOUS_THRESHOLD = 3;
const VOLUME_KEY = "sonora.volume";

interface Player {
  current: Track | null;
  playing: boolean;
  time: number;
  duration: number;
  volume: number;
  shuffle: boolean;
  repeat: RepeatMode;
  /** Joue `track` ; la suite enchaîne sur `context` (toute la bibliothèque par défaut). */
  play: (track: Track, context?: Track[]) => void;
  playShuffled: (tracks: Track[]) => void;
  toggle: () => void;
  next: () => void;
  previous: () => void;
  seek: (seconds: number) => void;
  setVolume: (volume: number) => void;
  toggleMute: () => void;
  toggleShuffle: () => void;
  cycleRepeat: () => void;
}

const PlayerContext = createContext<Player | null>(null);

function savedVolume(): number {
  try {
    const v = Number(localStorage.getItem(VOLUME_KEY));
    return Number.isFinite(v) && v >= 0 && v <= 1 && localStorage.getItem(VOLUME_KEY) !== null ? v : 0.7;
  } catch {
    return 0.7;
  }
}

export function PlayerProvider({ children }: { children: ReactNode }) {
  const { tracks, refreshPlays } = useLibrary();
  const audio = useRef<HTMLAudioElement>(new Audio());
  const queue = useRef(new PlaybackQueue());
  const volumeBeforeMute = useRef(0.7);

  const [current, setCurrent] = useState<Track | null>(null);
  const [playing, setPlaying] = useState(false);
  const [time, setTime] = useState(0);
  const [duration, setDuration] = useState(0);
  const [volume, setVolumeState] = useState(savedVolume);
  const [shuffle, setShuffle] = useState(false);
  const [repeat, setRepeat] = useState<RepeatMode>("off");

  const load = useCallback(
    (track: Track) => {
      const el = audio.current;
      el.src = api.url(track.audioUrl);
      el.play().catch(() => setPlaying(false));
      setCurrent(track);
      setTime(0);
      setDuration(track.durationSeconds);
      api.recordPlay(track.id).then(refreshPlays).catch(() => {});

      if ("mediaSession" in navigator) {
        navigator.mediaSession.metadata = new MediaMetadata({
          title: track.title,
          artist: track.artist,
          artwork: track.coverUrl ? [{ src: api.url(track.coverUrl), sizes: "600x600", type: "image/jpeg" }] : [],
        });
      }
    },
    [refreshPlays],
  );

  const play = useCallback(
    (track: Track, context?: Track[]) => {
      queue.current.start(context && context.length > 0 ? context : tracks, track);
      load(track);
    },
    [tracks, load],
  );

  const playShuffled = useCallback(
    (list: Track[]) => {
      if (list.length === 0) return;
      const mixed = shuffleInPlace([...list]);
      play(mixed[0], mixed);
    },
    [play],
  );

  const next = useCallback(() => {
    const track = queue.current.next();
    if (track) load(track);
  }, [load]);

  const previous = useCallback(() => {
    const el = audio.current;
    if (el.currentTime > PREVIOUS_THRESHOLD) {
      el.currentTime = 0;
      return;
    }
    const track = queue.current.previous();
    if (track) load(track);
  }, [load]);

  const toggle = useCallback(() => {
    const el = audio.current;
    if (!queue.current.current) {
      if (tracks.length > 0) play(tracks[0], tracks);
      return;
    }
    if (el.paused) el.play().catch(() => {});
    else el.pause();
  }, [tracks, play]);

  const seek = useCallback((seconds: number) => {
    audio.current.currentTime = seconds;
    setTime(seconds);
  }, []);

  const setVolume = useCallback((v: number) => {
    audio.current.volume = v;
    setVolumeState(v);
    try {
      localStorage.setItem(VOLUME_KEY, String(v));
    } catch {
      // stockage indisponible (navigation privée) : tant pis
    }
  }, []);

  const toggleMute = useCallback(() => {
    if (audio.current.volume > 0) {
      volumeBeforeMute.current = audio.current.volume;
      setVolume(0);
    } else {
      setVolume(volumeBeforeMute.current || 0.7);
    }
  }, [setVolume]);

  const toggleShuffle = useCallback(() => {
    queue.current.toggleShuffle();
    setShuffle(queue.current.shuffle);
  }, []);

  const cycleRepeat = useCallback(() => {
    queue.current.cycleRepeat();
    setRepeat(queue.current.repeat);
  }, []);

  // Événements de l'élément <audio>. "ended" passe par une ref pour toujours voir le dernier `load`.
  const onEnded = useRef<() => void>(() => {});
  onEnded.current = () => {
    const track = queue.current.nextAuto();
    if (track) load(track);
    else audio.current.currentTime = 0;
  };

  useEffect(() => {
    const el = audio.current;
    el.volume = volume;
    const handlers: Record<string, () => void> = {
      play: () => setPlaying(true),
      pause: () => setPlaying(false),
      timeupdate: () => setTime(el.currentTime),
      loadedmetadata: () => setDuration(el.duration),
      ended: () => onEnded.current(),
    };
    Object.entries(handlers).forEach(([event, handler]) => el.addEventListener(event, handler));
    return () => Object.entries(handlers).forEach(([event, handler]) => el.removeEventListener(event, handler));
    // Branché une seule fois : l'élément audio vit aussi longtemps que l'application.
  }, []);

  // Commandes du système (touches multimédia, écran de verrouillage du téléphone).
  useEffect(() => {
    if (!("mediaSession" in navigator)) return;
    const session = navigator.mediaSession;
    session.setActionHandler("play", toggle);
    session.setActionHandler("pause", toggle);
    session.setActionHandler("nexttrack", next);
    session.setActionHandler("previoustrack", previous);
  }, [toggle, next, previous]);

  // Raccourcis clavier : Espace, Ctrl/⌘ + ←/→.
  useEffect(() => {
    const onKey = (e: KeyboardEvent) => {
      const target = e.target as HTMLElement;
      const typing = target.tagName === "INPUT" || target.tagName === "TEXTAREA" || target.isContentEditable;
      if (e.code === "Space" && !typing) {
        e.preventDefault();
        toggle();
      } else if ((e.ctrlKey || e.metaKey) && e.key === "ArrowRight") {
        e.preventDefault();
        next();
      } else if ((e.ctrlKey || e.metaKey) && e.key === "ArrowLeft") {
        e.preventDefault();
        previous();
      }
    };
    window.addEventListener("keydown", onKey);
    return () => window.removeEventListener("keydown", onKey);
  }, [toggle, next, previous]);

  const value = useMemo<Player>(
    () => ({
      current, playing, time, duration, volume, shuffle, repeat,
      play, playShuffled, toggle, next, previous, seek, setVolume, toggleMute, toggleShuffle, cycleRepeat,
    }),
    [current, playing, time, duration, volume, shuffle, repeat,
      play, playShuffled, toggle, next, previous, seek, setVolume, toggleMute, toggleShuffle, cycleRepeat],
  );

  return <PlayerContext.Provider value={value}>{children}</PlayerContext.Provider>;
}

export function usePlayer(): Player {
  const player = useContext(PlayerContext);
  if (!player) throw new Error("usePlayer doit être utilisé dans <PlayerProvider>");
  return player;
}
