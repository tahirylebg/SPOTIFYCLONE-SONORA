import { useState } from "react";
import type { Track } from "../api/types";
import { formatDuration } from "../format";
import { usePlayer } from "../player/PlayerProvider";
import { Cover } from "./Cover";
import { Icon } from "./Icon";

interface TrackTableProps {
  tracks: Track[];
  header?: boolean;
}

/** Écran tactile : pas de survol ni de vrai double-clic, un simple toucher lance la lecture. */
const isTouch = () => window.matchMedia("(hover: none)").matches;

/** Liste de morceaux : clic = sélection, double-clic ou Entrée = lecture avec la liste comme contexte. */
export function TrackTable({ tracks, header = false }: TrackTableProps) {
  const { current, play } = usePlayer();
  const [selected, setSelected] = useState<string | null>(null);

  return (
    <div className="track-table" role="list">
      {header && (
        <div className="list-header">
          <span className="col-num">#</span>
          <span className="col-title">Titre</span>
          <span className="col-duration">
            <Icon name="clock" size={14} />
          </span>
        </div>
      )}
      {tracks.map((track, i) => {
        const playing = current?.id === track.id;
        return (
          <div
            key={track.id}
            role="listitem"
            tabIndex={0}
            className={`track-row ${playing ? "is-playing" : ""} ${selected === track.id ? "is-selected" : ""}`}
            onClick={() => (isTouch() ? play(track, tracks) : setSelected(track.id))}
            onDoubleClick={() => play(track, tracks)}
            onKeyDown={(e) => e.key === "Enter" && play(track, tracks)}
          >
            <span className="col-num">
              {playing ? <Icon name="equalizer" size={14} className="playing-icon" /> : <span className="track-number">{i + 1}</span>}
              <button className="row-play" aria-label={`Lire ${track.title}`} onClick={() => play(track, tracks)}>
                <Icon name="play" size={14} />
              </button>
            </span>
            <Cover url={track.coverUrl} label={track.title} size={40} />
            <span className="col-title">
              <span className="track-title">{track.title}</span>
              <span className="track-meta">{track.artist}</span>
            </span>
            <span className="col-duration track-duration">{formatDuration(track.durationSeconds)}</span>
          </div>
        );
      })}
    </div>
  );
}
