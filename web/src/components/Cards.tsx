import type { MouseEvent } from "react";
import { useNavigate } from "react-router-dom";
import type { Artist, Track } from "../api/types";
import { plural } from "../format";
import { useLibrary } from "../library/LibraryProvider";
import { usePlayer } from "../player/PlayerProvider";
import { Cover } from "./Cover";
import { Icon } from "./Icon";

/** Bouton rond vert "lecture", qui apparaît au survol des cartes et des tuiles. */
export function PlayButton({ onPlay, size = 44, label }: { onPlay: () => void; size?: number; label: string }) {
  const handle = (e: MouseEvent) => {
    e.stopPropagation();
    onPlay();
  };
  return (
    <button className="fab" style={{ width: size, height: size }} onClick={handle} aria-label={label}>
      <Icon name="play" size={size * 0.45} />
    </button>
  );
}

export function TrackCard({ track, context }: { track: Track; context: Track[] }) {
  const { play } = usePlayer();
  const start = () => play(track, context);
  return (
    <div className="card" onClick={start}>
      <div className="card-visual">
        <Cover url={track.coverUrl} label={track.title} size={156} />
        <PlayButton onPlay={start} label={`Lire ${track.title}`} />
      </div>
      <div className="card-title">{track.title}</div>
      <div className="card-subtitle">{track.artist}</div>
    </div>
  );
}

export function ArtistCard({ artist }: { artist: Artist }) {
  const navigate = useNavigate();
  const { play } = usePlayer();
  const { trackById } = useLibrary();
  const tracks = artist.trackIds.map(trackById).filter((t): t is Track => t !== undefined);

  return (
    <div className="card" onClick={() => navigate(`/recherche?q=${encodeURIComponent(artist.name)}`)}>
      <div className="card-visual">
        <Cover url={artist.coverUrl} label={artist.name} size={156} round />
        <PlayButton onPlay={() => tracks.length > 0 && play(tracks[0], tracks)} label={`Lire ${artist.name}`} />
      </div>
      <div className="card-title">{artist.name}</div>
      <div className="card-subtitle">Artiste · {plural(artist.trackCount, "titre")}</div>
    </div>
  );
}

/** Tuile horizontale de l'accueil. */
export function QuickTile({ track, context }: { track: Track; context: Track[] }) {
  const { play, current } = usePlayer();
  const start = () => play(track, context);
  return (
    <div className={`quick-tile ${current?.id === track.id ? "is-playing" : ""}`} onClick={start}>
      <Cover url={track.coverUrl} label={track.title} size={64} />
      <span className="quick-tile-title">{track.title}</span>
      <PlayButton onPlay={start} size={36} label={`Lire ${track.title}`} />
    </div>
  );
}
