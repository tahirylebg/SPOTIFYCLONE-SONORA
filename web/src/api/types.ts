// Types renvoyés par l'API Sonora (voir backend/…/web/Dtos.java).
// Fichier sans dépendance à React ni au navigateur : réutilisable tel quel par l'appli mobile.

export interface Track {
  id: string;
  title: string;
  artist: string;
  /** 0 si la durée n'a pas pu être lue. */
  durationSeconds: number;
  /** Absent si le fichier n'a pas de pochette. */
  coverUrl?: string;
  audioUrl: string;
}

export interface Artist {
  name: string;
  trackCount: number;
  trackIds: string[];
  coverUrl?: string;
}

export interface SearchResult {
  tracks: Track[];
  artists: Artist[];
}

export interface Play {
  trackId: string;
  count: number;
  /** Date ISO-8601. */
  lastPlayedAt: string;
}
