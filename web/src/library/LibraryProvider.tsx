import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from "react";
import { api } from "../api/client";
import type { Artist, Play, Track } from "../api/types";

interface Library {
  tracks: Track[];
  artists: Artist[];
  /** Historique, du plus récent au plus ancien. */
  plays: Play[];
  loading: boolean;
  error: string | null;
  trackById: (id: string) => Track | undefined;
  playCount: (id: string) => number;
  /** Morceaux récemment écoutés, dans l'ordre de l'historique. */
  recent: Track[];
  refreshPlays: () => void;
  reload: () => void;
}

const LibraryContext = createContext<Library | null>(null);

export function LibraryProvider({ children }: { children: ReactNode }) {
  const [tracks, setTracks] = useState<Track[]>([]);
  const [artists, setArtists] = useState<Artist[]>([]);
  const [plays, setPlays] = useState<Play[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const reload = useCallback(() => {
    setLoading(true);
    Promise.all([api.tracks(), api.artists(), api.plays()])
      .then(([t, a, p]) => {
        setTracks(t);
        setArtists(a);
        setPlays(p);
        setError(null);
      })
      .catch(() => setError("Impossible de joindre l'API Sonora. Le backend est-il lancé sur le port 8080 ?"))
      .finally(() => setLoading(false));
  }, []);

  const refreshPlays = useCallback(() => {
    api.plays().then(setPlays).catch(() => {});
  }, []);

  useEffect(() => {
    reload();
  }, [reload]);

  const value = useMemo<Library>(() => {
    const byId = new Map(tracks.map((t) => [t.id, t]));
    const counts = new Map(plays.map((p) => [p.trackId, p.count]));
    return {
      tracks,
      artists,
      plays,
      loading,
      error,
      trackById: (id) => byId.get(id),
      playCount: (id) => counts.get(id) ?? 0,
      recent: plays.map((p) => byId.get(p.trackId)).filter((t): t is Track => t !== undefined),
      refreshPlays,
      reload,
    };
  }, [tracks, artists, plays, loading, error, refreshPlays, reload]);

  return <LibraryContext.Provider value={value}>{children}</LibraryContext.Provider>;
}

export function useLibrary(): Library {
  const library = useContext(LibraryContext);
  if (!library) throw new Error("useLibrary doit être utilisé dans <LibraryProvider>");
  return library;
}
