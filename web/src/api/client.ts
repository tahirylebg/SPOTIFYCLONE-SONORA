import type { Artist, Play, SearchResult, Track } from "./types";

/**
 * Client de l'API Sonora. Sans dépendance à React : l'appli mobile pourra le
 * réutiliser en passant l'adresse du serveur (ex. "http://192.168.1.20:8080").
 */
export function createApi(baseUrl = "") {
  async function request<T>(path: string, init?: RequestInit): Promise<T> {
    const response = await fetch(baseUrl + path, init);
    if (!response.ok) {
      throw new Error(`${init?.method ?? "GET"} ${path} → ${response.status}`);
    }
    return response.status === 204 ? (undefined as T) : ((await response.json()) as T);
  }

  return {
    tracks: () => request<Track[]>("/api/tracks"),
    artists: () => request<Artist[]>("/api/artists"),
    search: (q: string, signal?: AbortSignal) =>
      request<SearchResult>(`/api/search?q=${encodeURIComponent(q)}`, { signal }),
    plays: () => request<Play[]>("/api/plays"),
    recordPlay: (trackId: string) => request<Play>(`/api/plays/${trackId}`, { method: "POST" }),
    rescan: () => request<{ trackCount: number }>("/api/library/rescan", { method: "POST" }),
    /** Transforme une URL relative de l'API (audioUrl, coverUrl) en URL absolue. */
    url: (path: string) => baseUrl + path,
  };
}

export type SonoraApi = ReturnType<typeof createApi>;

// VITE_API_URL permet de viser un backend déployé ailleurs ; vide = même origine (proxy Vite en dev).
export const api = createApi(import.meta.env.VITE_API_URL ?? "");
